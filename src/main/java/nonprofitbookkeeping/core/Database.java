
package nonprofitbookkeeping.core;

import java.nio.file.Path;
import java.sql.*;

public final class Database {
    private static Database INSTANCE;
    private final String url;
    private final String user = "sa";
    private final String pass = "";

    private Database(Path dbFile) {
        this.url = "jdbc:h2:file:" + dbFile.toAbsolutePath().toString()
                 + ";AUTO_SERVER=TRUE;MODE=MySQL";
    }

    public static synchronized void init(Path dbFile) {
        if (dbFile == null) throw new IllegalArgumentException("dbFile required");
        INSTANCE = new Database(dbFile);
    }

    public static synchronized boolean isInitialized() {
        return INSTANCE != null;
    }

    public static Database get() {
        if (INSTANCE == null) throw new IllegalStateException("Database not initialized");
        return INSTANCE;
    }
    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(this.url, this.user, this.pass);
    }

    public void ensureSchema() throws SQLException {
        try (Connection c = getConnection(); Statement st = c.createStatement()) {
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
            st.execute("ALTER TABLE company_profile ADD COLUMN IF NOT EXISTS legal_structure VARCHAR(128);");
            st.execute("ALTER TABLE company_profile ADD COLUMN IF NOT EXISTS tax_id VARCHAR(128);");
            st.execute("ALTER TABLE company_profile ADD COLUMN IF NOT EXISTS company_file_dir VARCHAR(512);");
            st.execute("ALTER TABLE company_profile ADD COLUMN IF NOT EXISTS company_file_name VARCHAR(255);");

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
            st.execute("ALTER TABLE account ADD COLUMN IF NOT EXISTS supplemental_kinds VARCHAR(255);");
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
                  budget_tracking VARCHAR(512),
                  associated_fund_name VARCHAR(128)
                )
            """);
            st.execute("ALTER TABLE journal_transaction ALTER COLUMN budget_tracking VARCHAR(512);");
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
            st.execute("CREATE INDEX IF NOT EXISTS txn_supplemental_txn_idx ON txn_supplemental_line(txn_id)");
            st.execute("CREATE INDEX IF NOT EXISTS txn_supplemental_entry_idx ON txn_supplemental_line(entry_id)");
            st.execute("CREATE INDEX IF NOT EXISTS txn_supplemental_kind_idx ON txn_supplemental_line(line_kind)");

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
            st.execute("UPDATE donor SET external_id = name WHERE external_id IS NULL AND name IS NOT NULL;");

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
            st.execute("CREATE INDEX IF NOT EXISTS txn_supplemental_person_idx ON txn_supplemental_line(counterparty_person_id)");

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
            st.execute("CREATE INDEX IF NOT EXISTS undeposited_funds_item_idx ON undeposited_funds_item(id)");

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
    }
}
