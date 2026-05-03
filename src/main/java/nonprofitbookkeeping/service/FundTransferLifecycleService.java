package nonprofitbookkeeping.service;

import nonprofitbookkeeping.core.Database;
import nonprofitbookkeeping.persistence.FundTransferIntegrityRepository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Service that enforces fund transfer lifecycle transitions against
 * fund_transfer_status_transition policy table and logs integrity events.
 */
public final class FundTransferLifecycleService
{
    public void transitionStatus(long transferId, String toStatus, Long postedTxnId, String detail)
        throws SQLException
    {
        try (Connection c = Database.get().getConnection())
        {
            boolean autoCommit = c.getAutoCommit();
            boolean txCommitted = false;
            c.setAutoCommit(false);
            try
            {
                String fromStatus = fetchCurrentStatus(c, transferId);
                if (fromStatus == null)
                {
                    throw new IllegalArgumentException("Fund transfer not found: " + transferId);
                }

                if (!FundTransferIntegrityRepository.isTransitionAllowed(c, fromStatus, toStatus))
                {
                    FundTransferIntegrityRepository.appendIntegrityEvent(c, transferId,
                        "INVALID_STATUS_TRANSITION",
                        "from=" + fromStatus + ", to=" + toStatus + ", detail=" + safe(detail));
                    FundTransferIntegrityRepository.enqueueRepairIssue(c, transferId,
                        "INVALID_STATUS_TRANSITION",
                        "from=" + fromStatus + ", to=" + toStatus,
                        "Move transfer to a valid intermediate status per policy table.");
                    c.commit();
                    txCommitted = true;
                    throw new IllegalStateException(
                        "Illegal fund transfer status transition: " + fromStatus + " -> " + toStatus);
                }

                if ("POSTED".equals(toStatus) && postedTxnId == null)
                {
                    throw new IllegalArgumentException("POSTED transition requires postedTxnId.");
                }
                if (!"POSTED".equals(toStatus) && postedTxnId != null)
                {
                    throw new IllegalArgumentException("Only POSTED status may include postedTxnId.");
                }
                if ("POSTED".equals(toStatus))
                {
                    FinanceWriteEnforcement.requireFacadeScope("fund_transfer.status/posted_txn_id");
                }

                String updateSql = """
                    UPDATE fund_transfer
                    SET status = ?, posted_txn_id = ?, updated_at = CURRENT_TIMESTAMP
                    WHERE id = ?
                """;
                try (PreparedStatement ps = c.prepareStatement(updateSql))
                {
                    ps.setString(1, toStatus);
                    if (postedTxnId == null)
                    {
                        ps.setNull(2, java.sql.Types.BIGINT);
                    }
                    else
                    {
                        ps.setLong(2, postedTxnId);
                    }
                    ps.setLong(3, transferId);
                    ps.executeUpdate();
                }

                FundTransferIntegrityRepository.appendIntegrityEvent(c, transferId,
                    "STATUS_TRANSITION",
                    "from=" + fromStatus + ", to=" + toStatus + ", detail=" + safe(detail));

                c.commit();
                txCommitted = true;
            }
            catch (Exception e)
            {
                if (!txCommitted)
                {
                    c.rollback();
                }
                if (e instanceof SQLException sql)
                {
                    throw sql;
                }
                throw e;
            }
            finally
            {
                c.setAutoCommit(autoCommit);
            }
        }
    }

    private String fetchCurrentStatus(Connection c, long transferId) throws SQLException
    {
        String sql = "SELECT status FROM fund_transfer WHERE id = ?";
        try (PreparedStatement ps = c.prepareStatement(sql))
        {
            ps.setLong(1, transferId);
            try (ResultSet rs = ps.executeQuery())
            {
                return rs.next() ? rs.getString(1) : null;
            }
        }
    }

    private String safe(String v)
    {
        return v == null ? "" : v;
    }
}
