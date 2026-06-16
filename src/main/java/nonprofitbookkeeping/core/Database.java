
package nonprofitbookkeeping.core;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * The Class Database.
 */
public final class Database
{
	
	/** The instance. */
	private static Database INSTANCE;
	
	/** The url. */
	private final String url;
	
	/** The user. */
	private final String user = "sa";
	
	/** The pass. */
	private final String pass = "";
	
	/**
	 * Instantiates a new database.
	 *
	 * @param dbFile the db file
	 */
	private Database(Path dbFile)
	{
		this.url = "jdbc:h2:file:" + dbFile.toAbsolutePath().toString() +
			";AUTO_SERVER=TRUE;MODE=MySQL";
		
	}
	
	/**
	 * Inits the.
	 *
	 * @param dbFile the db file
	 */
	public static synchronized void init(Path dbFile)
	{
		if (dbFile == null)
			throw new IllegalArgumentException("dbFile required");
		INSTANCE = new Database(dbFile);
		
	}
	
	/**
	 * Checks if is initialized.
	 *
	 * @return true, if is initialized
	 */
	public static synchronized boolean isInitialized()
	{
		return INSTANCE != null;
		
	}
	
	/**
	 * Gets the.
	 *
	 * @return the database
	 */
	public static Database get()
	{
		if (INSTANCE == null)
			throw new IllegalStateException("Database not initialized");
		return INSTANCE;
		
	}
	
	/**
	 * Gets the connection.
	 *
	 * @return the connection
	 * @throws SQLException the SQL exception
	 */
	public Connection getConnection() throws SQLException
	{
		return DriverManager.getConnection(this.url, this.user, this.pass);
		
	}
	
	public String getJdbcUrl()
	{
		return this.url;
	}
	
	public String getUser()
	{
		return this.user;
	}
	
	public String getPass()
	{
		return this.pass;
	}
	
	/**
	 * Ensure schema.
	 *
	 * @throws SQLException the SQL exception
	 */
	public void ensureSchema() throws SQLException
	{
		FlywayMigrationRunner.migrateCurrentDatabaseIfEnabled();
		try (Connection c = getConnection())
		{
			new DatabaseCompatibilityBackfills().run(c);
			runFinancePostingEnforcementPreflight(c);
		}
		
	}
	
	private void runFinancePostingEnforcementPreflight(Connection c) throws SQLException
	{
		String mode = System.getProperty("nonprofitbookkeeping.financeWriteEnforcement", "enforce");
		try (Statement st = c.createStatement();
		     ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM v_finance_posting_enforcement_exceptions"))
		{
			rs.next();
			int count = rs.getInt(1);
			if (count > 0)
			{
				System.err.println("[FINANCE_ENFORCEMENT_PREFLIGHT] Exceptions found: " + count);
				if ("enforce".equalsIgnoreCase(mode))
				{
					throw new SQLException("Finance posting enforcement preflight failed; review v_finance_posting_enforcement_exceptions.");
				}
			}
		}
	}

}
