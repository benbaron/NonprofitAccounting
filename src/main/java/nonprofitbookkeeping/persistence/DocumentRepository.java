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
 * <p>The legacy implementation wrote a number of small JSON documents to files inside the
 * company directory (for example {@code budgets.json}, {@code sales.json}, etc.).  The new
 * persistence model stores those JSON payloads in the {@code document} table so that the
 * information lives inside the database and participates in regular database backups.</p>
 *
 * <p>Raw SCLX import payloads are intentionally excluded from database persistence. They are
 * not retained so that large source JSON blobs do not bloat the {@code document} table or the
 * underlying H2 file.</p>
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
	private static final String DELETE_SCLX_RAW_SQL =
		"DELETE FROM document WHERE name LIKE ?";
	private static final String SCLX_RAW_PREFIX = "sclx.raw.";
	
	/**
	 * Preserved for import-lifecycle callers; raw SCLX payloads are no longer
	 * cached or persisted.
	 */
	public static void clearThreadScopedEphemeralDocuments()
	{
		// Raw SCLX payloads are intentionally not cached or persisted.
	}
	
	private static boolean isEphemeralSclxRawDocument(String name)
	{
		return name != null && name.startsWith(SCLX_RAW_PREFIX);
	}
	
	/**
	 * Stores or replaces the JSON payload associated with the supplied document name.
	 *
	 * @param name    logical name of the document (e.g. {@code budgets})
	 * @param content JSON payload to persist
	 * @throws SQLException if the database update fails
	 */
	public void upsert(String name, String content) throws SQLException
	{
		if (isEphemeralSclxRawDocument(name))
		{
			deleteLegacySclxRawDocuments();
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
	 *
	 * @param name logical document name
	 * @return an {@link Optional} containing the payload, or empty when no document is stored
	 * @throws SQLException if the database query fails
	 */
	public Optional<String> find(String name) throws SQLException
	{
		if (isEphemeralSclxRawDocument(name))
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
	 *
	 * @param name logical document name
	 * @throws SQLException if the delete fails
	 */
	public void delete(String name) throws SQLException
	{
		if (isEphemeralSclxRawDocument(name))
		{
			deleteLegacySclxRawDocuments();
			return;
		}
		
		try (Connection c = Database.get().getConnection();
			PreparedStatement ps = c.prepareStatement(DELETE_SQL))
		{
			ps.setString(1, name);
			ps.executeUpdate();
		}
		
	}

	private void deleteLegacySclxRawDocuments() throws SQLException
	{
		try (Connection c = Database.get().getConnection();
			PreparedStatement ps = c.prepareStatement(DELETE_SCLX_RAW_SQL))
		{
			ps.setString(1, SCLX_RAW_PREFIX + "%");
			ps.executeUpdate();
		}
		catch (SQLException ex)
		{
			String message = ex.getMessage();
			if ("42S04".equals(ex.getSQLState())
				|| (message != null
					&& message.contains("Table \"DOCUMENT\" not found")))
			{
				return;
			}
			throw ex;
		}
		catch (IllegalStateException ex)
		{
			if ("Database not initialized".equals(ex.getMessage()))
			{
				return;
			}
			throw ex;
		}
	}
	
}
