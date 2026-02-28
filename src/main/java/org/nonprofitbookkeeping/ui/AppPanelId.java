package org.nonprofitbookkeeping.ui;

/** Stable identifiers for workspace panels. */
public enum AppPanelId
{
    DASHBOARD,

    LEDGER_REGISTER,
    TXN_EDITOR,

    SCHEDULES,
    INVENTORY,

    BUDGET_EDITOR,
    BUDGET_VS_ACTUAL,

    ASSETS_REGISTER,
    DEPRECIATION_RUNS,

    /**
     * @deprecated Use REPORTS_WORKSPACE. Kept as a temporary compatibility alias for cross-branch merges.
     */
    @Deprecated
    REPORT_LIBRARY,
    REPORTS_WORKSPACE,

    CHART_OF_ACCOUNTS,
    FUNDS,
    SETTINGS
}
