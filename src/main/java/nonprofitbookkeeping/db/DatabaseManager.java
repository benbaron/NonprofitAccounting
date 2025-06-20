package nonprofitbookkeeping.db;

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
    }
}
