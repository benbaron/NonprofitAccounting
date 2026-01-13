package nonprofitbookkeeping.persistence;

import nonprofitbookkeeping.core.Database;
import nonprofitbookkeeping.model.AccountSide;
import nonprofitbookkeeping.model.AccountingEntry;
import nonprofitbookkeeping.model.AccountingTransaction;

import java.sql.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Repository responsible for persisting {@link AccountingTransaction} entries and
 * their related journal information. Operations are designed to run within single
 * database transactions to keep parent transactions aligned with their entries.
 */
public class JournalRepository
{
    private static final Logger LOGGER =
        LoggerFactory.getLogger(JournalRepository.class);

    /**
     * Inserts or updates a single journal transaction and its entries within an
     * isolated transaction.
     *
     * @param txn transaction to persist
     * @throws SQLException if any statement fails
     */
    public void upsertTransaction(AccountingTransaction txn) throws SQLException
    {
        Connection c = null;
        boolean originalAutoCommit = true;

        try
        {
            c = Database.get().getConnection();
            originalAutoCommit = c.getAutoCommit();
            c.setAutoCommit(false);

            if (LOGGER.isDebugEnabled())
            {
                LOGGER.debug("upsertTransaction(): begin txn id={} (autoCommit was {})",
                    txn == null ? null : txn.getId(),
                    originalAutoCommit);
            }

            writeTransaction(c, txn);

            c.commit();

            if (LOGGER.isDebugEnabled())
            {
                LOGGER.debug("upsertTransaction(): commit txn id={}",
                    txn == null ? null : txn.getId());
            }
        }
        catch (SQLException ex)
        {
            if (LOGGER.isWarnEnabled())
            {
                LOGGER.warn("upsertTransaction(): failure txn id={}, attempting rollback",
                    txn == null ? null : txn.getId(),
                    ex);
            }

            if (c != null)
            {
                try
                {
                    c.rollback();
                }
                catch (SQLException rollbackEx)
                {
                    ex.addSuppressed(rollbackEx);
                }
            }

            throw ex;
        }
        finally
        {
            if (c != null)
            {
                try
                {
                    c.setAutoCommit(originalAutoCommit);
                }
                catch (SQLException ex)
                {
                    LOGGER.warn("upsertTransaction(): failed restoring autoCommit={}", originalAutoCommit, ex);
                }
            }

            closeQuietly(c, "Connection (upsertTransaction)");
        }
    }

    /**
     * Replaces the existing journal contents with the supplied transactions.
     * All operations execute inside a single transaction so the journal cannot
     * be left partially updated.
     *
     * @param transactions complete set of transactions to store
     * @throws SQLException if any database interaction fails
     */
    public void replaceAll(List<AccountingTransaction> transactions)
        throws SQLException
    {
        Connection c = null;
        boolean originalAutoCommit = true;

        try
        {
            c = Database.get().getConnection();
            originalAutoCommit = c.getAutoCommit();
            c.setAutoCommit(false);

            if (LOGGER.isDebugEnabled())
            {
                LOGGER.debug("replaceAll(): begin (count={}) (autoCommit was {})",
                    transactions == null ? 0 : transactions.size(),
                    originalAutoCommit);
            }

            replaceAll(c, transactions);

            c.commit();

            if (LOGGER.isDebugEnabled())
            {
                LOGGER.debug("replaceAll(): commit");
            }
        }
        catch (SQLException ex)
        {
            if (LOGGER.isWarnEnabled())
            {
                LOGGER.warn("replaceAll(): failure, attempting rollback", ex);
            }

            if (c != null)
            {
                try
                {
                    c.rollback();
                }
                catch (SQLException rollbackEx)
                {
                    ex.addSuppressed(rollbackEx);
                }
            }

            throw ex;
        }
        finally
        {
            if (c != null)
            {
                try
                {
                    c.setAutoCommit(originalAutoCommit);
                }
                catch (SQLException ex)
                {
                    LOGGER.warn("replaceAll(): failed restoring autoCommit={}", originalAutoCommit, ex);
                }
            }

            closeQuietly(c, "Connection (replaceAll)");
        }
    }

    /**
     * Performs the transaction replacement using the provided connection. Existing
     * rows are cleared before inserting the supplied transactions to ensure
     * consistent ordering and associations.
     *
     * @param c             open database connection
     * @param transactions  transactions to insert
     * @throws SQLException if any statement fails
     */
    void replaceAll(Connection c, List<AccountingTransaction> transactions)
        throws SQLException
    {
        if (c == null)
        {
            throw new IllegalArgumentException("connection required");
        }

        Statement st = null;
        try
        {
            if (LOGGER.isDebugEnabled())
            {
                LOGGER.debug("replaceAll(conn): clearing tables transaction_info, journal_entry, journal_transaction");
            }

            st = c.createStatement();

            int a = st.executeUpdate("DELETE FROM transaction_info");
            int b = st.executeUpdate("DELETE FROM journal_entry");
            int d = st.executeUpdate("DELETE FROM journal_transaction");

            if (LOGGER.isDebugEnabled())
            {
                LOGGER.debug("replaceAll(conn): deleted rows transaction_info={}, journal_entry={}, journal_transaction={}",
                    a, b, d);
            }
        }
        finally
        {
            closeQuietly(st, "Statement (replaceAll delete)");
        }

        if (transactions != null)
        {
            for (AccountingTransaction txn : transactions)
            {
                if (txn == null)
                {
                    continue;
                }

                writeTransaction(c, txn);
            }
        }
    }

    /**
     * Loads all journal transactions, including their entries and supplemental
     * transaction info, ordered by identifier.
     *
     * @return ordered list of persisted transactions
     * @throws SQLException if any query fails
     */
    public List<AccountingTransaction> listTransactions() throws SQLException
    {
        Map<Integer, AccountingTransaction> byId = new LinkedHashMap<>();
        List<AccountingTransaction> transactions = new ArrayList<>();

        Connection c1 = null;
        PreparedStatement ps1 = null;
        ResultSet rs1 = null;

        final String txnSql =
            """
                    SELECT id, booking_ts, date_text, memo, to_from, check_number,
                           clear_bank, budget_tracking, associated_fund_name
                    FROM journal_transaction
                    ORDER BY id
                """;

        try
        {
            c1 = Database.get().getConnection();

            if (LOGGER.isDebugEnabled())
            {
                LOGGER.debug("listTransactions(): SQL (journal_transaction):\n{}", txnSql);
            }

            ps1 = c1.prepareStatement(txnSql);
            rs1 = ps1.executeQuery();

            while (rs1.next())
            {
                AccountingTransaction txn = new AccountingTransaction();
                txn.setId(rs1.getInt("id"));
                txn.setBookingDateTimestamp(rs1.getLong("booking_ts"));
                txn.setDate(rs1.getString("date_text"));
                txn.setMemo(rs1.getString("memo"));
                txn.setToFrom(rs1.getString("to_from"));
                txn.setCheckNumber(rs1.getString("check_number"));
                txn.setClearBank(rs1.getString("clear_bank"));
                txn.setBudgetTracking(rs1.getString("budget_tracking"));
                txn.setAssociatedFundName(rs1.getString("associated_fund_name"));
                txn.setEntries(new LinkedHashSet<>());
                txn.setInfo(new LinkedHashMap<>());

                transactions.add(txn);
                byId.put(txn.getId(), txn);
            }

            if (LOGGER.isDebugEnabled())
            {
                LOGGER.debug("listTransactions(): loaded {} journal_transaction rows", transactions.size());
            }
        }
        finally
        {
            closeQuietly(rs1, "ResultSet (listTransactions journal_transaction)");
            closeQuietly(ps1, "PreparedStatement (listTransactions journal_transaction)");
            closeQuietly(c1, "Connection (listTransactions journal_transaction)");
        }

        if (!byId.isEmpty())
        {
            // --- entries ---
            Connection c2 = null;
            PreparedStatement ps2 = null;
            ResultSet rs2 = null;

            final String entrySql =
                """
                        SELECT txn_id, amount, account_number, account_side, account_name, fund_number, id
                        FROM journal_entry
                        ORDER BY id
                    """;

            try
            {
                c2 = Database.get().getConnection();

                if (LOGGER.isDebugEnabled())
                {
                    LOGGER.debug("listTransactions(): SQL (journal_entry):\n{}", entrySql);
                }

                ps2 = c2.prepareStatement(entrySql);
                rs2 = ps2.executeQuery();

                int entryCount = 0;

                while (rs2.next())
                {
                    AccountingTransaction txn = byId.get(rs2.getInt("txn_id"));
                    if (txn == null)
                    {
                        continue;
                    }

                    String sideText = rs2.getString("account_side");
                    AccountSide side = sideText == null ? AccountSide.UNKNOWN :
                        AccountSide.fromString(sideText);

                    AccountingEntry entry =
                        new AccountingEntry(rs2.getBigDecimal("amount"),
                            rs2.getString("account_number"),
                            side,
                            rs2.getString("account_name"));

                    entry.setFundNumber(rs2.getString("fund_number"));
                    txn.addEntry(entry);
                    entryCount++;
                }

                if (LOGGER.isDebugEnabled())
                {
                    LOGGER.debug("listTransactions(): loaded {} journal_entry rows (matched to existing txns)", entryCount);
                }
            }
            finally
            {
                closeQuietly(rs2, "ResultSet (listTransactions journal_entry)");
                closeQuietly(ps2, "PreparedStatement (listTransactions journal_entry)");
                closeQuietly(c2, "Connection (listTransactions journal_entry)");
            }

            // --- info ---
            Connection c3 = null;
            PreparedStatement ps3 = null;
            ResultSet rs3 = null;

            final String infoSql =
                """
                        SELECT txn_id, k, v FROM transaction_info ORDER BY txn_id, k
                    """;

            try
            {
                c3 = Database.get().getConnection();

                if (LOGGER.isDebugEnabled())
                {
                    LOGGER.debug("listTransactions(): SQL (transaction_info):\n{}", infoSql);
                }

                ps3 = c3.prepareStatement(infoSql);
                rs3 = ps3.executeQuery();

                int infoCount = 0;

                while (rs3.next())
                {
                    AccountingTransaction txn = byId.get(rs3.getInt("txn_id"));
                    if (txn == null)
                    {
                        continue;
                    }

                    txn.getInfo().put(rs3.getString("k"), rs3.getString("v"));
                    infoCount++;
                }

                if (LOGGER.isDebugEnabled())
                {
                    LOGGER.debug("listTransactions(): loaded {} transaction_info rows (matched to existing txns)", infoCount);
                }
            }
            finally
            {
                closeQuietly(rs3, "ResultSet (listTransactions transaction_info)");
                closeQuietly(ps3, "PreparedStatement (listTransactions transaction_info)");
                closeQuietly(c3, "Connection (listTransactions transaction_info)");
            }
        }

        return transactions;
    }

    /**
     * Writes the transaction header, entries, and arbitrary transaction metadata
     * using the provided connection. Existing entries and metadata for the same
     * transaction id are removed before inserting the current values.
     *
     * @param c   open connection participating in an outer transaction
     * @param txn transaction to write
     * @throws SQLException if any statement fails
     */
    private void writeTransaction(Connection c, AccountingTransaction txn)
        throws SQLException
    {
        if (txn == null)
        {
            throw new IllegalArgumentException("transaction required");
        }

        ensureAccountsExist(c, txn.getEntries());

        final String upsertTxn =
            """
                MERGE INTO journal_transaction(id, booking_ts, date_text, memo, to_from, check_number,
                               clear_bank, budget_tracking, associated_fund_name)
                        KEY(id)
                        VALUES(?,?,?,?,?,?,?,?,?)
            """;

        PreparedStatement ps = null;
        try
        {
            if (LOGGER.isDebugEnabled())
            {
                LOGGER.debug("writeTransaction(): upsert journal_transaction SQL:\n{}", upsertTxn);
                LOGGER.debug("writeTransaction(): upsert params txn_id={} booking_ts={} date_text={} memo={} to_from={} check_number={} clear_bank={} budget_tracking={} associated_fund_name={}",
                    txn.getId(),
                    txn.getBookingDateTimestamp(),
                    txn.getDate(),
                    txn.getMemo(),
                    txn.getToFrom(),
                    txn.getCheckNumber(),
                    txn.getClearBank(),
                    txn.getBudgetTracking(),
                    txn.getAssociatedFundName());
            }

            ps = c.prepareStatement(upsertTxn);

            int i = 0;
            ps.setInt(++i, txn.getId());
            ps.setLong(++i, txn.getBookingDateTimestamp());
            ps.setString(++i, txn.getDate());
            ps.setString(++i, txn.getMemo());
            ps.setString(++i, txn.getToFrom());
            ps.setString(++i, txn.getCheckNumber());
            ps.setString(++i, txn.getClearBank());
            ps.setString(++i, txn.getBudgetTracking());
            ps.setString(++i, txn.getAssociatedFundName());

            int rows = ps.executeUpdate();

            if (LOGGER.isDebugEnabled())
            {
                LOGGER.debug("writeTransaction(): upsert journal_transaction affectedRows={} txn_id={}", rows, txn.getId());
            }
        }
        finally
        {
            closeQuietly(ps, "PreparedStatement (writeTransaction upsert journal_transaction)");
        }

        // Delete existing entries
        PreparedStatement delEntries = null;
        final String delEntriesSql = "DELETE FROM journal_entry WHERE txn_id=?";
        try
        {
            if (LOGGER.isDebugEnabled())
            {
                LOGGER.debug("writeTransaction(): delete journal_entry SQL: {}", delEntriesSql);
                LOGGER.debug("writeTransaction(): delete journal_entry params txn_id={}", txn.getId());
            }

            delEntries = c.prepareStatement(delEntriesSql);
            delEntries.setInt(1, txn.getId());

            int deleted = delEntries.executeUpdate();

            if (LOGGER.isDebugEnabled())
            {
                LOGGER.debug("writeTransaction(): delete journal_entry deletedRows={} txn_id={}", deleted, txn.getId());
            }
        }
        finally
        {
            closeQuietly(delEntries, "PreparedStatement (writeTransaction delete journal_entry)");
        }

        // Insert entries
        PreparedStatement insEntries = null;
        final String insEntriesSql =
            """
                INSERT INTO journal_entry(txn_id, amount, account_number, account_side, account_name, fund_number)
                VALUES (?,?,?,?,?,?)
            """;

        try
        {
            if (LOGGER.isDebugEnabled())
            {
                LOGGER.debug("writeTransaction(): insert journal_entry SQL:\n{}", insEntriesSql);
            }

            insEntries = c.prepareStatement(insEntriesSql);

            List<AccountingEntry> batchEntries = new ArrayList<>();

            for (AccountingEntry e : txn.getEntries())
            {
                if (e == null)
                {
                    LOGGER.warn("Skipping null journal entry for transaction id={}", txn.getId());
                    continue;
                }

                if (e.getAmount() == null)
                {
                    throw new SQLException("Journal entry amount is required for transaction id="
                        + txn.getId() + ", entry=" + e);
                }

                if (e.getAccountNumber() == null || e.getAccountNumber().isBlank())
                {
                    throw new SQLException("Journal entry account number is required for transaction id="
                        + txn.getId() + ", entry=" + e);
                }

                if (LOGGER.isDebugEnabled())
                {
                    LOGGER.debug("writeTransaction(): journal_entry batch item txn_id={} amount={} account_number={} account_side={} account_name={} fund_number={}",
                        txn.getId(),
                        e.getAmount(),
                        e.getAccountNumber(),
                        e.getAccountSide() == null ? null : e.getAccountSide().name(),
                        e.getAccountName(),
                        e.getFundNumber());
                }

                int j = 0;
                insEntries.setInt(++j, txn.getId());
                insEntries.setBigDecimal(++j, e.getAmount());
                insEntries.setString(++j, e.getAccountNumber());
                insEntries.setString(++j, e.getAccountSide() == null ? null :
                    e.getAccountSide().name());
                insEntries.setString(++j, e.getAccountName());
                insEntries.setString(++j, e.getFundNumber());

                insEntries.addBatch();
                batchEntries.add(e);
            }

            int[] results;
            try
            {
                results = insEntries.executeBatch();
            }
            catch (BatchUpdateException ex)
            {
                logBatchFailure(txn, batchEntries, ex.getUpdateCounts(), ex);
                throw ex;
            }

            if (LOGGER.isDebugEnabled())
            {
                int inserted = 0;
                for (int result : results)
                {
                    if (result > 0)
                    {
                        inserted += result;
                    }
                    else if (result == Statement.SUCCESS_NO_INFO)
                    {
                        inserted++;
                    }
                    else if (result == Statement.EXECUTE_FAILED)
                    {
                        LOGGER.warn("Failed journal entry batch item for transaction id={}", txn.getId());
                    }
                }

                LOGGER.debug("Inserted {} journal entries for transaction id={}", inserted, txn.getId());
            }
        }
        finally
        {
            closeQuietly(insEntries, "PreparedStatement (writeTransaction insert journal_entry)");
        }

        // Delete existing info
        PreparedStatement delInfo = null;
        final String delInfoSql = "DELETE FROM transaction_info WHERE txn_id=?";
        try
        {
            if (LOGGER.isDebugEnabled())
            {
                LOGGER.debug("writeTransaction(): delete transaction_info SQL: {}", delInfoSql);
                LOGGER.debug("writeTransaction(): delete transaction_info params txn_id={}", txn.getId());
            }

            delInfo = c.prepareStatement(delInfoSql);
            delInfo.setInt(1, txn.getId());

            int deleted = delInfo.executeUpdate();

            if (LOGGER.isDebugEnabled())
            {
                LOGGER.debug("writeTransaction(): delete transaction_info deletedRows={} txn_id={}", deleted, txn.getId());
            }
        }
        finally
        {
            closeQuietly(delInfo, "PreparedStatement (writeTransaction delete transaction_info)");
        }

        Map<String, String> info = txn.getInfo();

        if (info != null && !info.isEmpty())
        {
            PreparedStatement insInfo = null;
            final String insInfoSql = "INSERT INTO transaction_info(txn_id, k, v) VALUES (?,?,?)";

            try
            {
                if (LOGGER.isDebugEnabled())
                {
                    LOGGER.debug("writeTransaction(): insert transaction_info SQL: {}", insInfoSql);
                }

                insInfo = c.prepareStatement(insInfoSql);

                for (Map.Entry<String, String> en : info.entrySet())
                {
                    if (LOGGER.isDebugEnabled())
                    {
                        LOGGER.debug("writeTransaction(): transaction_info batch item txn_id={} k={} v={}",
                            txn.getId(), en.getKey(), en.getValue());
                    }

                    insInfo.setInt(1, txn.getId());
                    insInfo.setString(2, en.getKey());
                    insInfo.setString(3, en.getValue());
                    insInfo.addBatch();
                }

                int[] results = insInfo.executeBatch();

                if (LOGGER.isDebugEnabled())
                {
                    int inserted = 0;
                    if (results != null)
                    {
                        for (int r : results)
                        {
                            if (r > 0)
                            {
                                inserted += r;
                            }
                            else if (r == Statement.SUCCESS_NO_INFO)
                            {
                                inserted++;
                            }
                        }
                    }

                    LOGGER.debug("Inserted {} transaction_info rows for transaction id={}", inserted, txn.getId());
                }
            }
            finally
            {
                closeQuietly(insInfo, "PreparedStatement (writeTransaction insert transaction_info)");
            }
        }
        else
        {
            if (LOGGER.isDebugEnabled())
            {
                LOGGER.debug("writeTransaction(): no transaction_info rows to insert for txn id={}", txn.getId());
            }
        }
    }

    private void logBatchFailure(AccountingTransaction txn,
        List<AccountingEntry> batchEntries,
        int[] updateCounts,
        BatchUpdateException ex)
    {
        LOGGER.error("Journal entry batch insert failed for transaction id={}",
            txn.getId(),
            ex);
        if (updateCounts == null)
        {
            return;
        }
        int entriesToLog = Math.min(updateCounts.length, batchEntries.size());
        for (int idx = 0; idx < entriesToLog; idx++)
        {
            if (updateCounts[idx] == Statement.EXECUTE_FAILED)
            {
                LOGGER.error("Failed journal entry at batch index {} for transaction id={}: {}",
                    idx,
                    txn.getId(),
                    batchEntries.get(idx));
            }
        }
    }

    private void ensureAccountsExist(Connection c,
        Iterable<AccountingEntry> entries)
        throws SQLException
    {
        if (entries == null)
        {
            return;
        }

        PreparedStatement ps = null;
        final String sql = "MERGE INTO account(account_number, name) KEY(account_number) VALUES (?,?)";

        try
        {
            if (LOGGER.isDebugEnabled())
            {
                LOGGER.debug("ensureAccountsExist(): SQL: {}", sql);
            }

            ps = c.prepareStatement(sql);

            int count = 0;

            for (AccountingEntry entry : entries)
            {
                if (entry == null)
                {
                    continue;
                }

                String accountNumber = entry.getAccountNumber();
                if (accountNumber == null || accountNumber.isBlank())
                {
                    continue;
                }

                String accountName = entry.getAccountName();
                if (accountName == null || accountName.isBlank())
                {
                    accountName = accountNumber;
                }

                if (LOGGER.isDebugEnabled())
                {
                    LOGGER.debug("ensureAccountsExist(): batch item account_number={} name={}", accountNumber, accountName);
                }

                ps.setString(1, accountNumber);
                ps.setString(2, accountName);
                ps.addBatch();
                count++;
            }

            int[] results = ps.executeBatch();

            if (LOGGER.isDebugEnabled())
            {
                int merged = 0;
                if (results != null)
                {
                    for (int r : results)
                    {
                        if (r > 0)
                        {
                            merged += r;
                        }
                        else if (r == Statement.SUCCESS_NO_INFO)
                        {
                            merged++;
                        }
                    }
                }

                LOGGER.debug("ensureAccountsExist(): executed batch items={} affectedApprox={}", count, merged);
            }
        }
        finally
        {
            closeQuietly(ps, "PreparedStatement (ensureAccountsExist)");
        }
    }

    private static void closeQuietly(AutoCloseable c, String what)
    {
        if (c == null)
        {
            return;
        }
        try
        {
            c.close();
        }
        catch (Exception ex)
        {
            // close() failures should not mask the real exception, but are useful for diagnostics.
            LOGGER.warn("Failed to close {}", what, ex);
        }
    }
}
