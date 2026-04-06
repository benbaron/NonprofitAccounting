package nonprofitbookkeeping.tools;

import nonprofitbookkeeping.core.Database;
import org.h2.tools.Recover;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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
    public record RepairResult(Path recoveryScript, List<Path> backupFiles)
    {
    }

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
        migrateWithRepairInfo(dbPath, outputScript);
    }

    public static RepairResult migrateWithRepairInfo(Path dbPath, Path outputScript)
        throws SQLException, IOException
    {
        Path effectiveDbPath = normalizeDbPath(dbPath);
        Database.init(effectiveDbPath);
        RepairResult repairResult = null;
        try
        {
            Database.get().ensureSchema();
        }
        catch (SQLException ex)
        {
            if (!H2ScriptCompanyExporter.isFileCorruption(ex))
            {
                throw ex;
            }
            try
            {
                repairResult = repairCorruptedDatabase(effectiveDbPath);
            }
            catch (Exception repairEx)
            {
                SQLException wrapped = new SQLException(
                    "Database is corrupted and automatic repair failed.",
                    repairEx);
                wrapped.addSuppressed(ex);
                throw wrapped;
            }
        }

        if (outputScript != null)
        {
            exportScript(outputScript);
        }
        return repairResult;
    }

    public static RepairResult repairCorruptedDatabase(Path dbPath) throws Exception
    {
        Path effectiveDbPath = normalizeDbPath(dbPath);
        Path recoveryScript = createRecoveryScript(effectiveDbPath);
        List<Path> backupFiles = backupExistingDbFiles(effectiveDbPath);

        Database.init(effectiveDbPath);
        Database.get().ensureSchema();
        H2ScriptCompanyImporter.importScript(recoveryScript);
        Database.get().ensureSchema();
        return new RepairResult(recoveryScript, List.copyOf(backupFiles));
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

    static Path backupPathFor(Path originalFile)
    {
        String timestamp = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS").format(Instant.now().atZone(ZoneOffset.UTC));
        String entropy = UUID.randomUUID().toString().substring(0, 8);
        return originalFile.resolveSibling(originalFile.getFileName() + ".corrupt-" + timestamp + "-" + entropy + ".bak");
    }

    private static List<Path> backupExistingDbFiles(Path dbBasePath) throws IOException
    {
        Path mvDb = dbBasePath.resolveSibling(dbBasePath.getFileName() + ".mv.db");
        Path traceDb = dbBasePath.resolveSibling(dbBasePath.getFileName() + ".trace.db");
        List<Path> backups = new ArrayList<>();

        if (Files.exists(mvDb))
        {
            Path backup = backupPathFor(mvDb);
            Files.move(mvDb, backup, StandardCopyOption.REPLACE_EXISTING);
            backups.add(backup);
        }
        if (Files.exists(traceDb))
        {
            Path backup = backupPathFor(traceDb);
            Files.move(traceDb, backup, StandardCopyOption.REPLACE_EXISTING);
            backups.add(backup);
        }
        return backups;
    }

    private static Path createRecoveryScript(Path dbBasePath) throws Exception
    {
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
        return recoveredScript;
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
