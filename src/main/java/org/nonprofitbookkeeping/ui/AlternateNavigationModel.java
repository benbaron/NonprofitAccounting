package org.nonprofitbookkeeping.ui;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Defines parent/subpanel relationships used by alternate-shell navigation. */
final class AlternateNavigationModel
{
    private static final Map<AppPanelId, List<AppPanelId>> HIERARCHY = Map.of(
        AppPanelId.DASHBOARD, List.of(AppPanelId.CHART_OF_ACCOUNTS, AppPanelId.LEDGER_REGISTER),
        AppPanelId.CHART_OF_ACCOUNTS, List.of(),
        AppPanelId.LEDGER_REGISTER, List.of(),
        AppPanelId.INVENTORY, List.of(),
        AppPanelId.FUNDS, List.of(),
        AppPanelId.DONORS, List.of(),
        AppPanelId.REPORTS_WORKSPACE, List.of(AppPanelId.ASSETS_REGISTER, AppPanelId.BUDGET_VS_ACTUAL, AppPanelId.DEPRECIATION_RUNS),
        AppPanelId.SCHEDULES, List.of(),
        AppPanelId.BUDGET_EDITOR, List.of());

    AppPanelId parentPanelFor(AppPanelId panelId)
    {
        return switch (panelId)
        {
            case CHART_OF_ACCOUNTS, LEDGER_REGISTER, INVENTORY, FUNDS, DONORS, REPORTS_WORKSPACE, SCHEDULES, BUDGET_EDITOR -> panelId;
            case ASSETS_REGISTER, BUDGET_VS_ACTUAL, DEPRECIATION_RUNS -> AppPanelId.REPORTS_WORKSPACE;
            default -> AppPanelId.DASHBOARD;
        };
    }

    List<AppPanelId> subPanelsFor(AppPanelId parent)
    {
        Set<AppPanelId> unique = new LinkedHashSet<>(HIERARCHY.getOrDefault(parent, List.of()));
        return List.copyOf(unique);
    }
}
