package org.nonprofitbookkeeping.ui;

import java.io.IOException;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.List;
import java.util.prefs.Preferences;

import nonprofitbookkeeping.core.Database;
import nonprofitbookkeeping.model.CurrentCompany;
import nonprofitbookkeeping.persistence.CompanyRepository;
import nonprofitbookkeeping.persistence.CompanyRepository.CompanyRecord;
import nonprofitbookkeeping.preferences.PreferencesManager;
import nonprofitbookkeeping.service.PreferencesService;

/** Shared data-context operations used by alternate shell DB/company selectors. */
public class AlternateDataContextService
{
    private final CompanyRepository companyRepository;
    private final AlternateRecentsStore recentsStore;
    private final AlternateDatabaseContextSwitcher databaseContextSwitcher;
    private Path activeDatabaseBasePath;
    private Long activeCompanyId;
    private String activeCompanyLabel;

    public AlternateDataContextService()
    {
        this(new CompanyRepository(),
            new AlternateRecentsStore(new JavaPreferencesStore(Preferences.userNodeForPackage(AlternateDataContextService.class))),
            new AlternateDatabaseContextSwitcher());
    }

    AlternateDataContextService(CompanyRepository companyRepository, PreferencesStore preferencesStore)
    {
        this(companyRepository, new AlternateRecentsStore(preferencesStore), new AlternateDatabaseContextSwitcher());
    }

    AlternateDataContextService(CompanyRepository companyRepository, AlternateRecentsStore recentsStore, AlternateDatabaseContextSwitcher databaseContextSwitcher)
    {
        this.companyRepository = companyRepository;
        this.recentsStore = recentsStore;
        this.databaseContextSwitcher = databaseContextSwitcher;
        String lastPath = PreferencesManager.getLastDatabasePath();
        if (lastPath != null && !lastPath.isBlank())
        {
            this.activeDatabaseBasePath = normalizeH2Base(Path.of(lastPath)).toAbsolutePath().normalize();
        }
    }

    public void openDatabase(Path selectedPath) throws Exception
    {
        Path basePath = normalizeH2Base(selectedPath);
        databaseContextSwitcher.openDatabase(basePath);
        transitionDatabaseContext(basePath);
    }

    public void setActiveDatabaseBasePath(Path databasePath)
    {
        if (databasePath == null)
        {
            this.activeDatabaseBasePath = null;
            return;
        }
        this.activeDatabaseBasePath = normalizeH2Base(databasePath).toAbsolutePath().normalize();
    }

    public Path activeDatabaseBasePath()
    {
        return this.activeDatabaseBasePath;
    }

    public Long activeCompanyId()
    {
        return activeCompanyId;
    }

    public String activeCompanyLabel()
    {
        return activeCompanyLabel;
    }

    public List<CompanyRecord> listCompanies() throws SQLException
    {
        if (!Database.isInitialized())
        {
            return List.of();
        }
        return companyRepository.listCompanies();
    }

    public void openCompany(long companyId, String companyLabel) throws IOException
    {
        CurrentCompany.loadFromPersistent(companyId);
        transitionCompanyContext(companyId, companyLabel);
    }

    public List<String> recentDatabasePaths()
    {
        return recentsStore.recentDatabasePaths();
    }

    public List<RecentCompanyChoice> recentCompanies()
    {
        return recentsStore.recentCompanies(activeDatabaseBasePath);
    }

    Path normalizeH2Base(Path filePath)
    {
        String path = filePath.toAbsolutePath().toString();
        if (path.endsWith(".mv.db"))
        {
            path = path.substring(0, path.length() - ".mv.db".length());
        }
        return Path.of(path);
    }

    private void transitionDatabaseContext(Path basePath)
    {
        setActiveDatabaseBasePath(basePath);
        activeCompanyId = null;
        activeCompanyLabel = null;
        PreferencesService.setLastUsedCompanyId(null);
        recentsStore.rememberDatabase(basePath);
    }

    private void transitionCompanyContext(long companyId, String companyLabel)
    {
        activeCompanyId = companyId;
        activeCompanyLabel = companyLabel;
        PreferencesService.setLastUsedCompanyId(companyId);
        recentsStore.rememberCompany(activeDatabaseBasePath, companyId, companyLabel);
    }

    interface PreferencesStore
    {
        String get(String key, String defaultValue);
        void put(String key, String value);
    }

    static class JavaPreferencesStore implements PreferencesStore
    {
        private final Preferences prefs;

        JavaPreferencesStore(Preferences prefs)
        {
            this.prefs = prefs;
        }

        @Override
        public String get(String key, String defaultValue)
        {
            return prefs.get(key, defaultValue);
        }

        @Override
        public void put(String key, String value)
        {
            prefs.put(key, value);
        }
    }

    public record RecentCompanyChoice(long id, String label) {}
}
