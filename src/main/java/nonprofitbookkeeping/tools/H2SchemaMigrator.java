package nonprofitbookkeeping.tools;

import nonprofitbookkeeping.core.Database;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Upgrades an existing H2 company database to the latest schema level using the
 * application's built-in migration logic.
 *
 * <p>This tool is intended for users that only have a database file and no
 * legacy application build. It can update the DB in-place and optionally export
 * a post-migration SQL script for backup/transport.</p>
 */
public final class H2SchemaMigrator
{
    private H2SchemaMigrator()
    {
    }

    public static void main(String[] args) throws Exception
    {
        if (args.length < 1 || args.length > 2)
        {
            System.err.println("Usage: H2SchemaMigrator <db-path|db.mv.db> [output.sql]");
            System.exit(1);
        }

        Path dbPath = normalizeDbPath(Paths.get(args[0]));
        Path outputScript = args.length == 2 ? Paths.get(args[1]) : null;
        migrate(dbPath, outputScript);

        if (outputScript == null)
        {
            System.out.printf("Schema migration complete for %s%n", dbPath.toAbsolutePath());
        }
        else
        {
            System.out.printf("Schema migration complete for %s and script written to %s%n",
                dbPath.toAbsolutePath(), outputScript.toAbsolutePath());
        }
    }

    /**
     * Migrates the supplied H2 database to the latest schema and optionally emits
     * an SQL script snapshot.
     */
    public static void migrate(Path dbPath, Path outputScript)
        throws SQLException, IOException
    {
        Path effectiveDbPath = normalizeDbPath(dbPath);
        Database.init(effectiveDbPath);
        Database.get().ensureSchema();

        if (outputScript != null)
        {
            exportScript(outputScript);
        }
    }

    static Path normalizeDbPath(Path providedPath)
    {
        if (providedPath == null)
        {
            throw new IllegalArgumentException("dbPath cannot be null");
        }

        String raw = providedPath.toString();
        if (raw.endsWith(".mv.db"))
        {
            return Path.of(raw.substring(0, raw.length() - ".mv.db".length()));
        }
        if (raw.endsWith(".trace.db"))
        {
            return Path.of(raw.substring(0, raw.length() - ".trace.db".length()));
        }
        return providedPath;
    }

    private static void exportScript(Path target) throws SQLException, IOException
    {
        Path absoluteTarget = target.toAbsolutePath();
        Path parent = absoluteTarget.getParent();
        if (parent != null)
        {
            Files.createDirectories(parent);
        }

        String escaped = absoluteTarget.toString().replace("'", "''");
        try (Connection connection = Database.get().getConnection();
             Statement statement = connection.createStatement())
        {
            statement.execute("SCRIPT DROP TO '" + escaped + "'");
        }
    }
}
