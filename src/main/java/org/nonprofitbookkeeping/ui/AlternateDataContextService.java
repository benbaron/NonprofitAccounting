package org.nonprofitbookkeeping.ui;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.prefs.Preferences;
import java.util.stream.Collectors;

import nonprofitbookkeeping.core.Database;
import nonprofitbookkeeping.model.CurrentCompany;
import nonprofitbookkeeping.persistence.CompanyRepository;
import nonprofitbookkeeping.persistence.CompanyRepository.CompanyRecord;
import nonprofitbookkeeping.preferences.PreferencesManager;
import nonprofitbookkeeping.service.PreferencesService;
import nonprofitbookkeeping.tools.H2SchemaMigrator;
import nonprofitbookkeeping.tools.H2ScriptCompanyExporter;

/** Shared data-context operations used by alternate shell DB/company selectors. */
public class AlternateDataContextService
{
    private static final String RECENT_DATABASES_KEY = "alternate.recent.databases";
    private static final String RECENT_COMPANIES_PREFIX = "alternate.recent.companies.v4.";
    private static final int MAX_RECENTS = 8;
    private final CompanyRepository companyRepository;
    private final PreferencesStore preferencesStore;
    private Path activeDatabaseBasePath;

    public AlternateDataContextService()
    {
        this(new CompanyRepository(), new JavaPreferencesStore(Preferences.userNodeForPackage(AlternateDataContextService.class)));
    }

    AlternateDataContextService(CompanyRepository companyRepository, PreferencesStore preferencesStore)
    {
        this.companyRepository = companyRepository;
        this.preferencesStore = preferencesStore;
        String lastPath = PreferencesManager.getLastDatabasePath();
        if (lastPath != null && !lastPath.isBlank())
        {
            this.activeDatabaseBasePath = normalizeH2Base(Path.of(lastPath)).toAbsolutePath().normalize();
        }
    }

    public void openDatabase(Path selectedPath) throws Exception
    {
        Path basePath = normalizeH2Base(selectedPath);
        setActiveDatabaseBasePath(basePath);
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
        pushRecent(RECENT_DATABASES_KEY, basePath.toAbsolutePath().toString());
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
        PreferencesService.setLastUsedCompanyId(companyId);
        pushRecent(recentCompaniesKeyForCurrentDatabase(), encodeCompanyRecent(companyId, companyLabel));
    }

    public List<String> recentDatabasePaths()
    {
        return readRecents(RECENT_DATABASES_KEY);
    }

    public List<RecentCompanyChoice> recentCompanies()
    {
        return readRecents(recentCompaniesKeyForCurrentDatabase()).stream()
            .map(this::parseRecentCompany)
            .filter(choice -> choice != null)
            .collect(Collectors.toList());
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

    private String recentCompaniesKeyForCurrentDatabase()
    {
        if (this.activeDatabaseBasePath != null)
        {
            return RECENT_COMPANIES_PREFIX + encodeKey(this.activeDatabaseBasePath.toString());
        }
        return RECENT_COMPANIES_PREFIX + "none";
    }

    private void pushRecent(String key, String value)
    {
        Set<String> ordered = new LinkedHashSet<>();
        ordered.add(value);
        ordered.addAll(readRecents(key));
        List<String> trimmed = new ArrayList<>(ordered).subList(0, Math.min(MAX_RECENTS, ordered.size()));
        preferencesStore.put(key, String.join("\n", trimmed));
    }

    private List<String> readRecents(String key)
    {
        String raw = preferencesStore.get(key, "");
        if (raw.isBlank())
        {
            return List.of();
        }
        return raw.lines().map(String::trim).filter(s -> !s.isBlank()).toList();
    }

    private String encodeCompanyRecent(long id, String label)
    {
        String safeLabel = label == null ? "" : label.replace("\t", " ");
        return id + "\t" + safeLabel;
    }

    private String encodeKey(String value)
    {
        return Base64.getUrlEncoder().withoutPadding()
            .encodeToString(value.getBytes(StandardCharsets.UTF_8));
    }

    private RecentCompanyChoice parseRecentCompany(String value)
    {
        String[] parts = value.split("\t", 2);
        try
        {
            long id = Long.parseLong(parts[0]);
            String label = parts.length > 1 ? parts[1] : "Company ID " + id;
            return new RecentCompanyChoice(id, label.isBlank() ? "Company ID " + id : label);
        }
        catch (Exception ex)
        {
            return null;
        }
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
