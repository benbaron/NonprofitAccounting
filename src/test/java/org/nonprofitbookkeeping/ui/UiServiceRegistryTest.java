package org.nonprofitbookkeeping.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class UiServiceRegistryTest
{
    @AfterEach
    void unbindCompatibilityContext()
    {
        UiServiceRegistry.bindSessionContext(null);
    }

    @Test
    void bindSessionContextCopiesInitialDatabaseAndCompanyState()
    {
        UiSessionContext source = new UiSessionContext();
        source.openDatabase(Path.of("/tmp/books-one"));
        source.openCompany(11L, "Company One");

        UiServiceRegistry.bindSessionContext(source);

        UiSessionContext compatibility = UiServiceRegistry.compatibilitySessionContext();
        assertEquals(Path.of("/tmp/books-one"), compatibility.activeDatabaseBasePath());
        assertEquals(11L, compatibility.activeCompanyId());
        assertEquals("Company One", compatibility.activeCompanyDisplayLabel());
        assertTrue(compatibility.isCompanyOpen());
    }

    @Test
    void rebindingDetachesOldSessionListeners()
    {
        UiSessionContext first = new UiSessionContext();
        UiSessionContext second = new UiSessionContext();
        first.openDatabase(Path.of("/tmp/first"));
        second.openDatabase(Path.of("/tmp/second"));

        UiServiceRegistry.bindSessionContext(first);
        UiServiceRegistry.bindSessionContext(second);
        first.openDatabase(Path.of("/tmp/stale-first-change"));
        second.openDatabase(Path.of("/tmp/current-second-change"));

        UiSessionContext compatibility = UiServiceRegistry.compatibilitySessionContext();
        assertEquals(Path.of("/tmp/current-second-change"), compatibility.activeDatabaseBasePath());
        assertFalse(compatibility.isCompanyOpen());
    }

    @Test
    void boundContextCompanyLabelChangesStayInSync()
    {
        UiSessionContext source = new UiSessionContext();
        source.openDatabase(Path.of("/tmp/books"));
        source.openCompany(22L, "Original Label");
        UiServiceRegistry.bindSessionContext(source);

        source.openCompany(22L, "Renamed Label");

        UiSessionContext compatibility = UiServiceRegistry.compatibilitySessionContext();
        assertEquals(22L, compatibility.activeCompanyId());
        assertEquals("Renamed Label", compatibility.activeCompanyDisplayLabel());
    }

    @Test
    void unbindingClearsCompatibilityContextAndStopsFutureUpdates()
    {
        UiSessionContext source = new UiSessionContext();
        source.openDatabase(Path.of("/tmp/books"));
        UiServiceRegistry.bindSessionContext(source);

        UiServiceRegistry.bindSessionContext(null);
        source.openDatabase(Path.of("/tmp/ignored"));

        UiSessionContext compatibility = UiServiceRegistry.compatibilitySessionContext();
        assertNull(compatibility.activeDatabaseBasePath());
        assertFalse(compatibility.isDatabaseOpen());
    }
}
