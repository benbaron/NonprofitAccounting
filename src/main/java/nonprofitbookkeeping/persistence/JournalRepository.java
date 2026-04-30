
package nonprofitbookkeeping.persistence;

import jakarta.enterprise.context.ApplicationScoped;

import nonprofitbookkeeping.core.Database;
import nonprofitbookkeeping.model.AccountSide;
import nonprofitbookkeeping.model.AccountingEntry;
import nonprofitbookkeeping.model.AccountingTransaction;
import nonprofitbookkeeping.model.supplemental.TxnSupplementalLineBase;
import nonprofitbookkeeping.persistence.supplemental.TxnSupplementalLineMapper;
import nonprofitbookkeeping.persistence.supplemental.TxnSupplementalLineRecord;
import nonprofitbookkeeping.persistence.supplemental.TxnSupplementalLineRepository;

import java.sql.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// TODO: Auto-generated Javadoc
/**
 * JournalRepository
 * -----------------
 *
 * This repository is responsible for persisting and loading the application's journal:
 * a set of accounting transactions (headers) plus their line items (entries) and
 * optional key/value metadata (transaction_info).
 *
 * Schema assumptions (as implied by the SQL in this class):
 *
 * 1) journal_transaction
 *    - One row per transaction/header.
 *    - Primary key (or merge key) is id.
 *    - Stores date/memo/payee/check/etc and other header attributes.
 *
 * 2) journal_entry
 *    - One row per transaction line item.
 *    - Has a foreign key-like column txn_id referencing journal_transaction.id.
 *    - Contains amount, account_number, debit/credit side, account name, fund number.
 *
 * 3) transaction_info
 *    - Optional key/value metadata for a transaction.
 *    - Has txn_id to relate metadata back to journal_transaction.id.
 *
 * 4) account
 *    - Chart-of-accounts master table.
 *    - Has account_number and a display name.
 *    - ensureAccountsExist() upserts accounts referenced by entries so
 *      journal_entry rows don't reference missing accounts.
 *
 * Transaction / atomicity goals:
 *
 * - upsertTransaction(txn) uses a *single DB transaction*:
 *     MERGE header, replace entries, replace metadata, then COMMIT.
 *   If any step fails, the caller sees an exception and the DB is rolled back.
 *
 * - replaceAll(transactions) uses a *single DB transaction*:
 *     clear tables then insert all supplied txns. If any insert fails, rollback.
 *
 * Replace strategy for child rows:
 *
 * - Instead of trying to diff journal entries/metadata, this class deletes all
 *   existing child rows for a transaction id and re-inserts the current set.
 *   This is simple and avoids partial updates or stale rows.
 */
@ApplicationScoped
public class JournalRepository
{
	private static final Object NEXT_TXN_ID_MONITOR = new Object();
	private static int LAST_RESERVED_TXN_ID = 0;
	
	/** The Constant LOGGER. */
	private static final Logger LOGGER =
		LoggerFactory.getLogger(JournalRepository.class);
	
	/**
	 * Inserts or updates a single journal transaction and its entries inside
	 * a single database transaction.
	 *
	 * SQL actions performed (via writeTransaction):
	 *  - MERGE INTO journal_transaction ... KEY(id) ... : upsert the header row
	 *  - DELETE FROM journal_entry WHERE txn_id=?      : remove old line items
	 *  - INSERT INTO journal_entry(...) VALUES(...)    : insert new line items
	 *  - DELETE FROM transaction_info WHERE txn_id=?   : remove old metadata
	 *  - INSERT INTO transaction_info(...) VALUES(...) : insert new metadata
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
			// Acquire a new connection from Database (likely a pool or
			// DriverManager wrapper).
			c = Database.get().getConnection();
			
			// Save original autocommit so we can restore it before closing.
			originalAutoCommit = c.getAutoCommit();
			
			// Turn off autocommit to start an explicit transaction.
			c.setAutoCommit(false);
			
			if (LOGGER.isDebugEnabled())
			{
				LOGGER.debug(
					"upsertTransaction(): begin txn id={} (autoCommit was {})",
					txn == null ? null : txn.getId(),
					originalAutoCommit);
			}
			
			// Perform the upsert+replace operations on this open transaction.
			writeTransaction(c, txn);
			
			// If everything succeeded, commit the transaction.
			c.commit();
			
			if (LOGGER.isDebugEnabled())
			{
				LOGGER.debug("upsertTransaction(): commit txn id={}",
					txn == null ? null : txn.getId());
			}
			
		}
		catch (SQLException ex)
		{
			// Any failure: attempt rollback to prevent partial updates.
			LOGGER.warn(
				"upsertTransaction(): failure txn id={}, attempting rollback",
				txn == null ? null : txn.getId(),
				ex);
			
			if (c != null)
			{
				
				try
				{
					c.rollback();
				}
				catch (SQLException rollbackEx)
				{
					// If rollback fails too, preserve that information without
					// masking the real error.
					ex.addSuppressed(rollbackEx);
				}
				
			}
			
			throw ex;
		}
		finally
		{
			
			// Best-effort restore the original autocommit setting.
			if (c != null)
			{
				
				try
				{
					c.setAutoCommit(originalAutoCommit);
				}
				catch (SQLException ex)
				{
					LOGGER.warn(
						"upsertTransaction(): failed restoring autoCommit={}",
						originalAutoCommit, ex);
				}
				
			}
			
			// Always close the connection.
			closeQuietly(c, "Connection (upsertTransaction)");
		}
		
	}

	public Optional<AccountingTransaction> findTransactionById(int txnId)
		throws SQLException
	{
		try (Connection c = Database.get().getConnection();
			 PreparedStatement ps = c.prepareStatement("""
				 SELECT id, booking_ts, date_text, memo, to_from, check_number,
				        clear_bank, bank_name, reconciled, budget_tracking, associated_fund_name
				 FROM journal_transaction
				 WHERE id = ?
			 """))
		{
			ps.setInt(1, txnId);
			try (ResultSet rs = ps.executeQuery())
			{
				if (!rs.next())
				{
					return Optional.empty();
				}
				AccountingTransaction txn = new AccountingTransaction();
				txn.setId(rs.getInt("id"));
				txn.setBookingDateTimestamp(rs.getLong("booking_ts"));
				txn.setDate(rs.getString("date_text"));
				txn.setMemo(rs.getString("memo"));
				txn.setToFrom(rs.getString("to_from"));
				txn.setCheckNumber(rs.getString("check_number"));
				txn.setClearBank(rs.getString("clear_bank"));
				txn.setBank(rs.getString("bank_name"));
				txn.setReconciled(rs.getBoolean("reconciled"));
				txn.setBudgetTracking(rs.getString("budget_tracking"));
				txn.setAssociatedFundName(rs.getString("associated_fund_name"));
				txn.setEntries(new LinkedHashSet<>());
				txn.setInfo(new LinkedHashMap<>());
				loadEntriesAndInfo(c, txn);
				return Optional.of(txn);
			}
		}
	}

	public int reserveNextTransactionId() throws SQLException
	{
		synchronized (NEXT_TXN_ID_MONITOR)
		{
			try (Connection c = Database.get().getConnection())
			{
				boolean originalAutoCommit = c.getAutoCommit();
				c.setAutoCommit(false);
				try (Statement lockStmt = c.createStatement();
					 PreparedStatement ps = c.prepareStatement(
						 "SELECT COALESCE(MAX(id), 0) + 1 FROM journal_transaction");
					 ResultSet rs = ps.executeQuery())
				{
					try (ResultSet ignored = lockStmt.executeQuery(
						"SELECT id FROM journal_transaction ORDER BY id DESC LIMIT 1 FOR UPDATE"))
					{
						// Lock acquisition side-effect only.
					}
						rs.next();
						int dbNext = rs.getInt(1);
						int next = Math.max(dbNext, LAST_RESERVED_TXN_ID + 1);
						LAST_RESERVED_TXN_ID = next;
						c.commit();
						return next;
				}
				catch (SQLException ex)
				{
					c.rollback();
					throw ex;
				}
				finally
				{
					c.setAutoCommit(originalAutoCommit);
				}
			}
		}
	}

	private void loadEntriesAndInfo(Connection c, AccountingTransaction txn)
		throws SQLException
	{
		try (PreparedStatement ps = c.prepareStatement("""
			SELECT amount, account_number, account_side, account_name, fund_number
			FROM journal_entry
			WHERE txn_id = ?
			ORDER BY id
		"""))
		{
			ps.setInt(1, txn.getId());
			try (ResultSet rs = ps.executeQuery())
			{
				while (rs.next())
				{
					AccountSide side = AccountSide.fromString(
						rs.getString("account_side"));
					AccountingEntry entry = new AccountingEntry(
						rs.getBigDecimal("amount"),
						rs.getString("account_number"),
						side,
						rs.getString("account_name"));
					entry.setFundNumber(rs.getString("fund_number"));
					txn.addEntry(entry);
				}
			}
		}
		try (PreparedStatement ps = c.prepareStatement("""
			SELECT k, v FROM transaction_info WHERE txn_id = ?
		"""))
		{
			ps.setInt(1, txn.getId());
			try (ResultSet rs = ps.executeQuery())
			{
				while (rs.next())
				{
					txn.getInfo().put(rs.getString("k"), rs.getString("v"));
				}
			}
		}
	}
	
	
	/**
	 * Performs the replacement using the provided already-open connection.
	 *
	 * SQL actions:
	 *
	 * 1) Clear dependent tables first:
	 *    - DELETE FROM transaction_info
	 *      Removes all metadata rows for all transactions.
	 *
	 *    - DELETE FROM journal_entry
	 *      Removes all line items for all transactions.
	 *
	 *    - DELETE FROM journal_transaction
	 *      Removes all transaction headers.
	 *
	 *    The ordering matters if foreign keys exist (or to avoid constraint issues):
	 *      child tables deleted before parent table.
	 *
	 * 2) Insert the supplied transactions:
	 *    Each transaction is written using writeTransaction(), which upserts the header
	 *    and replaces its children.
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
			List<Integer> legacyTxnIds =
				CanonicalJournalSyncAdapter.listLegacyTxnIds(c);
			CanonicalJournalSyncAdapter.deleteLegacyTxnIds(c, legacyTxnIds);
			
			if (LOGGER.isDebugEnabled())
			{
				LOGGER.debug(
					"replaceAll(conn): clearing tables txn_supplemental_line, transaction_info, journal_entry, journal_transaction");
			}
			
			st = c.createStatement();
			
			// Delete supplemental lines first (depends on
			// journal_transaction/journal_entry).
			int s = st.executeUpdate("DELETE FROM txn_supplemental_line");
			
			// Delete all transaction_info next (depends on
			// journal_transaction).
			int a = st.executeUpdate("DELETE FROM transaction_info");
			
			// Delete all journal entries next (depends on journal_transaction).
			int b = st.executeUpdate("DELETE FROM journal_entry");
			
			// Finally delete the transaction headers.
			int d = st.executeUpdate("DELETE FROM journal_transaction");
			
			if (LOGGER.isDebugEnabled())
			{
				LOGGER.debug(
					"replaceAll(conn): deleted rows txn_supplemental_line={}, transaction_info={}, journal_entry={}, journal_transaction={}",
					s, a, b, d);
			}
			
		}
		finally
		{
			closeQuietly(st, "Statement (replaceAll delete)");
		}
		
		// Now insert the supplied data set.
		if (transactions != null)
		{
			
			for (AccountingTransaction txn : transactions)
			{
				
				if (txn == null)
				{
					continue;
				}
				
				// This writes header + entries + metadata (and upserts accounts
				// referenced by entries).
				writeTransaction(c, txn);
			}
			
		}
		
	}
	
	/**
	 * Loads all transactions (headers), then loads all entries and all info and attaches them.
	 * 
	 * SQL actions:
	 * 
	 * 1) Load headers:
	 *    SELECT ... FROM journal_transaction ORDER BY id
	 * 
	 * 2) Load entries:
	 *    SELECT ... FROM journal_entry ORDER BY id
	 *    For each row, find the parent AccountingTransaction by txn_id and add an AccountingEntry.
	 * 
	 * 3) Load info:
	 *    SELECT txn_id, k, v FROM transaction_info ORDER BY txn_id, k
	 *    For each row, attach to txn.getInfo() map.
	 * 
	 * Note: This method uses separate connections for each query (as the original did).
	 * In most DBs it’s fine; if you want strict snapshot consistency, you could use one
	 * connection/transaction, but this is typically acceptable for read journal use.
	 *
	 * @return the list
	 * @throws SQLException the SQL exception
	 */
	public static List<AccountingTransaction> listTransactions()
		throws SQLException
	{
		Map<Integer, AccountingTransaction> byId = new LinkedHashMap<>();
		List<AccountingTransaction> transactions = new ArrayList<>();
		
		Connection c1 = null;
		PreparedStatement ps1 = null;
		ResultSet rs1 = null;
		
		// Query #1: transaction headers ordered by id
		final String txnSql =
			"""
				    SELECT id, booking_ts, date_text, memo, to_from, check_number,
				           clear_bank, bank_name, reconciled, budget_tracking, associated_fund_name
				    FROM journal_transaction
				    ORDER BY id
				""";
		
		try
		{
			c1 = Database.get().getConnection();
			
			if (LOGGER.isDebugEnabled())
			{
				LOGGER.debug(
					"listTransactions(): SQL (journal_transaction):\n{}",
					txnSql);
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
				txn.setBank(rs1.getString("bank_name"));
				txn.setReconciled(rs1.getBoolean("reconciled"));
				txn.setBudgetTracking(rs1.getString("budget_tracking"));
				txn.setAssociatedFundName(
					rs1.getString("associated_fund_name"));
				
				// Prepare containers for children.
				txn.setEntries(new LinkedHashSet<>());
				txn.setInfo(new LinkedHashMap<>());
				
				transactions.add(txn);
				byId.put(txn.getId(), txn);
			}
			
			if (LOGGER.isDebugEnabled())
			{
				LOGGER.debug(
					"listTransactions(): loaded {} journal_transaction rows",
					transactions.size());
			}
			
		}
		finally
		{
			closeQuietly(rs1,
				"ResultSet (listTransactions journal_transaction)");
			closeQuietly(ps1,
				"PreparedStatement (listTransactions journal_transaction)");
			closeQuietly(c1,
				"Connection (listTransactions journal_transaction)");
		}
		
		// Only load children if we actually have transactions.
		if (!byId.isEmpty())
		{
			// -------- Query #2: all entries --------
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
					LOGGER.debug("listTransactions(): SQL (journal_entry):\n{}",
						entrySql);
				}
				
				ps2 = c2.prepareStatement(entrySql);
				rs2 = ps2.executeQuery();
				
				int entryCount = 0;
				
				while (rs2.next())
				{
					// Map each entry row back to its parent transaction.
					AccountingTransaction txn = byId.get(rs2.getInt("txn_id"));
					
					if (txn == null)
					{
						// If the DB has orphan rows (shouldn't happen), skip.
						continue;
					}
					
					// Convert side text to enum.
					String sideText = rs2.getString("account_side");
					AccountSide side = sideText == null ? AccountSide.UNKNOWN :
						AccountSide.fromString(sideText);
					
					AccountingEntry entry =
						new AccountingEntry(rs2.getBigDecimal("amount"),
							rs2.getString("account_number"),
							side,
							rs2.getString("account_name"));
					
					entry.setFundNumber(rs2.getString("fund_number"));
					
					// Add to parent transaction's entry set.
					txn.addEntry(entry);
					entryCount++;
				}
				
				if (LOGGER.isDebugEnabled())
				{
					LOGGER.debug(
						"listTransactions(): loaded {} journal_entry rows (matched to existing txns)",
						entryCount);
				}
				
			}
			finally
			{
				closeQuietly(rs2, "ResultSet (listTransactions journal_entry)");
				closeQuietly(ps2,
					"PreparedStatement (listTransactions journal_entry)");
				closeQuietly(c2, "Connection (listTransactions journal_entry)");
			}
			
			// -------- Query #3: all metadata --------
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
					LOGGER.debug(
						"listTransactions(): SQL (transaction_info):\n{}",
						infoSql);
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
					
					// Attach metadata as a map entry.
					txn.getInfo().put(rs3.getString("k"), rs3.getString("v"));
					infoCount++;
				}
				
				if (LOGGER.isDebugEnabled())
				{
					LOGGER.debug(
						"listTransactions(): loaded {} transaction_info rows (matched to existing txns)",
						infoCount);
				}
				
			}
			finally
			{
				closeQuietly(rs3,
					"ResultSet (listTransactions transaction_info)");
				closeQuietly(ps3,
					"PreparedStatement (listTransactions transaction_info)");
				closeQuietly(c3,
					"Connection (listTransactions transaction_info)");
			}
			
			TxnSupplementalLineRepository supplementalRepo =
				new TxnSupplementalLineRepository();
			
			for (AccountingTransaction txn : transactions)
			{
				List<TxnSupplementalLineRecord> records =
					supplementalRepo.listByTxnId(txn.getId());
				List<TxnSupplementalLineBase> beans = new ArrayList<>();
				
				for (TxnSupplementalLineRecord record : records)
				{
					beans.add(TxnSupplementalLineMapper.toBean(record));
				}
				
				txn.setSupplementalLines(beans);
			}
			
		}
		
		return transactions;
		
	}
	
	/**
	 * Writes one AccountingTransaction using the provided connection (participating in an outer transaction).
	 * 
	 * SQL actions, in order:
	 * 
	 * A) ensureAccountsExist()
	 *    MERGE INTO account(account_number, name) KEY(account_number) VALUES (?,?)
	 *    - This ensures every account referenced by the journal entries exists in the master account table.
	 *    - If the account already exists, it is updated (depending on DB semantics) or left as-is.
	 *    - If it does not exist, it is inserted.
	 * 
	 * B) Upsert the transaction header (journal_transaction):
	 *    MERGE INTO journal_transaction(...) KEY(id) VALUES (?,?,?,?,?,?,?,?,?)
	 *    - This is an upsert keyed by id:
	 *      If a row with id exists, update it.
	 *      If not, insert it.
	 * 
	 * C) Replace journal_entry rows for this transaction id:
	 *    1) DELETE FROM journal_entry WHERE txn_id=?
	 *       - Remove all old entries to prevent stale line items.
	 *    2) INSERT INTO journal_entry(...) VALUES(?,?,?,?,?,?)
	 *       - Insert the current entry set.
	 *       - Uses batch inserts for efficiency.
	 * 
	 * D) Replace transaction_info rows for this transaction id:
	 *    1) DELETE FROM transaction_info WHERE txn_id=?
	 *    2) INSERT INTO transaction_info(txn_id, k, v) VALUES (?,?,?)
	 *       - Insert current metadata as a batch.
	 * 
	 * The delete then insert approach guarantees that what’s in the DB matches exactly what’s
	 * in the AccountingTransaction object, without having to compute diffs.
	 *
	 * @param c the c
	 * @param txn the txn
	 * @throws SQLException the SQL exception
	 */
	private static void writeTransaction(Connection c,
		AccountingTransaction txn)
		throws SQLException
	{
		
		if (txn == null)
		{
			throw new IllegalArgumentException("transaction required");
		}
		
		// Ensure chart-of-accounts rows exist for any accounts referenced by
		// these journal entries.
		ensureAccountsExist(c, txn.getEntries());
		
		// Header upsert SQL: merge/update-or-insert the transaction header row.
		final String upsertTxn =
			"""
				    MERGE INTO journal_transaction(id, booking_ts, date_text, memo, to_from, check_number,
				                   clear_bank, bank_name, reconciled, budget_tracking, associated_fund_name)
				            KEY(id)
				            VALUES(?,?,?,?,?,?,?,?,?,?,?)
				""";
		
		PreparedStatement ps = null;
		
		try
		{
			
			if (LOGGER.isDebugEnabled())
			{
				LOGGER.debug(
					"writeTransaction(): upsert journal_transaction SQL:\n{}",
					upsertTxn);
				LOGGER.debug(
					"writeTransaction(): upsert params txn_id={} booking_ts={} date_text={} memo={} to_from={} check_number={} clear_bank={} bank_name={} reconciled={} budget_tracking={} associated_fund_name={}",
					txn.getId(),
					txn.getBookingDateTimestamp(),
					txn.getDate(),
					txn.getMemo(),
					txn.getToFrom(),
					txn.getCheckNumber(),
					txn.getClearBank(),
					txn.getBank(),
					txn.isReconciled(),
					txn.getBudgetTracking(),
					txn.getAssociatedFundName());
			}
			
			ps = c.prepareStatement(upsertTxn);
			
			// Bind values to ? placeholders.
			int i = 0;
			ps.setInt(++i, txn.getId());
			ps.setLong(++i, txn.getBookingDateTimestamp());
			ps.setString(++i, txn.getDate());
			ps.setString(++i, txn.getMemo());
			ps.setString(++i, txn.getToFrom());
			ps.setString(++i, txn.getCheckNumber());
			ps.setString(++i, txn.getClearBank());
			ps.setString(++i, txn.getBank());
			ps.setBoolean(++i, txn.isReconciled());
			ps.setString(++i, txn.getBudgetTracking());
			ps.setString(++i, txn.getAssociatedFundName());
			
			// Execute the upsert (rows affected depends on DB).
			int rows = ps.executeUpdate();
			
			if (LOGGER.isDebugEnabled())
			{
				LOGGER.debug(
					"writeTransaction(): upsert journal_transaction affectedRows={} txn_id={}",
					rows, txn.getId());
			}
			
		}
		finally
		{
			closeQuietly(ps,
				"PreparedStatement (writeTransaction upsert journal_transaction)");
		}
		
		// Delete existing entries for this transaction id.
		PreparedStatement delEntries = null;
		final String delEntriesSql = "DELETE FROM journal_entry WHERE txn_id=?";
		
		try
		{
			
			if (LOGGER.isDebugEnabled())
			{
				LOGGER.debug("writeTransaction(): delete journal_entry SQL: {}",
					delEntriesSql);
				LOGGER.debug(
					"writeTransaction(): delete journal_entry params txn_id={}",
					txn.getId());
			}
			
			delEntries = c.prepareStatement(delEntriesSql);
			delEntries.setInt(1, txn.getId());
			
			int deleted = delEntries.executeUpdate();
			
			if (LOGGER.isDebugEnabled())
			{
				LOGGER.debug(
					"writeTransaction(): delete journal_entry deletedRows={} txn_id={}",
					deleted, txn.getId());
			}
			
		}
		finally
		{
			closeQuietly(delEntries,
				"PreparedStatement (writeTransaction delete journal_entry)");
		}
		
		// Insert new entries for this transaction id (batch insert).
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
				LOGGER.debug(
					"writeTransaction(): insert journal_entry SQL:\n{}",
					insEntriesSql);
			}
			
			insEntries = c.prepareStatement(insEntriesSql);
			
			// Track entries so we can identify which batch item failed on
			// BatchUpdateException.
			List<AccountingEntry> batchEntries = new ArrayList<>();
			
			for (AccountingEntry e : txn.getEntries())
			{
				
				if (e == null)
				{
					LOGGER.warn(
						"Skipping null journal entry for transaction id={}",
						txn.getId());
					continue;
				}
				
				// Basic validation for required entry fields.
				if (e.getAmount() == null)
				{
					throw new SQLException(
						"Journal entry amount is required for transaction id=" +
							txn.getId() + ", entry=" + e);
				}
				
				if (e.getAccountNumber() == null ||
					e.getAccountNumber().isBlank())
				{
					throw new SQLException(
						"Journal entry account number is required for transaction id=" +
							txn.getId() + ", entry=" + e);
				}
				
				if (LOGGER.isDebugEnabled())
				{
					LOGGER.debug(
						"writeTransaction(): journal_entry batch item txn_id={} amount={} account_number={} account_side={} account_name={} fund_number={}",
						txn.getId(),
						e.getAmount(),
						e.getAccountNumber(),
						e.getAccountSide() == null ? null :
							e.getAccountSide().name(),
						e.getAccountName(),
						e.getFundNumber());
				}
				
				// Bind entry values. The ordering matches the INSERT column
				// list.
				int j = 0;
				insEntries.setInt(++j, txn.getId());
				insEntries.setBigDecimal(++j, e.getAmount());
				insEntries.setString(++j, e.getAccountNumber());
				insEntries.setString(++j, e.getAccountSide() == null ? null :
					e.getAccountSide().name());
				insEntries.setString(++j, e.getAccountName());
				insEntries.setString(++j, e.getFundNumber());
				
				// Add this row to the batch.
				insEntries.addBatch();
				batchEntries.add(e);
			}
			
			// Execute the batch insert.
			int[] results;
			
			try
			{
				results = insEntries.executeBatch();
			}
			catch (BatchUpdateException ex)
			{
				// BatchUpdateException provides per-item updateCounts; log
				// which item(s) failed.
				logBatchFailure(txn, batchEntries, ex.getUpdateCounts(), ex);
				throw ex;
			}
			
			// Summarize results in debug logs.
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
						LOGGER.warn(
							"Failed journal entry batch item for transaction id={}",
							txn.getId());
					}
					
				}
				
				LOGGER.debug(
					"Inserted {} journal entries for transaction id={}",
					inserted, txn.getId());
			}
			
		}
		finally
		{
			closeQuietly(insEntries,
				"PreparedStatement (writeTransaction insert journal_entry)");
		}
		
		// Delete existing metadata for this transaction id.
		PreparedStatement delInfo = null;
		final String delInfoSql = "DELETE FROM transaction_info WHERE txn_id=?";
		
		try
		{
			
			if (LOGGER.isDebugEnabled())
			{
				LOGGER.debug(
					"writeTransaction(): delete transaction_info SQL: {}",
					delInfoSql);
				LOGGER.debug(
					"writeTransaction(): delete transaction_info params txn_id={}",
					txn.getId());
			}
			
			delInfo = c.prepareStatement(delInfoSql);
			delInfo.setInt(1, txn.getId());
			
			int deleted = delInfo.executeUpdate();
			
			if (LOGGER.isDebugEnabled())
			{
				LOGGER.debug(
					"writeTransaction(): delete transaction_info deletedRows={} txn_id={}",
					deleted, txn.getId());
			}
			
		}
		finally
		{
			closeQuietly(delInfo,
				"PreparedStatement (writeTransaction delete transaction_info)");
		}
		
		// Insert new metadata (if any).
		Map<String, String> info = txn.getInfo();
		
		if (info != null && !info.isEmpty())
		{
			PreparedStatement insInfo = null;
			final String insInfoSql =
				"INSERT INTO transaction_info(txn_id, k, v) VALUES (?,?,?)";
			
			try
			{
				
				if (LOGGER.isDebugEnabled())
				{
					LOGGER.debug(
						"writeTransaction(): insert transaction_info SQL: {}",
						insInfoSql);
				}
				
				insInfo = c.prepareStatement(insInfoSql);
				
				for (Map.Entry<String, String> en : info.entrySet())
				{
					
					if (LOGGER.isDebugEnabled())
					{
						LOGGER.debug(
							"writeTransaction(): transaction_info batch item txn_id={} k={} v={}",
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
					
					LOGGER.debug(
						"Inserted {} transaction_info rows for transaction id={}",
						inserted, txn.getId());
				}
				
			}
			finally
			{
				closeQuietly(insInfo,
					"PreparedStatement (writeTransaction insert transaction_info)");
			}
			
		}
		else
		{
			
			if (LOGGER.isDebugEnabled())
			{
				LOGGER.debug(
					"writeTransaction(): no transaction_info rows to insert for txn id={}",
					txn.getId());
			}
			
		}
		
		insertSupplementalLines(c, txn);
		CanonicalJournalSyncAdapter.syncTransaction(c, txn);
		
	}
	
	/**
	 * Insert supplemental lines.
	 *
	 * @param c the c
	 * @param txn the txn
	 * @throws SQLException the SQL exception
	 */
	private static void insertSupplementalLines(Connection c,
		AccountingTransaction txn)
		throws SQLException
	{
		
		if (txn == null)
		{
			return;
		}
		
		List<TxnSupplementalLineRecord> records = new ArrayList<>();
		
		for (TxnSupplementalLineBase line : txn.getSupplementalLines())
		{
			records.add(TxnSupplementalLineMapper.toRecord(line));
		}
		
		TxnSupplementalLineRepository repo =
			new TxnSupplementalLineRepository();
		repo.replaceForTxn(c, txn.getId(), records);
		
	}
	
	/**
	 * Logs details about a failed batch insert.
	 * 
	 * When JDBC executes a batch, it may fail on one row; updateCounts tells you which.
	 * This method logs the transaction id and each failing entry at its batch index.
	 *
	 * @param txn the txn
	 * @param batchEntries the batch entries
	 * @param updateCounts the update counts
	 * @param ex the ex
	 */
	private static void logBatchFailure(AccountingTransaction txn,
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
				LOGGER.error(
					"Failed journal entry at batch index {} for transaction id={}: {}",
					idx,
					txn.getId(),
					batchEntries.get(idx));
			}
			
		}
		
	}
	
	/**
	 * Ensures that every account referenced by the given journal entries exists in the account table.
	 * 
	 * SQL action:
	 *   MERGE INTO account(account_number, name) KEY(account_number) VALUES (?,?)
	 * 
	 * This is an upsert keyed by account_number:
	 * - If the account exists, it may update the name (DB dependent).
	 * - If it does not exist, it inserts it.
	 * 
	 * Why do this here?
	 * - journal_entry.account_number references an account. If your DB enforces
	 *   referential integrity (foreign keys), inserts into journal_entry can fail
	 *   if account rows are missing.
	 * - Even without FK constraints, the UI/reporting usually expects accounts
	 *   to exist in the master table.
	 *
	 * @param c the c
	 * @param entries the entries
	 * @throws SQLException the SQL exception
	 */
	private static void ensureAccountsExist(Connection c,
		Iterable<AccountingEntry> entries)
		throws SQLException
	{
		
		if (entries == null)
		{
			return;
		}
		
		PreparedStatement ps = null;
		final String sql =
			"MERGE INTO account(account_number, name) KEY(account_number) VALUES (?,?)";
		
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
					// Fall back to using the account number as the name if none
					// is provided.
					accountName = accountNumber;
				}
				
				if (LOGGER.isDebugEnabled())
				{
					LOGGER.debug(
						"ensureAccountsExist(): batch item account_number={} name={}",
						accountNumber, accountName);
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
				
				LOGGER.debug(
					"ensureAccountsExist(): executed batch items={} affectedApprox={}",
					count, merged);
			}
			
		}
		finally
		{
			closeQuietly(ps, "PreparedStatement (ensureAccountsExist)");
		}
		
	}
	
	/**
	 * Best-effort closer for JDBC resources.
	 * 
	 * Why not let close() throw?
	 * - If a statement fails and then close() fails, we generally care about the statement failure.
	 * - Close failures are still useful for diagnostics, so we log them at WARN.
	 *
	 * @param c the c
	 * @param what the what
	 */
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
			LOGGER.warn("Failed to close {}", what, ex);
		}
		
	}
	
}
