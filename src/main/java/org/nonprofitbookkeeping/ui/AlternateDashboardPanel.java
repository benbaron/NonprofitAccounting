package org.nonprofitbookkeeping.ui;

import javafx.scene.Node;
import nonprofitbookkeeping.ui.panels.DashboardNavigation;
import nonprofitbookkeeping.ui.panels.SharedDashboardPanelFX;

/** New-shell adapter for the shared dashboard surface. */
public class AlternateDashboardPanel implements AppPanel
{
    static final String EMPTY_STATE = AlternateDashboardModel.EMPTY_STATE;
    static final String NOT_WIRED_STATE = AlternateDashboardModel.NOT_WIRED_STATE;

    private final SharedDashboardPanelFX dashboard;

    public AlternateDashboardPanel(UiSessionContext sessionContext,
        UiServiceProvider services)
    {
        this(sessionContext, services, new DashboardNavigation() { });
    }

    public AlternateDashboardPanel(UiSessionContext sessionContext,
        UiServiceProvider services, DashboardNavigation navigation)
    {
        this.dashboard = new SharedDashboardPanelFX(navigation);
    }

    @Override
    public String title()
    {
        return "Dashboard";
    }

    @Override
    public Node root()
    {
        return this.dashboard;
    }

    void refresh()
    {
        this.dashboard.reloadData();
    }
}
