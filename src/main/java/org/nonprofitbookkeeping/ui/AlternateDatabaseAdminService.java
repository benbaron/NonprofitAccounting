package org.nonprofitbookkeeping.ui;

import nonprofitbookkeeping.tools.H2SchemaMigrator;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.sql.SQLException;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/** Service boundary for native alternate database administration workflows. */
public class AlternateDatabaseAdminService
{
    private final UiServiceProvider.DatabaseAdministrationService databaseAdministrationService;
    private final UiSessionContext sessionContext;

    public AlternateDatabaseAdminService(UiServiceProvider.DatabaseAdministrationService databaseAdministrationService,
        UiSessionContext sessionContext)
    {
        this.databaseAdministrationService = Objects.requireNonNull(databaseAdministrationService);
        this.sessionContext = Objects.requireNonNull(sessionContext);
    }

    public DatabaseOpenService.OpenResult openDatabase(Path databasePath) throws Exception
    {
        validateSupportedExistingDatabase(databasePath);
        return databaseAdministrationService.openDatabase(normalizeBase(databasePath));
    }

    public void closeDatabase()
    {
        databaseAdministrationService.closeDatabase();
    }

    public AdminResult importDatabase(Path sourcePath, Path targetPath, boolean openAfterImport) throws Exception
    {
        validateSupportedExistingDatabase(sourcePath);
        validateTarget(targetPath, false);
        Path normalizedTargetFile = normalizeFile(targetPath);
        Files.createDirectories(parentOrCurrent(normalizedTargetFile));
        Files.copy(normalizeFile(sourcePath), normalizedTargetFile);
        DatabaseOpenService.OpenResult openResult = null;
        if (openAfterImport)
        {
            openResult = databaseAdministrationService.openDatabase(normalizeBase(normalizedTargetFile));
        }
        return new AdminResult(normalizeFile(sourcePath), normalizedTargetFile, null, normalizedTargetFile,
            "Imported database to " + normalizedTargetFile.toAbsolutePath(), openResult, List.of());
    }

    public AdminResult exportDatabase(Path sourcePath, Path targetPath, boolean overwrite) throws IOException
    {
        Path effectiveSource = sourcePath != null ? sourcePath : activeDatabaseFile();
        validateSupportedExistingDatabase(effectiveSource);
        validateTarget(targetPath, overwrite);
        Path normalizedTargetFile = normalizeFile(targetPath);
        Files.createDirectories(parentOrCurrent(normalizedTargetFile));
        if (overwrite)
        {
            Files.copy(normalizeFile(effectiveSource), normalizedTargetFile, StandardCopyOption.REPLACE_EXISTING);
        }
        else
        {
            Files.copy(normalizeFile(effectiveSource), normalizedTargetFile);
        }
        return new AdminResult(normalizeFile(effectiveSource), normalizedTargetFile, null, normalizedTargetFile,
            "Exported database backup to " + normalizedTargetFile.toAbsolutePath(), null, List.of());
    }

    public AdminResult validateDatabase(Path databasePath) throws IOException
    {
        validateSupportedExistingDatabase(databasePath);
        return new AdminResult(normalizeFile(databasePath), null, null, normalizeFile(databasePath),
            "Database file is present and uses a supported H2 extension.", null, List.of());
    }

    public AdminResult repairDatabase(Path databasePath, boolean backupConfirmed, boolean openAfterRepair) throws Exception
    {
        validateSupportedExistingDatabase(databasePath);
        Path basePath = normalizeBase(databasePath);
        if (isActiveDatabase(basePath) && !backupConfirmed)
        {
            throw new IllegalStateException("Repair of the active database requires explicit backup confirmation before exclusive access.");
        }
        if (isActiveDatabase(basePath))
        {
            closeDatabase();
        }
        H2SchemaMigrator.RepairResult repairResult = repairCorruptedDatabase(basePath);
        DatabaseOpenService.OpenResult openResult = null;
        if (openAfterRepair)
        {
            openResult = databaseAdministrationService.openDatabase(basePath);
        }
        return new AdminResult(normalizeFile(databasePath), null,
            repairResult.backupFiles().isEmpty() ? null : repairResult.backupFiles().get(0),
            repairResult.recoveryScript(), "Repair completed for " + basePath.toAbsolutePath(), openResult,
            repairResult.backupFiles());
    }

    public AdminResult migrateSchema(Path databasePath, Path outputScript) throws SQLException, IOException
    {
        validateSupportedExistingDatabase(databasePath);
        Path basePath = normalizeBase(databasePath);
        if (isActiveDatabase(basePath))
        {
            closeDatabase();
        }
        H2SchemaMigrator.RepairResult repairResult = H2SchemaMigrator.migrateWithRepairInfo(basePath, outputScript);
        List<Path> backups = repairResult == null ? List.of() : repairResult.backupFiles();
        return new AdminResult(normalizeFile(databasePath), outputScript, backups.isEmpty() ? null : backups.get(0),
            outputScript, "Schema migration completed for " + basePath.toAbsolutePath(), null, backups);
    }

    H2SchemaMigrator.RepairResult repairCorruptedDatabase(Path basePath) throws Exception
    {
        return H2SchemaMigrator.repairCorruptedDatabase(basePath);
    }

    private void validateSupportedExistingDatabase(Path databasePath) throws IOException
    {
        if (databasePath == null)
        {
            throw new IllegalArgumentException("Database path is required.");
        }
        if (!isSupportedDatabasePath(databasePath))
        {
            throw new IllegalArgumentException("Unsupported database extension: " + databasePath);
        }
        if (!Files.exists(normalizeFile(databasePath)))
        {
            throw new IOException("Database file does not exist: " + normalizeFile(databasePath));
        }
    }

    private void validateTarget(Path targetPath, boolean overwrite) throws IOException
    {
        if (targetPath == null)
        {
            throw new IllegalArgumentException("Target path is required.");
        }
        if (!isSupportedDatabasePath(targetPath))
        {
            throw new IllegalArgumentException("Unsupported database extension: " + targetPath);
        }
        Path file = normalizeFile(targetPath);
        if (Files.exists(file) && !overwrite)
        {
            throw new IOException("Target already exists: " + file);
        }
    }

    private Path activeDatabaseFile()
    {
        Path base = sessionContext.activeDatabaseBasePath();
        if (base == null)
        {
            throw new IllegalStateException("Open a database before exporting the active database.");
        }
        return normalizeFile(base);
    }

    private boolean isActiveDatabase(Path basePath)
    {
        Path activeBase = sessionContext.activeDatabaseBasePath();
        return activeBase != null && activeBase.toAbsolutePath().normalize().equals(basePath.toAbsolutePath().normalize());
    }

    static boolean isSupportedDatabasePath(Path path)
    {
        String lower = path.getFileName().toString().toLowerCase(Locale.ROOT);
        return lower.endsWith(".mv.db") || lower.endsWith(".h2.db") || lower.endsWith(".db")
            || !lower.contains(".");
    }

    static Path normalizeBase(Path path)
    {
        String raw = path.toString();
        String lower = raw.toLowerCase(Locale.ROOT);
        if (lower.endsWith(".mv.db")) return Path.of(raw.substring(0, raw.length() - ".mv.db".length()));
        if (lower.endsWith(".h2.db")) return Path.of(raw.substring(0, raw.length() - ".h2.db".length()));
        if (lower.endsWith(".db")) return Path.of(raw.substring(0, raw.length() - ".db".length()));
        return path;
    }

    static Path normalizeFile(Path path)
    {
        String raw = path.toString();
        String lower = raw.toLowerCase(Locale.ROOT);
        if (lower.endsWith(".mv.db") || lower.endsWith(".h2.db") || lower.endsWith(".db")) return path;
        return path.resolveSibling(path.getFileName() + ".mv.db");
    }

    private static Path parentOrCurrent(Path path)
    {
        Path parent = path.toAbsolutePath().getParent();
        return parent == null ? Path.of(".") : parent;
    }

    public record AdminResult(Path sourcePath, Path targetPath, Path backupPath, Path resultPath, String message,
                              DatabaseOpenService.OpenResult openResult, List<Path> backupPaths) {}
}
