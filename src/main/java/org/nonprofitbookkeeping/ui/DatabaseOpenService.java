package org.nonprofitbookkeeping.ui;

import nonprofitbookkeeping.core.Database;
import nonprofitbookkeeping.core.FlywayMigrationRunner;
import nonprofitbookkeeping.preferences.PreferencesManager;
import nonprofitbookkeeping.tools.H2SchemaMigrator;
import nonprofitbookkeeping.tools.H2ScriptCompanyExporter;

import java.nio.file.Path;
import java.sql.SQLException;

/**
 * Shared database-open workflow for the classic and alternate UI shells.
 *
 * <p>Both shells must run the same schema-authority transition sequence:
 * initialize the selected H2 database, run Flyway migrations, then run the
 * legacy compatibility/backfill path through {@link Database#ensureSchema()}.
 * Keeping this in one service prevents the two UI interfaces from drifting.</p>
 */
final class DatabaseOpenService
{
    private DatabaseOpenService()
    {
        // utility class
    }

    static OpenResult openDatabase(Path basePath) throws Exception
    {
        Database.init(basePath);
        H2SchemaMigrator.RepairResult repairResult = null;
        try
        {
            FlywayMigrationRunner.migrateCurrentDatabaseIfEnabled();
            Database.get().ensureSchema();
        }
        catch (SQLException ex)
        {
            if (!H2ScriptCompanyExporter.isFileCorruption(ex))
            {
                throw ex;
            }
            repairResult = H2SchemaMigrator.repairCorruptedDatabase(basePath);
        }
        PreferencesManager.setLastDatabasePath(basePath.toAbsolutePath() + ".mv.db");
        return new OpenResult(basePath, repairResult);
    }

    record OpenResult(Path basePath, H2SchemaMigrator.RepairResult repairResult)
    {
        String successMessage()
        {
            String message = "Database ready: " + basePath.toAbsolutePath();
            if (repairResult != null && !repairResult.backupFiles().isEmpty())
            {
                message += "\nRecovered from corruption. Backups:\n" +
                    String.join("\n", repairResult.backupFiles().stream()
                        .map(path -> path.toAbsolutePath().toString())
                        .toList());
            }
            return message;
        }
    }
}
