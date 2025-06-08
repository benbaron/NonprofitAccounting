package nonprofitbookkeeping.ui.panels;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects; // Added import
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.Arrays; // Added import for Arrays.asList

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
 * A JavaFX panel that displays account activity (transactions) from a given {@link Ledger}.
 * It provides UI controls for selecting an account and filtering transactions by date,
 * memo, and amount. The transactions are displayed in a {@link TableView}.
 * This class is a JavaFX rewrite of an earlier Swing component.
 */
public class AccountsActivityPanelFX extends BorderPane {

    /* ───────────────────────── UI fields ───────────────────────── */
    /** ComboBox for selecting the account whose activity is to be displayed. */
    private final ComboBox<String> accountSelector = new ComboBox<>();
    /** TextField for entering a date string to filter transactions. */
    private final TextField filterDateField = new TextField();
    /** TextField for entering a memo string (case-insensitive) to filter transactions. */
    private final TextField filterMemoField = new TextField();
    /** TextField for entering an amount to filter transactions (exact match). */
    private final TextField filterAmountField = new TextField();

    /** TableView to display the filtered account transactions. */
    private final TableView<TransactionRow> table = new TableView<>();
    /** ObservableList that backs the {@code table}, holding {@link TransactionRow} objects. */
    private final ObservableList<TransactionRow> backingList = FXCollections.observableArrayList();

    /** A list of all {@link AccountingTransaction}s from the provided ledger, serving as the master data source. */
    private final List<AccountingTransaction> transactions;

    /** Stores the parsed BigDecimal value from the amount filter field ({@link #filterAmountField}).
     *  It is updated by {@link #applyFilters()} and used in the filtering predicate.
     *  Will be {@code null} if the amount filter field is empty or contains an invalid number format. */
    private BigDecimal amountFilter = null; // Renamed from the local variable in applyFilters to be a field

    /**
     * Constructs a new {@code AccountsActivityPanelFX}.
     * Initializes the UI layout, including account selection, filter fields,
     * the transaction table, and action buttons. It populates the account selector
     * based on the current company's chart of accounts and applies initial filters
     * (which usually results in an empty table until a filter is applied or an account selected).
     *
     * @param ledger The {@link Ledger} containing the transactions to be displayed and filtered.
     *               It's assumed that the ledger and its transaction list are non-null;
     *               if {@code ledger.getTransactions()} is null, an empty list is used.
     */
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

    /**
     * Creates and configures the HBox pane containing the account selector ComboBox.
     * Populates the ComboBox with account names from the current company's Chart of Accounts.
     * Sets an action on the ComboBox to re-apply filters when the selection changes.
     *
     * @return The configured {@link HBox} for account selection.
     */
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

    /**
     * Creates and configures the HBox pane containing filter TextFields (Date, Memo, Amount)
     * and an "Apply" button to trigger filtering.
     *
     * @return The configured {@link HBox} for filtering controls.
     */
    private HBox filterPane() {
        HBox box = new HBox(10);
        box.setPadding(new Insets(5));
        box.setStyle(
            "-fx-border-color: lightgray; -fx-border-radius: 4; -fx-border-insets: 4; -fx-border-style: segments(4)");
        Button apply = new Button("Apply Filters"); // Changed label for clarity
        apply.setOnAction(e -> applyFilters());
        box.getChildren().addAll(
            new Label("Filter by Date:"), this.filterDateField,
            new Label("Memo contains:"), this.filterMemoField,
            new Label("Amount equals:"), this.filterAmountField,
            apply);
        return box;
    }

    /**
     * Creates and configures the HBox pane for action buttons at the bottom of the panel.
     * Currently includes "Reconcile" and "Import Statement" buttons with placeholder actions.
     *
     * @return The configured {@link HBox} containing action buttons.
     */
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

    /**
     * Configures the columns for the transactions {@link TableView}.
     * Sets up columns for Date, Description, Amount, Balance, and Memo,
     * binding them to the properties of the {@link TransactionRow} class.
     * Sets the table's items to the {@link #backingList} and defines a column resize policy.
     * The {@code @SuppressWarnings({ "unchecked", "deprecation" })} might be related to raw type usage
     * or specific cell value factory patterns if an older JavaFX version was targeted,
     * but seems generally okay with PropertyValueFactory or direct lambdas for modern JavaFX.
     * The deprecation warning is likely related to the direct use of field names in PropertyValueFactory
     * if not strictly matching bean property naming conventions, but lambdas avoid this.
     * Here, lambdas are used for cell value factories, which is type-safe.
     */
    @SuppressWarnings({ "unchecked", "deprecation" }) // Review if still necessary with lambda cell factories
    private void configureTable() {
        TableColumn<TransactionRow, String> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(d -> d.getValue().dateProperty()); // Use property for binding

        TableColumn<TransactionRow, String> descCol = new TableColumn<>("Description");
        descCol.setCellValueFactory(d -> d.getValue().descriptionProperty());

        TableColumn<TransactionRow, BigDecimal> amtCol = new TableColumn<>("Amount");
        amtCol.setCellValueFactory(d -> d.getValue().amountProperty());

        TableColumn<TransactionRow, BigDecimal> balCol = new TableColumn<>("Balance");
        balCol.setCellValueFactory(d -> d.getValue().balanceProperty());

        TableColumn<TransactionRow, String> memoCol = new TableColumn<>("Memo");
        memoCol.setCellValueFactory(d -> d.getValue().memoProperty());

        this.table.getColumns().setAll(dateCol, descCol, amtCol, balCol, memoCol); // Use setAll to replace existing columns
        this.table.setItems(this.backingList);
        this.table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
    }

    /* ───────────────────────── Logic ───────────────────────── */
    // BigDecimal amountFilter = null; // Field moved to class level

    /**
     * Applies the currently selected filters (account, date, memo, amount) to the
     * master list of transactions ({@link #transactions}) and updates the
     * {@link #backingList} of the table with the filtered results.
     * Each transaction is converted to a {@link TransactionRow} for display.
     */
    private void applyFilters() {
        String acct = this.accountSelector.getValue(); // Selected account name from ComboBox
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
    /**
     * Represents a single row in the account activity table.
     * This class uses JavaFX properties ({@link SimpleStringProperty}, {@link SimpleObjectProperty})
     * to enable data binding with the {@link TableView} columns.
     */
    public static class TransactionRow {
        /** The date of the transaction. */
        final SimpleStringProperty date;
        /** The description of the transaction (often the payee or a general description). */
        final SimpleStringProperty description;
        /** The amount of the transaction relevant to the selected account. */
        final SimpleObjectProperty<BigDecimal> amount;
        /** The running balance of the account after this transaction. */
        final SimpleObjectProperty<BigDecimal> balance;
        /** The memo associated with the transaction. */
        final SimpleStringProperty memo;

        /**
         * Constructs a {@code TransactionRow} from an {@link AccountingTransaction}.
         * It populates the row's properties based on the transaction's details.
         * The 'amount' is the transaction's total amount (typically sum of debits),
         * and 'balance' is the account's balance *after* this transaction
         * (though {@code t.countAccountBalance()} might represent the account's overall current balance,
         * not a running balance for this specific row in historical context; this needs clarification
         * based on {@code countAccountBalance} implementation).
         *
         * @param t The {@link AccountingTransaction} to represent as a table row. Assumed non-null.
         */
        TransactionRow(AccountingTransaction t) {
            // Assumes 't' is non-null due to filter(Objects::nonNull) in applyFilters()
            this.date = new SimpleStringProperty(Objects.toString(t.getDate(), ""));
            // Description for the row: uses transaction's description if available, otherwise its memo.
            // This differs from original column mapping which might have shown one or the other.
            String desc = t.getDescription();
            if (desc == null || desc.trim().isEmpty()) {
                desc = t.getMemo();
            }
            this.description = new SimpleStringProperty(Objects.toString(desc, ""));

            BigDecimal totalAmount = t.getTotalAmount(); // This is often sum of debits. For activity, might need entry-specific amount.
            this.amount = new SimpleObjectProperty<>(totalAmount != null ? totalAmount : BigDecimal.ZERO);

            // The 'balance' here is derived from t.countAccountBalance().
            // If this represents the account's total balance *after* this transaction in a chronological list, it's a running balance.
            // If it's just the current balance of the account object 't.getAccount()', it's not a running balance for the row.
            // The Javadoc for countAccountBalance() should clarify this. For now, assuming it's intended as a relevant balance.
            BigDecimal accountBalance = t.countAccountBalance();
            this.balance = new SimpleObjectProperty<>(accountBalance != null ? accountBalance : BigDecimal.ZERO);

            this.memo = new SimpleStringProperty(Objects.toString(t.getMemo(), ""));
        }

        // Property getter methods for TableView CellValueFactory
        /** Returns the date property. @return the date property */
        public SimpleStringProperty dateProperty() { return date; }
        /** Returns the description property. @return the description property */
        public SimpleStringProperty descriptionProperty() { return description; }
        /** Returns the amount property. @return the amount property */
        public SimpleObjectProperty<BigDecimal> amountProperty() { return amount; }
        /** Returns the balance property. @return the balance property */
        public SimpleObjectProperty<BigDecimal> balanceProperty() { return balance; }
        /** Returns the memo property. @return the memo property */
        public SimpleStringProperty memoProperty() { return memo; }
    }
}
