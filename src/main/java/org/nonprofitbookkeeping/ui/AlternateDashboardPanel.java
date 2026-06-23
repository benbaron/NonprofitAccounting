package org.nonprofitbookkeeping.ui;

import javafx.application.Platform;
import javafx.scene.Node;
import javafx.stage.Window;
import nonprofitbookkeeping.ui.LedgerNavigationContext;
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
        this(sessionContext, services, null);
    }

    public AlternateDashboardPanel(UiSessionContext sessionContext,
        UiServiceProvider services, DashboardNavigation navigation)
    {
        this.dashboard = new SharedDashboardPanelFX(
            navigation == null ? defaultNavigation() : navigation);
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

    private DashboardNavigation defaultNavigation()
    {
        return new DashboardNavigation()
        {
            @Override
            public void openCashAndBank()
            {
                open(AppPanelId.LEDGER_REGISTER);
            }

            @Override
            public void openLedger()
            {
                open(AppPanelId.LEDGER_REGISTER);
            }

            @Override
            public void openFunds()
            {
                open(AppPanelId.FUNDS);
            }

            @Override
            public void openReports()
            {
                open(AppPanelId.REPORTS_WORKSPACE);
            }

            @Override
            public void openReconciliation()
            {
                open(AppPanelId.RECONCILIATION);
            }

            @Override
            public void openUndepositedFunds()
            {
                Platform.runLater(() -> {
                    MainWindowAlternate shell = findShell();
                    if (shell != null)
                    {
                        shell.openUndepositedFundsDirect();
                    }
                });
            }

            @Override
            public void openTransactionInLedger(int transactionId)
            {
                LedgerNavigationContext.requestTransaction(transactionId);
                open(AppPanelId.LEDGER_REGISTER);
            }

            private void open(AppPanelId panelId)
            {
                Platform.runLater(() -> {
                    MainWindowAlternate shell = findShell();
                    if (shell != null)
                    {
                        shell.openPanel(panelId);
                    }
                });
            }
        };
    }

    private MainWindowAlternate findShell()
    {
        for (Window window : Window.getWindows())
        {
            if (window.isShowing() && window.getScene() != null &&
                window.getScene().getRoot() instanceof MainWindowAlternate shell)
            {
                return shell;
            }
        }
        return null;
    }
}
