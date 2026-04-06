package nonprofitbookkeeping.tools;

import nonprofitbookkeeping.core.Database;
import org.h2.tools.Recover;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
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
		catch (SQLException ex)
		{
			if (!isFileCorruption(ex))
			{
				throw ex;
			}
			try
			{
				exportWithRecoverTool(scriptFile);
			}
			catch (Exception recoveryEx)
			{
				SQLException wrapped = new SQLException(
					"Database appears corrupted and fallback export using H2 Recover also failed.",
					recoveryEx);
				wrapped.addSuppressed(ex);
				throw wrapped;
			}
		}
	}

	public static boolean isFileCorruption(Throwable throwable)
	{
		Throwable current = throwable;
		while (current != null)
		{
			String message = current.getMessage();
			if (message != null)
			{
				String lower = message.toLowerCase();
				if (lower.contains("file corrupted") || lower.contains("mvstoreexception") ||
					lower.contains("double mark"))
				{
					return true;
				}
			}
			current = current.getCause();
		}
		return false;
	}

	static Path extractDatabaseBasePath(String jdbcUrl)
	{
		if (jdbcUrl == null || !jdbcUrl.startsWith("jdbc:h2:file:"))
		{
			throw new IllegalStateException("Unsupported H2 JDBC URL: " + jdbcUrl);
		}

		String pathPart = jdbcUrl.substring("jdbc:h2:file:".length());
		int optionsIndex = pathPart.indexOf(';');
		if (optionsIndex >= 0)
		{
			pathPart = pathPart.substring(0, optionsIndex);
		}
		return Path.of(pathPart).toAbsolutePath();
	}

	private static void exportWithRecoverTool(Path scriptFile) throws Exception
	{
		Path dbBasePath = extractDatabaseBasePath(Database.get().getJdbcUrl());
		Path dbDir = dbBasePath.getParent();
		if (dbDir == null)
		{
			dbDir = Path.of(".").toAbsolutePath();
		}
		String dbName = dbBasePath.getFileName().toString();

		new Recover().runTool("-dir", dbDir.toString(), "-db", dbName);

		Path recoveredScript = dbDir.resolve(dbName + ".h2.sql");
		if (!Files.exists(recoveredScript))
		{
			throw new IOException("Recover did not create expected file: " + recoveredScript);
		}

		Files.copy(recoveredScript, scriptFile.toAbsolutePath(),
			StandardCopyOption.REPLACE_EXISTING);
	}
}
