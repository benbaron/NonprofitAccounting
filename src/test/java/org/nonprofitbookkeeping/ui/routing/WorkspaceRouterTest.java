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
    void settingsRemainsAlternateCustomPaneAndReportsIsPanelHost()
    {
        assertTrue(router.decide(AppPanelId.SETTINGS).isAlternateCustomPane());
        assertTrue(router.decide(AppPanelId.REPORTS_WORKSPACE).isAlternateCustomPane());
    }
}
