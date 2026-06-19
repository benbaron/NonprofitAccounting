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
        if (id == AppPanelId.DASHBOARD)
        {
            return new WorkspaceRouteDecision(id, RouteTarget.DASHBOARD);
        }

        if (isAlternateCustomPane(id))
        {
            return new WorkspaceRouteDecision(id, RouteTarget.ALTERNATE_CUSTOM_PANE);
        }

        if (isPanelHostRoute(id))
        {
            return new WorkspaceRouteDecision(id, RouteTarget.PANEL_HOST);
        }

        return new WorkspaceRouteDecision(id, RouteTarget.PANEL_HOST);
    }

    private boolean isAlternateCustomPane(AppPanelId id)
    {
        return switch (id)
        {
            case SETTINGS, BUDGET_EDITOR, SCHEDULES, COMPANY_ADMIN, IMPORT_EXPORT -> true;
            default -> false;
        };
    }

    private boolean isPanelHostRoute(AppPanelId id)
    {
        return switch (id)
        {
            case CHART_OF_ACCOUNTS, LEDGER_REGISTER, REPORTS_WORKSPACE, FUNDS, INVENTORY,
                ASSETS_REGISTER, BUDGET_VS_ACTUAL, DEPRECIATION_RUNS, REPORT_LIBRARY, DATABASE_ADMIN -> true;
            default -> false;
        };
    }

}
