package nonprofitbookkeeping.ui.panels;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects; // Added import
import java.util.function.Predicate;
import java.util.stream.Collectors;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import nonprofitbookkeeping.model.Account; // Added import
import nonprofitbookkeeping.model.AccountingTransaction;
import nonprofitbookkeeping.model.ChartOfAccounts; // Added import
import nonprofitbookkeeping.model.Company;
import nonprofitbookkeeping.model.CurrentCompany;
import nonprofitbookkeeping.model.Ledger;

/**
 * JavaFX rewrite of {@code AccountsActivityPanel}.
 */
public class AccountsActivityPanelFX extends BorderPane {

    /* ───────────────────────── UI fields ───────────────────────── */
    private final ComboBox<String> accountSelector = new ComboBox<>();
    private final TextField filterDateField = new TextField();
    private final TextField filterMemoField = new TextField();
    private final TextField filterAmountField = new TextField();

    private final TableView<TransactionRow> table = new TableView<>();
    private final ObservableList<TransactionRow> backingList = FXCollections.observableArrayList();

    /* Back‑reference */
    private final List<AccountingTransaction> transactions;

    public AccountsActivityPanelFX(Ledger ledger) {
        // Assuming ledger and its transactions list are non-null from caller
        this.transactions = ledger.getTransactions() != null ? ledger.getTransactions() : FXCollections.emptyObservableList();
        setPadding(new Insets(10));

        /* NORTH: selectors + filters */
        VBox top = new VBox(10);
        top.getChildren().addAll(selectorPane(), filterPane());
        setTop(top);

        /* CENTER: table */
        configureTable();
        TitledPane titledTablePane = new TitledPane("Ledger", this.table);
        titledTablePane.setCollapsible(false);
        setCenter(titledTablePane);


        /* SOUTH: buttons */
        setBottom(buttonBar());

        /* initial fill */
        applyFilters();
    }

    /* ───────────────────────── Builders ───────────────────────── */

    private HBox selectorPane() {
        HBox box = new HBox(10);
        box.setPadding(new Insets(5));
        box.setStyle(
            "-fx-border-color: lightgray; -fx-border-radius: 4; -fx-border-insets: 4; -fx-border-style: segments(4)");

        Company company = CurrentCompany.getCompany();
        if (company != null && company.getChartOfAccounts() != null) {
            ChartOfAccounts coa = company.getChartOfAccounts();
            if (coa.getAccounts() != null) { // Check if the internal list of accounts is not null
                List<String> accountNames = coa.getAccounts().stream()
                                               .map(Account::getName)
                                               .filter(Objects::nonNull)
                                               .sorted()
                                               .collect(Collectors.toList());
                this.accountSelector.getItems().addAll(accountNames);
            }
        }
        if (!this.accountSelector.getItems().isEmpty()) {
            this.accountSelector.getSelectionModel().selectFirst();
        } else {
            this.accountSelector.setPlaceholder(new Label("No accounts in COA"));
        }

        this.accountSelector.setOnAction(e -> applyFilters());
        box.getChildren().addAll(new Label("Account:"), this.accountSelector);
        return box;
    }

    private HBox filterPane() {
        HBox box = new HBox(10);
        box.setPadding(new Insets(5));
        box.setStyle(
            "-fx-border-color: lightgray; -fx-border-radius: 4; -fx-border-insets: 4; -fx-border-style: segments(4)");
        Button apply = new Button("Apply");
        apply.setOnAction(e -> applyFilters());
        box.getChildren().addAll(
            new Label("Date:"), this.filterDateField,
            new Label("Memo:"), this.filterMemoField,
            new Label("Amount:"), this.filterAmountField,
            apply);
        return box;
    }

    private static HBox buttonBar() {
        HBox box = new HBox(10);
        box.setPadding(new Insets(10));
        Button reconcile = new Button("Reconcile");
        reconcile.setOnAction(
            e -> new Alert(Alert.AlertType.INFORMATION, "Reconciliation process would start here.")
                .showAndWait());
        Button importBtn = new Button("Import Statement (CSV/QIF/OFX)");
        importBtn.setOnAction(
            e -> new Alert(Alert.AlertType.INFORMATION, "Import dialog not implemented.")
                .showAndWait());
        box.getChildren().addAll(reconcile, importBtn);
        return box;
    }

    @SuppressWarnings({ "unchecked", "deprecation" }) private void configureTable() {
        TableColumn<TransactionRow, String> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(d -> d.getValue().date);

        TableColumn<TransactionRow, String> descCol = new TableColumn<>("Description");
        descCol.setCellValueFactory(d -> d.getValue().description);

        TableColumn<TransactionRow, BigDecimal> amtCol = new TableColumn<>("Amount");
        amtCol.setCellValueFactory(d -> d.getValue().amount);

        TableColumn<TransactionRow, BigDecimal> balCol = new TableColumn<>("Balance");
        balCol.setCellValueFactory(d -> d.getValue().balance);

        TableColumn<TransactionRow, String> memoCol = new TableColumn<>("Memo");
        memoCol.setCellValueFactory(d -> d.getValue().memo);

        this.table.getColumns().addAll(dateCol, descCol, amtCol, balCol, memoCol);
        this.table.setItems(this.backingList);
        this.table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN); // Changed policy
    }

    /* ───────────────────────── Logic ───────────────────────── */
    BigDecimal amountFilter = null;

    private void applyFilters() {
        String acct = this.accountSelector.getValue();
        String dateFilter = this.filterDateField.getText().trim();
        String memoFilter = this.filterMemoField.getText().trim().toLowerCase();
        String amountText = this.filterAmountField.getText().trim();

        this.amountFilter = null; // Reset before trying to parse
        try {
            if (!amountText.isEmpty()) {
                this.amountFilter = new BigDecimal(amountText);
            }
        } catch (NumberFormatException ignore) {
            // amountFilter remains null, or you could show an error
            System.err.println("Invalid amount format in filter: " + amountText);
        }

        Predicate<AccountingTransaction> predicate = t -> {
            if (t == null) return false;

            // Account Name Check
            // This check needs to be more robust if t.getAccountName() can be null or if 'acct' (from ComboBox) can be null
            if (acct == null) return false; // Or handle differently if no account is selected (e.g. show all)
            String accountName = t.getAccountName();
            if (!Objects.equals(accountName, acct)) {
                return false;
            }

            // Date Filter Check
            String transactionDate = t.getDate();
            if (!dateFilter.isEmpty() && (transactionDate == null || !transactionDate.contains(dateFilter))) {
                return false;
            }

            // Memo Filter Check (using getMemo() as per TransactionRow update)
            String transactionMemo = t.getMemo();
            if (!memoFilter.isEmpty() && (transactionMemo == null || !transactionMemo.toLowerCase().contains(memoFilter))) {
                return false;
            }

            // Amount Filter Check
            if (this.amountFilter != null) {
                BigDecimal totalAmount = t.getTotalAmount();
                if (totalAmount == null || totalAmount.compareTo(this.amountFilter) != 0) {
                    return false;
                }
            }
            return true;
        };

        this.backingList.clear();
        if (this.transactions != null) {
            List<AccountingTransaction> filtered =
                this.transactions.stream()
                                 .filter(Objects::nonNull) // Ensure transaction 't' itself is not null
                                 .filter(predicate)
                                 .collect(Collectors.toList());
            this.backingList.setAll(filtered.stream().map(TransactionRow::new).collect(Collectors.toList()));
        }
    }

    /* ───────────────────────── Table row ───────────────────────── */
    public static class TransactionRow {
        final SimpleStringProperty date = new SimpleStringProperty();
        final SimpleStringProperty description = new SimpleStringProperty();
        final SimpleObjectProperty<BigDecimal> amount = new SimpleObjectProperty<>();
        final SimpleObjectProperty<BigDecimal> balance = new SimpleObjectProperty<>();
        final SimpleStringProperty memo = new SimpleStringProperty();

        TransactionRow(AccountingTransaction t) {
            // Assumes 't' is non-null due to filter(Objects::nonNull) in applyFilters()
            this.date.set(Objects.toString(t.getDate(), ""));
            this.description.set(Objects.toString(t.getDescription(), "")); // As per original column mapping

            BigDecimal totalAmount = t.getTotalAmount();
            this.amount.set(totalAmount != null ? totalAmount : BigDecimal.ZERO);

            BigDecimal accountBalance = t.countAccountBalance(); // Assumes t.getAccount() is handled inside.
            this.balance.set(accountBalance != null ? accountBalance : BigDecimal.ZERO);

            this.memo.set(Objects.toString(t.getMemo(), ""));
        }
    }
}
