
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
			ensureMigrationTables(st);
			ensureAccountAndLegacyJournalTables(st);
			ensureJpaTables(st);
			ensureJpaConstraints(st);
			backfillLegacyTxnMap(c);
			ensurePeopleAndCounterparty(st);
			runReconciledDataBackfill(c);
			ensureRemainingLegacyTables(st);
			ensureOperationalLinkageTables(st);
			runOperationalLinkBackfillMigration(c);
			runFinancePostingEnforcementPreflight(c);
		}
		
	}
	
	private void ensureMigrationTables(Statement st) throws SQLException
	{
		st.execute("""
			    CREATE TABLE IF NOT EXISTS schema_migration_history(
			      migration_key VARCHAR(128) PRIMARY KEY,
			      applied_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
			    )
			""");
	}
	
	private void ensureAccountAndLegacyJournalTables(Statement st)
		throws SQLException
	{
		st.execute(
			"UPDATE account SET code = account_number WHERE code IS NULL;");
		st.execute(
			"UPDATE account SET normal_balance = CASE WHEN UPPER(COALESCE(increase_side, 'DEBIT')) IN ('CREDIT','CR') THEN 'CREDIT' ELSE 'DEBIT' END WHERE normal_balance IS NULL;");
	}
	
	private void ensureJpaTables(Statement st) throws SQLException
	{
		st.execute("""
			    CREATE TABLE IF NOT EXISTS chart_of_accounts(
			      id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
			      name VARCHAR(200) NOT NULL,
			      version VARCHAR(50) NOT NULL,
			      status VARCHAR(20) NOT NULL,
			      created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
			      updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
			    )
			""");
		st.execute("""
			    CREATE TABLE IF NOT EXISTS fund(
			      id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
			      code VARCHAR(64) NOT NULL,
			      name VARCHAR(200) NOT NULL,
			      fund_type VARCHAR(30) NOT NULL,
			      parent_id BIGINT,
			      is_active BOOLEAN DEFAULT TRUE NOT NULL,
			      effective_from DATE,
			      effective_to DATE,
			      restriction_text CLOB,
			      created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
			      updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
			    )
			""");
		st.execute("CREATE UNIQUE INDEX IF NOT EXISTS uq_fund_code_idx ON fund(code);");
		st.execute("""
			    CREATE TABLE IF NOT EXISTS counterparty(
			      id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
			      display_name VARCHAR(200) NOT NULL,
			      kind VARCHAR(20) NOT NULL,
			      email VARCHAR(200),
			      phone VARCHAR(40),
			      is_active BOOLEAN DEFAULT TRUE NOT NULL
			    )
			""");
		st.execute(
			"CREATE INDEX IF NOT EXISTS ix_counterparty_name ON counterparty(display_name);");
		st.execute("""
			    CREATE TABLE IF NOT EXISTS txn(
			      id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
			      txn_date DATE NOT NULL,
			      payee_id BIGINT,
			      memo VARCHAR(500),
			      bank_account_id BIGINT,
			      created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
			      updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
			    )
			""");
		st.execute("CREATE INDEX IF NOT EXISTS ix_txn_date ON txn(txn_date);");
		st.execute("CREATE INDEX IF NOT EXISTS ix_txn_date_id ON txn(txn_date, id);");
		st.execute("CREATE INDEX IF NOT EXISTS ix_txn_bank_date ON txn(bank_account_id, txn_date);");
		st.execute("""
			    CREATE TABLE IF NOT EXISTS txn_split(
			      id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
			      txn_id BIGINT NOT NULL,
			      account_id BIGINT NOT NULL,
			      fund_id BIGINT NOT NULL,
			      activity_id BIGINT,
			      merchant_id BIGINT,
			      nmr_flag BOOLEAN DEFAULT FALSE NOT NULL,
			      notes VARCHAR(500),
			      amount_signed DECIMAL(19,4) NOT NULL
			    )
			""");
		st.execute("CREATE INDEX IF NOT EXISTS ix_split_txn ON txn_split(txn_id);");
		st.execute("CREATE INDEX IF NOT EXISTS ix_split_account ON txn_split(account_id);");
		st.execute("CREATE INDEX IF NOT EXISTS ix_split_fund ON txn_split(fund_id);");
		st.execute("CREATE INDEX IF NOT EXISTS ix_split_txn_amount ON txn_split(txn_id, amount_signed);");
		st.execute("CREATE INDEX IF NOT EXISTS ix_split_account_fund ON txn_split(account_id, fund_id);");
		st.execute("CREATE INDEX IF NOT EXISTS ix_split_fund_txn ON txn_split(fund_id, txn_id);");
		st.execute("""
			    CREATE TABLE IF NOT EXISTS activity(
			      id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
			      code VARCHAR(64) NOT NULL UNIQUE,
			      name VARCHAR(200) NOT NULL,
			      is_active BOOLEAN DEFAULT TRUE NOT NULL
			    )
			""");
		st.execute("""
			    CREATE TABLE IF NOT EXISTS merchant(
			      id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
			      name VARCHAR(200) NOT NULL UNIQUE,
			      is_active BOOLEAN DEFAULT TRUE NOT NULL
			    )
			""");
		st.execute("""
			    CREATE TABLE IF NOT EXISTS account_alias(
			      id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
			      account_id BIGINT NOT NULL,
			      alias_text VARCHAR(400) NOT NULL,
			      source VARCHAR(80),
			      is_active BOOLEAN DEFAULT TRUE NOT NULL
			    )
			""");
		st.execute("CREATE INDEX IF NOT EXISTS ix_account_alias_account_id ON account_alias(account_id);");
		st.execute("""
			    CREATE TABLE IF NOT EXISTS fund_alias(
			      id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
			      fund_id BIGINT NOT NULL,
			      alias_text VARCHAR(400) NOT NULL,
			      source VARCHAR(80),
			      is_active BOOLEAN DEFAULT TRUE NOT NULL
			    )
			""");
		st.execute("CREATE INDEX IF NOT EXISTS ix_fund_alias_fund_id ON fund_alias(fund_id);");
		st.execute("""
			    CREATE TABLE IF NOT EXISTS legacy_txn_map(
			      legacy_txn_id BIGINT PRIMARY KEY,
			      canonical_txn_id BIGINT NOT NULL,
			      migrated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
			      checksum VARCHAR(128),
			      UNIQUE(canonical_txn_id)
			    )
			""");
	}
	
	private void ensureJpaConstraints(Statement st) throws SQLException
	{
		st.execute("""
			    ALTER TABLE txn
			    ADD CONSTRAINT IF NOT EXISTS fk_txn_payee
			    FOREIGN KEY (payee_id) REFERENCES counterparty(id) ON DELETE SET NULL
			""");
		st.execute("""
			    ALTER TABLE txn
			    ADD CONSTRAINT IF NOT EXISTS fk_txn_bank_account
			    FOREIGN KEY (bank_account_id) REFERENCES account(id) ON DELETE SET NULL
			""");
		st.execute("""
			    ALTER TABLE txn_split
			    ADD CONSTRAINT IF NOT EXISTS fk_split_txn
			    FOREIGN KEY (txn_id) REFERENCES txn(id) ON DELETE CASCADE
			""");
		st.execute("""
			    ALTER TABLE txn_split
			    ADD CONSTRAINT IF NOT EXISTS fk_split_account
			    FOREIGN KEY (account_id) REFERENCES account(id)
			""");
		st.execute("""
			    ALTER TABLE txn_split
			    ADD CONSTRAINT IF NOT EXISTS fk_split_fund
			    FOREIGN KEY (fund_id) REFERENCES fund(id)
			""");
		st.execute("""
			    ALTER TABLE txn_split
			    ADD CONSTRAINT IF NOT EXISTS chk_txn_split_amount_nonzero
			    CHECK (amount_signed <> 0)
			""");
		st.execute("""
			    ALTER TABLE txn_split
			    ADD CONSTRAINT IF NOT EXISTS chk_txn_split_amount_scale
			    CHECK (amount_signed = ROUND(amount_signed, 2))
			""");
		st.execute("""
			    ALTER TABLE legacy_txn_map
			    ADD CONSTRAINT IF NOT EXISTS fk_legacy_map_txn
			    FOREIGN KEY (canonical_txn_id) REFERENCES txn(id) ON DELETE CASCADE
			""");
		st.execute("""
			    ALTER TABLE account
			    ADD CONSTRAINT IF NOT EXISTS fk_account_chart
			    FOREIGN KEY (chart_id) REFERENCES chart_of_accounts(id) ON DELETE SET NULL
			""");
		st.execute("""
			    ALTER TABLE account
			    ADD CONSTRAINT IF NOT EXISTS fk_account_parent
			    FOREIGN KEY (parent_id) REFERENCES account(id) ON DELETE SET NULL
			""");
		st.execute("""
			    ALTER TABLE account_alias
			    ADD CONSTRAINT IF NOT EXISTS fk_account_alias_account
			    FOREIGN KEY (account_id) REFERENCES account(id)
			""");
		st.execute("""
			    ALTER TABLE fund_alias
			    ADD CONSTRAINT IF NOT EXISTS fk_fund_alias_fund
			    FOREIGN KEY (fund_id) REFERENCES fund(id)
			""");
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
		st.execute("CREATE UNIQUE INDEX IF NOT EXISTS donor_external_id_idx ON donor(external_id);");
		st.execute(
			"UPDATE donor SET external_id = name WHERE external_id IS NULL AND name IS NOT NULL;");
		st.execute("UPDATE person SET type = 'DONOR' WHERE type IS NULL OR TRIM(type) = '';");
		st.execute("CREATE INDEX IF NOT EXISTS person_name_idx ON person(name)");
	}
	
	private void ensureRemainingLegacyTables(Statement st) throws SQLException
	{
		st.execute("""
			    CREATE TABLE IF NOT EXISTS undeposited_funds_item(
			      id BIGINT AUTO_INCREMENT PRIMARY KEY,
			      date_sent_received VARCHAR(32),
			      date_transfer_or_check VARCHAR(64),
			      date_on_statement VARCHAR(32),
			      name_of_person_business VARCHAR(255),
			      details_notes VARCHAR(512),
			      from_to_card_merchant VARCHAR(255),
			      account_for_payment_or_deposit VARCHAR(128),
			      amount DECIMAL(12,2),
			      date_reversed VARCHAR(32),
			      reversal_approved_by VARCHAR(255)
			    )
			""");
		st.execute(
			"CREATE INDEX IF NOT EXISTS undeposited_funds_item_idx ON undeposited_funds_item(id)");
		st.execute("""
			    CREATE TABLE IF NOT EXISTS document(
			      name VARCHAR(128) PRIMARY KEY,
			      content CLOB
			    )
			""");
		st.execute("""
			    CREATE TABLE IF NOT EXISTS json_storage(
			      storage_key VARCHAR(255) PRIMARY KEY,
			      payload CLOB
			    )
			""");
		st.execute("""
			    CREATE TABLE IF NOT EXISTS company_store(
			      id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
			      name VARCHAR(255) NOT NULL,
			      payload BLOB NOT NULL,
			      updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
			    )
			""");
		st.execute("""
			    ALTER TABLE IF EXISTS imported_asset_record
			    ADD COLUMN IF NOT EXISTS accumulated_depreciation DECIMAL(19,2)
			""");
	}

	private void ensureOperationalLinkageTables(Statement st) throws SQLException
	{
		st.execute("""
			    CREATE TABLE IF NOT EXISTS ledger_record(
			      ledger_record_id VARCHAR(255) PRIMARY KEY,
			      ledger_id VARCHAR(128) NOT NULL DEFAULT 'PRIMARY_LEDGER',
			      journal_entry_id BIGINT,
			      bank_id_record_id VARCHAR(255),
			      created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
			      updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
			    )
			""");
		st.execute(
			"CREATE INDEX IF NOT EXISTS ix_operational_link_backfill_open ON operational_link_backfill_queue(module_name, issue_code, resolved_at);");
		st.execute("""
			    ALTER TABLE ledger_record
			    ADD CONSTRAINT IF NOT EXISTS fk_ledger_record_journal_entry
			    FOREIGN KEY (journal_entry_id) REFERENCES journal_entry(id) ON DELETE SET NULL
			""");
		st.execute("""
			    ALTER TABLE ledger_record
			    ADD CONSTRAINT IF NOT EXISTS fk_ledger_record_bank_id
			    FOREIGN KEY (bank_id_record_id) REFERENCES bank_id_record(bank_id_record_id) ON DELETE SET NULL
			""");
		st.execute("""
			    ALTER TABLE banking_transaction_record
			    ADD CONSTRAINT IF NOT EXISTS fk_banking_txn_bank_id
			    FOREIGN KEY (bank_id_record_id) REFERENCES bank_id_record(bank_id_record_id) ON DELETE SET NULL
			""");
		st.execute("""
			    ALTER TABLE banking_transaction_record
			    ADD CONSTRAINT IF NOT EXISTS fk_banking_txn_journal
			    FOREIGN KEY (journal_txn_id) REFERENCES journal_transaction(id) ON DELETE SET NULL
			""");
		st.execute("""
			    ALTER TABLE banking_transaction_record
			    ADD CONSTRAINT IF NOT EXISTS fk_banking_txn_fund
			    FOREIGN KEY (fund_id) REFERENCES fund(id) ON DELETE SET NULL
			""");
		st.execute("""
			    ALTER TABLE donation_record
			    ADD CONSTRAINT IF NOT EXISTS fk_donation_record_journal
			    FOREIGN KEY (journal_txn_id) REFERENCES journal_transaction(id) ON DELETE SET NULL
			""");
		st.execute("""
			    ALTER TABLE donation_record
			    ADD CONSTRAINT IF NOT EXISTS fk_donation_record_donor
			    FOREIGN KEY (donor_external_id) REFERENCES donor(external_id) ON DELETE SET NULL
			""");
		st.execute("""
			    ALTER TABLE donation_journal_link
			    ADD CONSTRAINT IF NOT EXISTS fk_donation_link_donation
			    FOREIGN KEY (donation_id) REFERENCES donation_record(donation_id) ON DELETE CASCADE
			""");
		st.execute("""
			    ALTER TABLE donation_journal_link
			    ADD CONSTRAINT IF NOT EXISTS fk_donation_link_journal
			    FOREIGN KEY (journal_txn_id) REFERENCES journal_transaction(id) ON DELETE CASCADE
			""");
		st.execute("""
			    ALTER TABLE donation_journal_link
			    ADD CONSTRAINT IF NOT EXISTS ck_donation_link_role
			    CHECK (link_role IN ('ORIGINAL','REVERSAL','ADJUSTMENT'))
			""");
		st.execute(
			"CREATE INDEX IF NOT EXISTS ix_donation_record_journal ON donation_record(journal_txn_id);");
		st.execute(
			"CREATE INDEX IF NOT EXISTS ix_donation_link_journal ON donation_journal_link(journal_txn_id);");
		st.execute("""
			    ALTER TABLE asset_record_detail
			    ADD CONSTRAINT IF NOT EXISTS fk_asset_record_journal
			    FOREIGN KEY (journal_txn_id) REFERENCES journal_transaction(id) ON DELETE SET NULL
			""");
		st.execute("""
			    ALTER TABLE inventory_asset_link
			    ADD CONSTRAINT IF NOT EXISTS fk_inventory_asset_asset
			    FOREIGN KEY (asset_record_id) REFERENCES asset_record_detail(asset_record_id) ON DELETE CASCADE
			""");
		st.execute("""
			    ALTER TABLE inventory_asset_link
			    ADD CONSTRAINT IF NOT EXISTS fk_inventory_asset_journal
			    FOREIGN KEY (journal_txn_id) REFERENCES journal_transaction(id) ON DELETE SET NULL
			""");
		st.execute("""
			    ALTER TABLE depreciation_record
			    ADD CONSTRAINT IF NOT EXISTS fk_depreciation_record_run
			    FOREIGN KEY (depreciation_run_id) REFERENCES depreciation_run(depreciation_run_id) ON DELETE SET NULL
			""");
		st.execute("""
			    ALTER TABLE depreciation_record
			    ADD CONSTRAINT IF NOT EXISTS fk_depreciation_record_asset
			    FOREIGN KEY (asset_record_id) REFERENCES asset_record_detail(asset_record_id) ON DELETE SET NULL
			""");
		st.execute(
			"ALTER TABLE asset_record_detail ADD COLUMN IF NOT EXISTS asset_state VARCHAR(30) DEFAULT 'DRAFT' NOT NULL;");
		st.execute(
			"ALTER TABLE asset_record_detail ADD COLUMN IF NOT EXISTS in_service_date DATE;");
		st.execute(
			"ALTER TABLE asset_record_detail ADD COLUMN IF NOT EXISTS disposal_date DATE;");
		st.execute(
			"ALTER TABLE asset_record_detail ADD COLUMN IF NOT EXISTS depreciable_basis DECIMAL(19,2);");
		st.execute(
			"ALTER TABLE asset_record_detail ADD COLUMN IF NOT EXISTS salvage_value DECIMAL(19,2) DEFAULT 0;");
		st.execute(
			"ALTER TABLE asset_record_detail ADD COLUMN IF NOT EXISTS useful_life_months INT;");
		st.execute(
			"ALTER TABLE asset_record_detail ADD COLUMN IF NOT EXISTS posted_acquisition_txn_id INT;");
		st.execute(
			"ALTER TABLE asset_record_detail ADD COLUMN IF NOT EXISTS posted_disposal_txn_id INT;");
		st.execute("""
			    ALTER TABLE asset_record_detail
			    ADD CONSTRAINT IF NOT EXISTS chk_asset_record_state_domain
			    CHECK (asset_state IN ('DRAFT','ACTIVE','HELD_FOR_SALE','DISPOSED','RETIRED'))
			""");
		st.execute("""
			    ALTER TABLE asset_record_detail
			    ADD CONSTRAINT IF NOT EXISTS chk_asset_record_disposal_state_consistency
			    CHECK (
			      (asset_state IN ('DISPOSED','RETIRED') AND disposal_date IS NOT NULL)
			      OR
			      (asset_state NOT IN ('DISPOSED','RETIRED') AND disposal_date IS NULL)
			    )
			""");
		st.execute("""
			    ALTER TABLE asset_record_detail
			    ADD CONSTRAINT IF NOT EXISTS chk_asset_record_in_service_after_acquired
			    CHECK (in_service_date IS NULL OR date_acquired IS NULL OR in_service_date >= date_acquired)
			""");
		st.execute("""
			    ALTER TABLE asset_record_detail
			    ADD CONSTRAINT IF NOT EXISTS fk_asset_record_posted_acquisition_journal
			    FOREIGN KEY (posted_acquisition_txn_id) REFERENCES journal_transaction(id) ON DELETE SET NULL
			""");
		st.execute("""
			    ALTER TABLE asset_record_detail
			    ADD CONSTRAINT IF NOT EXISTS fk_asset_record_posted_disposal_journal
			    FOREIGN KEY (posted_disposal_txn_id) REFERENCES journal_transaction(id) ON DELETE SET NULL
			""");
		st.execute("""
			    CREATE INDEX IF NOT EXISTS ix_asset_record_state_service_disposal
			    ON asset_record_detail(asset_state, in_service_date, disposal_date)
			""");
		st.execute("""
			    CREATE INDEX IF NOT EXISTS ix_asset_record_posted_acquisition
			    ON asset_record_detail(posted_acquisition_txn_id)
			""");
		st.execute(
			"ALTER TABLE depreciation_run ADD COLUMN IF NOT EXISTS period_start DATE;");
		st.execute(
			"ALTER TABLE depreciation_run ADD COLUMN IF NOT EXISTS period_end DATE;");
		st.execute(
			"ALTER TABLE depreciation_run ADD COLUMN IF NOT EXISTS run_status VARCHAR(20) DEFAULT 'DRAFT' NOT NULL;");
		st.execute(
			"ALTER TABLE depreciation_run ADD COLUMN IF NOT EXISTS is_locked BOOLEAN DEFAULT FALSE NOT NULL;");
		st.execute(
			"ALTER TABLE depreciation_run ADD COLUMN IF NOT EXISTS locked_at TIMESTAMP;");
		st.execute(
			"ALTER TABLE depreciation_run ADD COLUMN IF NOT EXISTS locked_by VARCHAR(120);");
		st.execute(
			"ALTER TABLE depreciation_run ADD COLUMN IF NOT EXISTS posted_txn_id INT;");
		st.execute("""
			    ALTER TABLE depreciation_run
			    ADD CONSTRAINT IF NOT EXISTS chk_depreciation_run_status_domain
			    CHECK (run_status IN ('DRAFT','CALCULATED','POSTED','VOIDED'))
			""");
		st.execute("""
			    ALTER TABLE depreciation_run
			    ADD CONSTRAINT IF NOT EXISTS chk_depreciation_run_period_order
			    CHECK (period_start IS NULL OR period_end IS NULL OR period_start <= period_end)
			""");
		st.execute("""
			    ALTER TABLE depreciation_run
			    ADD CONSTRAINT IF NOT EXISTS chk_depreciation_run_lock_metadata
			    CHECK ((is_locked = FALSE) OR (is_locked = TRUE AND locked_at IS NOT NULL))
			""");
		st.execute("""
			    ALTER TABLE depreciation_run
			    ADD CONSTRAINT IF NOT EXISTS chk_depreciation_run_posted_link_by_status
			    CHECK (
			      (run_status = 'POSTED' AND posted_txn_id IS NOT NULL)
			      OR
			      (run_status <> 'POSTED' AND posted_txn_id IS NULL)
			    )
			""");
		st.execute("""
			    ALTER TABLE depreciation_run
			    ADD CONSTRAINT IF NOT EXISTS fk_depreciation_run_posted_journal
			    FOREIGN KEY (posted_txn_id) REFERENCES journal_transaction(id) ON DELETE SET NULL
			""");
		st.execute("""
			    ALTER TABLE depreciation_run
			    ADD CONSTRAINT IF NOT EXISTS uq_depreciation_run_period UNIQUE (period_start, period_end)
			""");
		st.execute("""
			    CREATE INDEX IF NOT EXISTS ix_depreciation_run_status_period_end
			    ON depreciation_run(run_status, period_end, created_at)
			""");
		st.execute("""
			    CREATE INDEX IF NOT EXISTS ix_depreciation_run_posted_txn
			    ON depreciation_run(posted_txn_id)
			""");
		st.execute(
			"ALTER TABLE depreciation_record ADD COLUMN IF NOT EXISTS period_start DATE;");
		st.execute(
			"ALTER TABLE depreciation_record ADD COLUMN IF NOT EXISTS period_end DATE;");
		st.execute(
			"ALTER TABLE depreciation_record ADD COLUMN IF NOT EXISTS sequence_in_run INT;");
		st.execute(
			"ALTER TABLE depreciation_record ADD COLUMN IF NOT EXISTS posted_journal_txn_id INT;");
		st.execute(
			"ALTER TABLE depreciation_record ADD COLUMN IF NOT EXISTS reversal_journal_txn_id INT;");
		st.execute("""
			    ALTER TABLE depreciation_record
			    ADD CONSTRAINT IF NOT EXISTS chk_depreciation_record_period_order
			    CHECK (period_start IS NULL OR period_end IS NULL OR period_start <= period_end)
			""");
		st.execute("""
			    ALTER TABLE depreciation_record
			    ADD CONSTRAINT IF NOT EXISTS fk_depreciation_record_posted_journal
			    FOREIGN KEY (posted_journal_txn_id) REFERENCES journal_transaction(id) ON DELETE SET NULL
			""");
		st.execute("""
			    ALTER TABLE depreciation_record
			    ADD CONSTRAINT IF NOT EXISTS fk_depreciation_record_reversal_journal
			    FOREIGN KEY (reversal_journal_txn_id) REFERENCES journal_transaction(id) ON DELETE SET NULL
			""");
		st.execute("""
			    ALTER TABLE depreciation_record
			    ADD CONSTRAINT IF NOT EXISTS uq_depreciation_record_run_asset UNIQUE (depreciation_run_id, asset_record_id)
			""");
		st.execute("""
			    CREATE INDEX IF NOT EXISTS ix_depreciation_record_asset_period
			    ON depreciation_record(asset_record_id, period_end, depreciation_date)
			""");
		st.execute("""
			    CREATE INDEX IF NOT EXISTS ix_depreciation_record_run_sequence
			    ON depreciation_record(depreciation_run_id, sequence_in_run)
			""");
		st.execute(
			"ALTER TABLE inventory_asset_link ADD COLUMN IF NOT EXISTS link_type VARCHAR(40) DEFAULT 'COMPONENT' NOT NULL;");
		st.execute(
			"ALTER TABLE inventory_asset_link ADD COLUMN IF NOT EXISTS is_primary_link BOOLEAN DEFAULT FALSE NOT NULL;");
		st.execute("""
			    ALTER TABLE inventory_asset_link
			    ADD COLUMN IF NOT EXISTS primary_asset_inventory_key VARCHAR(255)
			    GENERATED ALWAYS AS (CASE WHEN is_primary_link THEN inventory_item_id ELSE NULL END)
			""");
		st.execute("""
			    ALTER TABLE inventory_asset_link
			    ADD CONSTRAINT IF NOT EXISTS chk_inventory_asset_link_type_domain
			    CHECK (link_type IN ('CAPITALIZED_FROM_INVENTORY','COMPONENT','DISPOSAL_SOURCE'))
			""");
		st.execute("""
			    CREATE INDEX IF NOT EXISTS ix_inventory_asset_asset_inventory
			    ON inventory_asset_link(asset_record_id, inventory_item_id)
			""");
		st.execute("""
			    CREATE UNIQUE INDEX IF NOT EXISTS uq_inventory_primary_asset_link
			    ON inventory_asset_link(primary_asset_inventory_key)
			""");
		st.execute("""
			    CREATE TABLE IF NOT EXISTS depreciation_run_event(
			      id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
			      depreciation_run_id VARCHAR(255) NOT NULL,
			      event_type VARCHAR(40) NOT NULL,
			      event_detail VARCHAR(1000),
			      actor VARCHAR(120),
			      occurred_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
			      CONSTRAINT fk_depreciation_run_event_run
			        FOREIGN KEY (depreciation_run_id) REFERENCES depreciation_run(depreciation_run_id) ON DELETE CASCADE
			    )
			""");
		st.execute("""
			    CREATE INDEX IF NOT EXISTS ix_depreciation_run_event_run_time
			    ON depreciation_run_event(depreciation_run_id, occurred_at)
			""");
		st.execute("""
			    ALTER TABLE grant_record
			    ADD CONSTRAINT IF NOT EXISTS fk_grant_record_donor
			    FOREIGN KEY (donor_id) REFERENCES donor(id) ON DELETE SET NULL
			""");
		st.execute("""
			    ALTER TABLE grant_record
			    ADD CONSTRAINT IF NOT EXISTS fk_grant_record_person
			    FOREIGN KEY (person_id) REFERENCES person(id) ON DELETE SET NULL
			""");
		st.execute("""
			    ALTER TABLE grant_record
			    ADD CONSTRAINT IF NOT EXISTS fk_grant_record_fund
			    FOREIGN KEY (fund_id) REFERENCES fund(id) ON DELETE SET NULL
			""");
		st.execute("""
			    ALTER TABLE grant_record
			    ADD CONSTRAINT IF NOT EXISTS fk_grant_record_journal
			    FOREIGN KEY (journal_txn_id) REFERENCES journal_transaction(id) ON DELETE SET NULL
			""");
		st.execute(
			"ALTER TABLE grant_record ADD COLUMN IF NOT EXISTS grantor VARCHAR(255);");
		st.execute(
			"ALTER TABLE grant_record ADD COLUMN IF NOT EXISTS amount DECIMAL(19,2);");
		st.execute(
			"ALTER TABLE grant_record ADD COLUMN IF NOT EXISTS date_awarded_text VARCHAR(64);");
		st.execute(
			"ALTER TABLE grant_record ADD COLUMN IF NOT EXISTS purpose VARCHAR(500);");
		st.execute(
			"ALTER TABLE grant_record ADD COLUMN IF NOT EXISTS status VARCHAR(64);");
		st.execute("ALTER TABLE grant_record ADD COLUMN IF NOT EXISTS award_date DATE;");
		st.execute("ALTER TABLE grant_record ADD COLUMN IF NOT EXISTS period_start DATE;");
		st.execute("ALTER TABLE grant_record ADD COLUMN IF NOT EXISTS period_end DATE;");
		st.execute("ALTER TABLE grant_record ADD COLUMN IF NOT EXISTS restriction_class VARCHAR(32) DEFAULT 'RESTRICTED' NOT NULL;");
		st.execute("ALTER TABLE grant_record ADD COLUMN IF NOT EXISTS restriction_release_rule VARCHAR(255);");
		st.execute("ALTER TABLE grant_record ADD COLUMN IF NOT EXISTS compliance_status VARCHAR(32) DEFAULT 'IN_GOOD_STANDING' NOT NULL;");
		st.execute("ALTER TABLE grant_record ADD COLUMN IF NOT EXISTS reporting_frequency VARCHAR(32);");
		st.execute("ALTER TABLE grant_record ADD COLUMN IF NOT EXISTS next_report_due DATE;");
		st.execute("ALTER TABLE grant_record ADD COLUMN IF NOT EXISTS closeout_date DATE;");
		st.execute("ALTER TABLE grant_record ADD COLUMN IF NOT EXISTS canonical_txn_id BIGINT;");
		st.execute("ALTER TABLE grant_record ADD COLUMN IF NOT EXISTS activity_id BIGINT;");
		st.execute("ALTER TABLE grant_record ADD COLUMN IF NOT EXISTS counterparty_id BIGINT;");
		st.execute("ALTER TABLE grant_record ADD COLUMN IF NOT EXISTS contact_person_id BIGINT;");
		st.execute("ALTER TABLE grant_record ADD COLUMN IF NOT EXISTS grant_reference_number VARCHAR(128);");
		st.execute("ALTER TABLE grant_record ADD COLUMN IF NOT EXISTS compliance_notes VARCHAR(2000);");
		st.execute("""
			    ALTER TABLE grant_record
			    ADD CONSTRAINT IF NOT EXISTS ck_grant_record_restriction_class
			    CHECK (restriction_class IN ('RESTRICTED','UNRESTRICTED','BOARD_DESIGNATED'))
			""");
		st.execute("""
			    ALTER TABLE grant_record
			    ADD CONSTRAINT IF NOT EXISTS ck_grant_record_compliance_status
			    CHECK (compliance_status IN ('IN_GOOD_STANDING','LATE_REPORT','AT_RISK','SUSPENDED','CLOSED'))
			""");
		st.execute("""
			    ALTER TABLE grant_record
			    ADD CONSTRAINT IF NOT EXISTS ck_grant_record_period_order
			    CHECK (period_start IS NULL OR period_end IS NULL OR period_start <= period_end)
			""");
		st.execute("""
			    ALTER TABLE grant_record
			    ADD CONSTRAINT IF NOT EXISTS ck_grant_record_award_amount_nonnegative
			    CHECK (amount IS NULL OR amount >= 0)
			""");
		st.execute("""
			    ALTER TABLE grant_record
			    ADD CONSTRAINT IF NOT EXISTS ck_grant_record_contact_presence
			    CHECK (donor_id IS NOT NULL OR person_id IS NOT NULL OR counterparty_id IS NOT NULL OR NULLIF(TRIM(grantor), '') IS NOT NULL)
			""");
		st.execute("""
			    ALTER TABLE grant_record
			    ADD CONSTRAINT IF NOT EXISTS fk_grant_record_activity
			    FOREIGN KEY (activity_id) REFERENCES activity(id) ON DELETE SET NULL
			""");
		st.execute("""
			    ALTER TABLE grant_record
			    ADD CONSTRAINT IF NOT EXISTS fk_grant_record_canonical_txn
			    FOREIGN KEY (canonical_txn_id) REFERENCES txn(id) ON DELETE SET NULL
			""");
		st.execute("""
			    ALTER TABLE grant_record
			    ADD CONSTRAINT IF NOT EXISTS fk_grant_record_counterparty
			    FOREIGN KEY (counterparty_id) REFERENCES counterparty(id) ON DELETE SET NULL
			""");
		st.execute("""
			    ALTER TABLE grant_record
			    ADD CONSTRAINT IF NOT EXISTS fk_grant_record_contact_person
			    FOREIGN KEY (contact_person_id) REFERENCES person(id) ON DELETE SET NULL
			""");
		st.execute(
			"CREATE INDEX IF NOT EXISTS grant_record_grant_id_idx ON grant_record(grant_id);");
		st.execute(
			"CREATE INDEX IF NOT EXISTS ix_grant_record_reporting_due ON grant_record(compliance_status, next_report_due);");
		st.execute(
			"CREATE INDEX IF NOT EXISTS ix_grant_record_restriction_fund ON grant_record(restriction_class, fund_id);");
		st.execute(
			"CREATE INDEX IF NOT EXISTS ix_grant_record_activity ON grant_record(activity_id);");
		st.execute(
			"CREATE INDEX IF NOT EXISTS ix_grant_record_counterparty ON grant_record(counterparty_id);");
		st.execute(
			"CREATE UNIQUE INDEX IF NOT EXISTS uq_grant_record_reference_number ON grant_record(grant_reference_number);");
		st.execute("""
			    CREATE TABLE IF NOT EXISTS grant_posting_link(
			      id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
			      grant_record_id VARCHAR(255) NOT NULL,
			      posting_model VARCHAR(16) NOT NULL,
			      txn_split_id BIGINT,
			      journal_entry_id BIGINT,
			      posting_role VARCHAR(16) DEFAULT 'REVENUE' NOT NULL,
			      recognized_amount DECIMAL(19,2) NOT NULL,
			      recognized_on DATE,
			      created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
			      updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
			    )
			""");
		st.execute("""
			    ALTER TABLE grant_posting_link
			    ADD CONSTRAINT IF NOT EXISTS ck_grant_posting_model
			    CHECK (posting_model IN ('CANONICAL','LEGACY'))
			""");
		st.execute("""
			    ALTER TABLE grant_posting_link
			    ADD CONSTRAINT IF NOT EXISTS ck_grant_posting_role
			    CHECK (posting_role IN ('REVENUE','DEFERRAL','RELEASE','ADJUSTMENT'))
			""");
		st.execute("""
			    ALTER TABLE grant_posting_link
			    ADD CONSTRAINT IF NOT EXISTS ck_grant_posting_amount_nonzero
			    CHECK (recognized_amount <> 0)
			""");
		st.execute("""
			    ALTER TABLE grant_posting_link
			    ADD CONSTRAINT IF NOT EXISTS ck_grant_posting_target_xor
			    CHECK (
			      (txn_split_id IS NOT NULL AND journal_entry_id IS NULL)
			      OR (txn_split_id IS NULL AND journal_entry_id IS NOT NULL)
			    )
			""");
		st.execute("""
			    ALTER TABLE grant_posting_link
			    ADD CONSTRAINT IF NOT EXISTS fk_grant_posting_grant
			    FOREIGN KEY (grant_record_id) REFERENCES grant_record(grant_record_id) ON DELETE CASCADE
			""");
		st.execute("""
			    ALTER TABLE grant_posting_link
			    ADD CONSTRAINT IF NOT EXISTS fk_grant_posting_split
			    FOREIGN KEY (txn_split_id) REFERENCES txn_split(id) ON DELETE CASCADE
			""");
		st.execute("""
			    ALTER TABLE grant_posting_link
			    ADD CONSTRAINT IF NOT EXISTS fk_grant_posting_entry
			    FOREIGN KEY (journal_entry_id) REFERENCES journal_entry(id) ON DELETE CASCADE
			""");
		st.execute(
			"CREATE INDEX IF NOT EXISTS ix_grant_posting_grant_role ON grant_posting_link(grant_record_id, posting_role, recognized_on);");
		st.execute(
			"CREATE INDEX IF NOT EXISTS ix_grant_posting_split ON grant_posting_link(txn_split_id);");
		st.execute(
			"CREATE INDEX IF NOT EXISTS ix_grant_posting_entry ON grant_posting_link(journal_entry_id);");
		st.execute("""
			    CREATE OR REPLACE VIEW v_grant_restriction_reporting AS
			    SELECT
			      gr.grant_record_id,
			      gr.grant_id,
			      gr.grant_reference_number,
			      gr.status,
			      gr.compliance_status,
			      gr.restriction_class,
			      f.code AS fund_code,
			      f.name AS fund_name,
			      a.code AS activity_code,
			      a.name AS activity_name,
			      COALESCE(d.name, p.name, cp.display_name, gr.grantor) AS donor_or_contact,
			      gr.amount AS awarded_amount,
			      COALESCE(pa.recognized_total, 0) AS recognized_amount,
			      COALESCE(pa.deferred_total, 0) AS deferred_amount,
			      (COALESCE(gr.amount, 0) - COALESCE(pa.recognized_total, 0)) AS unrecognized_balance,
			      gr.next_report_due
			    FROM grant_record gr
			    LEFT JOIN donor d ON d.id = gr.donor_id
			    LEFT JOIN person p ON p.id = COALESCE(gr.contact_person_id, gr.person_id)
			    LEFT JOIN counterparty cp ON cp.id = gr.counterparty_id
			    LEFT JOIN fund f ON f.id = gr.fund_id
			    LEFT JOIN activity a ON a.id = gr.activity_id
			    LEFT JOIN (
			      SELECT
			        gpl.grant_record_id,
			        SUM(CASE WHEN gpl.posting_role IN ('REVENUE','RELEASE') THEN gpl.recognized_amount ELSE 0 END) AS recognized_total,
			        SUM(CASE WHEN gpl.posting_role = 'DEFERRAL' THEN gpl.recognized_amount ELSE 0 END) AS deferred_total
			      FROM grant_posting_link gpl
			      GROUP BY gpl.grant_record_id
			    ) pa ON pa.grant_record_id = gr.grant_record_id
			""");

		st.execute("""
			    CREATE OR REPLACE VIEW v_finance_posting_enforcement_exceptions AS
			    SELECT 'fund_transfer' AS domain_table, CAST(id AS VARCHAR) AS domain_id,
			           status AS state_value, 'posted_txn_id_missing_or_unexpected' AS issue
			    FROM fund_transfer
			    WHERE (status = 'POSTED' AND posted_txn_id IS NULL)
			       OR (status <> 'POSTED' AND posted_txn_id IS NOT NULL)
			    UNION ALL
			    SELECT 'depreciation_run' AS domain_table, depreciation_run_id AS domain_id,
			           run_status AS state_value, 'posted_txn_id_missing_or_unexpected' AS issue
			    FROM depreciation_run
			    WHERE (run_status = 'POSTED' AND posted_txn_id IS NULL)
			       OR (run_status <> 'POSTED' AND posted_txn_id IS NOT NULL)
			    UNION ALL
			    SELECT 'grant_record' AS domain_table, gr.grant_record_id AS domain_id,
			           COALESCE(gr.status, '') AS state_value, 'canonical_txn_missing_posting_link' AS issue
			    FROM grant_record gr
			    WHERE gr.canonical_txn_id IS NOT NULL
			      AND NOT EXISTS (
			          SELECT 1 FROM grant_posting_link gpl
			          LEFT JOIN journal_entry je ON je.id = gpl.journal_entry_id
			          WHERE gpl.grant_record_id = gr.grant_record_id
			            AND je.txn_id = gr.canonical_txn_id
			      )
		""");
		st.execute("""
			    CREATE OR REPLACE VIEW v_finance_posting_enforcement_dashboard AS
			    SELECT domain_table, issue, COUNT(*) AS exception_count
			    FROM v_finance_posting_enforcement_exceptions
			    GROUP BY domain_table, issue
		""");
		st.execute("""
			    CREATE TABLE IF NOT EXISTS sale_record(
			      sale_id VARCHAR(255) PRIMARY KEY,
			      sale_date_text VARCHAR(64),
			      item VARCHAR(255),
			      qty INT NOT NULL,
			      unit_price DECIMAL(19,2),
			      unit_cost DECIMAL(19,2),
			      details CLOB,
			      created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
			      updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
			    )
			""");
		st.execute(
			"CREATE INDEX IF NOT EXISTS sale_record_date_idx ON sale_record(sale_date_text);");
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
