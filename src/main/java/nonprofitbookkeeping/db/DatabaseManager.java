package nonprofitbookkeeping.db;

<<<<<<< Upstream, based on origin/codex/read-provided-xlsx-file
<<<<<<< Upstream, based on origin/codex/read-provided-xlsx-file
import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Simple database manager providing connections to a SQLite database
 * located in a company's directory. Tables for budgets and budget lines
 * are created automatically when a connection is requested.
 */
public class DatabaseManager {
    /** File name of the SQLite database within a company directory. */
    private static final String DB_FILENAME = "company.db";

    /**
     * Obtains a connection to the SQLite database for the given company
     * directory. The database file will be created if it does not exist
     * and required tables are initialized.
     *
     * @param companyDirectory directory of the company
     * @return a {@link Connection} to the SQLite database
     * @throws SQLException if a database error occurs
     */
    public static Connection getConnection(File companyDirectory) throws SQLException {
        if (companyDirectory == null || !companyDirectory.isDirectory()) {
            throw new SQLException("Invalid company directory");
        }
        File dbFile = new File(companyDirectory, DB_FILENAME);
        String url = "jdbc:sqlite:" + dbFile.getAbsolutePath();
        Connection conn = DriverManager.getConnection(url);
        initializeDatabase(conn);
        return conn;
    }

    /**
     * Ensures that the budgets and budget_lines tables exist.
     */
    private static void initializeDatabase(Connection conn) throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(
                "CREATE TABLE IF NOT EXISTS budgets (" +
                "budget_id TEXT PRIMARY KEY, " +
                "budget_name TEXT, " +
                "fiscal_year INTEGER, " +
                "description TEXT, " +
                "currency TEXT, " +
                "applicable_fund_id TEXT" +
                ")");
            stmt.executeUpdate(
                "CREATE TABLE IF NOT EXISTS budget_lines (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "budget_id TEXT, " +
                "account_id TEXT, " +
                "account_name TEXT, " +
                "total_budgeted_amount TEXT, " +
                "periodicity TEXT, " +
                "periodic_amounts TEXT, " +
                "fund_id TEXT, " +
                "FOREIGN KEY (budget_id) REFERENCES budgets(budget_id)" +
                ")");
        }
=======
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

/**
 * Simple helper for managing the JPA {@link EntityManagerFactory}.
 */
public final class DatabaseManager {
    private static final EntityManagerFactory emf =
            Persistence.createEntityManagerFactory("nonprofitPU");

    private DatabaseManager() { }

    /**
     * Obtain a new {@link EntityManager}.
     *
     * @return EntityManager for the persistence unit
     */
    public static EntityManager getEntityManager() {
        return emf.createEntityManager();
>>>>>>> 61e85fc Implement JPA persistence for budgets
=======
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Utility class for obtaining JDBC connections to the application's
 * local SQLite database. Ensures the parent directory exists before
 * attempting to establish a connection.
 */
public final class DatabaseManager {

    /** Location of the database file used across the application. */
    public static final String JDBC_URL = "jdbc:sqlite:" + System.getProperty("user.home")
            + "/.m2/nonprofitbookkeeping/nonprofitdb";

    private DatabaseManager() {
        // Utility class
    }

    /**
     * Obtains a connection to the application database. The parent directory
     * is created if it does not already exist.
     *
     * @return an open {@link Connection}
     * @throws SQLException if the connection cannot be established
     */
    public static Connection getConnection() throws SQLException {
        ensureDirectoryExists();
        return DriverManager.getConnection(JDBC_URL);
    }

    private static void ensureDirectoryExists() {
        String pathString = JDBC_URL.replaceFirst("jdbc:sqlite:", "");
        Path dbPath = Path.of(pathString).getParent();
        if (dbPath != null && !Files.exists(dbPath)) {
            try {
                Files.createDirectories(dbPath);
            } catch (Exception e) {
                // Swallow exception to avoid failing before JDBC handles it
            }
        }
>>>>>>> ee43692 feat: add persistent DB config
    }
}
