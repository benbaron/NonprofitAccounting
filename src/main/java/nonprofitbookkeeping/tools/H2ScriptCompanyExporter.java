package nonprofitbookkeeping.tools;

import nonprofitbookkeeping.core.Database;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Exports the currently open H2 database as an executable SQL script.
 */
public final class H2ScriptCompanyExporter
{
	private H2ScriptCompanyExporter()
	{
	}

	/**
	 * Export the open database to the provided script path.
	 *
	 * @param scriptFile target SQL script path.
	 * @throws IOException  if the destination cannot be created.
	 * @throws SQLException if H2 fails to create the script.
	 */
	public static void exportScript(Path scriptFile)
		throws IOException, SQLException
	{
		if (scriptFile == null)
		{
			throw new IllegalArgumentException("scriptFile cannot be null");
		}

		Path parent = scriptFile.toAbsolutePath().getParent();
		if (parent != null)
		{
			Files.createDirectories(parent);
		}

		String sqlPath = scriptFile.toAbsolutePath().toString().replace("'", "''");
		try (Connection connection = Database.get().getConnection();
			Statement statement = connection.createStatement())
		{
			statement.execute("SCRIPT TO '" + sqlPath + "'");
		}
	}
}

