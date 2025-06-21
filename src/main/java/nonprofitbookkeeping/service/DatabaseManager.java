package nonprofitbookkeeping.service;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Simple SQLite database manager used for storing document attachment metadata.
 */
public class DatabaseManager {

private static final File DB_FILE = new File(System.getProperty("user.home"),
"NonprofitDocuments/attachments.db");
private static final String JDBC_URL = "jdbc:sqlite:" + DB_FILE.getAbsolutePath();

static {
DB_FILE.getParentFile().mkdirs();
try (Connection c = getConnection(); Statement st = c.createStatement()) {
st.executeUpdate("CREATE TABLE IF NOT EXISTS document_attachment (" +
"id INTEGER PRIMARY KEY AUTOINCREMENT," +
"transaction_id TEXT NOT NULL," +
"stored_file TEXT NOT NULL," +
"original_file TEXT NOT NULL)");
} catch (SQLException ex) {
ex.printStackTrace();
}
}

/** Obtain a connection to the local SQLite database. */
public static Connection getConnection() throws SQLException {
return DriverManager.getConnection(JDBC_URL);
}
}
