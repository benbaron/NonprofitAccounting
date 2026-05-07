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

        return new WorkspaceRouteDecision(id, RouteTarget.PANEL_HOST);
    }

    private boolean isAlternateCustomPane(AppPanelId id)
    {
        return id == AppPanelId.SETTINGS;
    }
}
