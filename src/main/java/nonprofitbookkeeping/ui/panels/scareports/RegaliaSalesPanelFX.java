package nonprofitbookkeeping.ui.panels.scareports;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import javafx.collections.FXCollections;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import nonprofitbookkeeping.reports.datasource.scareports.RegaliaSalesDtl7Bean;
import nonprofitbookkeeping.reports.datasource.scareports.RegaliaSalesRow;
import nonprofitbookkeeping.ui.panels.ReportRowPanel;

public class RegaliaSalesPanelFX extends VBox implements ReportRowPanel
{
        private final ComboBox<String> sectionField = new ComboBox<>(
                FXCollections.observableArrayList("PURCHASE", "SALE", "ADJUSTMENT"));
        private final TextField entryDateField = new TextField();
        private final TextField itemDescriptionField = new TextField();
        private final TextField quantityField = new TextField();
        private final TextField unitCostField = new TextField();
        private final TextField unitPriceField = new TextField();
        private final TextField amountField = new TextField();
        private final TextField counterpartyField = new TextField();
        private final TextField notesField = new TextField();

        private final TableView<RegaliaSalesRow> table =
                new TableView<>(FXCollections.observableArrayList());

        public RegaliaSalesPanelFX()
        {
                setSpacing(10);
                GridPane form = new GridPane();
                form.setHgap(5);
                form.setVgap(5);
                sectionField.getSelectionModel().selectFirst();

                int row = 0;
                form.add(new Label("Section:"), 0, row);
                form.add(sectionField, 1, row++);
                form.add(new Label("Entry Date:"), 0, row);
                form.add(entryDateField, 1, row++);
                form.add(new Label("Item Description:"), 0, row);
                form.add(itemDescriptionField, 1, row++);
                form.add(new Label("Quantity:"), 0, row);
                form.add(quantityField, 1, row++);
                form.add(new Label("Unit Cost:"), 0, row);
                form.add(unitCostField, 1, row++);
                form.add(new Label("Unit Price:"), 0, row);
                form.add(unitPriceField, 1, row++);
                form.add(new Label("Amount:"), 0, row);
                form.add(amountField, 1, row++);
                form.add(new Label("Counterparty:"), 0, row);
                form.add(counterpartyField, 1, row++);
                form.add(new Label("Notes:"), 0, row);
                form.add(notesField, 1, row++);

                Button add = new Button("Add");
                Button remove = new Button("Remove Selected");
                HBox buttons = new HBox(5, add, remove);
                form.add(buttons, 1, row);

                add.setOnAction(e -> {
                        table.getItems().add(buildRow());
                        clearForm();
                });
                remove.setOnAction(e -> {
                        RegaliaSalesRow selected =
                                table.getSelectionModel().getSelectedItem();
                        if (selected != null)
                        {
                                table.getItems().remove(selected);
                        }
                });


                setupTable();
                getChildren().addAll(form, table);
        }

        private void setupTable()
        {
                TableColumn<RegaliaSalesRow, String> sectionCol = new TableColumn<>("Section");
                sectionCol.setCellValueFactory(c ->
                        new javafx.beans.property.SimpleStringProperty(c.getValue().getSection()));
                TableColumn<RegaliaSalesRow, String> entryDateCol = new TableColumn<>("Entry Date");
                entryDateCol.setCellValueFactory(c ->
                        new javafx.beans.property.SimpleStringProperty(c.getValue().getEntryDate()));
                TableColumn<RegaliaSalesRow, String> itemDescCol =
                        new TableColumn<>("Item Description");
                itemDescCol.setCellValueFactory(c ->
                        new javafx.beans.property.SimpleStringProperty(c.getValue().getItemDescription()));
                TableColumn<RegaliaSalesRow, String> quantityCol = new TableColumn<>("Quantity");
                quantityCol.setCellValueFactory(c ->
                        new javafx.beans.property.SimpleStringProperty(c.getValue().getQuantity()));
                TableColumn<RegaliaSalesRow, BigDecimal> unitCostCol =
                        new TableColumn<>("Unit Cost");
                unitCostCol.setCellValueFactory(c ->
                        new javafx.beans.property.SimpleObjectProperty<>(c.getValue().getUnitCost()));
                TableColumn<RegaliaSalesRow, BigDecimal> unitPriceCol =
                        new TableColumn<>("Unit Price");
                unitPriceCol.setCellValueFactory(c ->
                        new javafx.beans.property.SimpleObjectProperty<>(c.getValue().getUnitPrice()));
                TableColumn<RegaliaSalesRow, BigDecimal> amountCol =
                        new TableColumn<>("Amount");
                amountCol.setCellValueFactory(c ->
                        new javafx.beans.property.SimpleObjectProperty<>(c.getValue().getAmount()));
                TableColumn<RegaliaSalesRow, String> counterpartyCol =
                        new TableColumn<>("Counterparty");
                counterpartyCol.setCellValueFactory(c ->
                        new javafx.beans.property.SimpleStringProperty(c.getValue().getCounterparty()));
                TableColumn<RegaliaSalesRow, String> notesCol = new TableColumn<>("Notes");
                notesCol.setCellValueFactory(c ->
                        new javafx.beans.property.SimpleStringProperty(c.getValue().getNotes()));
                table.getColumns().addAll(sectionCol, entryDateCol, itemDescCol,
                        quantityCol, unitCostCol, unitPriceCol, amountCol, counterpartyCol,
                        notesCol);
                table.setPrefHeight(150);
        }

        private RegaliaSalesRow buildRow()
        {
                return new RegaliaSalesRow(sectionField.getValue(),
                                entryDateField.getText(), itemDescriptionField.getText(),
                                quantityField.getText(), parse(unitCostField.getText()),
                                parse(unitPriceField.getText()),
                                parse(amountField.getText()), counterpartyField.getText(),
                                notesField.getText());
        }

        private static BigDecimal parse(String txt)
        {
                try
                {
                        return (txt == null || txt.isBlank()) ? null :
                                new BigDecimal(txt.trim());
                }
                catch (NumberFormatException ex)
                {
                        return null;
                }
        }

        private void clearForm()
        {
                entryDateField.clear();
                itemDescriptionField.clear();
                quantityField.clear();
                unitCostField.clear();
                unitPriceField.clear();
                amountField.clear();
                counterpartyField.clear();
                notesField.clear();
        }

        public List<RegaliaSalesRow> getRows()
        {
                return new ArrayList<>(table.getItems());
        }

        @Override
        public Object buildBean()
        {
                RegaliaSalesDtl7Bean bean = new RegaliaSalesDtl7Bean();
                bean.setRows(new ArrayList<>(getRows()));
                return bean;
        }
}

