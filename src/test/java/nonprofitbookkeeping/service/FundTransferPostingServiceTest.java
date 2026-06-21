package nonprofitbookkeeping.service;

import nonprofitbookkeeping.core.Database;
import nonprofitbookkeeping.core.FlywayMigrationRunner;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FundTransferPostingServiceTest {
    @TempDir Path tempDir;

    @Test
    void createAndAmendTransfer_usesFacadeAndLifecycle() throws Exception {
        Database.init(this.tempDir.resolve("fund-transfer"));
        FlywayMigrationRunner.migrateCurrentDatabaseIfEnabled();
        Database.get().ensureSchema();
        seed();
        long transferId = insertTransfer("APPROVED");

        FundTransferPostingService svc = new FundTransferPostingService();
        PostingReference created = svc.postTransfer(transferId,
            LocalDate.of(2026,4,10), "F1", "F2", new BigDecimal("50.00"),
            "initial", "1000", "2100");
        assertTrue(created.journalTxnId() > 0);
        assertEquals(2, svc.countFundLines(created.journalTxnId(), "F1"));
        assertEquals(2, svc.countFundLines(created.journalTxnId(), "F2"));

        PostingReference amended = svc.amendTransfer(transferId,
            created.journalTxnId(), LocalDate.of(2026,4,11), "F1", "F2",
            new BigDecimal("75.00"), "edited", "1000", "2100");
        assertTrue(amended.journalTxnId() > created.journalTxnId());
        assertEquals("POSTED", readStatus(transferId));
    }

    @Test
    void postTransfer_invalidFundPair_throws() throws Exception {
        Database.init(this.tempDir.resolve("fund-transfer-invalid"));
        FlywayMigrationRunner.migrateCurrentDatabaseIfEnabled();
        Database.get().ensureSchema();
        seed();
        long transferId = insertTransfer("APPROVED");

        FundTransferPostingService svc = new FundTransferPostingService();
        assertThrows(IllegalArgumentException.class, () -> svc.postTransfer(
            transferId, LocalDate.of(2026, 4, 10), "F1", "F1",
            new BigDecimal("50.00"), "initial", "1000", "2100"));
    }

    private void seed() throws Exception {
        try (Connection c = Database.get().getConnection();
             PreparedStatement ps = c.prepareStatement("MERGE INTO account(account_number,name,account_type,increase_side) KEY(account_number) VALUES (?,?,?,?)")) {
            ps.setString(1,"1000");ps.setString(2,"Cash");ps.setString(3,"ASSET");ps.setString(4,"DEBIT");ps.addBatch();
            ps.setString(1,"2100");ps.setString(2,"Due to/from funds");ps.setString(3,"LIABILITY");ps.setString(4,"CREDIT");ps.addBatch();
            ps.executeBatch();
        }
        try (Connection c = Database.get().getConnection();
             PreparedStatement ps = c.prepareStatement("INSERT INTO fund(code,name,fund_type) VALUES(?,?,?)")) {
            ps.setString(1,"F1");ps.setString(2,"Fund 1");ps.setString(3,"RESTRICTED");ps.addBatch();
            ps.setString(1,"F2");ps.setString(2,"Fund 2");ps.setString(3,"RESTRICTED");ps.addBatch();
            ps.executeBatch();
        }
    }

    private long insertTransfer(String status) throws Exception {
        try (Connection c = Database.get().getConnection();
             PreparedStatement ps = c.prepareStatement("INSERT INTO fund_transfer(transfer_date,from_fund_id,to_fund_id,amount,memo,status) VALUES(CURRENT_DATE,1,2,10,'m',?)", new String[]{"id"})) {
            ps.setString(1, status); ps.executeUpdate();
            try (var rs = ps.getGeneratedKeys()) { rs.next(); return rs.getLong(1);} }
    }

    private String readStatus(long id) throws Exception {
        try (Connection c = Database.get().getConnection();
             PreparedStatement ps = c.prepareStatement("SELECT status FROM fund_transfer WHERE id=?")) {
            ps.setLong(1,id); try (var rs = ps.executeQuery()) { rs.next(); return rs.getString(1);} }
    }
}
