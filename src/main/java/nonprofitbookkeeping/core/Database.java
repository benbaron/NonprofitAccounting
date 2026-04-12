
package nonprofitbookkeeping.core;

import java.nio.file.Path;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

// TODO: Auto-generated Javadoc
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

	private static final String SQL_COUNTERPARTY_FROM_PERSON =
		"INSERT INTO counterparty(display_name, kind, email, phone, is_active) " +
		"SELECT p.name, 'PERSON', p.email, p.phone, TRUE FROM person p " +
		"WHERE NOT EXISTS (SELECT 1 FROM counterparty c WHERE c.display_name = p.name AND c.kind = 'PERSON')";

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
			ensurePeopleAndCounterparty(st);
			runReconciledDataBackfill(c);
			ensureRemainingLegacyTables(st);
			ensureOperationalLinkageTables(st);
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
		st.execute("""
			    CREATE TABLE IF NOT EXISTS fund_alias(
			      id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
			      fund_id BIGINT NOT NULL,
			      alias_text VARCHAR(400) NOT NULL,
			      source VARCHAR(80),
			      is_active BOOLEAN DEFAULT TRUE NOT NULL
			    )
			""");
		st.execute("""
			    CREATE TABLE IF NOT EXISTS account_report_section(
			      id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
			      account_id BIGINT NOT NULL,
			      report_section_id BIGINT NOT NULL,
			      sort_order INT NOT NULL,
			      sign_policy VARCHAR(20) NOT NULL
			    )
			""");
		st.execute("""
			    CREATE TABLE IF NOT EXISTS account_schedule_requirement(
			      id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
			      account_id BIGINT NOT NULL,
			      schedule_kind_id BIGINT NOT NULL,
			      is_required BOOLEAN NOT NULL,
			      notes VARCHAR(500)
			    )
			""");
		st.execute("""
			    CREATE TABLE IF NOT EXISTS account_subtype_schedule_default(
			      id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
			      subtype VARCHAR(40) NOT NULL,
			      schedule_kind_id BIGINT NOT NULL
			    )
			""");
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
			    ALTER TABLE account
			    ADD CONSTRAINT IF NOT EXISTS fk_account_chart
			    FOREIGN KEY (chart_id) REFERENCES chart_of_accounts(id) ON DELETE SET NULL
			""");
		st.execute("""
			    ALTER TABLE account
			    ADD CONSTRAINT IF NOT EXISTS fk_account_parent
			    FOREIGN KEY (parent_id) REFERENCES account(id) ON DELETE SET NULL
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
			      phone VARCHAR(64)
			    )
			""");
		st.execute("ALTER TABLE person ADD COLUMN IF NOT EXISTS email VARCHAR(255);");
		st.execute("ALTER TABLE person ADD COLUMN IF NOT EXISTS phone VARCHAR(64);");
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
		st.execute(
			"CREATE INDEX IF NOT EXISTS grant_record_grant_id_idx ON grant_record(grant_id);");
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
