
package nonprofitbookkeeping.core;

import java.nio.file.Path;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

/**
 * The Class Database.
 */
public final class Database
{
	
	/** The instance. */
	private static Database INSTANCE;
	
	/** The url. */
	private final String url;
	
	/** The user. */
	private final String user = "sa";
	
	/** The pass. */
	private final String pass = "";
	
	private static final String MIGRATION_RECONCILED_BACKFILL_V1 = "reconciled-backfill-v1";
	private static final String MIGRATION_OPERATIONAL_LINK_BACKFILL_V1 = "operational-link-backfill-v1";

	private static final String SQL_COUNTERPARTY_FROM_PERSON =
		"INSERT INTO counterparty(display_name, kind, email, phone, is_active) " +
		"SELECT p.name, UPPER(COALESCE(p.type, 'DONOR')), p.email, p.phone, TRUE FROM person p " +
		"WHERE NOT EXISTS (SELECT 1 FROM counterparty c WHERE c.display_name = p.name AND c.kind = UPPER(COALESCE(p.type, 'DONOR')))";

	private static final String SQL_COUNTERPARTY_FROM_DONOR =
		"INSERT INTO counterparty(display_name, kind, email, phone, is_active) " +
		"SELECT d.name, 'DONOR', d.email, d.phone, TRUE FROM donor d " +
		"WHERE d.name IS NOT NULL AND NOT EXISTS " +
		"(SELECT 1 FROM counterparty c WHERE c.display_name = d.name AND c.kind = 'DONOR')";

	private static final String SQL_MIGRATION_EXISTS =
		"SELECT 1 FROM schema_migration_history WHERE migration_key = ?";

	private static final String SQL_MIGRATION_UPSERT =
		"MERGE INTO schema_migration_history (migration_key, applied_at) KEY(migration_key) VALUES (?, CURRENT_TIMESTAMP)";

private static final String SQL_DEFAULT_CHART_INSERT =
		"INSERT INTO chart_of_accounts(name, version, status, created_at, updated_at) " +
		"SELECT 'Default Legacy Chart','legacy','ACTIVE',CURRENT_TIMESTAMP,CURRENT_TIMESTAMP " +
		"WHERE NOT EXISTS (SELECT 1 FROM chart_of_accounts)";

	private static final String SQL_DEFAULT_FUND_INSERT =
		"INSERT INTO fund(id, code, name, fund_type, is_active, created_at, updated_at) " +
		"SELECT 1, 'GENERAL', 'General Fund', 'UNRESTRICTED', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP " +
		"WHERE NOT EXISTS (SELECT 1 FROM fund WHERE id = 1)";

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

	/**
	 * Instantiates a new database.
	 *
	 * @param dbFile the db file
	 */
	private Database(Path dbFile)
	{
		this.url = "jdbc:h2:file:" + dbFile.toAbsolutePath().toString() +
			";AUTO_SERVER=TRUE;MODE=MySQL";
		
	}
	
	/**
	 * Inits the.
	 *
	 * @param dbFile the db file
	 */
	public static synchronized void init(Path dbFile)
	{
		if (dbFile == null)
			throw new IllegalArgumentException("dbFile required");
		INSTANCE = new Database(dbFile);
		
	}
	
	/**
	 * Checks if is initialized.
	 *
	 * @return true, if is initialized
	 */
	public static synchronized boolean isInitialized()
	{
		return INSTANCE != null;
		
	}
	
	/**
	 * Gets the.
	 *
	 * @return the database
	 */
	public static Database get()
	{
		if (INSTANCE == null)
			throw new IllegalStateException("Database not initialized");
		return INSTANCE;
		
	}
	
	/**
	 * Gets the connection.
	 *
	 * @return the connection
	 * @throws SQLException the SQL exception
	 */
	public Connection getConnection() throws SQLException
	{
		return DriverManager.getConnection(this.url, this.user, this.pass);
		
	}
	
	public String getJdbcUrl()
	{
		return this.url;
	}
	
	public String getUser()
	{
		return this.user;
	}
	
	public String getPass()
	{
		return this.pass;
	}
	
	/**
	 * Ensure schema.
	 *
	 * @throws SQLException the SQL exception
	 */
	public void ensureSchema() throws SQLException
	{
		FlywayMigrationRunner.migrateCurrentDatabaseIfEnabled();
		try (Connection c = getConnection(); Statement st = c.createStatement())
		{
			ensureAccountAndLegacyJournalTables(st);
			backfillLegacyTxnMap(c);
			ensurePeopleAndCounterparty(st);
			runReconciledDataBackfill(c);
			runOperationalLinkBackfillMigration(c);
			runFinancePostingEnforcementPreflight(c);
		}
		
	}
	
	private void ensureAccountAndLegacyJournalTables(Statement st)
		throws SQLException
	{
		st.execute(
			"UPDATE account SET code = account_number WHERE code IS NULL;");
		st.execute(
			"UPDATE account SET normal_balance = CASE WHEN UPPER(COALESCE(increase_side, 'DEBIT')) IN ('CREDIT','CR') THEN 'CREDIT' ELSE 'DEBIT' END WHERE normal_balance IS NULL;");
	}
	
	private void backfillLegacyTxnMap(Connection c) throws SQLException
	{
		try (PreparedStatement ps = c.prepareStatement("""
			    INSERT INTO legacy_txn_map(legacy_txn_id, canonical_txn_id, checksum)
			    SELECT jt.id, t.id, NULL
			    FROM journal_transaction jt
			    JOIN txn t ON t.id = jt.id
			    LEFT JOIN legacy_txn_map m ON m.legacy_txn_id = jt.id
			    WHERE m.legacy_txn_id IS NULL
			"""))
		{
			ps.executeUpdate();
		}
	}

	private void ensurePeopleAndCounterparty(Statement st) throws SQLException
	{
		st.execute(
			"UPDATE donor SET external_id = name WHERE external_id IS NULL AND name IS NOT NULL;");
		st.execute("UPDATE person SET type = 'DONOR' WHERE type IS NULL OR TRIM(type) = '';");
	}
	
	private void runFinancePostingEnforcementPreflight(Connection c) throws SQLException
	{
		String mode = System.getProperty("nonprofitbookkeeping.financeWriteEnforcement", "enforce");
		try (Statement st = c.createStatement();
		     ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM v_finance_posting_enforcement_exceptions"))
		{
			rs.next();
			int count = rs.getInt(1);
			if (count > 0)
			{
				System.err.println("[FINANCE_ENFORCEMENT_PREFLIGHT] Exceptions found: " + count);
				if ("enforce".equalsIgnoreCase(mode))
				{
					throw new SQLException("Finance posting enforcement preflight failed; review v_finance_posting_enforcement_exceptions.");
				}
			}
		}
	}

	private void runReconciledDataBackfill(Connection c) throws SQLException
	{
		if (isMigrationApplied(c, MIGRATION_RECONCILED_BACKFILL_V1))
		{
			return;
		}
		
		try (Statement st = c.createStatement())
		{
			st.execute(
				SQL_DEFAULT_CHART_INSERT);
			st.execute(
				SQL_DEFAULT_FUND_INSERT);
			st.execute(
				SQL_ACCOUNT_CHART_UPDATE);
			st.execute(SQL_BACKFILL_TXN_INSERT);
			st.execute(SQL_BACKFILL_TXN_SPLIT_INSERT);
			st.execute(
				SQL_COUNTERPARTY_FROM_PERSON);
			st.execute(
				SQL_COUNTERPARTY_FROM_DONOR);
		}
		
		updateTxnDatesFromLegacyText(c);
		markMigrationApplied(c, MIGRATION_RECONCILED_BACKFILL_V1);
	}


	private void runOperationalLinkBackfillMigration(Connection c)
		throws SQLException
	{
		if (isMigrationApplied(c, MIGRATION_OPERATIONAL_LINK_BACKFILL_V1))
		{
			return;
		}
		try (Statement st = c.createStatement())
		{
			st.execute("""
				    UPDATE donation_record d
				    SET journal_txn_id = (
				      SELECT MIN(jt.id)
				      FROM journal_transaction jt
				      JOIN journal_entry je
				        ON je.txn_id = jt.id
				       AND UPPER(COALESCE(je.account_side, 'DEBIT')) = 'CREDIT'
				       AND je.amount = d.amount
				       AND je.account_number = d.revenue_account_number
				      WHERE COALESCE(jt.date_text, '') = COALESCE(CAST(d.donation_date AS VARCHAR(32)), '')
				        AND COALESCE(jt.to_from, '') = COALESCE(d.donor_external_id, '')
				        AND NOT EXISTS (
				          SELECT 1
				          FROM donation_record dx
				          WHERE dx.donation_id <> d.donation_id
				            AND dx.journal_txn_id = jt.id
				        )
				    )
				    WHERE d.journal_txn_id IS NULL
				""");
			st.execute("""
				    INSERT INTO donation_journal_link(donation_id, journal_txn_id, link_role, created_at, updated_at)
				    SELECT d.donation_id, d.journal_txn_id, 'ORIGINAL', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
				    FROM donation_record d
				    WHERE d.journal_txn_id IS NOT NULL
				      AND NOT EXISTS (
				        SELECT 1
				        FROM donation_journal_link l
				        WHERE l.donation_id = d.donation_id
				          AND l.journal_txn_id = d.journal_txn_id
				          AND l.link_role = 'ORIGINAL'
				      )
				""");
			st.execute("""
				    UPDATE grant_record g
				    SET journal_txn_id = (
				      SELECT MIN(ti.txn_id)
				      FROM transaction_info ti
				      WHERE ti.k = 'domain_record_id'
				        AND ti.v = g.grant_record_id
				    )
				    WHERE g.journal_txn_id IS NULL
				""");
			st.execute("""
				    INSERT INTO operational_link_backfill_queue(module_name, domain_id, issue_code, issue_detail)
				    SELECT 'DONATION',
				           d.donation_id,
				           'NO_MATCHED_JOURNAL_TXN',
				           'No deterministic journal transaction match found during phase-2 backfill.'
				    FROM donation_record d
				    WHERE d.journal_txn_id IS NULL
				      AND NOT EXISTS (
				        SELECT 1
				        FROM operational_link_backfill_queue q
				        WHERE q.module_name = 'DONATION'
				          AND q.domain_id = d.donation_id
				          AND q.issue_code = 'NO_MATCHED_JOURNAL_TXN'
				      )
				""");
			st.execute("""
				    INSERT INTO operational_link_backfill_queue(module_name, domain_id, issue_code, issue_detail)
				    SELECT 'GRANT',
				           g.grant_record_id,
				           'NO_MATCHED_JOURNAL_TXN',
				           'No transaction_info(domain_record_id) match found during phase-2 backfill.'
				    FROM grant_record g
				    WHERE g.journal_txn_id IS NULL
				      AND NOT EXISTS (
				        SELECT 1
				        FROM operational_link_backfill_queue q
				        WHERE q.module_name = 'GRANT'
				          AND q.domain_id = g.grant_record_id
				          AND q.issue_code = 'NO_MATCHED_JOURNAL_TXN'
				      )
				""");
		}
		markMigrationApplied(c, MIGRATION_OPERATIONAL_LINK_BACKFILL_V1);
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
	
	private boolean isMigrationApplied(Connection c, String key) throws SQLException
	{
		try (PreparedStatement ps = c.prepareStatement(
			SQL_MIGRATION_EXISTS))
		{
			ps.setString(1, key);
			try (ResultSet rs = ps.executeQuery())
			{
				return rs.next();
			}
		}
	}
	
	private void markMigrationApplied(Connection c, String key) throws SQLException
	{
		try (PreparedStatement ps = c.prepareStatement(
			SQL_MIGRATION_UPSERT))
		{
			ps.setString(1, key);
			ps.executeUpdate();
		}
	}
	
}
