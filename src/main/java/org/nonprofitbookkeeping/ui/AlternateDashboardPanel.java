package org.nonprofitbookkeeping.ui;

import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.stage.Window;
import nonprofitbookkeeping.service.UndepositedFundsService;
import nonprofitbookkeeping.ui.LedgerNavigationContext;
import nonprofitbookkeeping.ui.panels.DashboardNavigation;
import nonprofitbookkeeping.ui.panels.SharedDashboardPanelFX;
import nonprofitbookkeeping.ui.panels.UndepositedFundsPanelFX;

/** Panel-host adapter for the shared dashboard surface. */
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
                Platform.runLater(
                    AlternateDashboardPanel.this::showUndepositedFunds);
            }

            @Override
            public void openTransactionInLedger(int transactionId)
            {
                LedgerNavigationContext.requestTransaction(transactionId);
                open(AppPanelId.LEDGER_REGISTER);
            }

            private void open(AppPanelId panelId)
            {
                Platform.runLater(() -> openInOwningShell(panelId));
            }
        };
    }

    private void openInOwningShell(AppPanelId panelId)
    {
        for (Window window : Window.getWindows())
        {
            if (!window.isShowing() || window.getScene() == null)
            {
                continue;
            }
            if (window.getScene().getRoot() instanceof MainWindowAlternate shell)
            {
                shell.openPanel(panelId);
                return;
            }
            if (window.getScene().getRoot() instanceof MainWindow shell)
            {
                shell.openPanel(panelId);
                return;
            }
        }
    }

    private void showUndepositedFunds()
    {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Undeposited Funds");
        dialog.getDialogPane().setContent(new UndepositedFundsPanelFX(
            new UndepositedFundsService()));
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.getDialogPane().setPrefSize(1000, 700);
        dialog.setResizable(true);
        if (this.dashboard.getScene() != null &&
            this.dashboard.getScene().getWindow() != null)
        {
            dialog.initOwner(this.dashboard.getScene().getWindow());
        }
        dialog.showAndWait();
    }
}
