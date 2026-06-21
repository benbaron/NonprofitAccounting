package org.nonprofitbookkeeping.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

class AlternateRecentsStoreTest
{
    @Test
    void recentCompaniesAreScopedPerDatabaseAndInvalidEntriesAreIgnored()
    {
        Map<String, String> backing = new HashMap<>();
        AlternateDataContextService.PreferencesStore store = new InMemoryStore(backing);
        AlternateRecentsStore recentsStore = new AlternateRecentsStore(store);
        Path dbA = Path.of("/tmp/a").toAbsolutePath().normalize();
        Path dbB = Path.of("/tmp/b").toAbsolutePath().normalize();

        backing.put(AlternateRecentsStore.recentCompaniesKey(dbA), "bad\n10\tAcme Co");
        backing.put(AlternateRecentsStore.recentCompaniesKey(dbB), "20\tBeta Co");

        List<AlternateDataContextService.RecentCompanyChoice> scoped = recentsStore.recentCompanies(dbB);
        assertEquals(1, scoped.size());
        assertEquals(20L, scoped.get(0).id());
        assertEquals("Beta Co", scoped.get(0).label());
    }

    @Test
    void rememberDatabaseAndCompanyDeduplicatesAndKeepsMostRecentFirst()
    {
        Map<String, String> backing = new HashMap<>();
        AlternateRecentsStore recentsStore = new AlternateRecentsStore(new InMemoryStore(backing));
        Path dbA = Path.of("/tmp/a").toAbsolutePath().normalize();

        recentsStore.rememberDatabase(dbA);
        recentsStore.rememberDatabase(dbA);
        recentsStore.rememberCompany(dbA, 1L, "Alpha");
        recentsStore.rememberCompany(dbA, 1L, "Alpha");
        recentsStore.rememberCompany(dbA, 2L, "Beta");

        assertEquals(List.of(dbA.toString()), recentsStore.recentDatabasePaths());
        assertEquals(List.of(2L, 1L), recentsStore.recentCompanies(dbA).stream().map(AlternateDataContextService.RecentCompanyChoice::id).toList());
    }

    private record InMemoryStore(Map<String, String> backing) implements AlternateDataContextService.PreferencesStore
    {
        @Override
        public String get(String key, String defaultValue)
        {
            return this.backing.getOrDefault(key, defaultValue);
        }

        @Override
        public void put(String key, String value)
        {
            this.backing.put(key, value);
        }
    }
}
