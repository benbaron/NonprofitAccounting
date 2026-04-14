package nonprofitbookkeeping.service;

import nonprofitbookkeeping.core.Database;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FundTransferLifecycleServiceTest
{
    @TempDir
    Path tempDir;

    @Test
    void transitionStatus_validPolicyTransition_updatesTransferAndLogsEvent() throws Exception
    {
        initDb();
        long transferId = seedTransfer("DRAFT");

        FundTransferLifecycleService svc = new FundTransferLifecycleService();
        svc.transitionStatus(transferId, "APPROVED", null, "approved by reviewer");

        try (Connection c = Database.get().getConnection())
        {
            try (PreparedStatement ps = c.prepareStatement(
                "SELECT status, posted_txn_id FROM fund_transfer WHERE id = ?"))
            {
                ps.setLong(1, transferId);
                try (ResultSet rs = ps.executeQuery())
                {
                    rs.next();
                    assertEquals("APPROVED", rs.getString(1));
                    assertNull(rs.getObject(2));
                }
            }

            try (PreparedStatement ps = c.prepareStatement(
                "SELECT COUNT(*) FROM fund_transfer_integrity_event WHERE transfer_id = ? AND event_type = 'STATUS_TRANSITION'"))
            {
                ps.setLong(1, transferId);
                try (ResultSet rs = ps.executeQuery())
                {
                    rs.next();
                    assertEquals(1, rs.getInt(1));
                }
            }
        }
    }

    @Test
    void transitionStatus_invalidPolicyTransition_writesRepairQueueAndThrows() throws Exception
    {
        initDb();
        long transferId = seedTransfer("DRAFT");

        FundTransferLifecycleService svc = new FundTransferLifecycleService();
        assertThrows(IllegalStateException.class,
            () -> svc.transitionStatus(transferId, "POSTED", 99L, "skip intermediate states"));

        try (Connection c = Database.get().getConnection();
             PreparedStatement ps = c.prepareStatement(
                 "SELECT COUNT(*) FROM fund_transfer_repair_queue WHERE transfer_id = ? AND issue_code = 'INVALID_STATUS_TRANSITION'"))
        {
            ps.setLong(1, transferId);
            try (ResultSet rs = ps.executeQuery())
            {
                rs.next();
                assertEquals(1, rs.getInt(1));
            }
        }
    }

    private void initDb() throws Exception
    {
        Path dbPath = tempDir.resolve("fund-transfer-lifecycle");
        Database.init(dbPath);
        Database.get().ensureSchema();
    }

    private long seedTransfer(String status) throws Exception
    {
        try (Connection c = Database.get().getConnection())
        {
            long fromFund = insertFund(c, "F100", "Operating");
            long toFund = insertFund(c, "F200", "Programs");

            try (PreparedStatement ps = c.prepareStatement(
                """
                INSERT INTO fund_transfer(transfer_date, from_fund_id, to_fund_id, amount, memo, status, posted_txn_id)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """,
                PreparedStatement.RETURN_GENERATED_KEYS))
            {
                ps.setDate(1, Date.valueOf(LocalDate.of(2026, 4, 1)));
                ps.setLong(2, fromFund);
                ps.setLong(3, toFund);
                ps.setBigDecimal(4, new BigDecimal("125.00"));
                ps.setString(5, "Seed transfer");
                ps.setString(6, status);
                ps.setObject(7, null);
                ps.executeUpdate();
                try (ResultSet rs = ps.getGeneratedKeys())
                {
                    rs.next();
                    return rs.getLong(1);
                }
            }
        }
    }

    private long insertFund(Connection c, String code, String name) throws Exception
    {
        try (PreparedStatement ps = c.prepareStatement(
            "INSERT INTO fund(code, name, fund_type) VALUES (?, ?, 'UNRESTRICTED')",
            PreparedStatement.RETURN_GENERATED_KEYS))
        {
            ps.setString(1, code);
            ps.setString(2, name);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys())
            {
                rs.next();
                return rs.getLong(1);
            }
        }
    }
}
