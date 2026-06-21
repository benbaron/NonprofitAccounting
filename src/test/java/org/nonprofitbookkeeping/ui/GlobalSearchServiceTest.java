package org.nonprofitbookkeeping.ui;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

class GlobalSearchServiceTest
{
    @Test
    void noDatabaseReturnsAvailableResultsWithoutFailingMissingDomains()
    {
        UiSessionContext context = new UiSessionContext();
        try (UiServiceProvider provider = new UiServiceProvider(context))
        {
            List<GlobalSearchResult> results = assertDoesNotThrow(() -> provider.globalSearch().search("Balance"));

            assertTrue(results.stream().allMatch(result -> result.type() == SearchResultType.REPORT),
                "database-scoped domains should be skipped when no database is open");
            assertFalse(provider.hasDatabaseServicesForCurrentContext(),
                "global search must not eagerly create database services without an open database");
        }
    }

    @Test
    void blankQueryReturnsNoResults()
    {
        UiSessionContext context = new UiSessionContext();
        try (UiServiceProvider provider = new UiServiceProvider(context))
        {
            assertEquals(List.of(), provider.globalSearch().search("   "));
        }
    }

    @Test
    void resultMappingCarriesRouteAndHonestPlaceholderAction()
    {
        GlobalSearchResult result = new GlobalSearchResult(SearchResultType.ACCOUNT, "1000 • Cash",
            "Posting account • ASSET", AppPanelId.CHART_OF_ACCOUNTS,
            "Account drilldown is not implemented yet; opening Chart of Accounts.");

        assertEquals(SearchResultType.ACCOUNT, result.type());
        assertEquals("1000 • Cash", result.title());
        assertEquals("Posting account • ASSET", result.subtitle());
        assertEquals(AppPanelId.CHART_OF_ACCOUNTS, result.targetPanelId());
        assertTrue(result.hasRoute());
        assertTrue(result.targetDescription().contains("not implemented yet"));
    }
}
