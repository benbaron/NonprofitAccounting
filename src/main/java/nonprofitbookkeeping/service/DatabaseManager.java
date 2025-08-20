package nonprofitbookkeeping.service;

<<<<<<< HEAD
import nonprofitbookkeeping.model.attachment.DocumentAttachment;

import java.io.File;
import java.sql.*;

/**
 * Simple SQLite based database manager used for storing document attachment metadata.
 */
public class DatabaseManager {
    private final Connection connection;

    public DatabaseManager(File dbFile) throws SQLException {
        String url = "jdbc:sqlite:" + dbFile.getAbsolutePath();
        this.connection = DriverManager.getConnection(url);
        initSchema();
    }

    private void initSchema() throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS document_attachments (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "transaction_id TEXT NOT NULL, " +
                "original_name TEXT NOT NULL, " +
                "stored_name TEXT NOT NULL" +
                ")";
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
        }
    }

    public long insertAttachment(String txnId, String originalName, String storedName) throws SQLException {
        String sql = "INSERT INTO document_attachments(transaction_id, original_name, stored_name) VALUES(?,?,?)";
        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, txnId);
            ps.setString(2, originalName);
            ps.setString(3, storedName);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
            }
        }
        return -1L;
    }

    public DocumentAttachment getAttachment(long id) throws SQLException {
        String sql = "SELECT id, transaction_id, original_name, stored_name FROM document_attachments WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new DocumentAttachment(
                            rs.getLong("id"),
                            rs.getString("transaction_id"),
                            rs.getString("original_name"),
                            rs.getString("stored_name")
                    );
                }
            }
        }
        return null;
    }
}
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
=======

import nonprofitbookkeeping.model.attachment.DocumentAttachment;

import java.io.File;
import java.sql.*;

/**
 * Simple SQLite based database manager used for storing document attachment metadata.
 */
public class DatabaseManager {
    private final Connection connection;

    public DatabaseManager(File dbFile) throws SQLException {
        String url = "jdbc:sqlite:" + dbFile.getAbsolutePath();
        this.connection = DriverManager.getConnection(url);
        initSchema();
    }

    private void initSchema() throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS document_attachments (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "transaction_id TEXT NOT NULL, " +
                "original_name TEXT NOT NULL, " +
                "stored_name TEXT NOT NULL" +
                ")";
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
        }
    }

    public long insertAttachment(String txnId, String originalName, String storedName) throws SQLException {
        String sql = "INSERT INTO document_attachments(transaction_id, original_name, stored_name) VALUES(?,?,?)";
        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, txnId);
            ps.setString(2, originalName);
            ps.setString(3, storedName);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
            }
        }
        return -1L;
    }

    public DocumentAttachment getAttachment(long id) throws SQLException {
        String sql = "SELECT id, transaction_id, original_name, stored_name FROM document_attachments WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new DocumentAttachment(
                            rs.getLong("id"),
                            rs.getString("transaction_id"),
                            rs.getString("original_name"),
                            rs.getString("stored_name")
                    );
                }
            }
        }
        return null;
    }
>>>>>>> branch 'feature/m2database' of git@github.com:benbaron/NonprofitAccounting.git
}
