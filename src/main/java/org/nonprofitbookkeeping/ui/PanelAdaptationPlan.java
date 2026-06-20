package org.nonprofitbookkeeping.ui;

import java.util.Map;

/**
 * Declares which panels should be adapted from legacy implementations versus
 * rebuilt natively in the alternate shell.
 */
public final class PanelAdaptationPlan
{
    public enum Strategy
    {
        ADAPT_LEGACY,
        BUILD_NATIVE
    }
    public enum Phase
    {
        PHASE_1,
        PHASE_2,
        PHASE_3
    }

    private static final Map<AppPanelId, Strategy> STRATEGIES = Map.ofEntries(
        Map.entry(AppPanelId.CHART_OF_ACCOUNTS, Strategy.ADAPT_LEGACY),
        Map.entry(AppPanelId.LEDGER_REGISTER, Strategy.ADAPT_LEGACY),
        Map.entry(AppPanelId.REPORTS_WORKSPACE, Strategy.BUILD_NATIVE),
        Map.entry(AppPanelId.FUNDS, Strategy.ADAPT_LEGACY),
        Map.entry(AppPanelId.INVENTORY, Strategy.ADAPT_LEGACY),
        Map.entry(AppPanelId.ASSETS_REGISTER, Strategy.ADAPT_LEGACY),
        Map.entry(AppPanelId.BUDGET_VS_ACTUAL, Strategy.ADAPT_LEGACY),
        Map.entry(AppPanelId.DEPRECIATION_RUNS, Strategy.ADAPT_LEGACY),
        Map.entry(AppPanelId.BUDGET_EDITOR, Strategy.BUILD_NATIVE),
        Map.entry(AppPanelId.SCHEDULES, Strategy.BUILD_NATIVE),
        Map.entry(AppPanelId.DASHBOARD, Strategy.BUILD_NATIVE),
        Map.entry(AppPanelId.SETTINGS, Strategy.BUILD_NATIVE));

    private PanelAdaptationPlan() {}

    public static Strategy strategyFor(AppPanelId panelId)
    {
        return STRATEGIES.getOrDefault(panelId, Strategy.ADAPT_LEGACY);
    }

    public static Phase phaseFor(AppPanelId panelId)
    {
        return switch (panelId)
        {
            case DASHBOARD, SETTINGS, CHART_OF_ACCOUNTS, LEDGER_REGISTER -> Phase.PHASE_1;
            case REPORTS_WORKSPACE, FUNDS, INVENTORY, ASSETS_REGISTER, BUDGET_VS_ACTUAL, DEPRECIATION_RUNS -> Phase.PHASE_2;
            default -> Phase.PHASE_3;
        };
    }
}
