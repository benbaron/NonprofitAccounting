package org.nonprofitbookkeeping.ui;

import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;

/**
 * Represents the LedgerRegisterPanel component in the nonprofit bookkeeping application.
 */
public class LedgerRegisterPanel implements AppPanel
{
    static final String NO_SERVICE_DATA_MESSAGE = "No service-backed data source is wired for this panel yet.";

    private final AlternatePanelScaffold root = new AlternatePanelScaffold("Ledger Register");
    private final TableView<Row> txnTable = new TableView<>();
    private final Label status = new Label(NO_SERVICE_DATA_MESSAGE);

    public LedgerRegisterPanel()
    {
        Label range = new Label();
        range.textProperty().bind(Bindings.createStringBinding(() -> "Date Range: " + DateRangeContext.get(),
            DateRangeContext.selectedProperty()));
        range.getStyleClass().add("alternate-panel-subtitle");

        Button refresh = new Button("Refresh");
        HBox filterBar = new HBox(8, range);
        root.setSubtitle("Read-only ledger view for the selected date range.");
        root.setSecondaryActions(java.util.List.of(refresh));
        root.setFilterBar(filterBar);
        root.setFooter(status);

        buildTable();
        txnTable.setPlaceholder(new Label(NO_SERVICE_DATA_MESSAGE));
        root.setContent(txnTable);
        root.showEmpty(NO_SERVICE_DATA_MESSAGE);

        txnTable.setRowFactory(tv -> {
            TableRow<Row> r = new TableRow<>();
            r.setOnMouseClicked(e -> {
                if (r.isEmpty())
                {
                    return;
                }
                if (e.getButton() == javafx.scene.input.MouseButton.SECONDARY)
                {
                    ContextMenu cm = new ContextMenu();
                    MenuItem details = new MenuItem("Show Details");
                    details.setOnAction(ev -> showDetails(r.getItem()));
                    cm.getItems().add(details);
                    r.setContextMenu(cm);
                }
            });
            return r;
        });

        refresh.setOnAction(e -> {
            status.setText(NO_SERVICE_DATA_MESSAGE);
            root.showEmpty(NO_SERVICE_DATA_MESSAGE);
        });
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
        // Read-only register: no create action.
    }

    public record Row(String date, String payee, String memo, String bank, String status)
    {
    }
}
