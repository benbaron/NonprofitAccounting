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
        assertTrue(this.router.decide(AppPanelId.CHART_OF_ACCOUNTS).isPanelHost());
        assertTrue(this.router.decide(AppPanelId.LEDGER_REGISTER).isPanelHost());
        assertTrue(this.router.decide(AppPanelId.INVENTORY).isPanelHost());
    }

    @Test
    void dashboardRemainsNativeShellRoute()
    {
        assertTrue(this.router.decide(AppPanelId.DASHBOARD).isDashboard());
    }

    @Test
    void settingsUsesPanelHostAndBudgetSchedulesRemainAlternateCustomPanes()
    {
        assertTrue(this.router.decide(AppPanelId.SETTINGS).isPanelHost());
        assertTrue(this.router.decide(AppPanelId.BUDGET_EDITOR).isAlternateCustomPane());
        assertTrue(this.router.decide(AppPanelId.SCHEDULES).isAlternateCustomPane());
        assertTrue(this.router.decide(AppPanelId.REPORTS_WORKSPACE).isPanelHost());
        assertTrue(this.router.decide(AppPanelId.REPORT_LIBRARY).isPanelHost());
        assertTrue(this.router.decide(AppPanelId.ASSETS_REGISTER).isPanelHost());
        assertTrue(this.router.decide(AppPanelId.BUDGET_VS_ACTUAL).isPanelHost());
        assertTrue(this.router.decide(AppPanelId.DEPRECIATION_RUNS).isPanelHost());
        assertTrue(this.router.decide(AppPanelId.FUNDS).isPanelHost());
        assertTrue(this.router.decide(AppPanelId.DATABASE_ADMIN).isPanelHost());
        assertTrue(this.router.decide(AppPanelId.IMPORT_EXPORT).isPanelHost());
    }
}
