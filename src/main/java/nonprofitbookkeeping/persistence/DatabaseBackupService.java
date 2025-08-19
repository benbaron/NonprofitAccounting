
package nonprofitbookkeeping.persistence;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/** Service for backing up the H2 database to a SQL script file. */
public class DatabaseBackupService
{
	/**
	 * Write a backup of the current database to the given file path.
	 *
	 * @param filePath destination of the SQL script
	 * @throws SQLException if the backup operation fails
	 */
	public void backupTo(String filePath) throws SQLException
	{
		
		try (Connection conn = DatabaseManager.getConnection();
			Statement stmt = conn.createStatement())
		{
			stmt.execute("SCRIPT TO '" + filePath + "'");
		}
		
	}
	
	/**
	 * Restore the database from the given SQL script.
	 *
	 * @param filePath location of the backup SQL script
	 * @throws SQLException if the restore operation fails
	 */
	public void restoreFrom(String filePath) throws SQLException
	{
		
		try (Connection conn = DatabaseManager.getConnection();
			Statement stmt = conn.createStatement())
		{
			stmt.execute("RUNSCRIPT FROM '" + filePath + "'");
		}
		
	}
	
}
