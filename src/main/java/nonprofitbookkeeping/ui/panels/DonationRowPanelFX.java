package nonprofitbookkeeping.ui.panels;

import java.io.File;
import java.math.BigDecimal;
import java.util.Objects;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.util.converter.BigDecimalStringConverter;
import nonprofitbookkeeping.reports.datasource.scareports.DonationRow;
import nonprofitbookkeeping.service.DonationRowService;

/**
 * Panel for viewing and editing {@link DonationRow} entries. Uses a
 * {@link TableView} with editable columns and buttons to add, edit and delete
 * rows. Data is persisted via the injected {@link DonationRowService}.
 */
public class DonationRowPanelFX extends BorderPane {

    private final DonationRowService service;
    private final File companyDirectory;
    private final ObservableList<DonationRow> rows = FXCollections.observableArrayList();
    private final TableView<DonationRow> table = new TableView<>();

    public DonationRowPanelFX(DonationRowService service, File companyDirectory) {
        this.service = Objects.requireNonNull(service);
        this.companyDirectory = companyDirectory;
        if (this.companyDirectory != null) {
            try {
                this.service.loadRows(this.companyDirectory);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        setPadding(new Insets(10));
        buildTable();
        setCenter(this.table);
        setBottom(buildButtons());
        refresh();
    }

    public DonationRowPanelFX(DonationRowService service) {
        this(service, null);
    }

    private void buildTable() {
        table.setEditable(true);
        TableColumn<DonationRow, String> orgCol = new TableColumn<>("Organization");
        orgCol.setCellValueFactory(new PropertyValueFactory<>("organizationName"));
        orgCol.setCellFactory(TextFieldTableCell.forTableColumn());
        orgCol.setOnEditCommit(e -> {
            e.getRowValue().setOrganizationName(e.getNewValue());
            save();
        });

        TableColumn<DonationRow, String> fedCol = new TableColumn<>("Fed ID");
        fedCol.setCellValueFactory(new PropertyValueFactory<>("fedIdNumber"));
        fedCol.setCellFactory(TextFieldTableCell.forTableColumn());
        fedCol.setOnEditCommit(e -> {
            e.getRowValue().setFedIdNumber(e.getNewValue());
            save();
        });

        TableColumn<DonationRow, String> reasonCol = new TableColumn<>("Reason");
        reasonCol.setCellValueFactory(new PropertyValueFactory<>("reason"));
        reasonCol.setCellFactory(TextFieldTableCell.forTableColumn());
        reasonCol.setOnEditCommit(e -> {
            e.getRowValue().setReason(e.getNewValue());
            save();
        });

        TableColumn<DonationRow, String> checkNoCol = new TableColumn<>("Check #");
        checkNoCol.setCellValueFactory(new PropertyValueFactory<>("checkNumber"));
        checkNoCol.setCellFactory(TextFieldTableCell.forTableColumn());
        checkNoCol.setOnEditCommit(e -> {
            e.getRowValue().setCheckNumber(e.getNewValue());
            save();
        });

        TableColumn<DonationRow, String> checkDateCol = new TableColumn<>("Check Date");
        checkDateCol.setCellValueFactory(new PropertyValueFactory<>("checkDate"));
        checkDateCol.setCellFactory(TextFieldTableCell.forTableColumn());
        checkDateCol.setOnEditCommit(e -> {
            e.getRowValue().setCheckDate(e.getNewValue());
            save();
        });

        TableColumn<DonationRow, BigDecimal> amtCol = new TableColumn<>("Amount");
        amtCol.setCellValueFactory(new PropertyValueFactory<>("amount"));
        amtCol.setCellFactory(TextFieldTableCell.forTableColumn(new BigDecimalStringConverter()));
        amtCol.setOnEditCommit(e -> {
            e.getRowValue().setAmount(e.getNewValue());
            save();
        });

        table.getColumns().addAll(orgCol, fedCol, reasonCol, checkNoCol, checkDateCol, amtCol);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setItems(rows);
    }

    private ToolBar buildButtons() {
        Button add = new Button("Add");
        Button edit = new Button("Edit");
        Button del = new Button("Delete");
        add.setOnAction(e -> showDialog(null));
        edit.setOnAction(e -> {
            DonationRow sel = table.getSelectionModel().getSelectedItem();
            if (sel != null) {
                showDialog(sel);
            }
        });
        del.setOnAction(e -> {
            DonationRow sel = table.getSelectionModel().getSelectedItem();
            if (sel != null) {
                service.deleteRow(sel);
                refresh();
                save();
            }
        });
        return new ToolBar(add, edit, del);
    }

    private void showDialog(DonationRow existing) {
        Dialog<DonationRow> dlg = new Dialog<>();
        dlg.setTitle(existing == null ? "Add Donation" : "Edit Donation");
        dlg.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        TextField orgF = new TextField();
        TextField fedF = new TextField();
        TextField reasonF = new TextField();
        TextField checkNoF = new TextField();
        TextField checkDateF = new TextField();
        TextField amtF = new TextField();

        if (existing != null) {
            orgF.setText(existing.getOrganizationName());
            fedF.setText(existing.getFedIdNumber());
            reasonF.setText(existing.getReason());
            checkNoF.setText(existing.getCheckNumber());
            checkDateF.setText(existing.getCheckDate());
            if (existing.getAmount() != null) {
                amtF.setText(existing.getAmount().toPlainString());
            }
        }

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(8);
        grid.setPadding(new Insets(10));
        grid.addRow(0, new Label("Organization:"), orgF);
        grid.addRow(1, new Label("Fed ID:"), fedF);
        grid.addRow(2, new Label("Reason:"), reasonF);
        grid.addRow(3, new Label("Check #:"), checkNoF);
        grid.addRow(4, new Label("Check Date:"), checkDateF);
        grid.addRow(5, new Label("Amount:"), amtF);
        dlg.getDialogPane().setContent(grid);

        dlg.setResultConverter(btn -> {
            if (btn == ButtonType.OK) {
                try {
                    BigDecimal amt = amtF.getText().isEmpty() ? null : new BigDecimal(amtF.getText().trim());
                    DonationRow r = existing != null ? existing : new DonationRow();
                    r.setOrganizationName(orgF.getText());
                    r.setFedIdNumber(fedF.getText());
                    r.setReason(reasonF.getText());
                    r.setCheckNumber(checkNoF.getText());
                    r.setCheckDate(checkDateF.getText());
                    r.setAmount(amt);
                    return r;
                } catch (NumberFormatException ex) {
                    new Alert(Alert.AlertType.ERROR, "Amount must be numeric").showAndWait();
                }
            }
            return null;
        });

        dlg.showAndWait().ifPresent(r -> {
            if (existing == null) {
                service.addRow(r);
            }
            refresh();
            save();
        });
    }

    private void refresh() {
        rows.setAll(service.listRows());
    }

    private void save() {
        if (companyDirectory != null) {
            try {
                service.saveRows(companyDirectory);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }
}

