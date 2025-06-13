
package nonprofitbookkeeping.ui.panels;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import nonprofitbookkeeping.model.Account; 
import nonprofitbookkeeping.model.AccountingTransaction;
import nonprofitbookkeeping.model.ChartOfAccounts; 
import nonprofitbookkeeping.model.Company;
import nonprofitbookkeeping.model.CurrentCompany;
import nonprofitbookkeeping.model.Ledger;
import nonprofitbookkeeping.model.CurrentCompany.CompanyChangeListener;

/**
 * A JavaFX panel that displays account activity (transactions) from a given {@link Ledger}.
 * It provides UI controls for selecting an account and filtering transactions by date,
 * memo, and amount. The transactions are displayed in a {@link TableView}.
 * This class is a JavaFX rewrite of an earlier Swing component.
 */
public class AccountsActivityPanelFX extends BorderPane
{
	
	/* ───────────────────────── UI fields ───────────────────────── */
	/** ComboBox for selecting the account whose activity is to be displayed. */
	private final ComboBox<String> accountSelector = new ComboBox<>();
	/** TextField for entering a date string to filter transactions. */
	private final TextField filterDateField = new TextField();
	/** TextField for entering a memo string (case-insensitive) to filter transactions. */
	private final TextField filterMemoField = new TextField();
	/** TextField for entering an amount to filter transactions (exact match). */
	private final TextField filterAmountField = new TextField();
	private Button applyFiltersButton; // Field for the button
	
	/** TableView to display the filtered account transactions. */
	private final TableView<TransactionRow> table = new TableView<>();
	/** ObservableList that backs the {@code table}, holding {@link TransactionRow} objects. */
	private final ObservableList<TransactionRow> backingList = FXCollections.observableArrayList();
	
	private List<AccountingTransaction> transactions; 
	// Made non-final to allow potential re-init,
	// though not done in this step
	
	/** Stores the parsed BigDecimal value from the amount filter field ({@link #filterAmountField}).
	 *  It is updated by {@link #applyFilters()} and used in the filtering predicate.
	 *  Will be {@code null} if the amount filter field is empty or contains an invalid number format. */
	private BigDecimal amountFilter = null; 
	// Renamed from the local variable in applyFilters to be
	// a field
	
	private AccountsActivityPanelCompanyListener companyListener;

	private HBox bottomButtonBar;
	
	
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
	public AccountsActivityPanelFX(Ledger ledger)
	{
		this.transactions = ledger != null && ledger.getTransactions() != null ?
			ledger.getTransactions() : FXCollections.emptyObservableList();
		setPadding(new Insets(10));
		
		HBox selectorPane = selectorPane(); // Builds and returns the HBox for account selector
		HBox filterPane = filterPane(); // Builds and returns the HBox for filters (includes
										// applyFiltersButton)
		
		VBox top = new VBox(10);
		top.getChildren().addAll(selectorPane, filterPane);
		setTop(top);
		
		configureTable();
		TitledPane titledTablePane = new TitledPane("Ledger", this.table);
		titledTablePane.setCollapsible(false);
		setCenter(titledTablePane);
		
		this.bottomButtonBar = buttonBar(); // Store the bottom button bar
		setBottom(this.bottomButtonBar);
		
		this.companyListener = new AccountsActivityPanelCompanyListener(this);
		CurrentCompany.CompanyListener.addCompanyListener(this.companyListener);
		
		handleCompanyChange(CurrentCompany.isOpen());
	}
	
	/* ───────────────────────── Builders ───────────────────────── */
	
	/**
	 * Creates and configures the HBox pane containing the account selector ComboBox.
	 * Populates the ComboBox with account names from the current company's Chart of Accounts.
	 * Sets an action on the ComboBox to re-apply filters when the selection changes.
	 *
	 * @return The configured {@link HBox} for account selection.
	 */
	private HBox selectorPane()
	{
		HBox box = new HBox(10);
		box.setPadding(new Insets(5));
		box.setStyle(
			"-fx-border-color: lightgray; -fx-border-radius: 4; -fx-border-insets: 4; -fx-border-style: segments(4)");

		this.accountSelector.setOnAction(e -> applyFilters());
		box.getChildren().addAll(new Label("Account:"), this.accountSelector);
		return box;
	}
	
	private void populateAccountSelector()
	{
		this.accountSelector.getItems().clear();
		Company company = CurrentCompany.getCompany();
		
		if (CurrentCompany.isOpen() && company != null && company.getChartOfAccounts() != null)
		{
			ChartOfAccounts coa = company.getChartOfAccounts();
			
			if (coa.getAccounts() != null)
			{
				List<String> accountNames = coa.getAccounts().stream()
					.map(Account::getName)
					.filter(Objects::nonNull)
					.sorted()
					.collect(Collectors.toList());
				this.accountSelector.getItems().addAll(accountNames);
			}
			
		}
		
		if (!this.accountSelector.getItems().isEmpty())
		{
			this.accountSelector.getSelectionModel().selectFirst();
		}
		else
		{
			this.accountSelector.setPlaceholder(new Label("No accounts available"));
		}
		
	}
	
	/**
	 * Creates and configures the HBox pane containing filter TextFields (Date, Memo, Amount)
	 * and an "Apply" button to trigger filtering.
	 *
	 * @return The configured {@link HBox} for filtering controls.
	 */
	private HBox filterPane()
	{
		HBox box = new HBox(10);
		box.setPadding(new Insets(5));
		box.setStyle(
			"-fx-border-color: lightgray; -fx-border-radius: 4; -fx-border-insets: 4; -fx-border-style: segments(4)");
		this.applyFiltersButton = new Button("Apply Filters");
		this.applyFiltersButton.setOnAction(e -> applyFilters());
		box.getChildren().addAll(
			new Label("Filter by Date:"), this.filterDateField,
			new Label("Memo contains:"), this.filterMemoField,
			new Label("Amount equals:"), this.filterAmountField,
			this.applyFiltersButton);
		return box;
	}
	
	/**
	 * Creates and configures the HBox pane for action buttons at the bottom of the panel.
	 * Currently includes "Reconcile" and "Import Statement" buttons with placeholder actions.
	 *
	 * @return The configured {@link HBox} containing action buttons.
	 */
	private static HBox buttonBar()
	{
		HBox box = new HBox(10);
		box.setPadding(new Insets(10));
		Button reconcile = new Button("Reconcile");
		reconcile.setOnAction(
			e -> new Alert(Alert.AlertType.INFORMATION, 
				"Reconciliation process would start here.")
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
	@SuppressWarnings({ "unchecked" }) // Review if still necessary with lambda cell factories
	private void configureTable()
	{
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

		// Use setAll to replace existing columns
		this.table.getColumns().
			setAll(dateCol, descCol, amtCol, balCol, memoCol);
		
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
	private void applyFilters()
	{		
		// Guard: Only apply filters if a company is open, otherwise table should be
		// empty.
		if (!CurrentCompany.isOpen())
		{
			this.backingList.clear();
			return;
		}
		
		String acct = this.accountSelector.getValue();
		String dateFilterStr = this.filterDateField.getText().trim();
		String memoFilterStr = this.filterMemoField.getText().trim().toLowerCase();
		String amountText = this.filterAmountField.getText().trim();
		
		this.amountFilter = null; // Reset before trying to parse
		
		try
		{
			
			if (!amountText.isEmpty())
			{
				this.amountFilter = new BigDecimal(amountText);
			}
			
		}
		catch (NumberFormatException ignore)
		{
			System.err.println("Invalid amount format in filter: " + amountText);
		}
		
		Predicate<AccountingTransaction> predicate = t -> {
			return accountingTransactionPred(acct, dateFilterStr, memoFilterStr, t);
		};
		
		this.backingList.clear();
		
		// this.transactions can be null if constructor was called with null ledger.
		if (this.transactions != null)
		{
			List<AccountingTransaction> filtered =
				this.transactions.stream()
					.filter(Objects::nonNull)
					.filter(predicate)
					.collect(Collectors.toList());
			this.backingList
				.setAll(filtered.stream().map(TransactionRow::new)
					.collect(Collectors.toList()));
		}
		
	}

	/**
	 * accountingTransactionPred
	 * 
	 * @param acct
	 * @param dateFilterStr
	 * @param memoFilterStr
	 * @param t
	 * 
	 * @return
	 */
	boolean accountingTransactionPred
	(
	 	String acct, 
	 	String dateFilterStr, 
	 	String memoFilterStr,
	 	AccountingTransaction t)
	{
		
		if (t == null)
			return false;
		if (acct == null)
			return false;
		
		String accountName = t.getAccountName();
		if (!Objects.equals(accountName, acct))
			return false;
		String transactionDate = t.getDate();
		if (!dateFilterStr.isEmpty() &&
			(transactionDate == null || !transactionDate.contains(dateFilterStr)))
			return false;
		String transactionMemo = t.getMemo();
		if (!memoFilterStr.isEmpty() &&
			(transactionMemo == null || !transactionMemo.toLowerCase().contains(memoFilterStr)))
			return false;
			
		if (this.amountFilter != null)
		{
			BigDecimal totalAmount = t.getTotalAmount();
			if (totalAmount == null || totalAmount.compareTo(this.amountFilter) != 0)
				return false;
		}
		
		return true;
		
	}
	
	/**
	 * Handle Company Change
	 * @param isOpen
	 */
	private void handleCompanyChange(boolean isOpen)
	{
		this.accountSelector.setDisable(!isOpen);
		this.filterDateField.setDisable(!isOpen);
		this.filterMemoField.setDisable(!isOpen);
		this.filterAmountField.setDisable(!isOpen);
		
		if (this.applyFiltersButton != null)
		{ // applyFiltersButton might not be initialized if constructor fails early
			this.applyFiltersButton.setDisable(!isOpen);
		}
		
		if (this.bottomButtonBar != null)
		{
			this.bottomButtonBar.getChildren().forEach(node -> node.setDisable(!isOpen));
		}
		
		if (isOpen)
		{
			populateAccountSelector();
			// If the ledger (and thus this.transactions) itself should change with the
			// company, this.transactions would need to be updated here from
			
			// CurrentCompany.getCompany().getLedger().
			// For now, it uses the ledger provided at construction.
			// If CurrentCompany's ledger is the one to use, then:
			// Company current = CurrentCompany.getCompany();
			// if (current != null && current.getLedger() != null) {
			// this.transactions = current.getLedger().getTransactions();
			// } else {
			// this.transactions = FXCollections.emptyObservableList();
			// }
			applyFilters(); 
			// This will refresh the table with potentially new accounts list and
			// existing transactions
		}
		else
		{
			this.accountSelector.getItems().clear();
			this.accountSelector.setPlaceholder(new Label("No company open"));
			this.filterDateField.clear();
			this.filterMemoField.clear();
			this.filterAmountField.clear();
			this.backingList.clear();
		}
		
	}
	
	/**
	 * AccountsActivityPanelCompanyListener
	 */
	private class AccountsActivityPanelCompanyListener implements CompanyChangeListener
	{
		private AccountsActivityPanelFX panel;
		
		public AccountsActivityPanelCompanyListener(AccountsActivityPanelFX panel)
		{
			this.panel = panel;
		}
		
		@Override public void companyChange(boolean isOpen)
		{
			panel.handleCompanyChange(isOpen);
		}
		
	}
	
	/**
	 * This class uses JavaFX properties ({@link SimpleStringProperty}, {@link SimpleObjectProperty})
	 * to enable data binding with the {@link TableView} columns.
	 */
	public static class TransactionRow
	{
		final SimpleStringProperty date;
		final SimpleStringProperty description;
		final SimpleObjectProperty<BigDecimal> amount;
		final SimpleObjectProperty<BigDecimal> balance;
		final SimpleStringProperty memo;
		
		TransactionRow(AccountingTransaction t)
		{
			this.date = new SimpleStringProperty(Objects.toString(t.getDate(), ""));
			String desc = t.getDescription();
			
			if (desc == null || desc.trim().isEmpty())
			{
				desc = t.getMemo();
			}
			
			this.description = new SimpleStringProperty(Objects.toString(desc, ""));
			BigDecimal totalAmount = t.getTotalAmount();
			this.amount =
				new SimpleObjectProperty<>(totalAmount != null ? totalAmount : BigDecimal.ZERO);
			BigDecimal accountBalance = t.countAccountBalance();
			this.balance = new SimpleObjectProperty<>(
				accountBalance != null ? accountBalance : BigDecimal.ZERO);
			this.memo = new SimpleStringProperty(Objects.toString(t.getMemo(), ""));
		}
		
		public SimpleStringProperty dateProperty()
		{
			return date;
		}
		
		public SimpleStringProperty descriptionProperty()
		{
			return description;
		}
		
		public SimpleObjectProperty<BigDecimal> amountProperty()
		{
			return amount;
		}
		
		public SimpleObjectProperty<BigDecimal> balanceProperty()
		{
			return balance;
		}
		
		public SimpleStringProperty memoProperty()
		{
			return memo;
		}
		
	}
	
}

