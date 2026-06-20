package org.nonprofitbookkeeping.ui;

import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;
import nonprofitbookkeeping.model.Account;
import nonprofitbookkeeping.service.LedgerTransactionQueryService;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.List;

/**
 * Native alternate-style workspace for service-backed fund administration and fund ledger drilldown.
 */
public class FundsPanel implements AppPanel
{
    private final AlternatePanelScaffold root = new AlternatePanelScaffold("Funds");
    private final AlternateFundsService service;
    private final TableView<AlternateFundsService.FundWorkspaceRow> fundsTable = new TableView<>();
    private final TableView<LedgerTransactionQueryService.LedgerTransactionRow> ledgerTable = new TableView<>();
    private final ComboBox<String> fromFund = new ComboBox<>();
    private final ComboBox<String> toFund = new ComboBox<>();
    private final ComboBox<Account> account = new ComboBox<>();
    private final DatePicker transferDate = new DatePicker(LocalDate.now());
    private final TextField amount = new TextField();
    private final TextField memo = new TextField();
    private final Label status = new Label();

    public FundsPanel()
    {
        this(new AlternateFundsService());
    }

    FundsPanel(AlternateFundsService service)
    {
        this.service = service;
        this.root.setSubtitle("Manage funds and post fund restriction reclassifications without directly editing display balances.");
        this.root.setWarningBanner("Restriction reclassification changes fund coding only. It is not a bank-account movement; record bank transfers in the banking workflow.");
        this.root.setFooter(this.status);
        buildActions();
        buildFundsTable();
        buildLedgerTable();
        buildTransferForm();
        this.root.setContent(new VBox(12, this.fundsTable, transferBox(), this.ledgerTable));
        reload();
    }

    @Override public String title() { return "Funds"; }
    @Override public Node root() { return this.root; }

    private void buildActions()
    {
        Button add = new Button("Add Fund");
        add.setOnAction(e -> addFund());
        Button edit = new Button("Edit Fund");
        edit.setOnAction(e -> editFund());
        Button deactivate = new Button("Deactivate Fund");
        deactivate.setOnAction(e -> deactivateFund());
        Button refresh = new Button("Refresh");
        refresh.setOnAction(e -> reload());
        this.root.setPrimaryActions(List.of(add, edit, deactivate));
        this.root.setSecondaryActions(List.of(refresh));
    }

    private void buildFundsTable()
    {
        TableColumn<AlternateFundsService.FundWorkspaceRow, String> name = new TableColumn<>("Fund");
        name.setCellValueFactory(v -> new SimpleStringProperty(v.getValue().name()));
        TableColumn<AlternateFundsService.FundWorkspaceRow, String> balance = new TableColumn<>("Ledger-derived Balance");
        balance.setCellValueFactory(v -> new SimpleStringProperty(formatMoney(v.getValue().ledgerBalance())));
        TableColumn<AlternateFundsService.FundWorkspaceRow, String> active = new TableColumn<>("Status");
        active.setCellValueFactory(v -> new SimpleStringProperty(v.getValue().active() ? "Active" : "Inactive"));
        this.fundsTable.getColumns().addAll(name, balance, active);
        this.fundsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        this.fundsTable.setPlaceholder(new Label("No funds are available for the active company."));
        this.fundsTable.getSelectionModel().selectedItemProperty().addListener((obs, old, row) -> loadLedgerDrilldown(row));
    }

    private void buildLedgerTable()
    {
        TableColumn<LedgerTransactionQueryService.LedgerTransactionRow, String> date = new TableColumn<>("Date");
        date.setCellValueFactory(v -> new SimpleStringProperty(v.getValue().transaction().getDate()));
        TableColumn<LedgerTransactionQueryService.LedgerTransactionRow, String> memoCol = new TableColumn<>("Memo");
        memoCol.setCellValueFactory(v -> new SimpleStringProperty(v.getValue().transaction().getMemo()));
        TableColumn<LedgerTransactionQueryService.LedgerTransactionRow, String> amountCol = new TableColumn<>("Fund Amount");
        amountCol.setCellValueFactory(v -> new SimpleStringProperty(formatMoney(v.getValue().displayAmount())));
        this.ledgerTable.getColumns().addAll(date, memoCol, amountCol);
        this.ledgerTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        this.ledgerTable.setPlaceholder(new Label("Select a fund to drill down to ledger transactions."));
    }

    private void buildTransferForm()
    {
        this.fromFund.setPromptText("From fund");
        this.toFund.setPromptText("To fund");
        this.account.setPromptText("Account for restriction entry");
        this.account.setConverter(accountConverter());
        this.amount.setPromptText("Amount");
        this.memo.setPromptText("Memo");
    }

    private Node transferBox()
    {
        Button post = new Button("Post Restriction Reclassification");
        post.setOnAction(e -> postReclassification());
        TextArea note = new TextArea("Use this form when dollars stay in the same bank account but move between fund restrictions. For real bank-account movement, use banking transfer/reconciliation workflows instead.");
        note.setEditable(false);
        note.setWrapText(true);
        note.setPrefRowCount(2);
        GridPane grid = new GridPane();
        grid.setHgap(8);
        grid.setVgap(8);
        grid.setPadding(new Insets(8));
        grid.addRow(0, new Label("Date"), this.transferDate, new Label("Amount"), this.amount);
        grid.addRow(1, new Label("From fund"), this.fromFund, new Label("To fund"), this.toFund);
        grid.addRow(2, new Label("Account"), this.account, new Label("Memo"), this.memo);
        grid.add(post, 1, 3, 2, 1);
        return new VBox(6, new Label("Fund Restriction Reclassification (not bank movement)"), note, grid);
    }

    private void reload()
    {
        try
        {
            if (!this.service.hasOpenCompany())
            {
                this.root.showEmpty("Open a company to manage funds.");
                this.status.setText("No company is open.");
                return;
            }
            List<AlternateFundsService.FundWorkspaceRow> rows = this.service.fundRows();
            this.fundsTable.getItems().setAll(rows);
            List<String> names = rows.stream().filter(AlternateFundsService.FundWorkspaceRow::active).map(AlternateFundsService.FundWorkspaceRow::name).toList();
            this.fromFund.getItems().setAll(names);
            this.toFund.getItems().setAll(names);
            this.account.getItems().setAll(this.service.transferAccounts());
            this.root.showContent();
            this.status.setText("Loaded " + rows.size() + " fund(s). Balances are calculated from ledger entries.");
        }
        catch (RuntimeException ex)
        {
            this.root.showError("Failed to load funds: " + UiErrors.safeMessage(ex));
            this.status.setText("Failed to load funds.");
        }
    }

    private void loadLedgerDrilldown(AlternateFundsService.FundWorkspaceRow row)
    {
        this.ledgerTable.getItems().setAll(row == null ? List.of() : this.service.transactionsForFund(row.name()));
    }

    private void addFund()
    {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Add Fund");
        dialog.setHeaderText("Enter a fund name. Opening/display balances are not edited directly.");
        dialog.showAndWait().ifPresent(name -> runAndReload(() -> this.service.addFund(name), "Fund added."));
    }

    private void editFund()
    {
        AlternateFundsService.FundWorkspaceRow selected = this.fundsTable.getSelectionModel().getSelectedItem();
        if (selected == null)
        {
            alert("Select a fund to edit.");
            return;
        }
        TextInputDialog dialog = new TextInputDialog(selected.name());
        dialog.setTitle("Edit Fund");
        dialog.setHeaderText("Rename fund. Ledger-derived balances cannot be edited here.");
        dialog.showAndWait().ifPresent(name -> runAndReload(() -> this.service.editFund(selected.name(), name), "Fund updated."));
    }

    private void deactivateFund()
    {
        AlternateFundsService.FundWorkspaceRow selected = this.fundsTable.getSelectionModel().getSelectedItem();
        if (selected == null)
        {
            alert("Select a fund to deactivate.");
            return;
        }
        new Alert(Alert.AlertType.CONFIRMATION, "Deactivate " + selected.name() + "? Existing ledger transactions remain intact.", ButtonType.OK, ButtonType.CANCEL)
            .showAndWait()
            .filter(ButtonType.OK::equals)
            .ifPresent(button -> runAndReload(() -> this.service.deactivateFund(selected.name()), "Fund deactivated."));
    }

    private void postReclassification()
    {
        try
        {
            BigDecimal parsedAmount = new BigDecimal(this.amount.getText().trim());
            this.service.recordRestrictionReclassification(this.transferDate.getValue(), this.memo.getText(), this.fromFund.getValue(), this.toFund.getValue(), parsedAmount, this.account.getValue());
            this.amount.clear();
            this.memo.clear();
            reload();
            alert("Fund restriction reclassification posted. No bank-account movement was recorded.");
        }
        catch (Exception ex)
        {
            alert(UiErrors.safeMessage(ex));
        }
    }

    private void runAndReload(CheckedAction action, String success)
    {
        try
        {
            action.run();
            reload();
            alert(success);
        }
        catch (Exception ex)
        {
            alert(UiErrors.safeMessage(ex));
        }
    }

    private static StringConverter<Account> accountConverter()
    {
        return new StringConverter<>()
        {
            @Override public String toString(Account account)
            {
                if (account == null) return "";
                String number = account.getAccountNumber() == null ? "" : account.getAccountNumber();
                String name = account.getName() == null ? "" : account.getName();
                return name.isBlank() ? number : number + " — " + name;
            }
            @Override public Account fromString(String value) { return null; }
        };
    }

    private static String formatMoney(BigDecimal value)
    {
        return NumberFormat.getCurrencyInstance().format(value == null ? BigDecimal.ZERO : value);
    }

    private static void alert(String message)
    {
        new Alert(Alert.AlertType.INFORMATION, message).showAndWait();
    }

    @FunctionalInterface private interface CheckedAction { void run() throws Exception; }
}
