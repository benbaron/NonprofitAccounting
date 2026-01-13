
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
		
		try (Connection c = Database.get().getConnection())
		{
			c.setAutoCommit(false);
			writeTransaction(c, txn);
			c.commit();
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
		
		try (Connection c = Database.get().getConnection())
		{
			boolean originalAutoCommit = c.getAutoCommit();
			c.setAutoCommit(false);
			
			try
			{
				replaceAll(c, transactions);
				c.commit();
			}
			catch (SQLException ex)
			{
				
				try
				{
					c.rollback();
				}
				catch (SQLException rollbackEx)
				{
					ex.addSuppressed(rollbackEx);
				}
				
				throw ex;
			}
			finally
			{
				c.setAutoCommit(originalAutoCommit);
			}
			
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
		
		try (Statement st = c.createStatement())
		{
			st.executeUpdate("DELETE FROM transaction_info");
			st.executeUpdate("DELETE FROM journal_entry");
			st.executeUpdate("DELETE FROM journal_transaction");
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
		
		try (Connection c = Database.get().getConnection();
			PreparedStatement ps = c.prepareStatement(
				"""
					    SELECT id, booking_ts, date_text, memo, to_from, check_number,
					           clear_bank, budget_tracking, associated_fund_name
					    FROM journal_transaction
					    ORDER BY id
					""");
			ResultSet rs = ps.executeQuery())
		{
			
			while (rs.next())
			{
				AccountingTransaction txn = new AccountingTransaction();
				txn.setId(rs.getInt("id"));
				txn.setBookingDateTimestamp(rs.getLong("booking_ts"));
				txn.setDate(rs.getString("date_text"));
				txn.setMemo(rs.getString("memo"));
				txn.setToFrom(rs.getString("to_from"));
				txn.setCheckNumber(rs.getString("check_number"));
				txn.setClearBank(rs.getString("clear_bank"));
				txn.setBudgetTracking(rs.getString("budget_tracking"));
				txn.setAssociatedFundName(rs.getString("associated_fund_name"));
				txn.setEntries(new LinkedHashSet<>());
				txn.setInfo(new LinkedHashMap<>());
				transactions.add(txn);
				byId.put(txn.getId(), txn);
			}
			
		}
		
		if (!byId.isEmpty())
		{
			
			try (Connection c = Database.get().getConnection();
				PreparedStatement ps = c.prepareStatement(
					"""
						    SELECT txn_id, amount, account_number, account_side, account_name, fund_number, id
						    FROM journal_entry
						    ORDER BY id
						""");
				ResultSet rs = ps.executeQuery())
			{
				
				while (rs.next())
				{
					AccountingTransaction txn = byId.get(rs.getInt("txn_id"));
					
					if (txn == null)
					{
						continue;
					}
					
					String sideText = rs.getString("account_side");
					AccountSide side = sideText == null ? AccountSide.UNKNOWN :
						AccountSide.fromString(sideText);
					AccountingEntry entry =
						new AccountingEntry(rs.getBigDecimal("amount"),
							rs.getString("account_number"),
							side,
							rs.getString("account_name"));
					entry.setFundNumber(rs.getString("fund_number"));
					txn.addEntry(entry);
				}
				
			}
			
			try (Connection c = Database.get().getConnection();
				PreparedStatement ps = c.prepareStatement(
					"""
						    SELECT txn_id, k, v FROM transaction_info ORDER BY txn_id, k
						""");
				ResultSet rs = ps.executeQuery())
			{
				
				while (rs.next())
				{
					AccountingTransaction txn = byId.get(rs.getInt("txn_id"));
					
					if (txn == null)
					{
						continue;
					}
					
					txn.getInfo().put(rs.getString("k"), rs.getString("v"));
				}
				
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
		ensureAccountsExist(c, txn.getEntries());
		
		String upsertTxn =
			"""
				MERGE INTO journal_transaction(id, booking_ts, date_text, memo, to_from, check_number,
	                       clear_bank, budget_tracking, associated_fund_name)
					    KEY(id)
					    VALUES(?,?,?,?,?,?,?,?,?)
			""";
		
		try (PreparedStatement ps = c.prepareStatement(upsertTxn))
		{
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
			ps.executeUpdate();
		}
		
		try (PreparedStatement del =
			c.prepareStatement("DELETE FROM journal_entry WHERE txn_id=?"))
		{
			del.setInt(1, txn.getId());
			del.executeUpdate();
		}
		
		try (PreparedStatement ins = c.prepareStatement(
			"""
		    INSERT INTO journal_entry(txn_id, amount, account_number, account_side, account_name, fund_number)
		    VALUES (?,?,?,?,?,?)
			"""))
		{
			
			for (AccountingEntry e : txn.getEntries())
			{
				int j = 0;
				ins.setInt(++j, txn.getId());
				ins.setBigDecimal(++j, e.getAmount());
				ins.setString(++j, e.getAccountNumber());
				ins.setString(++j, e.getAccountSide() == null ? null :
					e.getAccountSide().name());
				ins.setString(++j, e.getAccountName());
				ins.setString(++j, e.getFundNumber());
				ins.addBatch();
			}
			
			int[] results = ins.executeBatch();
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
				}
				LOGGER.debug("Inserted {} journal entries for transaction id={}",
					inserted,
					txn.getId());
			}
		}
		
		try (PreparedStatement del =
			c.prepareStatement("DELETE FROM transaction_info WHERE txn_id=?"))
		{
			del.setInt(1, txn.getId());
			del.executeUpdate();
		}
		
		Map<String, String> info = txn.getInfo();
		
		if (info != null && !info.isEmpty())
		{
			
			try (PreparedStatement ins = c.prepareStatement(
				"INSERT INTO transaction_info(txn_id, k, v) VALUES (?,?,?)"))
			{
				
				for (Map.Entry<String, String> en : info.entrySet())
				{
					ins.setInt(1, txn.getId());
					ins.setString(2, en.getKey());
					ins.setString(3, en.getValue());
					ins.addBatch();
				}
				
				ins.executeBatch();
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
		
		try (PreparedStatement ps = c.prepareStatement(
			"MERGE INTO account(account_number, name) KEY(account_number) VALUES (?,?)"))
		{
			
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
				
				ps.setString(1, accountNumber);
				ps.setString(2, accountName);
				ps.addBatch();
			}
			
			ps.executeBatch();
		}
		
	}
	
}
