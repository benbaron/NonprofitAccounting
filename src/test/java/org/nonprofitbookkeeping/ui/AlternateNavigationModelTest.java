package org.nonprofitbookkeeping.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.Test;

class AlternateNavigationModelTest
{
    @Test
    void dashboardExposesPhaseOneCoaAndJournalChildren()
    {
        AlternateNavigationModel model = new AlternateNavigationModel();
        assertEquals(List.of(AppPanelId.CHART_OF_ACCOUNTS, AppPanelId.LEDGER_REGISTER),
            model.subPanelsFor(AppPanelId.DASHBOARD));
    }

    @Test
    void coaAndLedgerRemainTopLevelParentsInPhaseOne()
    {
        AlternateNavigationModel model = new AlternateNavigationModel();
        assertEquals(AppPanelId.CHART_OF_ACCOUNTS, model.parentPanelFor(AppPanelId.CHART_OF_ACCOUNTS));
        assertEquals(AppPanelId.LEDGER_REGISTER, model.parentPanelFor(AppPanelId.LEDGER_REGISTER));
    }

    @Test
    void reportsWorkspaceRemainsParentForImportToolsSubpanels()
    {
        AlternateNavigationModel model = new AlternateNavigationModel();
        assertEquals(AppPanelId.REPORTS_WORKSPACE, model.parentPanelFor(AppPanelId.ASSETS_REGISTER));
        assertEquals(AppPanelId.REPORTS_WORKSPACE, model.parentPanelFor(AppPanelId.BUDGET_VS_ACTUAL));
        assertEquals(AppPanelId.REPORTS_WORKSPACE, model.parentPanelFor(AppPanelId.DEPRECIATION_RUNS));
    }
}
