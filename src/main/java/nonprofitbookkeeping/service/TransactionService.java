package nonprofitbookkeeping.service;

import nonprofitbookkeeping.model.AccountingEntry;
import nonprofitbookkeeping.model.AccountingTransaction;
import nonprofitbookkeeping.model.AccountSide;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Provides basic CRUD operations for {@link AccountingTransaction} using
 * the {@link DatabaseManager} utility to access the embedded H2 database.
 */
public class TransactionService {

    /**
     * Inserts the given transaction and all of its entries into the database.
     */
    public static void addTransaction(AccountingTransaction tx) throws SQLException {
        try (Connection conn = DatabaseManager.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO transaction(booking_timestamp, date, memo) VALUES (?,?,?)",
                    Statement.RETURN_GENERATED_KEYS)) {
                ps.setLong(1, tx.getBookingDateTimestamp());
                ps.setString(2, tx.getDate());
                ps.setString(3, tx.getMemo());
                ps.executeUpdate();
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        tx.setId(rs.getInt(1));
                    }
                }
            }
            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO entry(transaction_id, account_id, amount, account_side, account_name) VALUES (?,?,?,?,?)")) {
                for (AccountingEntry e : tx.getEntries()) {
                    ps.setInt(1, tx.getId());
                    ps.setString(2, e.getAccountNumber());
                    ps.setBigDecimal(3, e.getAmount());
                    ps.setString(4, e.getAccountSide().name());
                    ps.setString(5, e.getAccountName());
                    ps.addBatch();
                }
                ps.executeBatch();
            }
            conn.commit();
        }
    }

    /**
     * Retrieves all transactions from the database with their entries.
     */
    public static List<AccountingTransaction> getAllTransactions() throws SQLException {
        List<AccountingTransaction> list = new ArrayList<>();
        try (Connection conn = DatabaseManager.getConnection();
             Statement txStmt = conn.createStatement();
             ResultSet txRs = txStmt.executeQuery("SELECT transaction_id, booking_timestamp, date, memo FROM transaction")) {
            while (txRs.next()) {
                AccountingTransaction tx = new AccountingTransaction();
                tx.setId(txRs.getInt(1));
                tx.setBookingDateTimestamp(txRs.getLong(2));
                tx.setDate(txRs.getString(3));
                tx.setMemo(txRs.getString(4));
                tx.setEntries(fetchEntriesFor(conn, tx.getId()));
                list.add(tx);
            }
        }
        return list;
    }

    private static Set<AccountingEntry> fetchEntriesFor(Connection conn, int transactionId) throws SQLException {
        Set<AccountingEntry> entries = new HashSet<>();
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT account_id, amount, account_side, account_name FROM entry WHERE transaction_id=?")) {
            ps.setInt(1, transactionId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    AccountingEntry e = new AccountingEntry(
                            rs.getBigDecimal(2),
                            rs.getString(1),
                            AccountSide.valueOf(rs.getString(3)),
                            rs.getString(4));
                    entries.add(e);
                    e.setTransaction(null); // ensure transaction set later if needed
                }
            }
        }
        return entries;
    }

    /**
     * Updates the transaction row and replaces its entries.
     */
    public static void updateTransaction(AccountingTransaction tx) throws SQLException {
        try (Connection conn = DatabaseManager.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement ps = conn.prepareStatement(
                    "UPDATE transaction SET booking_timestamp=?, date=?, memo=? WHERE transaction_id=?")) {
                ps.setLong(1, tx.getBookingDateTimestamp());
                ps.setString(2, tx.getDate());
                ps.setString(3, tx.getMemo());
                ps.setInt(4, tx.getId());
                ps.executeUpdate();
            }
            try (PreparedStatement del = conn.prepareStatement("DELETE FROM entry WHERE transaction_id=?")) {
                del.setInt(1, tx.getId());
                del.executeUpdate();
            }
            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO entry(transaction_id, account_id, amount, account_side, account_name) VALUES (?,?,?,?,?)")) {
                for (AccountingEntry e : tx.getEntries()) {
                    ps.setInt(1, tx.getId());
                    ps.setString(2, e.getAccountNumber());
                    ps.setBigDecimal(3, e.getAmount());
                    ps.setString(4, e.getAccountSide().name());
                    ps.setString(5, e.getAccountName());
                    ps.addBatch();
                }
                ps.executeBatch();
            }
            conn.commit();
        }
    }

    /**
     * Deletes a transaction and all its entries by id.
     */
    public static void deleteTransaction(int id) throws SQLException {
        try (Connection conn = DatabaseManager.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement del = conn.prepareStatement("DELETE FROM entry WHERE transaction_id=?")) {
                del.setInt(1, id);
                del.executeUpdate();
            }
            try (PreparedStatement delTx = conn.prepareStatement("DELETE FROM transaction WHERE transaction_id=?")) {
                delTx.setInt(1, id);
                delTx.executeUpdate();
            }
            conn.commit();
        }
    }
}
