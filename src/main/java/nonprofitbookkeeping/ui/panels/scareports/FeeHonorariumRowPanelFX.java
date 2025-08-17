package nonprofitbookkeeping.ui.panels.scareports;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import javafx.collections.FXCollections;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import nonprofitbookkeeping.reports.datasource.scareports.FeeHonorariumRow;

/**
 * Simple editor for {@link FeeHonorariumRow} entries. Allows users to type a
 * row's fields in a small form and add them to a table. Rows can also be
 * removed. The collected list is later consumed when generating the related
 * report.
 */
public class FeeHonorariumRowPanelFX extends VBox {

    private final TextField codeField = new TextField();
    private final TextField organizationField = new TextField();
    private final TextField serviceField = new TextField();
    private final TextField amountField = new TextField();

    private final TableView<FeeHonorariumRow> table =
            new TableView<>(FXCollections.observableArrayList());

    public FeeHonorariumRowPanelFX() {
        setSpacing(10);
        GridPane form = new GridPane();
        form.setHgap(5);
        form.setVgap(5);

        int row = 0;
        form.add(new Label("Code:"), 0, row);
        form.add(codeField, 1, row++);
        form.add(new Label("Organization / Person:"), 0, row);
        form.add(organizationField, 1, row++);
        form.add(new Label("Service Provided:"), 0, row);
        form.add(serviceField, 1, row++);
        form.add(new Label("Amount:"), 0, row);
        form.add(amountField, 1, row++);

        Button add = new Button("Add");
        Button remove = new Button("Remove Selected");
        HBox buttons = new HBox(5, add, remove);
        form.add(buttons, 1, row);

        add.setOnAction(e -> {
            FeeHonorariumRow built = buildRow();
            if (built != null) {
                table.getItems().add(built);
                clearForm();
            }
        });
        remove.setOnAction(e -> {
            FeeHonorariumRow sel = table.getSelectionModel().getSelectedItem();
            if (sel != null) {
                table.getItems().remove(sel);
            }
        });

        setupTable();
        getChildren().addAll(form, table);
    }

    private void setupTable() {
        TableColumn<FeeHonorariumRow, String> codeCol = new TableColumn<>("Code");
        codeCol.setCellValueFactory(c ->
                new javafx.beans.property.SimpleStringProperty(c.getValue().getCode()));
        TableColumn<FeeHonorariumRow, String> orgCol = new TableColumn<>("Organization / Person");
        orgCol.setCellValueFactory(c ->
                new javafx.beans.property.SimpleStringProperty(c.getValue().getOrganizationOrPerson()));
        TableColumn<FeeHonorariumRow, String> serviceCol = new TableColumn<>("Service Provided");
        serviceCol.setCellValueFactory(c ->
                new javafx.beans.property.SimpleStringProperty(c.getValue().getServiceProvided()));
        TableColumn<FeeHonorariumRow, BigDecimal> amountCol = new TableColumn<>("Amount");
        amountCol.setCellValueFactory(c ->
                new javafx.beans.property.SimpleObjectProperty<>(c.getValue().getAmount()));
        table.getColumns().addAll(codeCol, orgCol, serviceCol, amountCol);
        table.setPrefHeight(150);
    }

    private FeeHonorariumRow buildRow() {
        try {
            BigDecimal amt = parse(amountField.getText());
            return new FeeHonorariumRow(codeField.getText(),
                    organizationField.getText(), serviceField.getText(), amt);
        } catch (NumberFormatException ex) {
            new Alert(Alert.AlertType.ERROR, "Amount must be numeric").showAndWait();
            return null;
        }
    }

    private static BigDecimal parse(String txt) {
        if (txt == null || txt.isBlank()) {
            return null;
        }
        return new BigDecimal(txt.trim());
    }

    private void clearForm() {
        codeField.clear();
        organizationField.clear();
        serviceField.clear();
        amountField.clear();
    }

    /**
     * Returns a copy of the rows currently entered in the table.
     */
    public List<FeeHonorariumRow> getRows() {
        return new ArrayList<>(table.getItems());
    }
}

