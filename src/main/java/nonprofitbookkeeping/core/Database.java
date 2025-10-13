
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
        return DriverManager.getConnection(url, user, pass);
    }

    public void ensureSchema() throws SQLException {
        try (Connection c = getConnection(); Statement st = c.createStatement()) {
            st.execute("""
                CREATE TABLE IF NOT EXISTS company_profile(
                  id INT PRIMARY KEY CHECK(id=1),
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
                  enable_multi_currency BOOLEAN
                )
            """);
            st.execute("MERGE INTO company_profile (id) KEY(id) VALUES (1)");

            st.execute("""
                CREATE TABLE IF NOT EXISTS account(
                  account_number VARCHAR(64) PRIMARY KEY,
                  name VARCHAR(255),
                  account_code VARCHAR(64),
                  account_type VARCHAR(64),
                  increase_side VARCHAR(16),
                  parent_account_id VARCHAR(64),
                  currency VARCHAR(8),
                  opening_balance DECIMAL(18,2) DEFAULT 0
                )
            """);
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
                  budget_tracking VARCHAR(64),
                  associated_fund_name VARCHAR(128)
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
                CREATE TABLE IF NOT EXISTS donor(
                  id BIGINT AUTO_INCREMENT PRIMARY KEY,
                  name VARCHAR(255) UNIQUE
                )
            """);

            st.execute("""
                CREATE TABLE IF NOT EXISTS json_storage(
                  storage_key VARCHAR(128) PRIMARY KEY,
                  payload CLOB
                )
            """);
        }
    }
}
