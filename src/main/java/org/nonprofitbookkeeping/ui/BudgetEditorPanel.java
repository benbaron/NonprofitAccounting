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
public class BudgetEditorPanel implements AppPanel, AppPanel.SaveAware
{
    static final String NO_SERVICE_DATA_MESSAGE = "No service-backed data source is wired for this panel yet.";

    private final BorderPane root = new BorderPane();
    private final TableView<BudgetRow> table = new TableView<>();
    private final Label status = new Label(NO_SERVICE_DATA_MESSAGE);
    private final ObservableList<String> fundChoices = FXCollections.observableArrayList();

    public BudgetEditorPanel()
    {
        this.root.setPadding(new Insets(8));
        Label title = new Label("Budget Editor");
        title.getStyleClass().add("panel-title");

        Button add = new Button("+ Add Budget Line");
        Button delete = new Button("Delete Selected");
        Button save = new Button("Save");
        Button cancel = new Button("Cancel");
        HBox actions = new HBox(8, add, delete, save, cancel);

        this.root.setTop(new VBox(6, title, actions, new Separator()));

        refreshFundChoices();
        this.table.setEditable(true);
        this.table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        this.table.getColumns().add(col("Account", BudgetRow::account));
        this.table.getColumns().add(fundCol());
        this.table.getColumns().add(col("Period", BudgetRow::period));
        this.table.getColumns().add(col("Budget Amount", BudgetRow::amount));
        this.table.setPlaceholder(new Label(NO_SERVICE_DATA_MESSAGE));
        this.root.setCenter(this.table);
        this.root.setBottom(new VBox(new Separator(), this.status));

        add.setOnAction(e -> {
            refreshFundChoices();
            this.status.setText(NO_SERVICE_DATA_MESSAGE);
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
        c.setCellFactory(ComboBoxTableCell.forTableColumn(this.fundChoices));
        c.setOnEditStart(event -> refreshFundChoices());
        c.setOnEditCommit(event -> this.table.getItems().set(
            event.getTablePosition().getRow(),
            event.getRowValue().withFund(event.getNewValue())));
        return c;
    }

    private void refreshFundChoices()
    {
        try
        {
            this.fundChoices.setAll(FundNameLookup.listActiveFundNames());
        }
        catch (SQLException ex)
        {
            this.fundChoices.clear();
            this.status.setText("Unable to load fund choices: " + ex.getMessage());
        }
    }

    private String defaultFundChoice()
    {
        return this.fundChoices.isEmpty() ? "" : this.fundChoices.get(0);
    }

    @Override public String title() { return "Budget Editor"; }
    @Override public Node root() { return this.root; }

    @Override public void onSave()
    {
        this.status.setText(NO_SERVICE_DATA_MESSAGE);
    }

    @Override public void onDelete()
    {
        BudgetRow selected = this.table.getSelectionModel().getSelectedItem();
        if (selected == null)
        {
            this.status.setText("Select a budget row to delete.");
            return;
        }
        this.table.getItems().remove(selected);
        this.status.setText("Deleted selected budget row.");
    }

    @Override public void onCancel()
    {
        this.table.getSelectionModel().clearSelection();
        this.status.setText("Cancelled budget edit and cleared the selection.");
    }

    public record BudgetRow(String account, String fund, String period, String amount)
    {
        BudgetRow withFund(String value) { return new BudgetRow(this.account, value, this.period, this.amount); }
    }

    @Override
    public SaveResult save()
    {
        return SaveResult.unsupported("Save is not supported because this panel has no persistence workflow yet.");
    }
}
