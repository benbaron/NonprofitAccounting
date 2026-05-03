
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
	private static final String MIGRATION_BANKING_RECON_SCHEMA_V1 = "banking-reconciliation-schema-v1";
	private static final String MIGRATION_REPORTING_SCHEDULE_CONFIG_V1 = "reporting-schedule-config-v1";
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
		try (Connection c = getConnection(); Statement st = c.createStatement())
		{
			ensureMigrationTables(st);
			ensureCompanyProfile(st);
			ensureAccountAndLegacyJournalTables(st);
			ensureJpaTables(st);
			ensureJpaConstraints(st);
			ensureFundTransferIntegrityArtifacts(st);
			backfillLegacyTxnMap(c);
			ensureCompatibilityViews(st);
			ensurePeopleAndCounterparty(st);
			runReconciledDataBackfill(c);
			ensureRemainingLegacyTables(st);
			ensureOperationalLinkageTables(st);
			runOperationalLinkBackfillMigration(c);
			runReportingScheduleConfigurationMigration(c);
			runBankingReconciliationSchemaMigration(c);
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
	
	private void ensureCompanyProfile(Statement st) throws SQLException
	{
		st.execute("""
			    CREATE TABLE IF NOT EXISTS company_profile(
			      id INT PRIMARY KEY,
			      name VARCHAR(255),
			      address VARCHAR(255),
			      phone VARCHAR(64),
			      email VARCHAR(255),
			      fiscal_year_start VARCHAR(16),
			      base_currency VARCHAR(8),
			      starting_balance_date VARCHAR(16),
			      chart_of_accounts_type VARCHAR(64),
			      admin_username VARCHAR(128),
			      admin_password VARCHAR(128),
			      default_bank_account VARCHAR(128),
			      enable_fund_accounting BOOLEAN,
			      enable_inventory BOOLEAN,
			      enable_multi_currency BOOLEAN,
			      legal_structure VARCHAR(128),
			      tax_id VARCHAR(128),
			      company_file_dir VARCHAR(512),
			      company_file_name VARCHAR(255)
			    )
			""");
		st.execute(
			"ALTER TABLE company_profile ADD COLUMN IF NOT EXISTS legal_structure VARCHAR(128);");
		st.execute(
			"ALTER TABLE company_profile ADD COLUMN IF NOT EXISTS tax_id VARCHAR(128);");
		st.execute(
			"ALTER TABLE company_profile ADD COLUMN IF NOT EXISTS company_file_dir VARCHAR(512);");
		st.execute(
			"ALTER TABLE company_profile ADD COLUMN IF NOT EXISTS company_file_name VARCHAR(255);");
	}
	
	private void ensureAccountAndLegacyJournalTables(Statement st)
		throws SQLException
	{
		st.execute("""
			    CREATE TABLE IF NOT EXISTS account(
			      account_number VARCHAR(64) PRIMARY KEY,
			      name VARCHAR(255),
			      account_code VARCHAR(64),
			      account_type VARCHAR(64),
			      increase_side VARCHAR(16),
			      parent_account_id VARCHAR(64),
			      currency VARCHAR(8),
			      opening_balance DECIMAL(18,2) DEFAULT 0,
			      supplemental_kinds VARCHAR(255)
			    )
			""");
		st.execute(
			"ALTER TABLE account ADD COLUMN IF NOT EXISTS supplemental_kinds VARCHAR(255);");
		st.execute(
			"ALTER TABLE account ADD COLUMN IF NOT EXISTS id BIGINT GENERATED BY DEFAULT AS IDENTITY;");
		st.execute(
			"ALTER TABLE account ADD COLUMN IF NOT EXISTS code VARCHAR(64);");
		st.execute(
			"ALTER TABLE account ADD COLUMN IF NOT EXISTS chart_id BIGINT;");
		st.execute(
			"ALTER TABLE account ADD COLUMN IF NOT EXISTS subtype VARCHAR(40);");
		st.execute(
			"ALTER TABLE account ADD COLUMN IF NOT EXISTS normal_balance VARCHAR(10);");
		st.execute(
			"ALTER TABLE account ADD COLUMN IF NOT EXISTS parent_id BIGINT;");
		st.execute(
			"ALTER TABLE account ADD COLUMN IF NOT EXISTS is_posting BOOLEAN DEFAULT TRUE;");
		st.execute(
			"ALTER TABLE account ADD COLUMN IF NOT EXISTS is_active BOOLEAN DEFAULT TRUE;");
		st.execute(
			"ALTER TABLE account ADD COLUMN IF NOT EXISTS effective_from DATE;");
		st.execute(
			"ALTER TABLE account ADD COLUMN IF NOT EXISTS effective_to DATE;");
		st.execute(
			"ALTER TABLE account ADD COLUMN IF NOT EXISTS description CLOB;");
		st.execute(
			"UPDATE account SET code = account_number WHERE code IS NULL;");
		st.execute(
			"UPDATE account SET normal_balance = CASE WHEN UPPER(COALESCE(increase_side, 'DEBIT')) IN ('CREDIT','CR') THEN 'CREDIT' ELSE 'DEBIT' END WHERE normal_balance IS NULL;");
		st.execute("CREATE UNIQUE INDEX IF NOT EXISTS account_id_udx ON account(id);");
		st.execute("CREATE INDEX IF NOT EXISTS account_chart_idx ON account(chart_id);");
		st.execute("CREATE INDEX IF NOT EXISTS account_parent_idx ON account(parent_id);");
		st.execute("""
			    CREATE TABLE IF NOT EXISTS account_fund(
			      account_number VARCHAR(64) NOT NULL,
			      fund_id VARCHAR(64) NOT NULL,
			      PRIMARY KEY(account_number, fund_id),
			      FOREIGN KEY (account_number) REFERENCES account(account_number) ON DELETE CASCADE
			    )
			""");
		st.execute("""
			    CREATE TABLE IF NOT EXISTS journal_transaction(
			      id INT PRIMARY KEY,
			      booking_ts BIGINT,
			      date_text VARCHAR(32),
			      memo VARCHAR(512),
			      to_from VARCHAR(255),
			      check_number VARCHAR(64),
			      clear_bank VARCHAR(64),
			      bank_name VARCHAR(128),
			      reconciled BOOLEAN DEFAULT FALSE,
			      budget_tracking VARCHAR(512),
			      associated_fund_name VARCHAR(128)
			    )
			""");
		st.execute(
			"ALTER TABLE journal_transaction ADD COLUMN IF NOT EXISTS bank_name VARCHAR(128);");
		st.execute(
			"ALTER TABLE journal_transaction ADD COLUMN IF NOT EXISTS reconciled BOOLEAN DEFAULT FALSE;");
		st.execute(
			"ALTER TABLE journal_transaction ALTER COLUMN budget_tracking VARCHAR(512);");
		st.execute("""
			    CREATE TABLE IF NOT EXISTS bank_statement(
			      id BIGINT AUTO_INCREMENT PRIMARY KEY,
			      bank_name VARCHAR(128) NOT NULL,
			      account_label VARCHAR(128),
			      statement_date DATE NOT NULL,
			      statement_balance DECIMAL(18,2),
			      ledger_balance DECIMAL(18,2),
			      outstanding DECIMAL(18,2),
			      bank_after_outstanding DECIMAL(18,2),
			      difference DECIMAL(18,2),
			      ledger_status VARCHAR(32),
			      institution_name VARCHAR(255),
			      institution_contact VARCHAR(255),
			      account_number VARCHAR(64),
			      account_type VARCHAR(64),
			      signature_requirement VARCHAR(64),
			      interest_bearing VARCHAR(32),
			      currency VARCHAR(16),
			      UNIQUE(bank_name, account_label, statement_date)
			    )
			""");
		st.execute("""
			    CREATE TABLE IF NOT EXISTS journal_entry(
			      id BIGINT AUTO_INCREMENT PRIMARY KEY,
			      txn_id INT NOT NULL,
			      amount DECIMAL(18,2) NOT NULL,
			      account_number VARCHAR(64) NOT NULL,
			      account_side VARCHAR(16),
			      account_name VARCHAR(255),
			      fund_number VARCHAR(64),
			      FOREIGN KEY (txn_id) REFERENCES journal_transaction(id) ON DELETE CASCADE,
			      FOREIGN KEY (account_number) REFERENCES account(account_number)
			    )
			""");
		st.execute("""
			    CREATE TABLE IF NOT EXISTS transaction_info(
			      txn_id INT NOT NULL,
			      k VARCHAR(128) NOT NULL,
			      v VARCHAR(1024),
			      PRIMARY KEY(txn_id, k),
			      FOREIGN KEY (txn_id) REFERENCES journal_transaction(id) ON DELETE CASCADE
			    )
			""");
		st.execute("""
			    CREATE TABLE IF NOT EXISTS txn_supplemental_line(
			      id BIGINT AUTO_INCREMENT PRIMARY KEY,
			      txn_id INT NOT NULL,
			      entry_id BIGINT,
			      line_kind VARCHAR(40) NOT NULL,
			      counterparty_person_id BIGINT,
			      description VARCHAR(500) NOT NULL,
			      reference VARCHAR(120),
			      amount DECIMAL(12,2) NOT NULL,
			      due_date DATE,
			      start_date DATE,
			      end_date DATE,
			      notes VARCHAR(1000),
			      created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
			      updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
			      FOREIGN KEY (txn_id) REFERENCES journal_transaction(id) ON DELETE CASCADE,
			      FOREIGN KEY (entry_id) REFERENCES journal_entry(id) ON DELETE SET NULL,
			      CONSTRAINT chk_txn_supplemental_amount_nonnegative CHECK (amount >= 0)
			    )
			""");
		st.execute("ALTER TABLE txn_supplemental_line ALTER COLUMN amount DECIMAL(12,2);");
		st.execute("""
			    ALTER TABLE txn_supplemental_line
			    ADD CONSTRAINT IF NOT EXISTS fk_txn_supplemental_entry
			    FOREIGN KEY (entry_id) REFERENCES journal_entry(id) ON DELETE SET NULL
			""");
		st.execute("""
			    ALTER TABLE txn_supplemental_line
			    ADD CONSTRAINT IF NOT EXISTS chk_txn_supplemental_amount_nonnegative
			    CHECK (amount >= 0)
			""");
		st.execute(
			"CREATE INDEX IF NOT EXISTS txn_supplemental_txn_idx ON txn_supplemental_line(txn_id)");
		st.execute(
			"CREATE INDEX IF NOT EXISTS txn_supplemental_entry_idx ON txn_supplemental_line(entry_id)");
		st.execute(
			"CREATE INDEX IF NOT EXISTS txn_supplemental_kind_idx ON txn_supplemental_line(line_kind)");
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
			    CREATE TABLE IF NOT EXISTS schedule_kind(
			      id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
			      code VARCHAR(40) NOT NULL,
			      name VARCHAR(200) NOT NULL
			    )
			""");
		st.execute(
			"CREATE UNIQUE INDEX IF NOT EXISTS uq_schedule_kind_code_idx ON schedule_kind(code);");
		st.execute("""
			    CREATE TABLE IF NOT EXISTS report_section(
			      id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
			      name VARCHAR(200) NOT NULL,
			      report_type VARCHAR(40) NOT NULL,
			      sort_order INT NOT NULL
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
			    CREATE TABLE IF NOT EXISTS account_report_section(
			      id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
			      account_id BIGINT NOT NULL,
			      report_section_id BIGINT NOT NULL,
			      sort_order INT NOT NULL,
			      sign_policy VARCHAR(20) NOT NULL
			    )
			""");
		st.execute(
			"CREATE INDEX IF NOT EXISTS ix_account_report_section_account_id ON account_report_section(account_id);");
		st.execute(
			"CREATE INDEX IF NOT EXISTS ix_account_report_section_report_section_id ON account_report_section(report_section_id);");
		st.execute("""
			    CREATE TABLE IF NOT EXISTS account_schedule_requirement(
			      id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
			      account_id BIGINT NOT NULL,
			      schedule_kind_id BIGINT NOT NULL,
			      is_required BOOLEAN NOT NULL,
			      notes VARCHAR(500)
			    )
			""");
		st.execute(
			"CREATE INDEX IF NOT EXISTS ix_account_schedule_requirement_account_id ON account_schedule_requirement(account_id);");
		st.execute(
			"CREATE INDEX IF NOT EXISTS ix_account_schedule_requirement_schedule_kind_id ON account_schedule_requirement(schedule_kind_id);");
		st.execute("""
			    CREATE TABLE IF NOT EXISTS account_subtype_schedule_default(
			      id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
			      subtype VARCHAR(40) NOT NULL,
			      schedule_kind_id BIGINT NOT NULL
			    )
			""");
		st.execute(
			"CREATE INDEX IF NOT EXISTS ix_account_subtype_schedule_default_schedule_kind_id ON account_subtype_schedule_default(schedule_kind_id);");
		st.execute("""
			    CREATE TABLE IF NOT EXISTS fund_transfer(
			      id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
			      transfer_date DATE NOT NULL,
			      from_fund_id BIGINT NOT NULL,
			      to_fund_id BIGINT NOT NULL,
			      amount DECIMAL(19,4) NOT NULL,
			      memo VARCHAR(500),
			      status VARCHAR(20) NOT NULL,
			      posted_txn_id BIGINT,
			      created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
			      updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
			    )
			""");
		st.execute(
			"CREATE INDEX IF NOT EXISTS ix_fund_transfer_posted_txn ON fund_transfer(posted_txn_id);");
		st.execute(
			"CREATE INDEX IF NOT EXISTS ix_fund_transfer_status_date ON fund_transfer(status, transfer_date, id);");
		st.execute(
			"CREATE INDEX IF NOT EXISTS ix_fund_transfer_from_to_date ON fund_transfer(from_fund_id, to_fund_id, transfer_date, id);");
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
			    ALTER TABLE fund_transfer
			    ADD CONSTRAINT IF NOT EXISTS chk_fund_transfer_status_domain
			    CHECK (status IN ('DRAFT','APPROVED','POSTING','POSTED','FAILED','VOIDED'))
			""");
		st.execute("""
			    ALTER TABLE fund_transfer
			    ADD CONSTRAINT IF NOT EXISTS chk_fund_transfer_posted_link_by_status
			    CHECK (
			      (status = 'POSTED' AND posted_txn_id IS NOT NULL)
			      OR
			      (status <> 'POSTED' AND posted_txn_id IS NULL)
			    )
			""");
		st.execute("""
			    ALTER TABLE fund_transfer
			    ADD CONSTRAINT IF NOT EXISTS uq_fund_transfer_posted_txn
			    UNIQUE (posted_txn_id)
			""");
		st.execute("""
			    ALTER TABLE fund_transfer
			    ADD CONSTRAINT IF NOT EXISTS chk_fund_transfer_amount_positive
			    CHECK (amount > 0)
			""");
		st.execute("""
			    ALTER TABLE fund_transfer
			    ADD CONSTRAINT IF NOT EXISTS chk_fund_transfer_distinct_funds
			    CHECK (from_fund_id <> to_fund_id)
			""");
		st.execute("""
			    ALTER TABLE fund_transfer
			    ADD CONSTRAINT IF NOT EXISTS fk_fund_transfer_from_fund
			    FOREIGN KEY (from_fund_id) REFERENCES fund(id)
			""");
		st.execute("""
			    ALTER TABLE fund_transfer
			    ADD CONSTRAINT IF NOT EXISTS fk_fund_transfer_to_fund
			    FOREIGN KEY (to_fund_id) REFERENCES fund(id)
			""");
		st.execute("""
			    ALTER TABLE fund_transfer
			    ADD CONSTRAINT IF NOT EXISTS fk_fund_transfer_posted_txn
			    FOREIGN KEY (posted_txn_id) REFERENCES txn(id) ON DELETE SET NULL
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
		st.execute("""
			    ALTER TABLE account_report_section
			    ADD CONSTRAINT IF NOT EXISTS fk_account_report_section_account
			    FOREIGN KEY (account_id) REFERENCES account(id)
			""");
		st.execute("""
			    ALTER TABLE account_report_section
			    ADD CONSTRAINT IF NOT EXISTS fk_account_report_section_report_section
			    FOREIGN KEY (report_section_id) REFERENCES report_section(id)
			""");
		st.execute("""
			    ALTER TABLE account_schedule_requirement
			    ADD CONSTRAINT IF NOT EXISTS fk_account_schedule_requirement_account
			    FOREIGN KEY (account_id) REFERENCES account(id)
			""");
		st.execute("""
			    ALTER TABLE account_schedule_requirement
			    ADD CONSTRAINT IF NOT EXISTS fk_account_schedule_requirement_schedule_kind
			    FOREIGN KEY (schedule_kind_id) REFERENCES schedule_kind(id)
			""");
		st.execute("""
			    ALTER TABLE account_subtype_schedule_default
			    ADD CONSTRAINT IF NOT EXISTS fk_account_subtype_schedule_default_schedule_kind
			    FOREIGN KEY (schedule_kind_id) REFERENCES schedule_kind(id)
			""");
	}

	private void ensureFundTransferIntegrityArtifacts(Statement st) throws SQLException
	{
		st.execute("""
			    CREATE TABLE IF NOT EXISTS fund_transfer_status_transition (
			      from_status VARCHAR(20) NOT NULL,
			      to_status VARCHAR(20) NOT NULL,
			      is_allowed BOOLEAN NOT NULL,
			      notes VARCHAR(300),
			      PRIMARY KEY (from_status, to_status)
			    )
			""");
		st.execute("""
			    MERGE INTO fund_transfer_status_transition (from_status, to_status, is_allowed, notes)
			    KEY (from_status, to_status)
			    VALUES
			      ('DRAFT', 'APPROVED', TRUE, 'Ready for posting.'),
			      ('DRAFT', 'VOIDED', TRUE, 'Cancelled before approval.'),
			      ('APPROVED', 'POSTING', TRUE, 'Posting transaction started.'),
			      ('APPROVED', 'VOIDED', TRUE, 'Cancelled after approval.'),
			      ('POSTING', 'POSTED', TRUE, 'Atomic post complete.'),
			      ('POSTING', 'FAILED', TRUE, 'Posting failed; no posting txn persisted.'),
			      ('FAILED', 'APPROVED', TRUE, 'Retry after remediation.'),
			      ('FAILED', 'VOIDED', TRUE, 'Abandon failed transfer.'),
			      ('POSTED', 'VOIDED', FALSE, 'Disallow direct void; require reversing transfer.'),
			      ('VOIDED', 'DRAFT', FALSE, 'No reopen from void.')
			""");
		st.execute("""
			    CREATE TABLE IF NOT EXISTS fund_transfer_integrity_event(
			      id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
			      transfer_id BIGINT NOT NULL,
			      event_type VARCHAR(40) NOT NULL,
			      event_detail VARCHAR(1000),
			      detected_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
			    )
			""");
		st.execute("""
			    ALTER TABLE fund_transfer_integrity_event
			    ADD CONSTRAINT IF NOT EXISTS fk_ft_integrity_event_transfer
			    FOREIGN KEY (transfer_id) REFERENCES fund_transfer(id) ON DELETE CASCADE
			""");
		st.execute("""
			    CREATE TABLE IF NOT EXISTS fund_transfer_repair_queue(
			      id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
			      transfer_id BIGINT NOT NULL,
			      issue_code VARCHAR(60) NOT NULL,
			      issue_detail VARCHAR(1000),
			      proposed_action VARCHAR(500),
			      detected_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
			      resolved_at TIMESTAMP,
			      resolved_by VARCHAR(120),
			      resolution_note VARCHAR(1000)
			    )
			""");
		st.execute("""
			    ALTER TABLE fund_transfer_repair_queue
			    ADD CONSTRAINT IF NOT EXISTS fk_ft_repair_transfer
			    FOREIGN KEY (transfer_id) REFERENCES fund_transfer(id) ON DELETE CASCADE
			""");
		st.execute("""
			    CREATE INDEX IF NOT EXISTS ix_ft_repair_open
			    ON fund_transfer_repair_queue(resolved_at, issue_code, detected_at)
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

	private void ensureCompatibilityViews(Statement st) throws SQLException
	{
		st.execute("""
			    CREATE TABLE IF NOT EXISTS rm_donation_summary(
			      txn_id BIGINT PRIMARY KEY,
			      total_amount DECIMAL(19,2) NOT NULL DEFAULT 0,
			      line_count INT NOT NULL DEFAULT 0,
			      refreshed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
			    )
			""");
		st.execute("""
			    CREATE TABLE IF NOT EXISTS rm_grant_summary(
			      txn_id BIGINT PRIMARY KEY,
			      grant_link_count INT NOT NULL DEFAULT 0,
			      refreshed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
			    )
			""");
		st.execute("""
			    CREATE TABLE IF NOT EXISTS rm_fund_summary(
			      txn_id BIGINT PRIMARY KEY,
			      primary_fund_code VARCHAR(64),
			      net_amount DECIMAL(19,2) NOT NULL DEFAULT 0,
			      refreshed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
			    )
			""");
		st.execute("""
			    CREATE TABLE IF NOT EXISTS rm_reconciliation_summary(
			      txn_id BIGINT PRIMARY KEY,
			      absolute_amount DECIMAL(19,2) NOT NULL DEFAULT 0,
			      split_count INT NOT NULL DEFAULT 0,
			      refreshed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
			    )
			""");
		st.execute("""
			    CREATE TABLE IF NOT EXISTS rm_depreciation_summary(
			      depreciation_run_id VARCHAR(255) PRIMARY KEY,
			      record_count INT NOT NULL DEFAULT 0,
			      net_depreciation_total DECIMAL(19,2) NOT NULL DEFAULT 0,
			      refreshed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
			    )
			""");
		st.execute("""
			    CREATE OR REPLACE VIEW v_journal_transaction AS
			    SELECT
			      COALESCE(m.legacy_txn_id, t.id) AS id,
			      EXTRACT(EPOCH FROM t.created_at) * 1000 AS booking_ts,
			      CAST(t.txn_date AS VARCHAR(32)) AS date_text,
			      t.memo AS memo,
			      cp.display_name AS to_from,
			      CAST(NULL AS VARCHAR(64)) AS check_number,
			      CAST(NULL AS VARCHAR(64)) AS clear_bank,
			      CAST(NULL AS VARCHAR(128)) AS bank_name,
			      FALSE AS reconciled,
			      CAST(NULL AS VARCHAR(512)) AS budget_tracking,
			      f.name AS associated_fund_name
			    FROM txn t
			    LEFT JOIN legacy_txn_map m ON m.canonical_txn_id = t.id
			    LEFT JOIN counterparty cp ON cp.id = t.payee_id
			    LEFT JOIN (
			      SELECT ts.txn_id, MIN(ts.fund_id) AS fund_id
			      FROM txn_split ts
			      GROUP BY ts.txn_id
			    ) tf ON tf.txn_id = t.id
			    LEFT JOIN fund f ON f.id = tf.fund_id
			""");
		st.execute("""
			    CREATE OR REPLACE VIEW v_journal_entry AS
			    SELECT
			      ts.id AS id,
			      COALESCE(m.legacy_txn_id, ts.txn_id) AS txn_id,
			      ABS(ts.amount_signed) AS amount,
			      a.account_number AS account_number,
			      CASE WHEN ts.amount_signed < 0 THEN 'CREDIT' ELSE 'DEBIT' END AS account_side,
			      a.name AS account_name,
			      f.code AS fund_number
			    FROM txn_split ts
			    JOIN txn t ON t.id = ts.txn_id
			    JOIN account a ON a.id = ts.account_id
			    JOIN fund f ON f.id = ts.fund_id
			    LEFT JOIN legacy_txn_map m ON m.canonical_txn_id = t.id
			""");
	}
	
	private void ensurePeopleAndCounterparty(Statement st) throws SQLException
	{
		st.execute("""
			    CREATE TABLE IF NOT EXISTS donor(
			      id BIGINT AUTO_INCREMENT PRIMARY KEY,
			      external_id VARCHAR(64) UNIQUE,
			      name VARCHAR(255),
			      email VARCHAR(255),
			      phone VARCHAR(64)
			    )
			""");
		st.execute("ALTER TABLE donor ADD COLUMN IF NOT EXISTS external_id VARCHAR(64);");
		st.execute("ALTER TABLE donor ADD COLUMN IF NOT EXISTS email VARCHAR(255);");
		st.execute("ALTER TABLE donor ADD COLUMN IF NOT EXISTS phone VARCHAR(64);");
		st.execute("ALTER TABLE donor ADD COLUMN IF NOT EXISTS name VARCHAR(255);");
		st.execute("CREATE UNIQUE INDEX IF NOT EXISTS donor_external_id_idx ON donor(external_id);");
		st.execute(
			"UPDATE donor SET external_id = name WHERE external_id IS NULL AND name IS NOT NULL;");
		st.execute("""
			    CREATE TABLE IF NOT EXISTS person(
			      id BIGINT AUTO_INCREMENT PRIMARY KEY,
			      name VARCHAR(255) NOT NULL,
			      email VARCHAR(255),
			      phone VARCHAR(64),
			      type VARCHAR(32) NOT NULL DEFAULT 'DONOR'
			    )
			""");
		st.execute("ALTER TABLE person ADD COLUMN IF NOT EXISTS email VARCHAR(255);");
		st.execute("ALTER TABLE person ADD COLUMN IF NOT EXISTS phone VARCHAR(64);");
		st.execute("ALTER TABLE person ADD COLUMN IF NOT EXISTS type VARCHAR(32) DEFAULT 'DONOR';");
		st.execute("UPDATE person SET type = 'DONOR' WHERE type IS NULL OR TRIM(type) = '';");
		st.execute("CREATE INDEX IF NOT EXISTS person_name_idx ON person(name)");
		st.execute("""
			    ALTER TABLE txn_supplemental_line
			    ADD CONSTRAINT IF NOT EXISTS fk_txn_supplemental_person
			    FOREIGN KEY (counterparty_person_id) REFERENCES person(id) ON DELETE SET NULL
			""");
		st.execute(
			"CREATE INDEX IF NOT EXISTS txn_supplemental_person_idx ON txn_supplemental_line(counterparty_person_id)");
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
			    CREATE TABLE IF NOT EXISTS bank_id_record(
			      bank_id_record_id VARCHAR(255) PRIMARY KEY,
			      bank_id VARCHAR(255) NOT NULL,
			      bank_name VARCHAR(255),
			      account_id VARCHAR(255),
			      account_type VARCHAR(64),
			      created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
			      updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
			      UNIQUE(bank_id, account_id)
			    )
			""");
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
		st.execute("""
			    CREATE TABLE IF NOT EXISTS banking_transaction_record(
			      banking_record_id VARCHAR(255) PRIMARY KEY,
			      bank_id_record_id VARCHAR(255),
			      journal_txn_id INT,
			      fund_id BIGINT,
			      transaction_date DATE,
			      transaction_id VARCHAR(255),
			      check_info VARCHAR(255),
			      transaction_type VARCHAR(64),
			      cleared_state VARCHAR(64),
			      amount DECIMAL(19,2),
			      memo VARCHAR(500),
			      created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
			      updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
			    )
			""");
		st.execute("""
			    CREATE TABLE IF NOT EXISTS asset_record_detail(
			      asset_record_id VARCHAR(255) PRIMARY KEY,
			      asset_type VARCHAR(128),
			      depreciation_method VARCHAR(64),
			      details CLOB,
			      date_acquired DATE,
			      date_sold DATE,
			      journal_txn_id INT,
			      created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
			      updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
			    )
			""");
		st.execute("""
			    CREATE TABLE IF NOT EXISTS inventory_asset_link(
			      inventory_item_id VARCHAR(255) NOT NULL,
			      asset_record_id VARCHAR(255) NOT NULL,
			      journal_txn_id INT,
			      created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
			      updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
			      PRIMARY KEY(inventory_item_id, asset_record_id)
			    )
			""");
		st.execute("""
			    CREATE TABLE IF NOT EXISTS depreciation_run(
			      depreciation_run_id VARCHAR(255) PRIMARY KEY,
			      run_date DATE NOT NULL,
			      notes VARCHAR(500),
			      created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
			    )
			""");
		st.execute("""
			    CREATE TABLE IF NOT EXISTS depreciation_record(
			      depreciation_record_id VARCHAR(255) PRIMARY KEY,
			      depreciation_run_id VARCHAR(255),
			      asset_record_id VARCHAR(255),
			      net_depreciation DECIMAL(19,2),
			      depreciation_date DATE,
			      depreciation_percentage DECIMAL(9,4),
			      amortization_schedule VARCHAR(64),
			      created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
			      updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
			    )
			""");
		st.execute("""
			    CREATE TABLE IF NOT EXISTS grant_record(
			      grant_record_id VARCHAR(255) PRIMARY KEY,
			      grant_id VARCHAR(255),
			      grantor VARCHAR(255),
			      amount DECIMAL(19,2),
			      date_awarded_text VARCHAR(64),
			      purpose VARCHAR(500),
			      status VARCHAR(64),
			      donor_id BIGINT,
			      person_id BIGINT,
			      fund_id BIGINT,
			      journal_txn_id INT,
			      details CLOB,
			      created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
			      updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
			    )
			""");
		st.execute("""
			    CREATE TABLE IF NOT EXISTS donation_record(
			      donation_id VARCHAR(255) PRIMARY KEY,
			      donor_external_id VARCHAR(64),
			      donation_date DATE,
			      amount DECIMAL(19,2) NOT NULL,
			      memo VARCHAR(500),
			      cash_account_number VARCHAR(64) NOT NULL,
			      revenue_account_number VARCHAR(64) NOT NULL,
			      fund_number VARCHAR(64),
			      journal_txn_id INT,
			      created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
			      updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
			    )
			""");
		st.execute("""
			    CREATE TABLE IF NOT EXISTS donation_journal_link(
			      donation_id VARCHAR(255) NOT NULL,
			      journal_txn_id INT NOT NULL,
			      link_role VARCHAR(16) DEFAULT 'ORIGINAL' NOT NULL,
			      created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
			      updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
			      PRIMARY KEY (donation_id, journal_txn_id, link_role)
			    )
			""");
		st.execute("""
			    CREATE TABLE IF NOT EXISTS operational_link_backfill_queue(
			      id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
			      module_name VARCHAR(64) NOT NULL,
			      domain_id VARCHAR(255) NOT NULL,
			      issue_code VARCHAR(64) NOT NULL,
			      issue_detail VARCHAR(1000),
			      created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
			      resolved_at TIMESTAMP,
			      resolution_note VARCHAR(1000),
			      UNIQUE(module_name, domain_id, issue_code)
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

	private void runBankingReconciliationSchemaMigration(Connection c)
		throws SQLException
	{
		if (isMigrationApplied(c, MIGRATION_BANKING_RECON_SCHEMA_V1))
		{
			return;
		}
		try (Statement st = c.createStatement())
		{
			st.execute(
				"ALTER TABLE bank_statement ADD COLUMN IF NOT EXISTS bank_id_record_id VARCHAR(255);");
			st.execute(
				"ALTER TABLE bank_statement ADD COLUMN IF NOT EXISTS period_start DATE;");
			st.execute(
				"ALTER TABLE bank_statement ADD COLUMN IF NOT EXISTS period_end DATE;");
			st.execute(
				"ALTER TABLE bank_statement ADD COLUMN IF NOT EXISTS status VARCHAR(24) DEFAULT 'OPEN' NOT NULL;");
			st.execute(
				"ALTER TABLE bank_statement ADD COLUMN IF NOT EXISTS imported_at TIMESTAMP;");
			st.execute(
				"ALTER TABLE bank_statement ADD COLUMN IF NOT EXISTS closed_at TIMESTAMP;");
			st.execute(
				"ALTER TABLE bank_statement ADD COLUMN IF NOT EXISTS retention_until DATE;");
			st.execute(
				"ALTER TABLE bank_statement ADD CONSTRAINT IF NOT EXISTS ck_bank_statement_status CHECK (status IN ('OPEN','IN_REVIEW','CLOSED','LOCKED'));");
			st.execute(
				"ALTER TABLE bank_statement ADD CONSTRAINT IF NOT EXISTS fk_bank_statement_bank_id FOREIGN KEY (bank_id_record_id) REFERENCES bank_id_record(bank_id_record_id) ON DELETE SET NULL;");
			st.execute(
				"CREATE INDEX IF NOT EXISTS idx_bank_statement_bank_period ON bank_statement(bank_id_record_id, period_start, period_end);");

			st.execute(
				"ALTER TABLE banking_transaction_record ADD COLUMN IF NOT EXISTS statement_id BIGINT;");
			st.execute(
				"ALTER TABLE banking_transaction_record ADD COLUMN IF NOT EXISTS import_batch_id VARCHAR(128);");
			st.execute(
				"ALTER TABLE banking_transaction_record ADD COLUMN IF NOT EXISTS source_system VARCHAR(64);");
			st.execute(
				"ALTER TABLE banking_transaction_record ADD COLUMN IF NOT EXISTS source_fingerprint VARCHAR(128);");
			st.execute(
				"ALTER TABLE banking_transaction_record ADD COLUMN IF NOT EXISTS normalized_description VARCHAR(512);");
			st.execute(
				"ALTER TABLE banking_transaction_record ADD COLUMN IF NOT EXISTS external_transaction_id VARCHAR(255);");
			st.execute(
				"ALTER TABLE banking_transaction_record ADD COLUMN IF NOT EXISTS match_status VARCHAR(24) DEFAULT 'NEW' NOT NULL;");
			st.execute(
				"ALTER TABLE banking_transaction_record ADD COLUMN IF NOT EXISTS matched_at TIMESTAMP;");
			st.execute(
				"ALTER TABLE banking_transaction_record ADD COLUMN IF NOT EXISTS duplicate_seen_count INT DEFAULT 0 NOT NULL;");
			st.execute(
				"ALTER TABLE banking_transaction_record ADD COLUMN IF NOT EXISTS last_seen_batch_id VARCHAR(128);");
			st.execute(
				"ALTER TABLE banking_transaction_record ADD COLUMN IF NOT EXISTS anomaly_duplicate BOOLEAN DEFAULT FALSE NOT NULL;");
			st.execute(
				"ALTER TABLE banking_transaction_record ADD COLUMN IF NOT EXISTS anomaly_amount_outlier BOOLEAN DEFAULT FALSE NOT NULL;");
			st.execute(
				"ALTER TABLE banking_transaction_record ADD COLUMN IF NOT EXISTS anomaly_date_outlier BOOLEAN DEFAULT FALSE NOT NULL;");
			st.execute(
				"ALTER TABLE banking_transaction_record ADD COLUMN IF NOT EXISTS anomaly_reason VARCHAR(512);");
			st.execute(
				"ALTER TABLE banking_transaction_record ADD COLUMN IF NOT EXISTS supersedes_banking_record_id VARCHAR(255);");
			st.execute(
				"UPDATE banking_transaction_record SET match_status = 'UNMATCHED' WHERE match_status IS NULL;");
			st.execute(
				"ALTER TABLE banking_transaction_record ADD CONSTRAINT IF NOT EXISTS ck_banking_match_status CHECK (match_status IN ('NEW','UNMATCHED','AUTO_MATCHED','MATCH_CONFIRMED','RECONCILED','DUPLICATE','ADJUSTED','STALE_UNMATCHED'));");
			st.execute(
				"ALTER TABLE banking_transaction_record ADD CONSTRAINT IF NOT EXISTS fk_banking_statement FOREIGN KEY (statement_id) REFERENCES bank_statement(id) ON DELETE SET NULL;");
			st.execute(
				"ALTER TABLE banking_transaction_record ADD CONSTRAINT IF NOT EXISTS fk_banking_supersedes FOREIGN KEY (supersedes_banking_record_id) REFERENCES banking_transaction_record(banking_record_id) ON DELETE SET NULL;");
			st.execute(
				"CREATE UNIQUE INDEX IF NOT EXISTS uq_banking_idempotent_fingerprint ON banking_transaction_record(bank_id_record_id, source_fingerprint);");
			st.execute(
				"CREATE INDEX IF NOT EXISTS idx_banking_open_items ON banking_transaction_record(bank_id_record_id, match_status, transaction_date);");
			st.execute(
				"CREATE INDEX IF NOT EXISTS idx_banking_anomaly_queue ON banking_transaction_record(bank_id_record_id, anomaly_duplicate, anomaly_amount_outlier, anomaly_date_outlier);");

			st.execute(
				"ALTER TABLE ledger_record ADD COLUMN IF NOT EXISTS banking_record_id VARCHAR(255);");
			st.execute(
				"ALTER TABLE ledger_record ADD COLUMN IF NOT EXISTS match_group_id VARCHAR(64);");
			st.execute(
				"ALTER TABLE ledger_record ADD COLUMN IF NOT EXISTS match_method VARCHAR(24);");
			st.execute(
				"ALTER TABLE ledger_record ADD COLUMN IF NOT EXISTS reviewer_user VARCHAR(128);");
			st.execute(
				"ALTER TABLE ledger_record ADD COLUMN IF NOT EXISTS reviewed_at TIMESTAMP;");
			st.execute(
				"ALTER TABLE ledger_record ADD COLUMN IF NOT EXISTS link_status VARCHAR(24) DEFAULT 'ACTIVE' NOT NULL;");
			st.execute(
				"ALTER TABLE ledger_record ADD CONSTRAINT IF NOT EXISTS ck_ledger_match_method CHECK (match_method IN ('AUTO','MANUAL','RULE','IMPORT_REPLAY') OR match_method IS NULL);");
			st.execute(
				"ALTER TABLE ledger_record ADD CONSTRAINT IF NOT EXISTS ck_ledger_link_status CHECK (link_status IN ('ACTIVE','VOIDED','SUPERSEDED'));");
			st.execute(
				"ALTER TABLE ledger_record ADD CONSTRAINT IF NOT EXISTS fk_ledger_banking_record FOREIGN KEY (banking_record_id) REFERENCES banking_transaction_record(banking_record_id) ON DELETE CASCADE;");
			st.execute(
				"CREATE INDEX IF NOT EXISTS idx_ledger_match_group ON ledger_record(match_group_id);");
			st.execute(
				"CREATE INDEX IF NOT EXISTS idx_ledger_banking_active ON ledger_record(banking_record_id, link_status);");
		}
		markMigrationApplied(c, MIGRATION_BANKING_RECON_SCHEMA_V1);
	}

	private void runReportingScheduleConfigurationMigration(Connection c)
		throws SQLException
	{
		if (isMigrationApplied(c, MIGRATION_REPORTING_SCHEDULE_CONFIG_V1))
		{
			return;
		}
		try (Statement st = c.createStatement())
		{
			st.execute("""
				    CREATE TABLE IF NOT EXISTS config_release(
				      id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
				      release_code VARCHAR(64) NOT NULL,
				      status VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
				      effective_from DATE NOT NULL,
				      effective_to DATE,
				      created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
				      created_by VARCHAR(80) NOT NULL DEFAULT 'migration',
				      approved_at TIMESTAMP,
				      approved_by VARCHAR(80),
				      notes VARCHAR(1000),
				      CONSTRAINT uq_config_release_code UNIQUE (release_code),
				      CONSTRAINT chk_config_release_status CHECK (status IN ('DRAFT','APPROVED','ACTIVE','RETIRED')),
				      CONSTRAINT chk_config_release_dates CHECK (effective_to IS NULL OR effective_to >= effective_from)
				    )
				""");

			st.execute("""
				    CREATE TABLE IF NOT EXISTS statement_section(
				      id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
				      config_release_id BIGINT NOT NULL,
				      report_type VARCHAR(40) NOT NULL,
				      section_code VARCHAR(80) NOT NULL,
				      section_name VARCHAR(200) NOT NULL,
				      sort_order INT NOT NULL DEFAULT 0,
				      is_active BOOLEAN NOT NULL DEFAULT TRUE,
				      created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
				      created_by VARCHAR(80) NOT NULL DEFAULT 'migration',
				      CONSTRAINT fk_statement_section_release
				        FOREIGN KEY (config_release_id) REFERENCES config_release(id) ON DELETE CASCADE,
				      CONSTRAINT chk_statement_section_report_type
				        CHECK (report_type IN ('INCOME_STATEMENT','BALANCE_SHEET','SCHEDULE','CUSTOM')),
				      CONSTRAINT chk_statement_section_code_not_blank CHECK (LENGTH(TRIM(section_code)) > 0),
				      CONSTRAINT chk_statement_section_name_not_blank CHECK (LENGTH(TRIM(section_name)) > 0),
				      CONSTRAINT uq_statement_section_release_code UNIQUE (config_release_id, report_type, section_code)
				    )
				""");

			st.execute("""
				    CREATE TABLE IF NOT EXISTS account_statement_mapping(
				      id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
				      config_release_id BIGINT NOT NULL,
				      account_id BIGINT NOT NULL,
				      statement_section_id BIGINT NOT NULL,
				      sign_policy VARCHAR(20) NOT NULL,
				      sort_order INT NOT NULL DEFAULT 0,
				      valid_from DATE NOT NULL,
				      valid_to DATE,
				      source VARCHAR(80) NOT NULL DEFAULT 'LEGACY_MIGRATION',
				      created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
				      created_by VARCHAR(80) NOT NULL DEFAULT 'migration',
				      change_reason VARCHAR(1000),
				      CONSTRAINT fk_asm_release FOREIGN KEY (config_release_id) REFERENCES config_release(id) ON DELETE CASCADE,
				      CONSTRAINT fk_asm_account FOREIGN KEY (account_id) REFERENCES account(id),
				      CONSTRAINT fk_asm_section FOREIGN KEY (statement_section_id) REFERENCES statement_section(id),
				      CONSTRAINT chk_asm_sign_policy CHECK (sign_policy IN ('NORMAL','INVERT')),
				      CONSTRAINT chk_asm_dates CHECK (valid_to IS NULL OR valid_to >= valid_from),
				      CONSTRAINT uq_asm_open_row UNIQUE (config_release_id, account_id, statement_section_id, valid_from)
				    )
				""");

			st.execute("""
				    CREATE TABLE IF NOT EXISTS schedule_requirement_rule(
				      id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
				      config_release_id BIGINT NOT NULL,
				      subject_kind VARCHAR(20) NOT NULL,
				      account_id BIGINT,
				      subtype VARCHAR(40),
				      schedule_kind_id BIGINT NOT NULL,
				      requirement_level VARCHAR(20) NOT NULL,
				      valid_from DATE NOT NULL,
				      valid_to DATE,
				      precedence SMALLINT NOT NULL DEFAULT 100,
				      source VARCHAR(80) NOT NULL DEFAULT 'LEGACY_MIGRATION',
				      rationale VARCHAR(1000),
				      created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
				      created_by VARCHAR(80) NOT NULL DEFAULT 'migration',
				      CONSTRAINT fk_srr_release FOREIGN KEY (config_release_id) REFERENCES config_release(id) ON DELETE CASCADE,
				      CONSTRAINT fk_srr_account FOREIGN KEY (account_id) REFERENCES account(id),
				      CONSTRAINT fk_srr_schedule_kind FOREIGN KEY (schedule_kind_id) REFERENCES schedule_kind(id),
				      CONSTRAINT chk_srr_subject_kind CHECK (subject_kind IN ('ACCOUNT','SUBTYPE')),
				      CONSTRAINT chk_srr_subject_ref CHECK (
				        (subject_kind = 'ACCOUNT' AND account_id IS NOT NULL AND subtype IS NULL)
				        OR
				        (subject_kind = 'SUBTYPE' AND account_id IS NULL AND subtype IS NOT NULL)
				      ),
				      CONSTRAINT chk_srr_req_level CHECK (requirement_level IN ('REQUIRED','OPTIONAL','EXCLUDED')),
				      CONSTRAINT chk_srr_dates CHECK (valid_to IS NULL OR valid_to >= valid_from)
				    )
				""");

			st.execute("""
				    CREATE TABLE IF NOT EXISTS config_change_event(
				      id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
				      config_release_id BIGINT NOT NULL,
				      entity_name VARCHAR(80) NOT NULL,
				      entity_pk VARCHAR(120) NOT NULL,
				      operation VARCHAR(20) NOT NULL,
				      changed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
				      changed_by VARCHAR(80) NOT NULL DEFAULT 'system',
				      reason VARCHAR(1000),
				      old_value_json CLOB,
				      new_value_json CLOB,
				      CONSTRAINT fk_cce_release FOREIGN KEY (config_release_id) REFERENCES config_release(id) ON DELETE CASCADE,
				      CONSTRAINT chk_cce_operation CHECK (operation IN ('INSERT','UPDATE','DELETE'))
				    )
				""");

			st.execute(
				"CREATE INDEX IF NOT EXISTS ix_statement_section_release_type ON statement_section(config_release_id, report_type, sort_order, id);");
			st.execute(
				"CREATE INDEX IF NOT EXISTS ix_asm_account_open ON account_statement_mapping(account_id, valid_to, config_release_id);");
			st.execute(
				"CREATE INDEX IF NOT EXISTS ix_asm_release_account_validity ON account_statement_mapping(config_release_id, account_id, valid_to);");
			st.execute(
				"CREATE INDEX IF NOT EXISTS ix_srr_subject_lookup ON schedule_requirement_rule(subject_kind, account_id, subtype, valid_to, precedence);");
			st.execute(
				"CREATE INDEX IF NOT EXISTS ix_srr_release_account_schedule_validity ON schedule_requirement_rule(config_release_id, subject_kind, account_id, schedule_kind_id, valid_to);");
			st.execute(
				"CREATE INDEX IF NOT EXISTS ix_srr_release_subtype_schedule_validity ON schedule_requirement_rule(config_release_id, subject_kind, subtype, schedule_kind_id, valid_to);");

			st.execute("""
				    MERGE INTO config_release (release_code, status, effective_from, notes)
				    KEY (release_code)
				    VALUES ('LEGACY_BASELINE_V1', 'ACTIVE', CURRENT_DATE, 'Backfilled baseline from legacy reporting/schedule tables.')
				""");
			st.execute("""
				    INSERT INTO statement_section (config_release_id, report_type, section_code, section_name, sort_order, created_by)
				    SELECT cr.id,
				           rs.report_type,
				           UPPER(REPLACE(TRIM(rs.name), ' ', '_')),
				           rs.name,
				           rs.sort_order,
				           'migration'
				    FROM report_section rs
				    JOIN config_release cr ON cr.release_code = 'LEGACY_BASELINE_V1'
				    WHERE NOT EXISTS (
				      SELECT 1
				      FROM statement_section ss
				      WHERE ss.config_release_id = cr.id
				        AND ss.report_type = rs.report_type
				        AND ss.section_code = UPPER(REPLACE(TRIM(rs.name), ' ', '_'))
				    )
				""");
			st.execute("""
				    INSERT INTO account_statement_mapping (
				      config_release_id,
				      account_id,
				      statement_section_id,
				      sign_policy,
				      sort_order,
				      valid_from,
				      valid_to,
				      source,
				      created_by,
				      change_reason
				    )
				    SELECT cr.id,
				           ars.account_id,
				           ss.id,
				           ars.sign_policy,
				           ars.sort_order,
				           CURRENT_DATE,
				           NULL,
				           'LEGACY_MIGRATION',
				           'migration',
				           'Baseline backfill from account_report_section'
				    FROM account_report_section ars
				    JOIN report_section rs ON rs.id = ars.report_section_id
				    JOIN config_release cr ON cr.release_code = 'LEGACY_BASELINE_V1'
				    JOIN statement_section ss
				      ON ss.config_release_id = cr.id
				     AND ss.report_type = rs.report_type
				     AND ss.section_code = UPPER(REPLACE(TRIM(rs.name), ' ', '_'))
				    WHERE NOT EXISTS (
				      SELECT 1
				      FROM account_statement_mapping asm
				      WHERE asm.config_release_id = cr.id
				        AND asm.account_id = ars.account_id
				        AND asm.statement_section_id = ss.id
				        AND asm.valid_to IS NULL
				    )
				""");
			st.execute("""
				    INSERT INTO schedule_requirement_rule (
				      config_release_id,
				      subject_kind,
				      account_id,
				      subtype,
				      schedule_kind_id,
				      requirement_level,
				      valid_from,
				      valid_to,
				      precedence,
				      source,
				      rationale,
				      created_by
				    )
				    SELECT cr.id,
				           'ACCOUNT',
				           asr.account_id,
				           NULL,
				           asr.schedule_kind_id,
				           CASE WHEN asr.is_required THEN 'REQUIRED' ELSE 'OPTIONAL' END,
				           CURRENT_DATE,
				           NULL,
				           10,
				           'LEGACY_MIGRATION',
				           COALESCE(asr.notes, 'Baseline backfill from account_schedule_requirement'),
				           'migration'
				    FROM account_schedule_requirement asr
				    JOIN config_release cr ON cr.release_code = 'LEGACY_BASELINE_V1'
				    WHERE NOT EXISTS (
				      SELECT 1
				      FROM schedule_requirement_rule srr
				      WHERE srr.config_release_id = cr.id
				        AND srr.subject_kind = 'ACCOUNT'
				        AND srr.account_id = asr.account_id
				        AND srr.schedule_kind_id = asr.schedule_kind_id
				        AND srr.valid_to IS NULL
				    )
				""");
			st.execute("""
				    INSERT INTO schedule_requirement_rule (
				      config_release_id,
				      subject_kind,
				      account_id,
				      subtype,
				      schedule_kind_id,
				      requirement_level,
				      valid_from,
				      valid_to,
				      precedence,
				      source,
				      rationale,
				      created_by
				    )
				    SELECT cr.id,
				           'SUBTYPE',
				           NULL,
				           assd.subtype,
				           assd.schedule_kind_id,
				           'REQUIRED',
				           CURRENT_DATE,
				           NULL,
				           100,
				           'LEGACY_MIGRATION',
				           'Baseline backfill from account_subtype_schedule_default',
				           'migration'
				    FROM account_subtype_schedule_default assd
				    JOIN config_release cr ON cr.release_code = 'LEGACY_BASELINE_V1'
				    WHERE NOT EXISTS (
				      SELECT 1
				      FROM schedule_requirement_rule srr
				      WHERE srr.config_release_id = cr.id
				        AND srr.subject_kind = 'SUBTYPE'
				        AND srr.subtype = assd.subtype
				        AND srr.schedule_kind_id = assd.schedule_kind_id
				        AND srr.valid_to IS NULL
				    )
				""");
		}
		markMigrationApplied(c, MIGRATION_REPORTING_SCHEDULE_CONFIG_V1);
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
