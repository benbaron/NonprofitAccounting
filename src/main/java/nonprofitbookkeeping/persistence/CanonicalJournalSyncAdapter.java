package nonprofitbookkeeping.persistence;

import nonprofitbookkeeping.model.AccountSide;
import nonprofitbookkeeping.model.AccountingEntry;
import nonprofitbookkeeping.model.AccountingTransaction;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Synchronizes legacy journal rows with canonical JPA txn / txn_split tables.
 */
final class CanonicalJournalSyncAdapter
{
    private static final List<DateTimeFormatter> DATE_FORMATS = List.of(
        DateTimeFormatter.ISO_LOCAL_DATE,
        DateTimeFormatter.BASIC_ISO_DATE,
        DateTimeFormatter.ofPattern("yyyy/MM/dd"),
        DateTimeFormatter.ofPattern("M/d/yyyy"),
        DateTimeFormatter.ofPattern("MM/dd/yyyy"),
        DateTimeFormatter.ofPattern("M-d-yyyy"),
        DateTimeFormatter.ofPattern("MM-dd-yyyy")
    );

    private CanonicalJournalSyncAdapter() {}

    static void syncTransaction(Connection c, AccountingTransaction txn) throws SQLException
    {
        if (txn == null)
        {
            return;
        }

        upsertTxn(c, txn);
        replaceSplits(c, txn);
    }

    static void deleteLegacyTxnIds(Connection c, Collection<Integer> txnIds) throws SQLException
    {
        if (txnIds == null || txnIds.isEmpty())
        {
            return;
        }

        try (PreparedStatement delSplits = c.prepareStatement("DELETE FROM txn_split WHERE txn_id = ?");
             PreparedStatement delTxn = c.prepareStatement("DELETE FROM txn WHERE id = ?"))
        {
            for (Integer id : txnIds)
            {
                if (id == null)
                {
                    continue;
                }
                delSplits.setLong(1, id);
                delSplits.addBatch();
                delTxn.setLong(1, id);
                delTxn.addBatch();
            }
            delSplits.executeBatch();
            delTxn.executeBatch();
        }
    }

    static List<Integer> listLegacyTxnIds(Connection c) throws SQLException
    {
        List<Integer> ids = new ArrayList<>();
        try (PreparedStatement ps = c.prepareStatement("SELECT id FROM journal_transaction");
             ResultSet rs = ps.executeQuery())
        {
            while (rs.next())
            {
                ids.add(rs.getInt(1));
            }
        }
        return ids;
    }

    private static void upsertTxn(Connection c, AccountingTransaction txn) throws SQLException
    {
        LocalDate date = parseDate(txn.getDate());
        if (date == null)
        {
            date = LocalDate.now();
        }

        try (PreparedStatement ps = c.prepareStatement(
            "MERGE INTO txn(id, txn_date, memo, created_at, updated_at) KEY(id) VALUES (?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)"))
        {
            ps.setInt(1, txn.getId());
            ps.setDate(2, Date.valueOf(date));
            ps.setString(3, txn.getMemo());
            ps.executeUpdate();
        }
    }

    private static void replaceSplits(Connection c, AccountingTransaction txn) throws SQLException
    {
        try (PreparedStatement del = c.prepareStatement("DELETE FROM txn_split WHERE txn_id = ?"))
        {
            del.setInt(1, txn.getId());
            del.executeUpdate();
        }

        if (txn.getEntries() == null || txn.getEntries().isEmpty())
        {
            return;
        }

        try (PreparedStatement ins = c.prepareStatement(
            "INSERT INTO txn_split(txn_id, account_id, fund_id, amount_signed, notes, nmr_flag) " +
                "SELECT ?, a.id, 1, ?, ?, FALSE FROM account a WHERE a.account_number = ?"))
        {
            for (AccountingEntry entry : txn.getEntries())
            {
                if (entry == null || entry.getAccountNumber() == null || entry.getAmount() == null)
                {
                    continue;
                }
                ins.setInt(1, txn.getId());
                ins.setBigDecimal(2, signedAmount(entry));
                ins.setString(3, entry.getAccountName());
                ins.setString(4, entry.getAccountNumber());
                ins.addBatch();
            }
            ins.executeBatch();
        }
    }

    private static BigDecimal signedAmount(AccountingEntry entry)
    {
        BigDecimal amount = entry.getAmount();
        if (entry.getAccountSide() == AccountSide.CREDIT)
        {
            return amount.negate();
        }
        return amount;
    }

    private static LocalDate parseDate(String raw)
    {
        if (raw == null || raw.isBlank())
        {
            return null;
        }

        String value = raw.trim();
        for (DateTimeFormatter formatter : DATE_FORMATS)
        {
            try
            {
                return LocalDate.parse(value, formatter);
            }
            catch (DateTimeParseException ignored)
            {
                // try next format
            }
        }
        return null;
    }
}
