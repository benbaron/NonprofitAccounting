package nonprofitbookkeeping.ui.panels;

import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.stage.Window;
import nonprofitbookkeeping.service.UndepositedFundsService;
import nonprofitbookkeeping.ui.LedgerNavigationContext;
import nonprofitbookkeeping.ui.MainApplicationView;
import org.nonprofitbookkeeping.ui.FundsPanel;

/** Navigation adapter for the tabbed classic shell dashboard. */
public class ClassicDashboardNavigation implements DashboardNavigation
{
    @Override
    public void openCashAndBank()
    {
        showPanel(MainApplicationView.PanelType.LEDGER);
    }

    @Override
    public void openLedger()
    {
        showPanel(MainApplicationView.PanelType.LEDGER);
    }

    @Override
    public void openFunds()
    {
        Platform.runLater(() -> showDialog("Funds", new FundsPanel().root()));
    }

    @Override
    public void openReports()
    {
        showPanel(MainApplicationView.PanelType.REPORTS);
    }

    @Override
    public void openReconciliation()
    {
        showPanel(MainApplicationView.PanelType.BANK_RECONCILIATION);
    }

    @Override
    public void openUndepositedFunds()
    {
        Platform.runLater(() -> showDialog("Undeposited Funds",
            new UndepositedFundsPanelFX(new UndepositedFundsService())));
    }

    @Override
    public void openTransactionInLedger(int transactionId)
    {
        LedgerNavigationContext.requestTransaction(transactionId);
        showPanel(MainApplicationView.PanelType.LEDGER);
    }

    private void showPanel(MainApplicationView.PanelType panelType)
    {
        Platform.runLater(() -> {
            MainApplicationView view = findMainApplicationView();
            if (view != null)
            {
                view.showPanel(panelType);
            }
        });
    }

    private MainApplicationView findMainApplicationView()
    {
        for (Window window : Window.getWindows())
        {
            if (window.isShowing() && window.getScene() != null &&
                window.getScene().getRoot() instanceof MainApplicationView view)
            {
                return view;
            }
        }
        return null;
    }

    private void showDialog(String title, Node content)
    {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle(title);
        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.getDialogPane().setPrefSize(1000, 700);
        dialog.setResizable(true);
        MainApplicationView view = findMainApplicationView();
        if (view != null && view.getScene() != null)
        {
            dialog.initOwner(view.getScene().getWindow());
        }
        dialog.showAndWait();
    }
}
