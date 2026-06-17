package org.nonprofitbookkeeping.ui;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.ComboBoxTableCell;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import nonprofitbookkeeping.ui.FundNameLookup;

import java.sql.SQLException;

/**
 * Represents the BudgetEditorPanel component in the nonprofit bookkeeping application.
 */
public class BudgetEditorPanel implements AppPanel
{
    static final String NO_SERVICE_DATA_MESSAGE = "No service-backed data source is wired for this panel yet.";

    private final BorderPane root = new BorderPane();
    private final TableView<BudgetRow> table = new TableView<>();
    private final Label status = new Label(NO_SERVICE_DATA_MESSAGE);
    private final ObservableList<String> fundChoices = FXCollections.observableArrayList();

    public BudgetEditorPanel()
    {
        root.setPadding(new Insets(8));
        Label title = new Label("Budget Editor");
        title.getStyleClass().add("panel-title");

        Button add = new Button("+ Add Budget Line");
        Button delete = new Button("Delete Selected");
        Button save = new Button("Save");
        Button cancel = new Button("Cancel");
        HBox actions = new HBox(8, add, delete, save, cancel);

        root.setTop(new VBox(6, title, actions, new Separator()));

        refreshFundChoices();
        table.setEditable(true);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        table.getColumns().add(col("Account", BudgetRow::account));
        table.getColumns().add(fundCol());
        table.getColumns().add(col("Period", BudgetRow::period));
        table.getColumns().add(col("Budget Amount", BudgetRow::amount));
        table.setPlaceholder(new Label(NO_SERVICE_DATA_MESSAGE));
        root.setCenter(table);
        root.setBottom(new VBox(new Separator(), status));

        add.setOnAction(e -> {
            refreshFundChoices();
            status.setText(NO_SERVICE_DATA_MESSAGE);
        });
        delete.setOnAction(e -> onDelete());
        save.setOnAction(e -> onSave());
        cancel.setOnAction(e -> onCancel());
    }

    private TableColumn<BudgetRow, String> col(String name, java.util.function.Function<BudgetRow, String> getter)
    {
        TableColumn<BudgetRow, String> c = new TableColumn<>(name);
        c.setCellValueFactory(v -> new SimpleStringProperty(getter.apply(v.getValue())));
        return c;
    }

    private TableColumn<BudgetRow, String> fundCol()
    {
        TableColumn<BudgetRow, String> c = col("Fund", BudgetRow::fund);
        c.setCellFactory(ComboBoxTableCell.forTableColumn(fundChoices));
        c.setOnEditStart(event -> refreshFundChoices());
        c.setOnEditCommit(event -> table.getItems().set(
            event.getTablePosition().getRow(),
            event.getRowValue().withFund(event.getNewValue())));
        return c;
    }

    private void refreshFundChoices()
    {
        try
        {
            fundChoices.setAll(FundNameLookup.listActiveFundNames());
        }
        catch (SQLException ex)
        {
            fundChoices.clear();
            status.setText("Unable to load fund choices: " + ex.getMessage());
        }
    }

    private String defaultFundChoice()
    {
        return fundChoices.isEmpty() ? "" : fundChoices.get(0);
    }

    @Override public String title() { return "Budget Editor"; }
    @Override public Node root() { return root; }

    @Override public void onSave()
    {
        status.setText(NO_SERVICE_DATA_MESSAGE);
    }

    @Override public void onDelete()
    {
        BudgetRow selected = table.getSelectionModel().getSelectedItem();
        if (selected == null)
        {
            status.setText("Select a budget row to delete.");
            return;
        }
        table.getItems().remove(selected);
        status.setText("Deleted selected budget row.");
    }

    @Override public void onCancel()
    {
        table.getSelectionModel().clearSelection();
        status.setText("Cancelled budget edit and cleared the selection.");
    }

    public record BudgetRow(String account, String fund, String period, String amount)
    {
        BudgetRow withFund(String value) { return new BudgetRow(account, value, period, amount); }
    }
}
