package org.nonprofitbookkeeping.ui;

import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import nonprofitbookkeeping.ui.panels.JournalEntryWorkspaceFX;

/**
 * Transaction editor route backed by migrated JournalEntryWorkspaceFX.
 */
public class TransactionEditorPanel implements AppPanel
{
    private final BorderPane root = new BorderPane();
    private final Runnable returnToLedger;
    private JournalEntryWorkspaceFX workspace;

    public TransactionEditorPanel(Runnable returnToLedger)
    {
        this.returnToLedger = returnToLedger;

        root.setPadding(new Insets(8));

        Label title = new Label("Transaction Editor");
        title.getStyleClass().add("panel-title");

        Button save = new Button("Save");
        Button post = new Button("Post / Validate");
        Button back = new Button("Return to Ledger");
        HBox actions = new HBox(8, save, post, back);
        root.setTop(new VBox(6, title, actions));

        save.setOnAction(e -> onSave());
        post.setOnAction(e -> onPostValidate());
        back.setOnAction(e -> returnToLedger.run());

        try
        {
            workspace = new JournalEntryWorkspaceFX();
            root.setCenter(workspace);
        }
        catch (IllegalStateException noCompany)
        {
            root.setCenter(new Label("Open a company to create or edit transactions."));
        }
    }

    @Override public String title() { return "Transaction Editor"; }
    @Override public Node root() { return root; }

    @Override
    public void onSave()
    {
        if (workspace != null)
        {
            workspace.getSaveButton().fire();
        }
    }

    @Override
    public void onPostValidate()
    {
        onSave();
    }
}
