package org.nonprofitbookkeeping.ui;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.nonprofitbookkeeping.service.AccountLookupService;

class UiServiceProviderTest
{
    @TempDir
    Path tempDir;

    @Test
    void noDatabaseDefersJpaAndRejectsDatabaseScopedServices()
    {
        UiSessionContext context = new UiSessionContext();
        try (UiServiceProvider provider = new UiServiceProvider(context))
        {
            assertFalse(provider.hasDatabaseServicesForCurrentContext());
            assertThrows(IllegalStateException.class, provider::accountLookup);
            assertNotNull(provider.databaseAdministration());
            assertNotNull(provider.importExport());
        }
    }

    @Test
    void databaseOpenCreatesServicesOnDemand() throws Exception
    {
        UiSessionContext context = new UiSessionContext();
        try (UiServiceProvider provider = new UiServiceProvider(context))
        {
            Path dbBase = this.tempDir.resolve("provider-open");
            provider.databaseAdministration().openDatabase(dbBase);

            assertFalse(provider.hasDatabaseServicesForCurrentContext(),
                "opening a database should not eagerly create JPA-backed services");
            assertNotNull(provider.accountLookup());
            assertNotNull(provider.fundLookup());
            assertNotNull(provider.fundBalance());
            assertNotNull(provider.scheduleEligibility());
            assertNotNull(provider.dashboardData());
            assertTrue(provider.hasDatabaseServicesForCurrentContext());
        }
    }

    @Test
    void companyOpenKeepsDatabaseScopedServicesAvailable() throws Exception
    {
        AlternateDataContextService contextService = new AlternateDataContextService();
        UiSessionContext context = contextService.sessionContext();
        try (UiServiceProvider provider = new UiServiceProvider(contextService))
        {
            provider.databaseAdministration().openDatabase(this.tempDir.resolve("provider-company"));
            context.openCompany(7L, "Company Seven");

            assertTrue(context.isCompanyOpen());
            assertNotNull(provider.accountLookup());
            assertNotNull(provider.companyAdministration());
        }
    }

    @Test
    void databaseChangeInvalidatesAndRebuildsServices() throws Exception
    {
        UiSessionContext context = new UiSessionContext();
        try (UiServiceProvider provider = new UiServiceProvider(context))
        {
            provider.databaseAdministration().openDatabase(this.tempDir.resolve("provider-first"));
            AccountLookupService first = provider.accountLookup();
            assertTrue(provider.hasDatabaseServicesForCurrentContext());

            provider.databaseAdministration().openDatabase(this.tempDir.resolve("provider-second"));

            assertFalse(provider.hasDatabaseServicesForCurrentContext(),
                "database path changes should invalidate existing services before next use");
            AccountLookupService second = provider.accountLookup();
            assertNotSame(first, second);
            assertTrue(provider.hasDatabaseServicesForCurrentContext());
        }
    }
}
