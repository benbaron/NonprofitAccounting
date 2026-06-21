package nonprofitbookkeeping.persistence;

import jakarta.enterprise.context.ApplicationScoped;
import nonprofitbookkeeping.core.Database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

/**
 * Repository used to persist small named JSON documents in the company H2
 * database.
 *
 * <p>Raw SCLX source documents are deliberately not retained. The imported
 * accounting records and compact import summary are the durable data; keeping
 * a second complete copy of the source file only enlarges the company
 * database.</p>
 */
@ApplicationScoped
public class DocumentRepository
{
    private static final String UPSERT_SQL =
        "MERGE INTO document(name, content) KEY(name) VALUES (?, ?)";
    private static final String SELECT_SQL =
        "SELECT content FROM document WHERE name = ?";
    private static final String DELETE_SQL =
        "DELETE FROM document WHERE name = ?";
    private static final String SCLX_RAW_PREFIX = "sclx.raw.";

    /**
     * Retained for source compatibility with callers from the earlier
     * thread-local raw-document implementation. There is now nothing to clear.
     */
    public static void clearThreadScopedEphemeralDocuments()
    {
        // Raw SCLX documents are not stored in memory or in the database.
    }

    private static boolean isRawSclxDocument(String name)
    {
        return name != null && name.startsWith(SCLX_RAW_PREFIX);
    }

    public void upsert(String name, String content) throws SQLException
    {
        if (isRawSclxDocument(name))
        {
            return;
        }

        try (Connection c = Database.get().getConnection();
             PreparedStatement ps = c.prepareStatement(UPSERT_SQL))
        {
            ps.setString(1, name);
            ps.setString(2, content);
            ps.executeUpdate();
        }
    }

    public Optional<String> find(String name) throws SQLException
    {
        if (isRawSclxDocument(name))
        {
            return Optional.empty();
        }

        try (Connection c = Database.get().getConnection();
             PreparedStatement ps = c.prepareStatement(SELECT_SQL))
        {
            ps.setString(1, name);
            try (ResultSet rs = ps.executeQuery())
            {
                if (rs.next())
                {
                    return Optional.ofNullable(rs.getString(1));
                }
            }
        }
        return Optional.empty();
    }

    public void delete(String name) throws SQLException
    {
        if (isRawSclxDocument(name))
        {
            return;
        }

        try (Connection c = Database.get().getConnection();
             PreparedStatement ps = c.prepareStatement(DELETE_SQL))
        {
            ps.setString(1, name);
            ps.executeUpdate();
        }
    }
}
