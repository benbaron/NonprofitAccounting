package org.nonprofitbookkeeping.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;

import nonprofitbookkeeping.persistence.CompanyRepository;

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
    void openDatabaseTransitionsActiveContextAndRemembersRecent() throws Exception
    {
        RecordingSwitcher switcher = new RecordingSwitcher();
        AlternateRecentsStore recentsStore = new AlternateRecentsStore(new InMemoryStore());
        AlternateDataContextService service = new AlternateDataContextService(new CompanyRepository(), recentsStore, switcher);

        service.openDatabase(Path.of("/tmp/context.mv.db"));

        assertEquals(Path.of("/tmp/context").toAbsolutePath().normalize(), service.activeDatabaseBasePath());
        assertNull(service.activeCompanyId());
        assertNull(service.activeCompanyLabel());
        assertEquals(Path.of("/tmp/context").toAbsolutePath().normalize(), switcher.lastOpenedBasePath);
        assertEquals(List.of(Path.of("/tmp/context").toAbsolutePath().normalize().toString()), service.recentDatabasePaths());
    }

    @Test
    void clearActiveCompanyContextResetsCompanyFields()
    {
        AlternateDataContextService service = new AlternateDataContextService();
        service.clearActiveCompanyContext();
        assertNull(service.activeCompanyId());
        assertNull(service.activeCompanyLabel());
    }

    @Test
    void contextFlagsAndDisplayLabelFollowActiveState()
    {
        AlternateDataContextService service = new AlternateDataContextService();

        assertTrue(!service.isDatabaseOpen());
        assertTrue(!service.isCompanyOpen());
        assertEquals("No company open", service.activeCompanyDisplayLabel());

        service.setActiveDatabaseBasePath(Path.of("/tmp/flags.mv.db"));

        assertTrue(service.isDatabaseOpen());
        assertTrue(!service.isCompanyOpen());
        assertEquals("No company open", service.activeCompanyDisplayLabel());
    }

    private static final class RecordingSwitcher extends AlternateDatabaseContextSwitcher
    {
        private Path lastOpenedBasePath;

        @Override
        void openDatabase(Path basePath)
        {
            this.lastOpenedBasePath = basePath;
        }
    }

    private static final class InMemoryStore implements AlternateDataContextService.PreferencesStore
    {
        private final java.util.Map<String, String> values = new java.util.HashMap<>();

        @Override
        public String get(String key, String defaultValue)
        {
            return this.values.getOrDefault(key, defaultValue);
        }

        @Override
        public void put(String key, String value)
        {
            this.values.put(key, value);
        }
    }
}
