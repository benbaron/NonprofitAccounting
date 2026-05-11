package org.nonprofitbookkeeping.ui.routing;

import org.nonprofitbookkeeping.ui.AppPanelId;

import static org.nonprofitbookkeeping.ui.routing.WorkspaceRouteDecision.RouteTarget;

/**
 * Central router for shell workspace decisions.
 */
public class WorkspaceRouter
{
    public WorkspaceRouteDecision decide(AppPanelId id)
    {
        if (isPanelHostRoute(id))
        {
            return new WorkspaceRouteDecision(id, RouteTarget.PANEL_HOST);
        }

        if (id == AppPanelId.DASHBOARD)
        {
            return new WorkspaceRouteDecision(id, RouteTarget.DASHBOARD);
        }

        if (isAlternateCustomPane(id))
        {
            return new WorkspaceRouteDecision(id, RouteTarget.ALTERNATE_CUSTOM_PANE);
        }

        return new WorkspaceRouteDecision(id, RouteTarget.PANEL_HOST);
    }

    private boolean isAlternateCustomPane(AppPanelId id)
    {
        return id == AppPanelId.SETTINGS;
    }

    private boolean isPanelHostRoute(AppPanelId id)
    {
        return switch (id)
        {
            case CHART_OF_ACCOUNTS, LEDGER_REGISTER, REPORTS_WORKSPACE, FUNDS, INVENTORY,
                ASSETS_REGISTER, BUDGET_VS_ACTUAL, DEPRECIATION_RUNS, BUDGET_EDITOR, SCHEDULES, REPORT_LIBRARY -> true;
            default -> false;
        };
    }
}
