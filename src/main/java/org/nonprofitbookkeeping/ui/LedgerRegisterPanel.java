package org.nonprofitbookkeeping.ui;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.ToolBar;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import nonprofitbookkeeping.model.AccountingTransaction;
import nonprofitbookkeeping.ui.panels.JournalPanelFX;

import java.math.BigDecimal;
import java.util.function.BiConsumer;

/**
 * Ledger register route backed by the migrated JournalPanelFX component.
 */
public class LedgerRegisterPanel implements AppPanel
{
    private final BorderPane root = new BorderPane();
    private final JournalPanelFX journalPanel = new JournalPanelFX();
    private final Runnable openTxnEditor;
    private final BiConsumer<String, String> inspector;
    private TableView<AccountingTransaction> table;

    public LedgerRegisterPanel(Runnable openTxnEditor, BiConsumer<String, String> inspector)
    {
        this.openTxnEditor = openTxnEditor;
        this.inspector = inspector;

        root.setPadding(new Insets(8));

        Label title = new Label("Ledger Register");
        Label range = new Label();
        range.textProperty().bind(Bindings.createStringBinding(() -> "Date Range: " + DateRangeContext.get(), DateRangeContext.selectedProperty()));
        title.getStyleClass().add("panel-title");

        Button newTxn = new Button("+ New Transaction");
        Button open = new Button("Open");
        HBox actions = new HBox(8, newTxn, open);

        VBox header = new VBox(6, title, range, actions, new Separator());
        root.setTop(header);
        root.setCenter(journalPanel);

        newTxn.setOnAction(e -> onNew());
        open.setOnAction(e -> openTxnEditor.run());

        Platform.runLater(this::wireJournalInteractions);
    }

    private void wireJournalInteractions()
    {
        table = findFirst(journalPanel, TableView.class);
        ToolBar toolbar = findFirst(journalPanel, ToolBar.class);

        if (toolbar != null)
        {
            for (Node item : toolbar.getItems())
            {
                if (item instanceof Button button && "New".equals(button.getText()))
                {
                    button.setOnAction(e -> openTxnEditor.run());
                }
            }
        }

        if (table == null)
        {
            return;
        }

        table.setRowFactory(tv -> {
            TableRow<AccountingTransaction> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (row.isEmpty())
                {
                    return;
                }
                AccountingTransaction tx = row.getItem();
                if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 1)
                {
                    inspector.accept("Ledger Selection", inspectorBody(tx));
                }
                if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2)
                {
                    openTxnEditor.run();
                }
            });
            return row;
        });
    }

    private String inspectorBody(AccountingTransaction tx)
    {
        return "txn-summary\n"
            + "- id: " + tx.getId() + "\n"
            + "- date: " + tx.getDate() + "\n"
            + "- memo: " + safe(tx.getMemo()) + "\n\n"
            + "drcr-preview\n"
            + "- debit: " + safe(tx.getDebit()) + "\n"
            + "- credit: " + safe(tx.getCredit()) + "\n\n"
            + "audit-fields\n"
            + "- to/from: " + safe(tx.getToFrom()) + "\n"
            + "- check #: " + safe(tx.getCheckNumber()) + "\n"
            + "- cleared bank: " + safe(tx.getClearBank());
    }

    private static String safe(String text)
    {
        return (text == null || text.isBlank()) ? "—" : text;
    }

    private static String safe(BigDecimal value)
    {
        return value == null ? "0" : value.toPlainString();
    }

    @SuppressWarnings("unchecked")
    private static <T> T findFirst(Node rootNode, Class<T> type)
    {
        if (type.isInstance(rootNode))
        {
            return (T) rootNode;
        }
        if (rootNode instanceof javafx.scene.Parent parent)
        {
            for (Node child : parent.getChildrenUnmodifiable())
            {
                T found = findFirst(child, type);
                if (found != null)
                {
                    return found;
                }
            }
        }
        return null;
    }

    @Override public String title() { return "Ledger Register"; }
    @Override public Node root() { return root; }

    @Override
    public void onNew()
    {
        openTxnEditor.run();
    }
}
