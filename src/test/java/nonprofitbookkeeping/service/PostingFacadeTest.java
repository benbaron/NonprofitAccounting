package nonprofitbookkeeping.service;

import nonprofitbookkeeping.core.Database;
import nonprofitbookkeeping.core.FlywayMigrationRunner;
import nonprofitbookkeeping.model.AccountSide;
import nonprofitbookkeeping.model.AccountingEntry;
import nonprofitbookkeeping.model.AccountingTransaction;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class PostingFacadeTest {
    @TempDir Path tempDir;

    @Test
    void post_reverse_amend_and_unbalanced_rejection() throws Exception {
        Path dbPath = this.tempDir.resolve("posting-facade");
        Database.init(dbPath);
        FlywayMigrationRunner.migrateCurrentDatabaseIfEnabled();
        Database.get().ensureSchema();
        seedAccounts();

        DefaultPostingFacade facade = new DefaultPostingFacade();
        PostingCommand cmd = new PostingCommand(txn(1, new BigDecimal("100.00")), "TEST", "r1", "ORIGINAL", "k1");
        PostingReference posted = facade.post(cmd);
        assertEquals("txn:1", posted.canonicalRef());
        assertEquals(1L, posted.canonicalTxnId());
        assertLegacyAndCanonicalRowsExist(1, 2);

        PostingReference reversed = facade.reverse(posted.journalTxnId(), "void");
        assertTrue(reversed.journalTxnId() > posted.journalTxnId());
        assertEquals("txn:" + reversed.journalTxnId(), reversed.canonicalRef());
        assertEquals((long) reversed.journalTxnId(), reversed.canonicalTxnId());
        assertLegacyAndCanonicalRowsExist(reversed.journalTxnId(), 2);

        PostingCommand amendCmd = new PostingCommand(txn(3, new BigDecimal("150.00")), "TEST", "r1", "ADJUSTMENT", "k2");
        PostingReference amended = facade.amend(posted.journalTxnId(), amendCmd, "adjust");
        assertEquals(3, amended.journalTxnId());
        assertEquals("txn:3", amended.canonicalRef());
        assertEquals(3L, amended.canonicalTxnId());
        assertLegacyAndCanonicalRowsExist(3, 2);

        PostingCommand bad = new PostingCommand(unbalancedTxn(4), "TEST", "r2", "ORIGINAL", "k3");
        assertThrows(IllegalArgumentException.class, () -> facade.post(bad));
    }

    @Test
    void canonicalTxnId_returnsNullForNonCanonicalReferences() {
        assertNull(new PostingReference(7, null).canonicalTxnId());
        assertNull(new PostingReference(7, "journal_transaction:7").canonicalTxnId());
        assertNull(new PostingReference(7, "txn:").canonicalTxnId());
    }

    private AccountingTransaction txn(int id, BigDecimal amount) {
        AccountingTransaction txn = new AccountingTransaction();
        txn.setId(id);
        txn.setDate(LocalDate.of(2026,4,22).toString());
        txn.setMemo("t" + id);
        txn.setInfo(Map.of());
        LinkedHashSet<AccountingEntry> entries = new LinkedHashSet<>();
        AccountingEntry d = new AccountingEntry(amount, "1000", AccountSide.DEBIT);
        AccountingEntry c = new AccountingEntry(amount, "4000", AccountSide.CREDIT);
        entries.add(d);entries.add(c);
        txn.setEntries(entries);
        return txn;
    }

    private AccountingTransaction unbalancedTxn(int id) {
        AccountingTransaction txn = new AccountingTransaction();
        txn.setId(id);
        txn.setDate(LocalDate.of(2026,4,22).toString());
        txn.setMemo("bad");
        txn.setInfo(Map.of());
        LinkedHashSet<AccountingEntry> entries = new LinkedHashSet<>();
        entries.add(new AccountingEntry(new BigDecimal("10"), "1000", AccountSide.DEBIT));
        entries.add(new AccountingEntry(new BigDecimal("9"), "4000", AccountSide.CREDIT));
        txn.setEntries(entries);
        return txn;
    }

    private void seedAccounts() throws Exception {
        try (Connection c = Database.get().getConnection();
             PreparedStatement ps = c.prepareStatement("""
                 MERGE INTO account(account_number, name, account_type, increase_side, code, normal_balance, chart_id, is_posting, is_active)
                 KEY(account_number) VALUES (?,?,?,?,?,?,?,?,?)
                 """)) {
            ps.setString(1, "1000"); ps.setString(2, "Cash"); ps.setString(3, "ASSET"); ps.setString(4, "DEBIT");
            ps.setString(5, "1000"); ps.setString(6, "DEBIT"); ps.setLong(7, 1L); ps.setBoolean(8, true); ps.setBoolean(9, true); ps.addBatch();
            ps.setString(1, "4000"); ps.setString(2, "Revenue"); ps.setString(3, "INCOME"); ps.setString(4, "CREDIT");
            ps.setString(5, "4000"); ps.setString(6, "CREDIT"); ps.setLong(7, 1L); ps.setBoolean(8, true); ps.setBoolean(9, true); ps.addBatch();
            ps.executeBatch();
        }
    }

    private void assertLegacyAndCanonicalRowsExist(int txnId, int expectedSplits) throws Exception {
        try (Connection c = Database.get().getConnection()) {
            assertEquals(1, count(c, "SELECT COUNT(*) FROM journal_transaction WHERE id = ?", txnId));
            assertEquals(expectedSplits, count(c, "SELECT COUNT(*) FROM journal_entry WHERE txn_id = ?", txnId));
            assertEquals(1, count(c, "SELECT COUNT(*) FROM txn WHERE id = ?", txnId));
            assertEquals(expectedSplits, count(c, "SELECT COUNT(*) FROM txn_split WHERE txn_id = ?", txnId));
        }
    }

    private int count(Connection c, String sql, int txnId) throws Exception {
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, txnId);
            try (ResultSet rs = ps.executeQuery()) {
                assertTrue(rs.next());
                return rs.getInt(1);
            }
        }
    }
}
