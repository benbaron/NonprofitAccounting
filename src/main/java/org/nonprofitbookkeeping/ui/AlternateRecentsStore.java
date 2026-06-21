package org.nonprofitbookkeeping.ui;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Base64;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.prefs.Preferences;
import java.util.stream.Collectors;

/** Handles persistence/parsing of recent database and company choices for the alternate shell. */
class AlternateRecentsStore
{
    static final String RECENT_COMPANIES_PREFIX = "alternate.recent.companies.v4.";
    private static final String RECENT_DATABASES_KEY = "alternate.recent.databases";
    private static final int MAX_RECENTS = 8;
    private final AlternateDataContextService.PreferencesStore preferencesStore;

    AlternateRecentsStore()
    {
        this(new AlternateDataContextService.JavaPreferencesStore(Preferences.userNodeForPackage(AlternateDataContextService.class)));
    }

    AlternateRecentsStore(AlternateDataContextService.PreferencesStore preferencesStore)
    {
        this.preferencesStore = preferencesStore;
    }

    void rememberDatabase(Path databaseBasePath)
    {
        pushRecent(RECENT_DATABASES_KEY, databaseBasePath.toAbsolutePath().toString());
    }

    List<String> recentDatabasePaths()
    {
        return readRecents(RECENT_DATABASES_KEY);
    }

    void rememberCompany(Path activeDatabaseBasePath, long companyId, String companyLabel)
    {
        pushRecent(recentCompaniesKey(activeDatabaseBasePath), encodeCompanyRecent(companyId, companyLabel));
    }

    List<AlternateDataContextService.RecentCompanyChoice> recentCompanies(Path activeDatabaseBasePath)
    {
        return readRecents(recentCompaniesKey(activeDatabaseBasePath)).stream()
            .map(this::parseRecentCompany)
            .filter(choice -> choice != null)
            .collect(Collectors.toList());
    }

    static String recentCompaniesKey(Path activeDatabaseBasePath)
    {
        if (activeDatabaseBasePath != null)
        {
            return RECENT_COMPANIES_PREFIX + encodeKey(activeDatabaseBasePath.toString());
        }
        return RECENT_COMPANIES_PREFIX + "none";
    }

    private void pushRecent(String key, String value)
    {
        Set<String> ordered = new LinkedHashSet<>();
        ordered.add(value);
        ordered.addAll(readRecents(key));
        List<String> trimmed = new ArrayList<>(ordered).subList(0, Math.min(MAX_RECENTS, ordered.size()));
        this.preferencesStore.put(key, String.join("\n", trimmed));
    }

    private List<String> readRecents(String key)
    {
        String raw = this.preferencesStore.get(key, "");
        if (raw.isBlank())
        {
            return List.of();
        }
        return raw.lines().map(String::trim).filter(s -> !s.isBlank()).toList();
    }

    private static String encodeCompanyRecent(long id, String label)
    {
        String safeLabel = label == null ? "" : label.replace("\t", " ");
        return id + "\t" + safeLabel;
    }

    private static String encodeKey(String value)
    {
        try
        {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder encoded = new StringBuilder("sha256.");
            for (int i = 0; i < 12; i++)
            {
                encoded.append(String.format("%02x", hash[i]));
            }
            return encoded.toString();
        }
        catch (Exception ex)
        {
            return Base64.getUrlEncoder().withoutPadding()
                .encodeToString(value.getBytes(StandardCharsets.UTF_8));
        }
    }

    private AlternateDataContextService.RecentCompanyChoice parseRecentCompany(String value)
    {
        String[] parts = value.split("\t", 2);
        try
        {
            long id = Long.parseLong(parts[0]);
            String label = parts.length > 1 ? parts[1] : "Company ID " + id;
            return new AlternateDataContextService.RecentCompanyChoice(id, label.isBlank() ? "Company ID " + id : label);
        }
        catch (Exception ex)
        {
            return null;
        }
    }
}
