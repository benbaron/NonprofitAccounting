package nonprofitbookkeeping.service.scaledger;

import nonprofitbookkeeping.TestDatabase;
import nonprofitbookkeeping.core.Database;
import nonprofitbookkeeping.model.AccountSide;
import nonprofitbookkeeping.model.AccountingEntry;
import nonprofitbookkeeping.model.AccountingTransaction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;

import static org.junit.jupiter.api.Assertions.assertEquals;

class JournalLedgerPersistenceGatewayStableIdTest
{
    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() throws Exception
    {
        TestDatabase.reset(this.tempDir);
    }

    @Test
    void reimportingSameSclxTransactionReusesDatabaseId() throws Exception
    {
        JournalLedgerPersistenceGateway gateway = new JournalLedgerPersistenceGateway();

        AccountingTransaction first = transaction("ledger-row-9", "First import");
        gateway.saveTransactionWithEntries(first);
        int firstId = first.getId();

        AccountingTransaction retry = transaction("ledger-row-9", "Corrected retry");
        gateway.saveTransactionWithEntries(retry);

        assertEquals(firstId, retry.getId());
        assertEquals(1, countMappings("ledger-row-9"));
        assertEquals("Corrected retry", loadMemo(firstId));
    }

    private static AccountingTransaction transaction(String sclxId, String memo)
    {
        AccountingTransaction transaction = new AccountingTransaction();
        transaction.setDate("2026-01-03");
        transaction.setMemo(memo);
        LinkedHashSet<AccountingEntry> entries = new LinkedHashSet<>();
        entries.add(new AccountingEntry(new BigDecimal("25.00"), "1000", AccountSide.DEBIT));
        entries.add(new AccountingEntry(new BigDecimal("25.00"), "4000", AccountSide.CREDIT));
        transaction.setEntries(entries);
        LinkedHashMap<String, String> info = new LinkedHashMap<>();
        info.put("sclx.transactionId", sclxId);
        transaction.setInfo(info);
        return transaction;
    }

    private static int countMappings(String sclxId) throws Exception
    {
        try (Connection connection = Database.get().getConnection();
             PreparedStatement ps = connection.prepareStatement(
                 "SELECT COUNT(*) FROM transaction_info WHERE k = 'sclx.transactionId' AND v = ?"))
        {
            ps.setString(1, sclxId);
            try (ResultSet rs = ps.executeQuery())
            {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    private static String loadMemo(int transactionId) throws Exception
    {
        try (Connection connection = Database.get().getConnection();
             PreparedStatement ps = connection.prepareStatement(
                 "SELECT memo FROM journal_transaction WHERE id = ?"))
        {
            ps.setInt(1, transactionId);
            try (ResultSet rs = ps.executeQuery())
            {
                rs.next();
                return rs.getString(1);
            }
        }
    }
}
