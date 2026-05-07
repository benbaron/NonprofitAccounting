package org.nonprofitbookkeeping.ui;

import java.nio.file.Path;
import java.sql.SQLException;

import nonprofitbookkeeping.core.Database;
import nonprofitbookkeeping.preferences.PreferencesManager;
import nonprofitbookkeeping.tools.H2SchemaMigrator;
import nonprofitbookkeeping.tools.H2ScriptCompanyExporter;

/** Encapsulates DB context switching and open+repair flow for alternate shell entry points. */
class AlternateDatabaseContextSwitcher
{
    void openDatabase(Path basePath) throws Exception
    {
        Database.init(basePath);
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
            H2SchemaMigrator.repairCorruptedDatabase(basePath);
        }
        PreferencesManager.setLastDatabasePath(basePath.toAbsolutePath() + ".mv.db");
    }
}
