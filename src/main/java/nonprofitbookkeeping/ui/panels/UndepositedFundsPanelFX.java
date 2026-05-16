package nonprofitbookkeeping.ui.panels;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.util.Callback;
import nonprofitbookkeeping.ui.UiSpacing;
import nonprofitbookkeeping.model.UndepositedFundsItem;
import nonprofitbookkeeping.service.UndepositedFundsService;

import java.math.BigDecimal;
import java.util.Optional;

public class UndepositedFundsPanelFX extends BorderPane
{
    private final UndepositedFundsService service;
    private final ObservableList<UndepositedFundsItem> items =
        FXCollections.observableArrayList();
    private final TableView<UndepositedFundsItem> table = new TableView<>();

    public UndepositedFundsPanelFX(UndepositedFundsService service)
    {
        this.service = service;
        setPadding(PanelChrome.PANEL_PADDING);
        buildTable();
        setTop(PanelChrome.topSection("Undeposited Funds"));
        setCenter(this.table);
        setBottom(buttonBar());
        refresh();
    }

    @SuppressWarnings({ "unchecked", "deprecation" })
    private void buildTable()
    {
        TableColumn<UndepositedFundsItem, String> dateSentCol =
            new TableColumn<>("Date Sent/Received");
        dateSentCol.setCellValueFactory(
            new PropertyValueFactory<>("date_sent_received"));

        TableColumn<UndepositedFundsItem, String> dateTransferCol =
            new TableColumn<>("Date Transfer/Check #");
        dateTransferCol.setCellValueFactory(
            new PropertyValueFactory<>("date_transfer_or_check"));

        TableColumn<UndepositedFundsItem, String> dateStatementCol =
            new TableColumn<>("Date on Statement");
        dateStatementCol.setCellValueFactory(
            new PropertyValueFactory<>("date_on_statement"));

        TableColumn<UndepositedFundsItem, String> nameCol =
            new TableColumn<>("Name of Person/Business");
        nameCol.setCellValueFactory(
            new PropertyValueFactory<>("name_of_person_business"));

        TableColumn<UndepositedFundsItem, String> detailsCol =
            new TableColumn<>("Details/Notes");
        detailsCol.setCellValueFactory(
            new PropertyValueFactory<>("details_notes"));

        TableColumn<UndepositedFundsItem, String> fromToCol =
            new TableColumn<>("From/To Card Merchant");
        fromToCol.setCellValueFactory(
            new PropertyValueFactory<>("from_to_card_merchant"));

        TableColumn<UndepositedFundsItem, String> accountCol =
            new TableColumn<>("Account for Payment or Deposit");
        accountCol.setCellValueFactory(
            new PropertyValueFactory<>("account_for_payment_or_deposit"));

        TableColumn<UndepositedFundsItem, BigDecimal> amountCol =
            new TableColumn<>("Amount");
        amountCol.setCellValueFactory(new PropertyValueFactory<>("amount"));

        TableColumn<UndepositedFundsItem, String> reversedCol =
            new TableColumn<>("Date Reversed");
        reversedCol.setCellValueFactory(
            new PropertyValueFactory<>("date_reversed"));

        TableColumn<UndepositedFundsItem, String> approvedCol =
            new TableColumn<>("Who Approved & Why");
        approvedCol.setCellValueFactory(
            new PropertyValueFactory<>("reversal_approved_by"));

        this.table.getColumns().addAll(
            dateSentCol,
            dateTransferCol,
            dateStatementCol,
            nameCol,
            detailsCol,
            fromToCol,
            accountCol,
            amountCol,
            reversedCol,
            approvedCol);
        this.table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        this.table.setItems(this.items);
    }

    private HBox buttonBar()
    {
        Button add = new Button("Add Item");
        Button edit = new Button("Edit");
        Button del = new Button("Delete");

        add.setOnAction(e -> itemDialog(null));
        edit.setOnAction(e -> {
            UndepositedFundsItem sel =
                this.table.getSelectionModel().getSelectedItem();
            if (sel != null)
            {
                itemDialog(sel);
            }
        });
        del.setOnAction(e -> {
            UndepositedFundsItem sel =
                this.table.getSelectionModel().getSelectedItem();
            if (sel != null)
            {
                this.service.deleteItem(sel.getId());
                refresh();
            }
        });

        HBox box = new HBox(10, add, edit, del);
        box.setPadding(new Insets(UiSpacing.SECTION_SPACING));
        return box;
    }

    private void itemDialog(UndepositedFundsItem existing)
    {
        Dialog<UndepositedFundsItem> dlg = new Dialog<>();
        dlg.setTitle(existing == null ?
            "Add Undeposited Funds Item" : "Edit Undeposited Funds Item");
        ButtonType okType = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
        dlg.getDialogPane().getButtonTypes().addAll(okType, ButtonType.CANCEL);

        TextField dateSentField = new TextField();
        TextField dateTransferField = new TextField();
        TextField dateStatementField = new TextField();
        TextField nameField = new TextField();
        TextField detailsField = new TextField();
        TextField fromToField = new TextField();
        TextField accountField = new TextField();
        TextField amountField = new TextField();
        TextField dateReversedField = new TextField();
        TextField approvedField = new TextField();

        if (existing != null)
        {
            dateSentField.setText(existing.getDate_sent_received());
            dateTransferField.setText(existing.getDate_transfer_or_check());
            dateStatementField.setText(existing.getDate_on_statement());
            nameField.setText(existing.getName_of_person_business());
            detailsField.setText(existing.getDetails_notes());
            fromToField.setText(existing.getFrom_to_card_merchant());
            accountField.setText(existing.getAccount_for_payment_or_deposit());
            amountField.setText(existing.getAmount() == null ?
                "" : existing.getAmount().toPlainString());
            dateReversedField.setText(existing.getDate_reversed());
            approvedField.setText(existing.getReversal_approved_by());
        }

        GridPane grid = new GridPane();
        grid.setHgap(8);
        grid.setVgap(8);
        grid.setPadding(PanelChrome.PANEL_PADDING);

        int row = 0;
        grid.addRow(row++, new Label("Date Sent/Received:"), dateSentField);
        grid.addRow(row++, new Label("Date Transfer/Check #:"),
            dateTransferField);
        grid.addRow(row++, new Label("Date on Statement:"),
            dateStatementField);
        grid.addRow(row++, new Label("Name of Person/Business:"), nameField);
        grid.addRow(row++, new Label("Details/Notes:"), detailsField);
        grid.addRow(row++, new Label("From/To Card Merchant:"), fromToField);
        grid.addRow(row++, new Label("Account for Payment or Deposit:"),
            accountField);
        grid.addRow(row++, new Label("Amount:"), amountField);
        grid.addRow(row++, new Label("Date Reversed:"), dateReversedField);
        grid.addRow(row++, new Label("Who Approved & Why:"), approvedField);

        dlg.getDialogPane().setContent(grid);

        dlg.setResultConverter(new Callback<ButtonType, UndepositedFundsItem>()
        {
            @Override
            public UndepositedFundsItem call(ButtonType buttonType)
            {
                if (buttonType != okType)
                {
                    return null;
                }

                UndepositedFundsItem item =
                    existing == null ? new UndepositedFundsItem() : existing;
                item.setDate_sent_received(dateSentField.getText());
                item.setDate_transfer_or_check(dateTransferField.getText());
                item.setDate_on_statement(dateStatementField.getText());
                item.setName_of_person_business(nameField.getText());
                item.setDetails_notes(detailsField.getText());
                item.setFrom_to_card_merchant(fromToField.getText());
                item.setAccount_for_payment_or_deposit(accountField.getText());
                item.setAmount(parseAmount(amountField.getText()));
                item.setDate_reversed(dateReversedField.getText());
                item.setReversal_approved_by(approvedField.getText());
                return item;
            }
        });

        Optional<UndepositedFundsItem> result = dlg.showAndWait();
        result.ifPresent(item ->
        {
            if (existing == null)
            {
                this.service.addItem(item);
            }
            else
            {
                this.service.updateItem(item);
            }
            refresh();
        });
    }

    private BigDecimal parseAmount(String text)
    {
        if (text == null || text.isBlank())
        {
            return null;
        }

        try
        {
            return new BigDecimal(text.trim());
        }
        catch (NumberFormatException ex)
        {
            Alert alert = new Alert(Alert.AlertType.ERROR,
                "Amount must be a valid number.");
            alert.setHeaderText("Invalid amount");
            alert.showAndWait();
            return null;
        }
    }

    private void refresh()
    {
        this.items.setAll(this.service.listItems());
        this.table.refresh();
    }
}
