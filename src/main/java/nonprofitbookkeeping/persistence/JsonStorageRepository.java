
package nonprofitbookkeeping.persistence;

import nonprofitbookkeeping.core.Database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

// TODO: Auto-generated Javadoc
/**
 * Simple repository that persists arbitrary JSON payloads inside the shared H2 database.
 * Values are stored by logical keys so multiple services can share the same underlying
 * storage table instead of writing individual JSON files to disk.
 */
public class JsonStorageRepository
{
	
	/** The Constant UPSERT_SQL. */
	private static final String UPSERT_SQL =
		"MERGE INTO json_storage(storage_key, payload) KEY(storage_key) VALUES (?, ?)";
	
	/** The Constant SELECT_SQL. */
	private static final String SELECT_SQL =
		"SELECT payload FROM json_storage WHERE storage_key = ?";
	
	/** The Constant DELETE_SQL. */
	private static final String DELETE_SQL =
		"DELETE FROM json_storage WHERE storage_key = ?";
	
	/**
	 * Writes or replaces the payload associated with the given key.
	 *
	 * @param key logical identifier for the payload
	 * @param payload JSON text to persist (may be {@code null} to clear)
	 * @throws SQLException the SQL exception
	 */
	public void save(String key, String payload) throws SQLException
	{
		
		if (payload == null)
		{
			delete(key);
			return;
		}
		
		try (Connection connection = Database.get().getConnection();
			PreparedStatement statement =
				connection.prepareStatement(UPSERT_SQL))
		{
			statement.setString(1, key);
			statement.setString(2, payload);
			statement.executeUpdate();
		}
		
	}
	
	/**
	 * Loads the JSON payload for the given key if present.
	 *
	 * @param key the key
	 * @return the optional
	 * @throws SQLException the SQL exception
	 */
	public Optional<String> load(String key) throws SQLException
	{
		
		try (Connection connection = Database.get().getConnection();
			PreparedStatement statement =
				connection.prepareStatement(SELECT_SQL))
		{
			statement.setString(1, key);
			
			try (ResultSet rs = statement.executeQuery())
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
	 * Removes the payload associated with the given key.
	 *
	 * @param key the key
	 * @throws SQLException the SQL exception
	 */
	public void delete(String key) throws SQLException
	{
		
		try (Connection connection = Database.get().getConnection();
			PreparedStatement statement =
				connection.prepareStatement(DELETE_SQL))
		{
			statement.setString(1, key);
			statement.executeUpdate();
		}
		
	}
	
}
