package org.nonprofitbookkeeping.ui.routing;

import org.nonprofitbookkeeping.ui.AppPanelId;

/**
 * Route target decision for a requested workspace panel id.
 */
public record WorkspaceRouteDecision(AppPanelId panelId, RouteTarget target)
{
    public enum RouteTarget
    {
        DASHBOARD,
        ALTERNATE_CUSTOM_PANE,
        PANEL_HOST
    }

    public boolean isDashboard()
    {
        return this.target == RouteTarget.DASHBOARD;
    }

    public boolean isAlternateCustomPane()
    {
        return this.target == RouteTarget.ALTERNATE_CUSTOM_PANE;
    }

    public boolean isPanelHost()
    {
        return this.target == RouteTarget.PANEL_HOST;
    }
}
