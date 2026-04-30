package nonprofitbookkeeping.service;

import nonprofitbookkeeping.core.Database;
import nonprofitbookkeeping.model.Grant;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GrantsFinancePostingServiceTest {
    @TempDir Path tempDir;

    @Test
    void awardAndEdit_postingsAndLinksPersist() throws Exception {
        Database.init(tempDir.resolve("grants-fin"));
        Database.get().ensureSchema();
        seedAccounts();
        seedGrant("GR-1");

        GrantsFinancePostingService svc = new GrantsFinancePostingService();
        Grant g = new Grant("G1","Grantor",new BigDecimal("100.00"),"2026-01-01","Program","ACTIVE");

        PostingReference posted = svc.postFinancialEvent("GR-1", g,
            GrantsFinancePostingService.GrantEventType.AWARD,
            LocalDate.of(2026,4,12), new BigDecimal("100.00"), "1000",
            "4000", "F1");
        assertTrue(posted.journalTxnId() > 0);
        assertEquals(1, countRows("SELECT COUNT(*) FROM grant_posting_link WHERE grant_record_id='GR-1'"));
        assertEquals("DEFERRAL", readLatestPostingRole("GR-1"));

        PostingReference adjusted = svc.editFinancialEvent("GR-1",
            posted.journalTxnId(), g,
            GrantsFinancePostingService.GrantEventType.RECOGNITION,
            LocalDate.of(2026,4,15), new BigDecimal("80.00"), "1000",
            "4000", "F1");
        assertTrue(adjusted.journalTxnId() > posted.journalTxnId());
        assertEquals("REVENUE", readLatestPostingRole("GR-1"));
        assertEquals(adjusted.journalTxnId(), readCanonicalTxnId("GR-1"));
        assertEquals(0, svc.orphanedGrantPostingLinks());

        svc.editNonFinancial(g, "Updated purpose", "CLOSED");
        assertEquals("Updated purpose", g.getPurpose());
        assertEquals("CLOSED", g.getStatus());
    }

    @Test
    void postFinancialEvent_unknownGrantRecord_throws() throws Exception {
        Database.init(tempDir.resolve("grants-fin-missing"));
        Database.get().ensureSchema();
        seedAccounts();

        GrantsFinancePostingService svc = new GrantsFinancePostingService();
        Grant g = new Grant("G1", "Grantor", new BigDecimal("10.00"),
            "2026-01-01", "Program", "ACTIVE");

        assertThrows(IllegalArgumentException.class, () ->
            svc.postFinancialEvent("MISSING", g,
                GrantsFinancePostingService.GrantEventType.AWARD,
                LocalDate.of(2026, 4, 12), new BigDecimal("10.00"),
                "1000", "4000", "F1"));
    }

    private String readLatestPostingRole(String grantRecordId) throws Exception {
        String sql = "SELECT posting_role FROM grant_posting_link WHERE grant_record_id=? ORDER BY id DESC FETCH FIRST 1 ROW ONLY";
        try (Connection c = Database.get().getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, grantRecordId);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getString(1);
            }
        }
    }

    private int readCanonicalTxnId(String grantRecordId) throws Exception {
        try (Connection c = Database.get().getConnection();
             PreparedStatement ps = c.prepareStatement("SELECT canonical_txn_id FROM grant_record WHERE grant_record_id=?")) {
            ps.setString(1, grantRecordId);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    private void seedGrant(String id) throws Exception {
        try (Connection c = Database.get().getConnection();
             PreparedStatement ps = c.prepareStatement("INSERT INTO grant_record(grant_record_id,grant_id,grantor,amount,date_awarded_text,purpose,status,details) VALUES(?,?,?,?,?,?,?,?)")) {
            ps.setString(1,id);ps.setString(2,"G1");ps.setString(3,"Grantor");ps.setBigDecimal(4,new BigDecimal("100.00"));
            ps.setString(5,"2026-01-01");ps.setString(6,"Program");ps.setString(7,"ACTIVE");ps.setString(8,"{}");ps.executeUpdate();
        }
    }

    private void seedAccounts() throws Exception {
        try (Connection c = Database.get().getConnection();
             PreparedStatement ps = c.prepareStatement("MERGE INTO account(account_number,name,account_type,increase_side) KEY(account_number) VALUES (?,?,?,?)")) {
            ps.setString(1,"1000");ps.setString(2,"Cash");ps.setString(3,"ASSET");ps.setString(4,"DEBIT");ps.addBatch();
            ps.setString(1,"4000");ps.setString(2,"Grant Income");ps.setString(3,"INCOME");ps.setString(4,"CREDIT");ps.addBatch();
            ps.executeBatch();
        }
    }

    private int countRows(String sql) throws Exception {
        try (Connection c = Database.get().getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             var rs = ps.executeQuery()) { rs.next(); return rs.getInt(1);} }
}
