package org.nonprofitbookkeeping.ui;

import java.io.IOException;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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
    private final UiSessionContext sessionContext;
    private Path activeDatabaseBasePath;
    private Long activeCompanyId;
    private String activeCompanyLabel;

    public AlternateDataContextService()
    {
        this(new CompanyRepository(),
            new AlternateRecentsStore(new JavaPreferencesStore(
                Preferences.userNodeForPackage(
                    AlternateDataContextService.class))),
            new AlternateDatabaseContextSwitcher());
    }

    AlternateDataContextService(CompanyRepository companyRepository,
        PreferencesStore preferencesStore)
    {
        this(companyRepository, new AlternateRecentsStore(preferencesStore),
            new AlternateDatabaseContextSwitcher());
    }

    AlternateDataContextService(CompanyRepository companyRepository,
        AlternateRecentsStore recentsStore,
        AlternateDatabaseContextSwitcher databaseContextSwitcher)
    {
        this(companyRepository, recentsStore, databaseContextSwitcher,
            new UiSessionContext());
    }

    AlternateDataContextService(CompanyRepository companyRepository,
        AlternateRecentsStore recentsStore,
        AlternateDatabaseContextSwitcher databaseContextSwitcher,
        UiSessionContext sessionContext)
    {
        this.companyRepository = companyRepository;
        this.recentsStore = recentsStore;
        this.databaseContextSwitcher = databaseContextSwitcher;
        this.sessionContext = sessionContext;
        UiServiceRegistry.bindSessionContext(this.sessionContext);
        String lastPath = PreferencesManager.getLastDatabasePath();
        if (lastPath != null && !lastPath.isBlank())
        {
            this.activeDatabaseBasePath = normalizeH2Base(Path.of(lastPath))
                .toAbsolutePath().normalize();
            this.sessionContext.openDatabase(this.activeDatabaseBasePath);
        }
    }

    public void openDatabase(Path selectedPath) throws Exception
    {
        Path basePath = normalizeH2Base(selectedPath);
        this.databaseContextSwitcher.openDatabase(basePath);
        transitionDatabaseContext(basePath);
    }

    public void setActiveDatabaseBasePath(Path databasePath)
    {
        this.activeDatabaseBasePath = databasePath == null ? null :
            normalizeH2Base(databasePath).toAbsolutePath().normalize();
        if (this.activeDatabaseBasePath == null)
        {
            this.sessionContext.clearDatabase();
        }
        else
        {
            this.sessionContext.openDatabase(this.activeDatabaseBasePath);
        }
        this.activeCompanyId = null;
        this.activeCompanyLabel = null;
    }

    public void clearActiveCompanyContext()
    {
        this.activeCompanyId = null;
        this.activeCompanyLabel = null;
        this.sessionContext.clearCompany();
        PreferencesService.setLastUsedCompanyId(null);
    }

    public UiSessionContext sessionContext()
    {
        return this.sessionContext;
    }

    public Path activeDatabaseBasePath()
    {
        return this.activeDatabaseBasePath;
    }

    public Long activeCompanyId()
    {
        return this.activeCompanyId;
    }

    public String activeCompanyLabel()
    {
        return this.activeCompanyLabel;
    }

    public boolean isDatabaseOpen()
    {
        return this.sessionContext.isDatabaseOpen();
    }

    public boolean isCompanyOpen()
    {
        return this.sessionContext.isCompanyOpen();
    }

    public String activeCompanyDisplayLabel()
    {
        return this.sessionContext.activeCompanyDisplayLabel();
    }

    public List<CompanyRecord> listCompanies() throws SQLException
    {
        if (!Database.isInitialized())
        {
            return List.of();
        }
        return this.companyRepository.listCompanies();
    }

    public void openCompany(long companyId, String companyLabel)
        throws IOException
    {
        CurrentCompany.loadFromPersistent(companyId);
        transitionCompanyContext(companyId, companyLabel);
    }

    public List<String> recentDatabasePaths()
    {
        return this.recentsStore.recentDatabasePaths();
    }

    /**
     * Returns recent companies with labels normalized from the currently open
     * database. This prevents stale display text from breaking selection-to-ID
     * resolution in the new UI.
     */
    public List<RecentCompanyChoice> recentCompanies()
    {
        List<RecentCompanyChoice> stored =
            this.recentsStore.recentCompanies(this.activeDatabaseBasePath);
        if (stored.isEmpty() || !Database.isInitialized())
        {
            return stored;
        }
        try
        {
            Map<Long, String> currentLabels = new LinkedHashMap<>();
            for (CompanyRecord record : this.companyRepository.listCompanies())
            {
                currentLabels.put(record.id(),
                    record.name() + " (ID: " + record.id() + ")");
            }
            return stored.stream()
                .filter(choice -> currentLabels.containsKey(choice.id()))
                .map(choice -> new RecentCompanyChoice(choice.id(),
                    currentLabels.get(choice.id())))
                .toList();
        }
        catch (SQLException ex)
        {
            return stored;
        }
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
        clearActiveCompanyContext();
        this.recentsStore.rememberDatabase(basePath);
    }

    private void transitionCompanyContext(long companyId,
        String companyLabel)
    {
        this.activeCompanyId = companyId;
        this.activeCompanyLabel = companyLabel;
        this.sessionContext.openCompany(companyId, companyLabel);
        PreferencesService.setLastUsedCompanyId(companyId);
        this.recentsStore.rememberCompany(this.activeDatabaseBasePath,
            companyId, companyLabel);
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
            return this.prefs.get(key, defaultValue);
        }

        @Override
        public void put(String key, String value)
        {
            this.prefs.put(key, value);
        }
    }

    public record RecentCompanyChoice(long id, String label)
    {
    }
}
