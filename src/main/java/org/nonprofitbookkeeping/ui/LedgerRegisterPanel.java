package org.nonprofitbookkeeping.ui;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import nonprofitbookkeeping.model.AccountingTransaction;
import nonprofitbookkeeping.service.LedgerTransactionQueryService;
import nonprofitbookkeeping.service.LedgerTransactionQueryService.LedgerEntryLine;
import nonprofitbookkeeping.service.LedgerTransactionQueryService.LedgerTransactionFilter;
import nonprofitbookkeeping.service.LedgerTransactionQueryService.LedgerTransactionRow;
import nonprofitbookkeeping.ui.panels.GeneralJournalEntryPanelFX;

/** Native alternate ledger register backed by real journal transactions. */
public class LedgerRegisterPanel implements AppPanel
{
    static final String NO_SERVICE_DATA_MESSAGE = "Open a company to view service-backed ledger transactions.";

    private final AlternatePanelScaffold root = new AlternatePanelScaffold("Ledger Register");
    private final LedgerTransactionQueryService service;
    private final TableView<RegisterRow> txnTable = new TableView<>();
    private final Label status = new Label(NO_SERVICE_DATA_MESSAGE);
    private final DatePicker fromDate = new DatePicker();
    private final DatePicker toDate = new DatePicker();
    private final ComboBox<String> account = new ComboBox<>();
    private final TextField memo = new TextField();
    private final TextField amount = new TextField();
    private final TextField fund = new TextField();
    private final ComboBox<String> cleared = triState("Any cleared", "Cleared", "Uncleared");
    private final ComboBox<String> reconciled = triState("Any reconciled", "Reconciled", "Unreconciled");

    public LedgerRegisterPanel()
    {
        this(new LedgerTransactionQueryService());
    }

    LedgerRegisterPanel(LedgerTransactionQueryService service)
    {
        this.service = service;
        root.setSubtitle("Service-backed journal and account activity register.");
        root.setSecondaryActions(List.of(button("Refresh", this::refresh), button("New", this::onNew), button("Edit/Open", this::openSelected), button("Import Review…", this::showImportReviewEntryPoints)));
        root.setFilterBar(filterBar());
        root.setFooter(status);
        buildTable();
        txnTable.setPlaceholder(new Label(NO_SERVICE_DATA_MESSAGE));
        root.setContent(txnTable);
        refresh();
    }

    private HBox filterBar()
    {
        memo.setPromptText("Memo/payee");
        amount.setPromptText("Amount");
        fund.setPromptText("Fund");
        account.setPromptText("All accounts");
        account.setEditable(true);
        Button apply = button("Apply Filters", this::refresh);
        return new HBox(8, new Label("From"), fromDate, new Label("To"), toDate, new Label("Account"), account,
            memo, amount, fund, cleared, reconciled, apply);
    }

    private void buildTable()
    {
        txnTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        txnTable.getColumns().add(col("Type", RegisterRow::type));
        txnTable.getColumns().add(col("Date", RegisterRow::date));
        txnTable.getColumns().add(col("Payee", RegisterRow::payee));
        txnTable.getColumns().add(col("Account", RegisterRow::account));
        txnTable.getColumns().add(col("Memo", RegisterRow::memo));
        txnTable.getColumns().add(col("Fund", RegisterRow::fund));
        txnTable.getColumns().add(col("Debit", row -> money(row.debit())));
        txnTable.getColumns().add(col("Credit", row -> money(row.credit())));
        txnTable.getColumns().add(col("Amount", row -> money(row.amount())));
        txnTable.getColumns().add(col("Status", RegisterRow::status));
        txnTable.setRowFactory(tv -> {
            TableRow<RegisterRow> row = new TableRow<>();
            row.setOnMouseClicked(event -> { if (event.getClickCount() == 2 && !row.isEmpty()) openSelected(); });
            return row;
        });
    }

    private void refresh()
    {
        LedgerTransactionFilter filter = currentFilter();
        account.setItems(FXCollections.observableArrayList(service.accountChoices()));
        List<LedgerTransactionRow> transactions = service.query(filter);
        txnTable.setItems(FXCollections.observableArrayList(transactions.stream().flatMap(row -> RegisterRow.from(row).stream()).toList()));
        if (txnTable.getItems().isEmpty())
        {
            status.setText(service.hasOpenCompany() ? "No ledger transactions match the current filters." : NO_SERVICE_DATA_MESSAGE);
            root.showEmpty(status.getText());
        }
        else
        {
            status.setText(txnTable.getItems().stream().filter(RegisterRow::header).count() + " transactions loaded from the active company ledger.");
            root.showContent();
        }
    }

    private LedgerTransactionFilter currentFilter()
    {
        return new LedgerTransactionFilter(fromDate.getValue(), toDate.getValue(), account.getEditor().getText(), memo.getText(), parseAmount(amount.getText()), fund.getText(), tri(cleared), tri(reconciled));
    }

    private BigDecimal parseAmount(String text)
    {
        try { return text == null || text.isBlank() ? null : new BigDecimal(text.trim()); }
        catch (NumberFormatException ex) { status.setText("Amount filter is not a valid decimal; ignoring it."); return null; }
    }

    private void openSelected()
    {
        RegisterRow selected = txnTable.getSelectionModel().getSelectedItem();
        AccountingTransaction transaction = selected == null ? null : selected.transaction();
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle(transaction == null ? "New Journal Entry" : "Edit Journal Entry");
        dialog.setHeaderText("Temporary adapter: legacy journal-entry workspace hosted from the alternate ledger register.");
        dialog.getDialogPane().setContent(new GeneralJournalEntryPanelFX(transaction, saved -> refresh()));
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.showAndWait();
    }

    private void showImportReviewEntryPoints()
    {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, "Import review queues are available from Import/Export and banking reconciliation workflows. Actual import mechanics remain there so previews, validation, and commit reporting stay centralized.");
        alert.setHeaderText("Import Review Queue Entry Points");
        alert.showAndWait();
    }

    @Override public String title() { return "Ledger Register"; }
    @Override public Node root() { return root; }
    @Override public void onNew() { openSelected(); }
    @Override public void onDelete()
    {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, "Posted accounting history is not silently deleted from this register. Use void/reverse workflows when supported, with confirmation and backup guidance.");
        alert.setHeaderText("Delete is not available");
        alert.showAndWait();
    }

    private Button button(String text, Runnable action) { Button button = new Button(text); button.setOnAction(e -> action.run()); return button; }
    private static ComboBox<String> triState(String any, String yes, String no) { ComboBox<String> box = new ComboBox<>(FXCollections.observableArrayList(any, yes, no)); box.getSelectionModel().selectFirst(); return box; }
    private Boolean tri(ComboBox<String> box) { return box.getSelectionModel().getSelectedIndex() == 1 ? Boolean.TRUE : box.getSelectionModel().getSelectedIndex() == 2 ? Boolean.FALSE : null; }
    private TableColumn<RegisterRow, String> col(String name, java.util.function.Function<RegisterRow, String> getter) { TableColumn<RegisterRow, String> c = new TableColumn<>(name); c.setCellValueFactory(v -> new SimpleStringProperty(getter.apply(v.getValue()))); return c; }
    private String money(BigDecimal value) { return value == null || value.compareTo(BigDecimal.ZERO) == 0 ? "" : value.toPlainString(); }

    record RegisterRow(boolean header, AccountingTransaction transaction, String type, String date, String payee, String account, String memo, String fund, BigDecimal debit, BigDecimal credit, BigDecimal amount, String status)
    {
        static List<RegisterRow> from(LedgerTransactionRow row)
        {
            AccountingTransaction tx = row.transaction();
            RegisterRow header = new RegisterRow(true, tx, "Transaction", tx.getDate(), tx.getToFrom(), "", tx.getMemo(), tx.getAssociatedFundName(), BigDecimal.ZERO, BigDecimal.ZERO, row.displayAmount(), status(tx));
            List<RegisterRow> lines = row.lines().stream().map(line -> detail(tx, line)).toList();
            java.util.ArrayList<RegisterRow> all = new java.util.ArrayList<>(); all.add(header); all.addAll(lines); return all;
        }
        static RegisterRow detail(AccountingTransaction tx, LedgerEntryLine line) { return new RegisterRow(false, tx, "  Line", tx.getDate(), "", line.account(), line.memo(), line.fund(), line.debit(), line.credit(), line.signedAmount(), ""); }
        static String status(AccountingTransaction tx) { return (tx.getClearBank() == null || tx.getClearBank().isBlank() ? "Uncleared" : "Cleared") + (tx.isReconciled() ? " • Reconciled" : " • Unreconciled"); }
    }
}
