
package nonprofitbookkeeping.persistence;

import nonprofitbookkeeping.core.Database;
import nonprofitbookkeeping.model.AccountingEntry;
import nonprofitbookkeeping.model.AccountingTransaction;

import java.sql.*;
import java.util.Map;

public class JournalRepository {

    public void upsertTransaction(AccountingTransaction txn) throws SQLException {
        String upsertTxn = """
            MERGE INTO journal_transaction(id, booking_ts, date_text, memo, to_from, check_number,
                                           clear_bank, budget_tracking, associated_fund_name)
            KEY(id)
            VALUES(?,?,?,?,?,?,?,?,?)
        """;
        try (Connection c = Database.get().getConnection()) {
            c.setAutoCommit(false);
            try (PreparedStatement ps = c.prepareStatement(upsertTxn)) {
                int i=0;
                ps.setInt(++i, txn.getId());
                ps.setLong(++i, txn.getBookingDateTimestamp());
                ps.setString(++i, txn.getDate());
                ps.setString(++i, txn.getMemo());
                ps.setString(++i, txn.getToFrom());
                ps.setString(++i, txn.getCheckNumber());
                ps.setString(++i, txn.getClearBank());
                ps.setString(++i, txn.getBudgetTracking());
                ps.setString(++i, txn.getAssociatedFundName());
                ps.executeUpdate();
            }
            try (PreparedStatement del = c.prepareStatement("DELETE FROM journal_entry WHERE txn_id=?")) {
                del.setInt(1, txn.getId());
                del.executeUpdate();
            }
            try (PreparedStatement ins = c.prepareStatement("""
                    INSERT INTO journal_entry(txn_id, amount, account_number, account_side, account_name, fund_number)
                    VALUES (?,?,?,?,?,?)
                """)) {
                for (AccountingEntry e : txn.getEntries()) {
                    int j=0;
                    ins.setInt(++j, txn.getId());
                    ins.setBigDecimal(++j, e.getAmount());
                    ins.setString(++j, e.getAccountNumber());
                    ins.setString(++j, e.getAccountSide()==null? null : e.getAccountSide().name());
                    ins.setString(++j, e.getAccountName());
                    ins.setString(++j, e.getFundNumber());
                    ins.addBatch();
                }
                ins.executeBatch();
            }
            try (PreparedStatement del = c.prepareStatement("DELETE FROM transaction_info WHERE txn_id=?")) {
                del.setInt(1, txn.getId());
                del.executeUpdate();
            }
            Map<String,String> info = txn.getInfo();
            if (info!=null) {
                try (PreparedStatement ins = c.prepareStatement(
                        "INSERT INTO transaction_info(txn_id, k, v) VALUES (?,?,?)")) {
                    for (Map.Entry<String,String> en : info.entrySet()) {
                        ins.setInt(1, txn.getId());
                        ins.setString(2, en.getKey());
                        ins.setString(3, en.getValue());
                        ins.addBatch();
                    }
                    ins.executeBatch();
                }
            }
            c.commit();
        }
    }
}
