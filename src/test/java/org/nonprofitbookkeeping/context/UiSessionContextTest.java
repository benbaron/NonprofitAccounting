package org.nonprofitbookkeeping.context;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.nonprofitbookkeeping.ui.UiSessionContext;

class UiSessionContextTest
{
    @Test
    void startsClosedWithNoCompanyLabel()
    {
        UiSessionContext context = new UiSessionContext();

        assertFalse(context.isDatabaseOpen());
        assertFalse(context.isCompanyOpen());
        assertNull(context.activeDatabaseBasePath());
        assertNull(context.activeCompanyId());
        assertEquals("No company open", context.activeCompanyDisplayLabel());
    }

    @Test
    void openingDatabaseClearsCompanyStateAndNotifiesBindings()
    {
        UiSessionContext context = new UiSessionContext();
        List<Boolean> databaseOpenTransitions = new ArrayList<>();
        context.databaseOpenProperty().addListener((obs, oldValue, newValue) -> databaseOpenTransitions.add(newValue));

        context.openCompany(42L, "Previous Company",
            new UiSessionContext.CompanyMetadata(true, true, true));
        context.openDatabase(Path.of("/tmp/books"));

        assertTrue(context.isDatabaseOpen());
        assertEquals(Path.of("/tmp/books"), context.activeDatabaseBasePath());
        assertFalse(context.isCompanyOpen());
        assertNull(context.activeCompanyId());
        assertEquals("No company open", context.activeCompanyDisplayLabel());
        assertFalse(context.isSampleCompany());
        assertFalse(context.isPopulatedCompany());
        assertFalse(context.isNewlyCreatedCompany());
        assertEquals(List.of(true), databaseOpenTransitions);
    }

    @Test
    void openingCompanyUpdatesIdentityLabelAndMetadata()
    {
        UiSessionContext context = new UiSessionContext();
        context.openDatabase(Path.of("/tmp/books"));

        context.openCompany(7L, "Demo Org", new UiSessionContext.CompanyMetadata(true, false, true));

        assertTrue(context.isCompanyOpen());
        assertEquals(7L, context.activeCompanyId());
        assertEquals("Demo Org", context.activeCompanyDisplayLabel());
        assertTrue(context.isSampleCompany());
        assertFalse(context.isPopulatedCompany());
        assertTrue(context.isNewlyCreatedCompany());
    }

    @Test
    void clearingDatabaseClearsCompanyAndMetadata()
    {
        UiSessionContext context = new UiSessionContext();
        context.openDatabase(Path.of("/tmp/books"));
        context.openCompany(7L, "Demo Org", new UiSessionContext.CompanyMetadata(true, true, true));

        context.clearDatabase();

        assertFalse(context.isDatabaseOpen());
        assertNull(context.activeDatabaseBasePath());
        assertFalse(context.isCompanyOpen());
        assertEquals("No company open", context.activeCompanyDisplayLabel());
        assertFalse(context.isSampleCompany());
        assertFalse(context.isPopulatedCompany());
        assertFalse(context.isNewlyCreatedCompany());
    }
}
