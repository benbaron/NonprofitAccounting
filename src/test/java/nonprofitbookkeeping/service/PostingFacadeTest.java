package nonprofitbookkeeping.service;

import nonprofitbookkeeping.core.Database;
import nonprofitbookkeeping.model.AccountSide;
import nonprofitbookkeeping.model.AccountingEntry;
import nonprofitbookkeeping.model.AccountingTransaction;
import nonprofitbookkeeping.persistence.JournalRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class PostingFacadeTest {
    @TempDir Path tempDir;

    @Test
    void post_reverse_amend_and_unbalanced_rejection() throws Exception {
        Path dbPath = tempDir.resolve("posting-facade");
        Database.init(dbPath);
        Database.get().ensureSchema();
        seedAccounts();

        DefaultPostingFacade facade = new DefaultPostingFacade();
        PostingCommand cmd = new PostingCommand(txn(1, new BigDecimal("100.00")), "TEST", "r1", "ORIGINAL", "k1");
        PostingReference posted = facade.post(cmd);
        assertEquals("journal_transaction:1", posted.canonicalRef());

        PostingReference reversed = facade.reverse(posted.journalTxnId(), "void");
        assertTrue(reversed.journalTxnId() > posted.journalTxnId());

        PostingCommand amendCmd = new PostingCommand(txn(3, new BigDecimal("150.00")), "TEST", "r1", "ADJUSTMENT", "k2");
        PostingReference amended = facade.amend(posted.journalTxnId(), amendCmd, "adjust");
        assertEquals(3, amended.journalTxnId());

        PostingCommand bad = new PostingCommand(unbalancedTxn(4), "TEST", "r2", "ORIGINAL", "k3");
        assertThrows(IllegalArgumentException.class, () -> facade.post(bad));
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
             PreparedStatement ps = c.prepareStatement("MERGE INTO account(account_number, name, account_type, increase_side) KEY(account_number) VALUES (?,?,?,?)")) {
            ps.setString(1, "1000"); ps.setString(2, "Cash"); ps.setString(3, "ASSET"); ps.setString(4, "DEBIT"); ps.addBatch();
            ps.setString(1, "4000"); ps.setString(2, "Revenue"); ps.setString(3, "INCOME"); ps.setString(4, "CREDIT"); ps.addBatch();
            ps.executeBatch();
        }
    }
}
