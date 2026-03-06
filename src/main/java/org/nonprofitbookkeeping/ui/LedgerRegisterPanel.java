package org.nonprofitbookkeeping.ui;

import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

/**
 * Represents the LedgerRegisterPanel component in the nonprofit bookkeeping application.
 */
public class LedgerRegisterPanel implements AppPanel
{
    private final BorderPane root = new BorderPane();
    private final TableView<Row> txnTable = new TableView<>();

    public LedgerRegisterPanel()
    {
        root.setPadding(new Insets(8));

        Label title = new Label("Ledger Register");
        Label range = new Label();
        range.textProperty().bind(Bindings.createStringBinding(() -> "Date Range: " + DateRangeContext.get(),
            DateRangeContext.selectedProperty()));
        title.getStyleClass().add("panel-title");

        Button newTxn = new Button("+ New Transaction");
        Button edit = new Button("Edit Selected");
        HBox actions = new HBox(8, newTxn, edit);

        VBox header = new VBox(6, title, range, actions, new Separator());
        root.setTop(header);

        buildTable();
        root.setCenter(txnTable);

        newTxn.setOnAction(e -> onNew());
        edit.setOnAction(e -> openSelected());

        txnTable.setRowFactory(tv -> {
            TableRow<Row> r = new TableRow<>();
            r.setOnMouseClicked(e -> {
                if (r.isEmpty())
                {
                    return;
                }
                if (e.getClickCount() == 2 && e.getButton() == javafx.scene.input.MouseButton.PRIMARY)
                {
                    txnTable.getSelectionModel().select(r.getIndex());
                    openRow(r.getItem());
                    return;
                }
                if (e.getButton() == javafx.scene.input.MouseButton.SECONDARY)
                {
                    ContextMenu cm = new ContextMenu();
                    MenuItem details = new MenuItem("Show Details");
                    details.setOnAction(ev -> showDetails(r.getItem()));
                    MenuItem editItem = new MenuItem("Edit Transaction");
                    editItem.setOnAction(ev -> openRow(r.getItem()));
                    cm.getItems().addAll(editItem, details);
                    r.setContextMenu(cm);
                }
            });
            return r;
        });

        txnTable.getItems().addAll(new Row("2026-01-05", "Payee A", "Memo A", "Cash/Bank", "Posted"),
            new Row("2026-01-12", "Payee B", "Memo B", "Cash/Bank", "Posted"));
    }

    private void buildTable()
    {
        txnTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        txnTable.getColumns().add(col("Date", Row::date));
        txnTable.getColumns().add(col("Payee", Row::payee));
        txnTable.getColumns().add(col("Memo", Row::memo));
        txnTable.getColumns().add(col("Bank", Row::bank));
        txnTable.getColumns().add(col("Status", Row::status));
    }

    private TableColumn<Row, String> col(String name, java.util.function.Function<Row, String> getter)
    {
        TableColumn<Row, String> c = new TableColumn<>(name);
        c.setCellValueFactory(v -> new SimpleStringProperty(getter.apply(v.getValue())));
        return c;
    }

    private void openSelected()
    {
        Row sel = txnTable.getSelectionModel().getSelectedItem();
        if (sel != null)
        {
            openRow(sel);
        }
    }

    private void openRow(Row row)
    {
        TransactionDraftContext.setSelectedRow(row);
        TransactionEditorPanel editor = new TransactionEditorPanel(row, null);
        editor.showAsDialog(root.getScene() == null ? null : root.getScene().getWindow(),
            row.date().isBlank() ? "New Transaction" : "Edit Transaction");
    }

    private void showDetails(Row row)
    {
        String details = "Date: " + row.date() + "\n" + "Payee: " + row.payee() + "\n" + "Memo: " + row.memo()
            + "\n" + "Bank: " + row.bank() + "\n" + "Status: " + row.status();
        Alert a = new Alert(Alert.AlertType.INFORMATION, details);
        a.setHeaderText("Transaction Details");
        a.showAndWait();
    }

    @Override
    public String title()
    {
        return "Ledger Register";
    }

    @Override
    public Node root()
    {
        return root;
    }

    @Override
    public void onNew()
    {
        openRow(new Row("", "", "", "", "Draft"));
    }

    public record Row(String date, String payee, String memo, String bank, String status)
    {
    }
}
