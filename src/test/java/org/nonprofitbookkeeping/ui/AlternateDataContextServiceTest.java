package org.nonprofitbookkeeping.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import nonprofitbookkeeping.persistence.CompanyRepository;
import nonprofitbookkeeping.preferences.PreferencesManager;

class AlternateDataContextServiceTest
{
    @Test
    void normalizeH2BaseStripsMvDbSuffix()
    {
        AlternateDataContextService service = new AlternateDataContextService();
        Path normalized = service.normalizeH2Base(Path.of("/tmp/books.mv.db"));
        assertEquals(Path.of("/tmp/books"), normalized);
    }

    @Test
    void listCompaniesReturnsEmptyWhenDatabaseNotInitialized() throws Exception
    {
        AlternateDataContextService service = new AlternateDataContextService();
        assertTrue(service.listCompanies().isEmpty());
    }

    @Test
    void setActiveDatabaseBasePathNormalizesAndStoresBasePath()
    {
        AlternateDataContextService service = new AlternateDataContextService();
        service.setActiveDatabaseBasePath(Path.of("/tmp/acct.mv.db"));
        assertEquals(Path.of("/tmp/acct").toAbsolutePath().normalize(), service.activeDatabaseBasePath());
    }

    @Test
    void recentCompaniesAreScopedToCurrentDatabaseAndIgnoreInvalidEntries()
    {
        Map<String, String> backing = new HashMap<>();
        AlternateDataContextService.PreferencesStore store = new AlternateDataContextService.PreferencesStore()
        {
            @Override
            public String get(String key, String defaultValue)
            {
                return backing.getOrDefault(key, defaultValue);
            }

            @Override
            public void put(String key, String value)
            {
                backing.put(key, value);
            }
        };

        PreferencesManager.setLastDatabasePath("/tmp/a.mv.db");
        String keyA = "alternate.recent.companies.v4." +
            Base64.getUrlEncoder().withoutPadding()
                .encodeToString("/tmp/a".getBytes(StandardCharsets.UTF_8));
        backing.put(keyA, "abc\n10\tAcme Co");

        PreferencesManager.setLastDatabasePath("/tmp/b.mv.db");
        String keyB = "alternate.recent.companies.v4." +
            Base64.getUrlEncoder().withoutPadding()
                .encodeToString("/tmp/b".getBytes(StandardCharsets.UTF_8));
        backing.put(keyB, "20\tBeta Co");

        AlternateDataContextService service = new AlternateDataContextService(new CompanyRepository(), store);
        List<AlternateDataContextService.RecentCompanyChoice> scoped = service.recentCompanies();
        assertEquals(1, scoped.size());
        assertEquals(20L, scoped.get(0).id());
        assertEquals("Beta Co", scoped.get(0).label());
    }
}
