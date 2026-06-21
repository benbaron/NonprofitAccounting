package org.nonprofitbookkeeping.ui;

import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import nonprofitbookkeeping.ui.LedgerRegisterPanel;
import nonprofitbookkeeping.ui.panels.JournalPanelFX;

/**
 * Panel-host wrapper for the grouped general-journal display.
 *
 * <p>The journal itself remains transaction-oriented. Transaction-ID links
 * open the ledger register focused on the matching persisted transaction.</p>
 */
public final class JournalWorkspacePanel implements AppPanel
{
    private final JournalPanelFX journalPanel;

    public JournalWorkspacePanel()
    {
        this.journalPanel = new JournalPanelFX(this::openLedgerTransaction);
    }

    @Override
    public String title()
    {
        return "Journal";
    }

    @Override
    public Node root()
    {
        return this.journalPanel;
    }

    @Override
    public void onNew()
    {
        // JournalPanelFX supplies its own New/Edit/Delete toolbar.
    }

    private void openLedgerTransaction(int transactionId)
    {
        LedgerRegisterPanel ledger = new LedgerRegisterPanel();
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Ledger Register — Transaction " + transactionId);
        dialog.getDialogPane().setContent(ledger.root());
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.getDialogPane().setPrefSize(1100, 720);
        dialog.setResizable(true);
        if (this.journalPanel.getScene() != null &&
            this.journalPanel.getScene().getWindow() != null)
        {
            dialog.initOwner(this.journalPanel.getScene().getWindow());
        }
        dialog.setOnShown(event ->
            Platform.runLater(() -> ledger.selectTransactionById(transactionId)));
        dialog.showAndWait();
    }
}
