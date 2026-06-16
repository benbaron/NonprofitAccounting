package nonprofitbookkeeping.core;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

final class ReconciledDataCompatibilityBackfill
{
    private static final String SQL_ACCOUNT_CHART_UPDATE =
        "UPDATE account SET chart_id = (SELECT MIN(id) FROM chart_of_accounts) WHERE chart_id IS NULL";

    private static final String SQL_BACKFILL_TXN_INSERT =
        """
            INSERT INTO txn(id, txn_date, memo, created_at, updated_at)
            SELECT jt.id, CURRENT_DATE, jt.memo, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
            FROM journal_transaction jt
            WHERE NOT EXISTS (SELECT 1 FROM txn t WHERE t.id = jt.id)
        """;

    private static final String SQL_BACKFILL_TXN_SPLIT_INSERT =
        """
            INSERT INTO txn_split(txn_id, account_id, fund_id, amount_signed, notes, nmr_flag)
            SELECT je.txn_id, a.id, 1, CASE WHEN UPPER(COALESCE(je.account_side,'DEBIT')) = 'CREDIT' THEN -ABS(je.amount) ELSE ABS(je.amount) END, je.account_name, FALSE
            FROM journal_entry je
            JOIN account a ON a.account_number = je.account_number
            WHERE EXISTS (SELECT 1 FROM txn t WHERE t.id = je.txn_id)
              AND EXISTS (SELECT 1 FROM fund f WHERE f.id = 1)
              AND NOT EXISTS (SELECT 1 FROM txn_split ts WHERE ts.txn_id = je.txn_id AND ts.account_id = a.id AND ts.amount_signed = CASE WHEN UPPER(COALESCE(je.account_side,'DEBIT')) = 'CREDIT' THEN -ABS(je.amount) ELSE ABS(je.amount) END)
        """;

    private static final List<DateTimeFormatter> LEGACY_DATE_FORMATS = List.of(
        DateTimeFormatter.ISO_LOCAL_DATE,
        DateTimeFormatter.BASIC_ISO_DATE,
        DateTimeFormatter.ofPattern("yyyy/MM/dd"),
        DateTimeFormatter.ofPattern("M/d/yyyy"),
        DateTimeFormatter.ofPattern("MM/dd/yyyy"),
        DateTimeFormatter.ofPattern("M-d-yyyy"),
        DateTimeFormatter.ofPattern("MM-dd-yyyy")
    );

    void run(Connection c) throws SQLException
    {
        try (Statement st = c.createStatement())
        {
            linkLegacyAccountsToDefaultChart(st);
            mirrorLegacyJournalTransactions(st);
            mirrorLegacyJournalSplits(st);
        }

        updateTxnDatesFromLegacyText(c);
    }

    private void linkLegacyAccountsToDefaultChart(Statement st)
        throws SQLException
    {
        st.execute(SQL_ACCOUNT_CHART_UPDATE);
    }

    private void mirrorLegacyJournalTransactions(Statement st)
        throws SQLException
    {
        st.execute(SQL_BACKFILL_TXN_INSERT);
    }

    private void mirrorLegacyJournalSplits(Statement st) throws SQLException
    {
        st.execute(SQL_BACKFILL_TXN_SPLIT_INSERT);
    }

    private void updateTxnDatesFromLegacyText(Connection c) throws SQLException
    {
        try (PreparedStatement ps =
            c.prepareStatement("SELECT id, date_text FROM journal_transaction");
            PreparedStatement upd =
                c.prepareStatement("UPDATE txn SET txn_date = ? WHERE id = ?");
            ResultSet rs = ps.executeQuery())
        {
            while (rs.next())
            {
                long id = rs.getLong(1);
                LocalDate parsed = parseLegacyDate(rs.getString(2));
                if (parsed != null)
                {
                    upd.setDate(1, Date.valueOf(parsed));
                    upd.setLong(2, id);
                    upd.addBatch();
                }
            }
            upd.executeBatch();
        }
    }

    private LocalDate parseLegacyDate(String raw)
    {
        if (raw == null || raw.isBlank())
        {
            return null;
        }
        String value = raw.trim();
        for (DateTimeFormatter f : LEGACY_DATE_FORMATS)
        {
            try
            {
                return LocalDate.parse(value, f);
            }
            catch (DateTimeParseException ignored)
            {
                // Try next pattern.
            }
        }
        return null;
    }
}
