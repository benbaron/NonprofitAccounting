package nonprofitbookkeeping.persistence;

import jakarta.enterprise.context.ApplicationScoped;
import nonprofitbookkeeping.core.Database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

/**
 * Simple repository used to persist small JSON documents inside the H2 database.
 *
 * <p>Complete raw SCLX files are interchange artifacts rather than application
 * records. Keys under {@code sclx.raw.*} are deliberately discarded and any
 * legacy raw SCLX documents are removed from the database.</p>
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
    private static final String DELETE_RAW_SCLX_SQL =
        "DELETE FROM document WHERE name LIKE 'sclx.raw.%'";
    private static final String SCLX_RAW_PREFIX = "sclx.raw.";

    /**
     * Retained for source compatibility with callers from the former
     * thread-local raw-payload implementation. There is no cache to clear.
     */
    public static void clearThreadScopedEphemeralDocuments()
    {
        // Raw SCLX documents are no longer retained.
    }

    private static boolean isRawSclxDocument(String name)
    {
        return name != null && name.startsWith(SCLX_RAW_PREFIX);
    }

    /**
     * Stores or replaces the JSON payload associated with the supplied name.
     * Raw SCLX payload keys are ignored and trigger cleanup of legacy copies.
     */
    public void upsert(String name, String content) throws SQLException
    {
        if (isRawSclxDocument(name))
        {
            deleteLegacyRawSclxDocuments();
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

    /**
     * Retrieves the JSON payload associated with the supplied document name.
     */
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

    /**
     * Removes a stored document.
     */
    public void delete(String name) throws SQLException
    {
        if (isRawSclxDocument(name))
        {
            deleteLegacyRawSclxDocuments();
            return;
        }

        try (Connection c = Database.get().getConnection();
             PreparedStatement ps = c.prepareStatement(DELETE_SQL))
        {
            ps.setString(1, name);
            ps.executeUpdate();
        }
    }

    private void deleteLegacyRawSclxDocuments() throws SQLException
    {
        try (Connection c = Database.get().getConnection();
             PreparedStatement ps = c.prepareStatement(DELETE_RAW_SCLX_SQL))
        {
            ps.executeUpdate();
        }
    }
}
