package org.nonprofitbookkeeping.ui;

import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

/**
 * Represents the BudgetEditorPanel component in the nonprofit bookkeeping application.
 */
public class BudgetEditorPanel implements AppPanel
{
    private final BorderPane root = new BorderPane();
    private final TableView<BudgetRow> table = new TableView<>();
    private final Label status = new Label("Ready");

    public BudgetEditorPanel()
    {
        root.setPadding(new Insets(8));
        Label title = new Label("Budget Editor");
        title.getStyleClass().add("panel-title");

        Button add = new Button("+ Add Budget Line");
        Button save = new Button("Save");
        HBox actions = new HBox(8, add, save);

        root.setTop(new VBox(6, title, actions, new Separator()));

        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        table.getColumns().add(col("Account", BudgetRow::account));
        table.getColumns().add(col("Fund", BudgetRow::fund));
        table.getColumns().add(col("Period", BudgetRow::period));
        table.getColumns().add(col("Budget Amount", BudgetRow::amount));
        table.getItems().addAll(
            new BudgetRow("Program Supplies", "General", "2026-Q1", "3500.00"),
            new BudgetRow("Office Rent", "General", "2026-Q1", "4800.00")
        );
        root.setCenter(table);
        root.setBottom(new VBox(new Separator(), status));

        add.setOnAction(e -> table.getItems().add(new BudgetRow("", "", "", "0.00")));
        save.setOnAction(e -> onSave());
    }

    private TableColumn<BudgetRow, String> col(String name, java.util.function.Function<BudgetRow, String> getter)
    {
        TableColumn<BudgetRow, String> c = new TableColumn<>(name);
        c.setCellValueFactory(v -> new SimpleStringProperty(getter.apply(v.getValue())));
        return c;
    }

    @Override public String title() { return "Budget Editor"; }
    @Override public Node root() { return root; }

    @Override public void onSave()
    {
        status.setText("Saved " + table.getItems().size() + " budget row(s)");
    }

    public record BudgetRow(String account, String fund, String period, String amount) {}
}
