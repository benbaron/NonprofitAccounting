package nonprofitbookkeeping.persistence;

import nonprofitbookkeeping.core.Database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Persistence helpers for fund transfer integrity policy artifacts.
 */
public final class FundTransferIntegrityRepository
{
    private FundTransferIntegrityRepository() {}

    public static boolean isTransitionAllowed(String fromStatus, String toStatus) throws SQLException
    {
        try (Connection c = Database.get().getConnection())
        {
            return isTransitionAllowed(c, fromStatus, toStatus);
        }
    }

    public static boolean isTransitionAllowed(Connection c, String fromStatus, String toStatus)
        throws SQLException
    {
        String sql = """
            SELECT is_allowed
            FROM fund_transfer_status_transition
            WHERE from_status = ? AND to_status = ?
        """;
        try (PreparedStatement ps = c.prepareStatement(sql))
        {
            ps.setString(1, fromStatus);
            ps.setString(2, toStatus);
            try (ResultSet rs = ps.executeQuery())
            {
                return rs.next() && rs.getBoolean(1);
            }
        }
    }

    public static void appendIntegrityEvent(Connection c, long transferId,
        String eventType, String detail) throws SQLException
    {
        String sql = """
            INSERT INTO fund_transfer_integrity_event(transfer_id, event_type, event_detail)
            VALUES (?, ?, ?)
        """;
        try (PreparedStatement ps = c.prepareStatement(sql))
        {
            ps.setLong(1, transferId);
            ps.setString(2, eventType);
            ps.setString(3, detail);
            ps.executeUpdate();
        }
    }

    public static void enqueueRepairIssue(Connection c, long transferId,
        String issueCode, String issueDetail, String proposedAction) throws SQLException
    {
        String sql = """
            INSERT INTO fund_transfer_repair_queue(transfer_id, issue_code, issue_detail, proposed_action)
            SELECT ?, ?, ?, ?
            WHERE NOT EXISTS (
              SELECT 1
              FROM fund_transfer_repair_queue q
              WHERE q.transfer_id = ?
                AND q.issue_code = ?
                AND q.resolved_at IS NULL
            )
        """;
        try (PreparedStatement ps = c.prepareStatement(sql))
        {
            ps.setLong(1, transferId);
            ps.setString(2, issueCode);
            ps.setString(3, issueDetail);
            ps.setString(4, proposedAction);
            ps.setLong(5, transferId);
            ps.setString(6, issueCode);
            ps.executeUpdate();
        }
    }
}
