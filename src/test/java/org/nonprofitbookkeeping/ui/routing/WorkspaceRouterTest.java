package org.nonprofitbookkeeping.ui.routing;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.nonprofitbookkeeping.ui.AppPanelId;

class WorkspaceRouterTest
{
    private final WorkspaceRouter router = new WorkspaceRouter();

    @Test
    void chartOfAccountsLedgerAndInventoryNowRouteThroughPanelHost()
    {
        assertTrue(router.decide(AppPanelId.CHART_OF_ACCOUNTS).isPanelHost());
        assertTrue(router.decide(AppPanelId.LEDGER_REGISTER).isPanelHost());
        assertTrue(router.decide(AppPanelId.INVENTORY).isPanelHost());
    }

    @Test
    void dashboardRemainsNativeShellRoute()
    {
        assertTrue(router.decide(AppPanelId.DASHBOARD).isDashboard());
    }

    @Test
    void settingsAndReportsRemainAlternateCustomPanes()
    {
        assertTrue(router.decide(AppPanelId.SETTINGS).isAlternateCustomPane());
        assertTrue(router.decide(AppPanelId.BUDGET_EDITOR).isAlternateCustomPane());
        assertTrue(router.decide(AppPanelId.SCHEDULES).isAlternateCustomPane());
        assertTrue(router.decide(AppPanelId.REPORTS_WORKSPACE).isPanelHost());
        assertTrue(router.decide(AppPanelId.REPORT_LIBRARY).isPanelHost());
        assertTrue(router.decide(AppPanelId.ASSETS_REGISTER).isPanelHost());
        assertTrue(router.decide(AppPanelId.BUDGET_VS_ACTUAL).isPanelHost());
        assertTrue(router.decide(AppPanelId.DEPRECIATION_RUNS).isPanelHost());
        assertTrue(router.decide(AppPanelId.FUNDS).isPanelHost());
    }
}
