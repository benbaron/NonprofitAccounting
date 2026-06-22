package nonprofitbookkeeping.ui;

import javafx.application.Platform;
import javafx.stage.Window;

/** Navigation bridge used by the legacy tabbed shell's grouped journal. */
public final class JournalShellNavigation
{
    private JournalShellNavigation()
    {
    }

    /** Opens the ledger tab and selects the requested transaction. */
    public static void openLedgerTransaction(int transactionId)
    {
        LedgerNavigationContext.requestTransaction(transactionId);
        Platform.runLater(() -> {
            for (Window window : Window.getWindows())
            {
                if (!window.isShowing() || window.getScene() == null)
                {
                    continue;
                }
                if (window.getScene().getRoot() instanceof MainApplicationView view)
                {
                    view.showPanel(MainApplicationView.PanelType.LEDGER);
                    return;
                }
            }
        });
    }
}
