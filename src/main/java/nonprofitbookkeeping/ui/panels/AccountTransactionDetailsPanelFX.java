
package nonprofitbookkeeping.ui.panels;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.MonthDay;
import java.util.List;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Comparator;
import java.util.stream.Collectors;
import nonprofitbookkeeping.util.FormatUtils;
import java.util.logging.Logger;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.ObjectProperty;
import javafx.scene.control.Tooltip;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.Callback;

import nonprofitbookkeeping.model.Account;
import nonprofitbookkeeping.model.Company;
import nonprofitbookkeeping.model.CurrentCompany;
import nonprofitbookkeeping.model.ChartOfAccounts;
import nonprofitbookkeeping.model.Ledger;
import nonprofitbookkeeping.model.AccountSide;
import nonprofitbookkeeping.model.AccountingEntry;
import nonprofitbookkeeping.model.AccountingTransaction;
import nonprofitbookkeeping.model.CurrentCompany.CompanyChangeListener;
import nonprofitbookkeeping.ui.helpers.AlertBox;
import nonprofitbookkeeping.model.ReportPeriodPreset;

/**
 * A JavaFX {@link BorderPane} that displays transaction details for a selected account
 * within a specified date range. It allows users to choose an account and date range,
 * then loads and shows relevant transactions in a {@link TableView}.
 * The panel also displays summary totals for debits, credits, and net change for the period.
 * It listens to company changes to refresh its state.
 */
public class AccountTransactionDetailsPanelFX extends BorderPane
{
	private static final Logger LOGGER =
		Logger.getLogger(AccountTransactionDetailsPanelFX.class.getName());
	/** ComboBox for selecting the account whose transactions are to be displayed. */
	private ComboBox<Account> accountSelectorComboBox;
	/** DatePicker for selecting the start date of the transaction period. */
	private DatePicker startDatePicker;
	/** DatePicker for selecting the end date of the transaction period. */
	private DatePicker endDatePicker;
	/** Button to trigger loading of transactions based on selected criteria. */
	private Button loadTransactionsButton;
	/** Button to refresh the table using the currently selected criteria. */
	private Button refreshButton;
	/** Button menu offering quick report ranges. */
	private MenuButton quickRangeButton;
	/** TableView to display the transaction details. */
	private TableView<TransactionDisplayRow> transactionsTable;
	/** ObservableList holding the {@link TransactionDisplayRow} objects for the table. */
	private ObservableList<TransactionDisplayRow> transactionDataList;
	
	/** Label to display the total debit amount for the selected period and account. */
	private Label totalDebitsLabel;
	/** Label to display the total credit amount for the selected period and account. */
	private Label totalCreditsLabel;
	/** Label to display the net change (debits - credits) for the selected period and account. */
	private Label netChangeLabel;
	
	/** Listener for changes in the currently open company, to refresh UI elements. */
	private CompanyChangeListener companyChangeListener;
	/** Tracks whether {@link #companyChangeListener} is currently registered. */
	private boolean companyChangeListenerRegistered;
	
	/**
	 * Constructs a new {@code AccountTransactionDetailsPanelFX}.
	 * Initializes the UI components including account selector, date pickers,
	 * transaction table, and summary labels. It also sets up a listener
	 * for company changes to update the account selector and clear data.
	 * The transaction table is initially empty, prompting the user to select criteria and load data.
	 */
	public AccountTransactionDetailsPanelFX()
	{
		setPadding(new Insets(10));
		
		// TOP: Controls
		GridPane controlsGrid = new GridPane();
		controlsGrid.setHgap(10);
		controlsGrid.setVgap(8);
		controlsGrid.setPadding(new Insets(5));
		
		this.accountSelectorComboBox = new ComboBox<>();
		this.accountSelectorComboBox.setPromptText("Select Account");
		this.accountSelectorComboBox
			.setTooltip(
				new Tooltip("Choose an account to review its transactions."));
		
		refreshAccountSelector();
		
		this.accountSelectorComboBox.setOnAction(e -> {
			this.transactionDataList.clear();
			this.transactionsTable
				.setPlaceholder(
					new Label(
						"Account selection changed. Click 'Load Transactions'."));
			this.totalDebitsLabel.setText(
				"Total Debits: " + FormatUtils.formatCurrency(BigDecimal.ZERO));
			this.totalCreditsLabel.setText("Total Credits: " +
				FormatUtils.formatCurrency(BigDecimal.ZERO));
			this.netChangeLabel.setText(
				"Net Change: " + FormatUtils.formatCurrency(BigDecimal.ZERO));
		});
		
		this.startDatePicker = new DatePicker();
		this.startDatePicker
			.setTooltip(new Tooltip("Start of the reporting period."));
		this.endDatePicker = new DatePicker();
		this.endDatePicker
			.setTooltip(new Tooltip("End of the reporting period."));
		this.loadTransactionsButton = new Button("Load Transactions");
		this.loadTransactionsButton.setTooltip(
			new Tooltip("Load transactions for the selected criteria."));
		this.loadTransactionsButton.setOnAction(e -> loadTransactionData());
		this.refreshButton = new Button("Refresh");
		this.refreshButton.setTooltip(new Tooltip(
			"Reload transactions while keeping the selected filters."));
		this.refreshButton.setOnAction(e -> refresh());
		
		controlsGrid.add(new Label("Account:"), 0, 0);
		controlsGrid.add(this.accountSelectorComboBox, 1, 0, 2, 1);
		controlsGrid.add(new Label("Start Date:"), 0, 1);
		controlsGrid.add(this.startDatePicker, 1, 1);
		controlsGrid.add(new Label("End Date:"), 0, 2);
		controlsGrid.add(this.endDatePicker, 1, 2);
		controlsGrid.add(this.loadTransactionsButton, 2, 2);
		controlsGrid.add(this.refreshButton, 3, 2);
		this.quickRangeButton = new MenuButton("Quick Ranges");
		this.quickRangeButton
			.setTooltip(new Tooltip("Apply preset date ranges."));
		this.quickRangeButton.setVisible(false);
		this.quickRangeButton.setManaged(false);
		controlsGrid.add(this.quickRangeButton, 4, 2);
		
		ScrollPane controlsScrollPane = new ScrollPane(controlsGrid);
		controlsScrollPane.setFitToWidth(true);
		controlsScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
		controlsScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
		setTop(controlsScrollPane);
		
		this.transactionDataList = FXCollections.observableArrayList();
		this.transactionsTable = new TableView<>(this.transactionDataList);
		this.transactionsTable
			.setColumnResizePolicy(
				TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
		this.transactionsTable.setPlaceholder(
			new Label(
				"Select account and date range, then click 'Load Transactions'."));
		setCenter(this.transactionsTable);
		
		// Set up column data
		setupTableColumns();
		
		HBox totalsBox = new HBox(20);
		totalsBox.setPadding(new Insets(10, 0, 0, 0));
		this.totalDebitsLabel = new Label(
			"Total Debits: " + FormatUtils.formatCurrency(BigDecimal.ZERO));
		this.totalCreditsLabel = new Label(
			"Total Credits: " + FormatUtils.formatCurrency(BigDecimal.ZERO));
		this.netChangeLabel = new Label(
			"Net Change: " + FormatUtils.formatCurrency(BigDecimal.ZERO));
		totalsBox.getChildren().addAll(this.totalDebitsLabel,
			this.totalCreditsLabel,
			this.netChangeLabel);
		setBottom(totalsBox);
		
		setupCompanyChangeListener(); // Call to setup listener
		
	}
	
	/** Applies the default reporting period from settings. */
	public void applyDefaultPeriod(ReportPeriodPreset preset,
		MonthDay fiscalYearStart)
	{
		
		if (preset == null)
		{
			return;
		}
		
		ReportPeriodPreset.DateRange range =
			preset.resolve(LocalDate.now(), fiscalYearStart);
		this.startDatePicker.setValue(range.getStart());
		this.endDatePicker.setValue(range.getEnd());
		
	}
	
	/** Configures visibility and actions for quick range presets. */
	public void configureQuickRanges(boolean showYearToDate,
		boolean showFullYear, boolean showLastMonth,
		MonthDay fiscalYearStart)
	{
		
		if (this.quickRangeButton == null)
		{
			return;
		}
		
		this.quickRangeButton.getItems().clear();
		MonthDay start =
			fiscalYearStart != null ? fiscalYearStart : MonthDay.of(1, 1);
		
		if (showYearToDate)
		{
			MenuItem item = new MenuItem("Year to Date");
			item.setOnAction(e -> applyDefaultPeriod(
				ReportPeriodPreset.YEAR_TO_DATE, start));
			this.quickRangeButton.getItems().add(item);
		}
		
		if (showFullYear)
		{
			MenuItem item = new MenuItem("Full Fiscal Year");
			item.setOnAction(
				e -> applyDefaultPeriod(ReportPeriodPreset.FULL_YEAR, start));
			this.quickRangeButton.getItems().add(item);
		}
		
		if (showLastMonth)
		{
			MenuItem item = new MenuItem("Last Month");
			item.setOnAction(
				e -> applyDefaultPeriod(ReportPeriodPreset.LAST_MONTH, start));
			this.quickRangeButton.getItems().add(item);
		}
		
		boolean visible = !this.quickRangeButton.getItems().isEmpty();
		this.quickRangeButton.setVisible(visible);
		this.quickRangeButton.setManaged(visible);
		
	}
	
	/**
	 * Sets up the columns for the {@link #transactionsTable}.
	 * Defines columns for Date, Transaction ID, Description, Debit, Credit, and Running Balance,
	 * and binds them to the properties of the {@link TransactionDisplayRow} class.
	 * Sets preferred widths and cell alignments for some columns.
	 */
	private void setupTableColumns()
	{
		this.transactionsTable.getColumns().clear();
		
		TableColumn<TransactionDisplayRow, String> dateCol =
			new TableColumn<>("Date");
		dateCol.setCellValueFactory(new PropertyValueFactory<>("date"));
		dateCol.setPrefWidth(100);
		
		TableColumn<TransactionDisplayRow, String> idCol =
			new TableColumn<>("Transaction ID");
		idCol.setCellValueFactory(new PropertyValueFactory<>("transactionId"));
		idCol.setPrefWidth(120);
		
		TableColumn<TransactionDisplayRow, String> descCol =
			new TableColumn<>("Description");
		descCol.setCellValueFactory(new PropertyValueFactory<>("description"));
		descCol.setPrefWidth(250);
		
		TableColumn<TransactionDisplayRow, String> toFromCol =
			new TableColumn<>("To/From");
		toFromCol.setCellValueFactory(new PropertyValueFactory<>("toFrom"));
		toFromCol.setPrefWidth(120);
		
		TableColumn<TransactionDisplayRow, String> checkCol =
			new TableColumn<>("Check #");
		checkCol.setCellValueFactory(new PropertyValueFactory<>("checkNumber"));
		checkCol.setPrefWidth(80);
		
		TableColumn<TransactionDisplayRow, String> clearBankCol =
			new TableColumn<>("Clear Bank");
		clearBankCol
			.setCellValueFactory(new PropertyValueFactory<>("clearBank"));
		clearBankCol.setPrefWidth(100);
		
		TableColumn<TransactionDisplayRow, String> budgetCol =
			new TableColumn<>("Budget Tracking");
		budgetCol
			.setCellValueFactory(new PropertyValueFactory<>("budgetTracking"));
		budgetCol.setPrefWidth(120);
		
		TableColumn<TransactionDisplayRow, String> fundNameCol =
			new TableColumn<>("Fund Name");
		fundNameCol.setCellValueFactory(new PropertyValueFactory<>("fundName"));
		fundNameCol.setPrefWidth(120);
		
		TableColumn<TransactionDisplayRow, String> fundNumCol =
			new TableColumn<>("Fund #");
		fundNumCol
			.setCellValueFactory(new PropertyValueFactory<>("fundNumber"));
		fundNumCol.setPrefWidth(80);
		
		TableColumn<TransactionDisplayRow, BigDecimal> debitCol =
			new TableColumn<>("Debit");
		debitCol.setCellValueFactory(new PropertyValueFactory<>("debit"));
		debitCol.setPrefWidth(100);
		debitCol.setStyle("-fx-alignment: CENTER-RIGHT;");
		
		TableColumn<TransactionDisplayRow, BigDecimal> creditCol =
			new TableColumn<>("Credit");
		creditCol.setCellValueFactory(new PropertyValueFactory<>("credit"));
		creditCol.setPrefWidth(100);
		creditCol.setStyle("-fx-alignment: CENTER-RIGHT;");
		
		TableColumn<TransactionDisplayRow, BigDecimal> balanceCol =
			new TableColumn<>("Running Balance");
		balanceCol
			.setCellValueFactory(new PropertyValueFactory<>("runningBalance"));
		balanceCol.setPrefWidth(120);
		balanceCol.setStyle("-fx-alignment: CENTER-RIGHT;");
		
		this.transactionsTable.getColumns().addAll(dateCol, idCol, descCol,
			toFromCol, checkCol, clearBankCol, budgetCol, fundNameCol,
			fundNumCol, debitCol, creditCol, balanceCol);
		
	}
	
	/**
	 * Refreshes the account selector with accounts from the currently open company.
	 * If no company is open or the chart of accounts is empty, the selector
	 * prompt will indicate that no accounts are available.
	 */
	private void refreshAccountSelector()
	{
		this.accountSelectorComboBox.getItems().clear();
		this.accountSelectorComboBox.setValue(null);
		
		Company company = CurrentCompany.getCompany();
		
		if (company != null && company.getChartOfAccounts() != null)
		{
			ChartOfAccounts coa = company.getChartOfAccounts();
			
			if (coa.getAccounts() != null)
			{
				List<Account> sortedAccounts =
					coa.getAccounts().stream().filter(Objects::nonNull)
						.sorted(Comparator.comparing(Account::getName,
							String.CASE_INSENSITIVE_ORDER))
						.collect(Collectors.toList());
				
				this.accountSelectorComboBox
					.setItems(
						FXCollections.observableArrayList(sortedAccounts));
				
				Callback<ListView<Account>, ListCell<Account>> cellFactory =
					lv -> new ListCell<Account>()
					{
						@Override
						protected void updateItem(Account item, boolean empty)
						{
							super.updateItem(item, empty);
							setText(empty ? null :
								item.getName() + " (" +
									item.getAccountNumber() + ")");
							
						}
						
					};
				
				this.accountSelectorComboBox.setCellFactory(cellFactory);
				this.accountSelectorComboBox
					.setButtonCell(cellFactory.call(null));
			}
			
		}
		
		if (this.accountSelectorComboBox.getItems().isEmpty())
		{
			this.accountSelectorComboBox.setPromptText("No accounts in COA");
		}
		else
		{
			this.accountSelectorComboBox.setPromptText("Select Account");
		}
		
	}
	
	
	/**
	 * Loads transaction data into the {@link #transactionsTable} based on the
	 * account selected in {@link #accountSelectorComboBox} and the date range
	 * from {@link #startDatePicker} and {@link #endDatePicker}.
	 * <p>
	 * It performs the following steps:
	 * <ol>
	 *   <li>Validates that an account and both start/end dates are selected. Shows error alerts if not.</li>
	 *   <li>Retrieves the current company, ledger, and chart of accounts. Shows error if unavailable.</li>
	 *   <li>Calculates the opening balance for the selected account as of the day before the start date.</li>
	 *   <li>Filters transactions from the ledger that fall within the selected date range and involve the selected account.</li>
	 *   <li>Sorts these filtered transactions by date and booking timestamp.</li>
	 *   <li>Creates a {@link TransactionDisplayRow} for each relevant transaction entry, calculating debit/credit amounts
	 *       and a running balance.</li>
	 *   <li>Populates the table with these display rows.</li>
	 *   <li>Updates the total debits, total credits, and net change labels for the period.</li>
	 *   <li>Sets a placeholder message if no transactions are found.</li>
	 * </ol>
	 * </p>
	 */
	private void loadTransactionData()
	{
		this.transactionDataList.clear();
		
		Account selectedAccount = this.accountSelectorComboBox.getValue();
		LocalDate startDate = this.startDatePicker.getValue();
		LocalDate endDate = this.endDatePicker.getValue();
		
		if (selectedAccount == null)
		{
			AlertBox.showError(this.getScene().getWindow(),
				"Please select an account.");
			return;
		}
		
		if (startDate == null || endDate == null)
		{
			AlertBox.showError(this.getScene().getWindow(),
				"Please select both a start and end date.");
			return;
		}
		
		if (endDate.isBefore(startDate))
		{
			AlertBox.showError(this.getScene().getWindow(),
				"End date cannot be before start date.");
			return;
		}
		
		Company company = CurrentCompany.getCompany();
		
		if (company == null || company.getLedger() == null ||
			company.getChartOfAccounts() == null)
		{
			AlertBox.showError(this.getScene().getWindow(),
				"Company data not available.");
			this.transactionsTable
				.setPlaceholder(
					new Label("Company data or ledger not available."));
			return;
		}
		
		BigDecimal runningBalance =
			selectedAccount.getOpeningBalance() != null ?
				selectedAccount.getOpeningBalance() : BigDecimal.ZERO;
		
		Ledger ledger = company.getLedger();
		
		if (ledger.getTransactions() != null)
		{
			// Sort all transactions once by date for calculating opening
			// balance correctly
			List<AccountingTransaction> allTransactionsSorted =
				ledger.getTransactions().stream().filter(Objects::nonNull)
					.filter(tx -> tx.getDate() != null &&
						!tx.getDate().trim().isEmpty())
					.sorted(Comparator.comparing(AccountingTransaction::getDate)
						.thenComparingLong(
							AccountingTransaction::getBookingDateTimestamp))
					.collect(Collectors.toList());
			
			for (AccountingTransaction tx : allTransactionsSorted)
			{
				
				try
				{
					LocalDate txDate = LocalDate.parse(tx.getDate());
					
					if (txDate.isBefore(startDate))
					{
						
						for (AccountingEntry entry : tx.getEntries())
						{
							
							if (entry.getAccount() != null &&
								entry.getAccount().getAccountNumber()
									.equals(selectedAccount.getAccountNumber()))
							{
								BigDecimal amount =
									entry.getAmount() != null ?
										entry.getAmount() :
										BigDecimal.ZERO;
								
								if (entry.getAccountSide() == AccountSide.DEBIT)
								{
									runningBalance = runningBalance.add(amount);
								}
								else
								{
									runningBalance =
										runningBalance.subtract(amount);
								}
								
							}
							
						}
						
					}
					
				}
				catch (java.time.format.DateTimeParseException e)
				{
					System.err
						.println(
							"Could not parse transaction date for opening balance calc: " +
								tx.getDate() + " for TX ID: " +
								tx.getBookingDateTimestamp());
				}
				
			}
			
		}
		
		BigDecimal periodDebitTotal = BigDecimal.ZERO;
		BigDecimal periodCreditTotal = BigDecimal.ZERO;
		List<TransactionDisplayRow> displayRows = new ArrayList<>();
		
		if (ledger.getTransactions() != null)
		{
			List<AccountingTransaction> periodTransactions = new ArrayList<>();
			
			for (AccountingTransaction tx : ledger.getTransactions())
			{ // Iterate again, or use the sorted list if performance allows
				if (tx.getDate() == null || tx.getDate().trim().isEmpty())
					continue;
				
				try
				{
					LocalDate txDate = LocalDate.parse(tx.getDate());
					
					if (!txDate.isBefore(startDate) && !txDate.isAfter(endDate))
					{
						
						for (AccountingEntry entry : tx.getEntries())
						{
							
							if (entry.getAccount() != null &&
								entry.getAccount().getAccountNumber()
									.equals(selectedAccount.getAccountNumber()))
							{
								periodTransactions.add(tx);
								break;
							}
							
						}
						
					}
					
				}
				catch (java.time.format.DateTimeParseException e)
				{
					// Logged during opening balance or handle again if
					// necessary
				}
				
			}
			
			// Sort only the period transactions if not using the pre-sorted
			// full list
			periodTransactions
				.sort(Comparator.comparing(AccountingTransaction::getDate)
					.thenComparingLong(
						AccountingTransaction::getBookingDateTimestamp));
			
			for (AccountingTransaction tx : periodTransactions)
			{
				
				for (AccountingEntry entry : tx.getEntries())
				{
					
					if (entry.getAccount() != null &&
						entry.getAccount().getAccountNumber()
							.equals(selectedAccount.getAccountNumber()))
					{
						BigDecimal debitAmount = BigDecimal.ZERO;
						BigDecimal creditAmount = BigDecimal.ZERO;
						BigDecimal entryAmount =
							entry.getAmount() != null ? entry.getAmount() :
								BigDecimal.ZERO;
						
						if (entry.getAccountSide() == AccountSide.DEBIT)
						{
							debitAmount = entryAmount;
							runningBalance = runningBalance.add(debitAmount);
							periodDebitTotal =
								periodDebitTotal.add(debitAmount);
						}
						else
						{
							creditAmount = entryAmount;
							runningBalance =
								runningBalance.subtract(creditAmount);
							periodCreditTotal =
								periodCreditTotal.add(creditAmount);
						}
						
						displayRows.add(new TransactionDisplayRow(tx.getDate(),
							String.valueOf(tx
								.getBookingDateTimestamp()),
							tx.getMemo() != null ?
								tx.getMemo() : "",
							tx.getToFrom() != null ?
								tx.getToFrom() : "",
							tx.getCheckNumber() !=
								null ? tx.getCheckNumber() : "",
							tx.getClearBank() !=
								null ? tx.getClearBank() : "",
							tx.getBudgetTracking() !=
								null ? tx.getBudgetTracking() : "",
							tx.getAssociatedFundName() !=
								null ? tx.getAssociatedFundName() : "",
							entry.getFundNumber() != null ?
								entry.getFundNumber() :
								"",
							debitAmount, creditAmount,
							new BigDecimal(runningBalance
								.toString())));
					}
					
				}
				
			}
			
		}
		
		this.transactionDataList.addAll(displayRows);
		
		this.totalDebitsLabel.setText("Total Debits (Period): " +
			FormatUtils.formatCurrency(periodDebitTotal));
		this.totalCreditsLabel
			.setText("Total Credits (Period): " +
				FormatUtils.formatCurrency(periodCreditTotal));
		BigDecimal netChange = periodDebitTotal.subtract(periodCreditTotal);
		this.netChangeLabel.setText(
			"Net Change (Period): " + FormatUtils.formatCurrency(netChange));
		
		if (this.transactionDataList.isEmpty())
		{
			this.transactionsTable.setPlaceholder(
				new Label(
					"No transactions found for the selected account and date range."));
		}
		else
		{
			this.transactionsTable.setPlaceholder(null);
		}
		
	}
	
	/**
	 * Refreshes the table based on the currently selected account and date
	 * range. This simply re-invokes {@link #loadTransactionData()} so any
	 * newly added or edited transactions are shown.
	 */
	public void refresh()
	{
		loadTransactionData();
		
	}
	
	/**
	 * Initializes the {@link #companyChangeListener} and registers it with
	 * {@link CurrentCompany.CompanyListener}. The listener clears any loaded
	 * data and refreshes the account selector whenever the active company
	 * changes. If a new company is opened, the list of selectable accounts is
	 * repopulated from its chart of accounts.
	 */
	private void setupCompanyChangeListener()
	{
		
		if (this.companyChangeListenerRegistered)
		{
			return; // Already registered for company change notifications
		}
		
		if (this.companyChangeListener != null)
		{
			// Clean up any stale listener before creating a new one
			CurrentCompany.CompanyListener
				.removeCompanyListener(this.companyChangeListener);
		}
		
		this.companyChangeListener = new CompanyChangeListener()
		{
			@Override
			public void companyChange(boolean companyNowOpen)
			{
				AccountTransactionDetailsPanelFX.this.transactionDataList
					.clear();
				AccountTransactionDetailsPanelFX.this.transactionsTable
					.setPlaceholder(new Label(
						"Company changed. Select account and date range, then click 'Load Transactions'."));
				AccountTransactionDetailsPanelFX.this.totalDebitsLabel
					.setText("Total Debits: " +
						FormatUtils.formatCurrency(BigDecimal.ZERO));
				AccountTransactionDetailsPanelFX.this.totalCreditsLabel
					.setText("Total Credits: " +
						FormatUtils.formatCurrency(BigDecimal.ZERO));
				AccountTransactionDetailsPanelFX.this.netChangeLabel
					.setText("Net Change: " +
						FormatUtils.formatCurrency(BigDecimal.ZERO));
				
				refreshAccountSelector();
				
			}
			
		};
		CurrentCompany.CompanyListener
			.addCompanyListener(this.companyChangeListener);
		this.companyChangeListenerRegistered = true;
		
	}
	
	/**
	 * Unregisters this panel's company change listener from
	 * {@link CurrentCompany.CompanyListener}. This should be called when
	 * the panel is no longer needed to avoid memory leaks from dangling
	 * listeners.
	 */
	public void dispose()
	{
		
		if (this.companyChangeListener != null)
		{
			CurrentCompany.CompanyListener
				.removeCompanyListener(this.companyChangeListener);
			this.companyChangeListener = null;
		}
		
		this.companyChangeListenerRegistered = false;
		
	}
	
	/**
	 * Represents a single row of data to be displayed in the transaction details table.
	 * This class uses JavaFX properties to enable data binding with TableView columns.
	 */
	public static class TransactionDisplayRow
	{
		/** The date of the transaction. */
		private final StringProperty date;
		/** The unique identifier of the transaction (e.g., booking timestamp). */
		private final StringProperty transactionId;
		/** A description or memo for the transaction. */
		private final StringProperty description;
		/** Payee or counterparty information. */
		private final StringProperty toFrom;
		/** Check number associated with the transaction. */
		private final StringProperty checkNumber;
		/** Clearing bank information. */
		private final StringProperty clearBank;
		/** Budget tracking notes. */
		private final StringProperty budgetTracking;
		/** Associated fund name for this transaction. */
		private final StringProperty fundName;
		/** Fund number for the entry. */
		private final StringProperty fundNumber;
		/** The debit amount affecting the selected account in this transaction. */
		private final ObjectProperty<BigDecimal> debit;
		/** The credit amount affecting the selected account in this transaction. */
		private final ObjectProperty<BigDecimal> credit;
		/** The running balance of the selected account after this transaction. */
		private final ObjectProperty<BigDecimal> runningBalance;
		
		/**
		 * Constructs a new {@code TransactionDisplayRow}.
		 *
		 * @param date The transaction date string.
		 * @param transactionId The transaction ID string.
		 * @param description The transaction description.
		 * @param debit The debit amount for this row.
		 * @param credit The credit amount for this row.
		 * @param runningBalance The running balance after this transaction.
		 */
		public TransactionDisplayRow(String date, String transactionId,
			String description,
			String toFrom, String checkNumber, String clearBank,
			String budgetTracking, String fundName, String fundNumber,
			BigDecimal debit, BigDecimal credit, BigDecimal runningBalance)
		{
			this.date = new SimpleStringProperty(date);
			this.transactionId = new SimpleStringProperty(transactionId);
			this.description = new SimpleStringProperty(description);
			this.toFrom = new SimpleStringProperty(toFrom);
			this.checkNumber = new SimpleStringProperty(checkNumber);
			this.clearBank = new SimpleStringProperty(clearBank);
			this.budgetTracking = new SimpleStringProperty(budgetTracking);
			this.fundName = new SimpleStringProperty(fundName);
			this.fundNumber = new SimpleStringProperty(fundNumber);
			this.debit = new SimpleObjectProperty<>(debit);
			this.credit = new SimpleObjectProperty<>(credit);
			this.runningBalance = new SimpleObjectProperty<>(runningBalance);
			
		}
		
		/**
		 * Gets the JavaFX property for the transaction date.
		 * @return The date property, as a {@link StringProperty}.
		 */
		public StringProperty dateProperty()
		{
			return this.date;
			
		}
		
		/**
		 * Gets the transaction date string.
		 * @return The date as a {@link String}.
		 */
		public String getDate()
		{
			return this.date.get();
			
		}
		
		/**
		 * Gets the JavaFX property for the transaction ID.
		 * @return The transaction ID property, as a {@link StringProperty}.
		 */
		public StringProperty transactionIdProperty()
		{
			return this.transactionId;
			
		}
		
		/**
		 * Gets the transaction ID string.
		 * @return The transaction ID as a {@link String}.
		 */
		public String getTransactionId()
		{
			return this.transactionId.get();
			
		}
		
		/**
		 * Gets the JavaFX property for the transaction description.
		 * @return The description property, as a {@link StringProperty}.
		 */
		public StringProperty descriptionProperty()
		{
			return this.description;
			
		}
		
		/**
		 * Gets the transaction description string.
		 * @return The description as a {@link String}.
		 */
		public String getDescription()
		{
			return this.description.get();
			
		}
		
		/**
		 * Gets the JavaFX property for the payee or counterparty field.
		 * @return the {@link StringProperty} representing the to/from value.
		 */
		public StringProperty toFromProperty()
		{
			return this.toFrom;
			
		}
		
		/**
		 * Gets the to/from string.
		 * @return the to/from value
		 */
		public String getToFrom()
		{
			return this.toFrom.get();
			
		}
		
		/**
		 * Gets the JavaFX property for the check number.
		 * @return the property for check number
		 */
		public StringProperty checkNumberProperty()
		{
			return this.checkNumber;
			
		}
		
		/**
		 * @return check number string
		 */
		public String getCheckNumber()
		{
			return this.checkNumber.get();
			
		}
		
		/**
		 * Gets the JavaFX property for the clearing bank field.
		 * @return the property for clearing bank
		 */
		public StringProperty clearBankProperty()
		{
			return this.clearBank;
			
		}
		
		/**
		 * @return clearing bank string
		 */
		public String getClearBank()
		{
			return this.clearBank.get();
			
		}
		
		/**
		 * JavaFX property for budget tracking notes.
		 * @return property for budget tracking
		 */
		public StringProperty budgetTrackingProperty()
		{
			return this.budgetTracking;
			
		}
		
		/**
		 * @return budget tracking notes
		 */
		public String getBudgetTracking()
		{
			return this.budgetTracking.get();
			
		}
		
		/**
		 * Property for associated fund name.
		 * @return fund name property
		 */
		public StringProperty fundNameProperty()
		{
			return this.fundName;
			
		}
		
		/**
		 * @return fund name string
		 */
		public String getFundName()
		{
			return this.fundName.get();
			
		}
		
		/**
		 * Property for fund number.
		 * @return fund number property
		 */
		public StringProperty fundNumberProperty()
		{
			return this.fundNumber;
			
		}
		
		/**
		 * @return fund number string
		 */
		public String getFundNumber()
		{
			return this.fundNumber.get();
			
		}
		
		/**
		 * Gets the JavaFX property for the debit amount.
		 * @return The debit amount property, as an {@link ObjectProperty} of {@link BigDecimal}.
		 */
		public ObjectProperty<BigDecimal> debitProperty()
		{
			return this.debit;
			
		}
		
		/**
		 * Gets the debit amount.
		 * @return The debit amount as a {@link BigDecimal}.
		 */
		public BigDecimal getDebit()
		{
			return this.debit.get();
			
		}
		
		/**
		 * Gets the JavaFX property for the credit amount.
		 * @return The credit amount property, as an {@link ObjectProperty} of {@link BigDecimal}.
		 */
		public ObjectProperty<BigDecimal> creditProperty()
		{
			return this.credit;
			
		}
		
		/**
		 * Gets the credit amount.
		 * @return The credit amount as a {@link BigDecimal}.
		 */
		public BigDecimal getCredit()
		{
			return this.credit.get();
			
		}
		
		/**
		 * Gets the JavaFX property for the running balance.
		 * @return The running balance property, as an {@link ObjectProperty} of {@link BigDecimal}.
		 */
		public ObjectProperty<BigDecimal> runningBalanceProperty()
		{
			return this.runningBalance;
			
		}
		
		/**
		 * Gets the running balance.
		 * @return The running balance as a {@link BigDecimal}.
		 */
		public BigDecimal getRunningBalance()
		{
			return this.runningBalance.get();
			
		}
		
	}
	
}
