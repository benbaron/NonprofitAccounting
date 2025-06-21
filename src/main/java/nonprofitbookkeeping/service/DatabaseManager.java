package nonprofitbookkeeping.service;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Utility class providing access to the embedded H2 database. The database file
 * is stored in the user's <code>.m2</code> directory by default.
 */
public final class DatabaseManager {
    private static final String DB_PATH = System.getProperty("user.home")
            + File.separator + ".m2" + File.separator + "nonprofitbookkeeping";
    private static final String JDBC_URL = "jdbc:h2:" + DB_PATH;
    private static final String USER = "sa";
    private static final String PASS = "";
    private static boolean initialized = false;

    private DatabaseManager() {}

    /**
     * Obtains a new connection to the embedded database. The first call will
     * also ensure all required tables exist.
     */
    public static Connection getConnection() throws SQLException {
        initializeDatabase();
        return DriverManager.getConnection(JDBC_URL, USER, PASS);
    }

    /**
     * Creates database tables if they are missing. This method is safe to call
     * multiple times and only performs initialization once.
     */
    private static synchronized void initializeDatabase() throws SQLException {
        if (initialized) {
            return;
        }
        try (Connection conn = DriverManager.getConnection(JDBC_URL, USER, PASS);
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS account(" +
                    "account_number VARCHAR(64) PRIMARY KEY," +
                    "name VARCHAR(255)," +
                    "account_code VARCHAR(64)," +
                    "account_type VARCHAR(64)," +
                    "increase_side VARCHAR(16)," +
                    "currency VARCHAR(32)," +
                    "opening_balance DECIMAL(15,2))");

            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS donor(" +
                    "donor_id VARCHAR(64) PRIMARY KEY," +
                    "name VARCHAR(255)," +
                    "total_donations DECIMAL(15,2)," +
                    "last_donation_date DATE," +
                    "donation_amount DECIMAL(15,2)," +
                    "donation_type VARCHAR(64)," +
                    "donation_date DATE)");

            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS inventory_item(" +
                    "item_id VARCHAR(64) PRIMARY KEY," +
                    "name VARCHAR(255)," +
                    "acquired DATE," +
                    "cost DECIMAL(15,2)," +
                    "accum_depreciation DECIMAL(15,2)," +
                    "net_value DECIMAL(15,2)," +
                    "life_years INTEGER," +
                    "depreciation_rate DECIMAL(8,4)," +
                    "depreciation_method VARCHAR(64))");

            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS grant(" +
                    "grant_id VARCHAR(64) PRIMARY KEY," +
                    "grantor VARCHAR(255)," +
                    "amount DECIMAL(15,2)," +
                    "date_awarded DATE," +
                    "purpose VARCHAR(255)," +
                    "status VARCHAR(64))");

            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS fund(" +
                    "fund_id VARCHAR(64) PRIMARY KEY," +
                    "name VARCHAR(255)," +
                    "balance DECIMAL(15,2))");

            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS account_fund(" +
                    "account_id VARCHAR(64)," +
                    "fund_id VARCHAR(64)," +
                    "PRIMARY KEY(account_id,fund_id))");

            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS transaction(" +
                    "transaction_id INTEGER AUTO_INCREMENT PRIMARY KEY," +
                    "booking_timestamp BIGINT," +
                    "date VARCHAR(16)," +
                    "memo VARCHAR(255))");

            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS entry(" +
                    "entry_id INTEGER AUTO_INCREMENT PRIMARY KEY," +
                    "transaction_id INTEGER," +
                    "account_id VARCHAR(64)," +
                    "amount DECIMAL(15,2)," +
                    "account_side VARCHAR(8)," +
                    "account_name VARCHAR(255))");

            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS customer(" +
                    "id VARCHAR(64) PRIMARY KEY," +
                    "name VARCHAR(255))");

            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS budget(" +
                    "budget_id VARCHAR(64) PRIMARY KEY," +
                    "budget_name VARCHAR(255)," +
                    "fiscal_year INTEGER," +
                    "description VARCHAR(255)," +
                    "currency VARCHAR(8)," +
                    "applicable_fund_id VARCHAR(64))");

            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS budget_line(" +
                    "line_id INTEGER AUTO_INCREMENT PRIMARY KEY," +
                    "budget_id VARCHAR(64)," +
                    "account_id VARCHAR(64)," +
                    "account_name VARCHAR(255)," +
                    "total_amount DECIMAL(15,2)," +
                    "periodicity VARCHAR(32)," +
                    "fund_id VARCHAR(64))");

            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS report_configuration(" +
                    "configuration_id VARCHAR(64) PRIMARY KEY," +
                    "user_given_name VARCHAR(255)," +
                    "report_type VARCHAR(64)," +
                    "date_selection_mode VARCHAR(64)," +
                    "relative_date_range VARCHAR(64)," +
                    "specific_start_date DATE," +
                    "specific_end_date DATE," +
                    "fund_ids VARCHAR(1024)," +
                    "output_format VARCHAR(32)," +
                    "account_ids VARCHAR(1024))");

            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS document_attachment(" +
                    "document_id VARCHAR(64) PRIMARY KEY," +
                    "transaction_id VARCHAR(64)," +
                    "file_path VARCHAR(255)," +
                    "original_name VARCHAR(255)," +
                    "upload_time BIGINT)");
        }
        initialized = true;
    }

    /**
     * Creates a backup of the database at the specified location using the H2
     * BACKUP command.
     *
     * @param target the file to write the backup to
     */
    public static void backupDatabase(File target) throws SQLException {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute("BACKUP TO '" + target.getAbsolutePath() + "'");
        }
    }
}
