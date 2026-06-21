package org.nonprofitbookkeeping.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.nonprofitbookkeeping.ui.UiSessionContext.CompanyMetadata;
import org.nonprofitbookkeeping.ui.UiSessionContext.SessionState;

class UiSessionContextTest
{
    @Test
    void startsWithoutDatabaseOrCompany()
    {
        UiSessionContext context = new UiSessionContext();

        assertEquals(SessionState.NO_DATABASE, context.sessionState());
        assertFalse(context.isDatabaseOpen());
        assertFalse(context.isCompanyOpen());
        assertNull(context.activeDatabaseBasePath());
        assertNull(context.activeCompanyId());
        assertEquals("No database open", context.sessionDisplayLabel());
        assertEquals("No company open", context.activeCompanyDisplayLabel());
    }

    @Test
    void openDatabaseClearsCompanyAndPublishesDatabaseOnlyState()
    {
        UiSessionContext context = new UiSessionContext();
        context.openCompany(12L, "Previous Company", new CompanyMetadata(true, true, true));

        context.openDatabase(Path.of("/tmp/books"));

        assertEquals(Path.of("/tmp/books"), context.activeDatabaseBasePath());
        assertEquals(SessionState.DATABASE_OPEN_NO_COMPANY, context.sessionState());
        assertTrue(context.isDatabaseOpen());
        assertFalse(context.isCompanyOpen());
        assertNull(context.activeCompanyId());
        assertEquals("Database open — no company open", context.sessionDisplayLabel());
        assertFalse(context.isSampleCompany());
        assertFalse(context.isPopulatedCompany());
        assertFalse(context.isNewlyCreatedCompany());
    }

    @Test
    void openCompanyPublishesCompanyStateAndMetadata()
    {
        UiSessionContext context = new UiSessionContext();
        context.openDatabase(Path.of("/tmp/books"));

        context.openCompany(42L, "Example Company", new CompanyMetadata(true, true, false));

        assertEquals(SessionState.COMPANY_OPEN, context.sessionState());
        assertTrue(context.isDatabaseOpen());
        assertTrue(context.isCompanyOpen());
        assertEquals(42L, context.activeCompanyId());
        assertEquals("Example Company", context.activeCompanyDisplayLabel());
        assertEquals("Example Company", context.sessionDisplayLabel());
        assertTrue(context.isSampleCompany());
        assertTrue(context.isPopulatedCompany());
        assertFalse(context.isNewlyCreatedCompany());
    }

    @Test
    void clearCompanyKeepsDatabaseOpenAndResetsMetadata()
    {
        UiSessionContext context = new UiSessionContext();
        context.openDatabase(Path.of("/tmp/books"));
        context.openCompany(42L, "Example Company", new CompanyMetadata(true, true, true));

        context.clearCompany();

        assertEquals(SessionState.DATABASE_OPEN_NO_COMPANY, context.sessionState());
        assertTrue(context.isDatabaseOpen());
        assertFalse(context.isCompanyOpen());
        assertNull(context.activeCompanyId());
        assertEquals("No company open", context.activeCompanyDisplayLabel());
        assertEquals("Database open — no company open", context.sessionDisplayLabel());
        assertFalse(context.isSampleCompany());
        assertFalse(context.isPopulatedCompany());
        assertFalse(context.isNewlyCreatedCompany());
    }

    @Test
    void clearDatabaseClearsAllActiveState()
    {
        UiSessionContext context = new UiSessionContext();
        context.openDatabase(Path.of("/tmp/books"));
        context.openCompany(42L, "Example Company", new CompanyMetadata(false, true, false));

        context.clearDatabase();

        assertEquals(SessionState.NO_DATABASE, context.sessionState());
        assertFalse(context.isDatabaseOpen());
        assertFalse(context.isCompanyOpen());
        assertNull(context.activeDatabaseBasePath());
        assertNull(context.activeCompanyId());
        assertEquals("No database open", context.sessionDisplayLabel());
        assertEquals("No company open", context.activeCompanyDisplayLabel());
        assertFalse(context.isPopulatedCompany());
    }
}
