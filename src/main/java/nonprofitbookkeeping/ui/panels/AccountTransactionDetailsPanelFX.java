
package nonprofitbookkeeping.ui.panels;

import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import nonprofitbookkeeping.model.Account;
import nonprofitbookkeeping.model.Company;
import nonprofitbookkeeping.model.CurrentCompany;
import nonprofitbookkeeping.model.ChartOfAccounts;
import nonprofitbookkeeping.model.Ledger;
import nonprofitbookkeeping.model.AccountSide;
import nonprofitbookkeeping.model.AccountingEntry;
import nonprofitbookkeeping.model.AccountingTransaction;
import nonprofitbookkeeping.model.CurrentCompany.CompanyChangeListener; // Added import
import nonprofitbookkeeping.ui.helpers.AlertBox;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.ObjectProperty;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Comparator;
import java.util.stream.Collectors;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.Callback;


public class AccountTransactionDetailsPanelFX extends BorderPane
{
	
	private ComboBox<Account> accountSelectorComboBox;
	private DatePicker startDatePicker;
	private DatePicker endDatePicker;
	private Button loadTransactionsButton;
	private TableView<TransactionDisplayRow> transactionsTable;
	private ObservableList<TransactionDisplayRow> transactionDataList;
	
	private Label totalDebitsLabel;
	private Label totalCreditsLabel;
	private Label netChangeLabel;
	
	private CompanyChangeListener companyChangeListener; // Added field
	
	public AccountTransactionDetailsPanelFX()
	{
		setPadding(new Insets(10));
		
		// TOP: Controls
		GridPane controlsGrid = new GridPane();
		controlsGrid.setHgap(10);
		controlsGrid.setVgap(8);
		controlsGrid.setPadding(new Insets(5));
		
		accountSelectorComboBox = new ComboBox<>();
		accountSelectorComboBox.setPromptText("Select Account");
		
		Company company = CurrentCompany.getCompany();
		
		if (company != null && company.getChartOfAccounts() != null)
		{
			ChartOfAccounts coa = company.getChartOfAccounts();
			
			if (coa.getAccounts() != null)
			{
				List<Account> sortedAccounts = coa.getAccounts().stream()
					.filter(Objects::nonNull)
					.sorted(Comparator.comparing(Account::getName, String.CASE_INSENSITIVE_ORDER))
					.collect(Collectors.toList());
				accountSelectorComboBox.setItems(FXCollections.observableArrayList(sortedAccounts));
				
				Callback<ListView<Account>, ListCell<Account>> cellFactory =
					lv -> new ListCell<Account>()
					{
						@Override protected void updateItem(Account item, boolean empty)
						{
							super.updateItem(item, empty);
							setText(empty ? null :
								item.getName() + " (" + item.getAccountNumber() + ")");
						}
						
					};
				accountSelectorComboBox.setCellFactory(cellFactory);
				accountSelectorComboBox.setButtonCell(cellFactory.call(null));
			}
			
		}
		
		if (accountSelectorComboBox.getItems().isEmpty())
		{
			accountSelectorComboBox.setPlaceholder(new Label("No accounts in COA"));
		}
		
		accountSelectorComboBox.setOnAction(e -> {
			transactionDataList.clear();
			transactionsTable
				.setPlaceholder(new Label("Account selection changed. Click 'Load Transactions'."));
			totalDebitsLabel.setText("Total Debits: 0.00");
			totalCreditsLabel.setText("Total Credits: 0.00");
			netChangeLabel.setText("Net Change: 0.00");
		});
		
		
		startDatePicker = new DatePicker();
		endDatePicker = new DatePicker();
		loadTransactionsButton = new Button("Load Transactions");
		loadTransactionsButton.setOnAction(e -> loadTransactionData());
		
		controlsGrid.add(new Label("Account:"), 0, 0);
		controlsGrid.add(accountSelectorComboBox, 1, 0, 2, 1);
		controlsGrid.add(new Label("Start Date:"), 0, 1);
		controlsGrid.add(startDatePicker, 1, 1);
		controlsGrid.add(new Label("End Date:"), 0, 2);
		controlsGrid.add(endDatePicker, 1, 2);
		controlsGrid.add(loadTransactionsButton, 2, 2);
		
		ScrollPane controlsScrollPane = new ScrollPane(controlsGrid);
		controlsScrollPane.setFitToWidth(true);
		controlsScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
		controlsScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
		setTop(controlsScrollPane);
		
		transactionDataList = FXCollections.observableArrayList();
		transactionsTable = new TableView<>(transactionDataList);
		setupTableColumns();
		transactionsTable
			.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
		transactionsTable.setPlaceholder(
			new Label("Select account and date range, then click 'Load Transactions'."));
		setCenter(transactionsTable);
		
		HBox totalsBox = new HBox(20);
		totalsBox.setPadding(new Insets(10, 0, 0, 0));
		totalDebitsLabel = new Label("Total Debits: 0.00");
		totalCreditsLabel = new Label("Total Credits: 0.00");
		netChangeLabel = new Label("Net Change: 0.00");
		totalsBox.getChildren().addAll(totalDebitsLabel, totalCreditsLabel, netChangeLabel);
		setBottom(totalsBox);
		
		setupCompanyChangeListener(); // Call to setup listener
	}
	
	private void setupTableColumns()
	{
		transactionsTable.getColumns().clear();
		
		TableColumn<TransactionDisplayRow, String> dateCol = new TableColumn<>("Date");
		dateCol.setCellValueFactory(new PropertyValueFactory<>("date"));
		dateCol.setPrefWidth(100);
		
		TableColumn<TransactionDisplayRow, String> idCol = new TableColumn<>("Transaction ID");
		idCol.setCellValueFactory(new PropertyValueFactory<>("transactionId"));
		idCol.setPrefWidth(120);
		
		TableColumn<TransactionDisplayRow, String> descCol = new TableColumn<>("Description");
		descCol.setCellValueFactory(new PropertyValueFactory<>("description"));
		descCol.setPrefWidth(250);
		
		TableColumn<TransactionDisplayRow, BigDecimal> debitCol = new TableColumn<>("Debit");
		debitCol.setCellValueFactory(new PropertyValueFactory<>("debit"));
		debitCol.setPrefWidth(100);
		debitCol.setStyle("-fx-alignment: CENTER-RIGHT;");
		
		TableColumn<TransactionDisplayRow, BigDecimal> creditCol = new TableColumn<>("Credit");
		creditCol.setCellValueFactory(new PropertyValueFactory<>("credit"));
		creditCol.setPrefWidth(100);
		creditCol.setStyle("-fx-alignment: CENTER-RIGHT;");
		
		TableColumn<TransactionDisplayRow, BigDecimal> balanceCol =
			new TableColumn<>("Running Balance");
		balanceCol.setCellValueFactory(new PropertyValueFactory<>("runningBalance"));
		balanceCol.setPrefWidth(120);
		balanceCol.setStyle("-fx-alignment: CENTER-RIGHT;");
		
		transactionsTable.getColumns().addAll(dateCol, idCol, descCol, debitCol, creditCol,
			balanceCol);
	}
	
	private void loadTransactionData()
	{
		transactionDataList.clear();
		
		Account selectedAccount = accountSelectorComboBox.getValue();
		LocalDate startDate = startDatePicker.getValue();
		LocalDate endDate = endDatePicker.getValue();
		
		if (selectedAccount == null)
		{
			AlertBox.showError(this.getScene().getWindow(), "Please select an account.");
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
		
		if (company == null || company.getLedger() == null || company.getChartOfAccounts() == null)
		{
			AlertBox.showError(this.getScene().getWindow(), "Company data not available.");
			transactionsTable.setPlaceholder(new Label("Company data or ledger not available."));
			return;
		}
		
		BigDecimal runningBalance = selectedAccount.getOpeningBalance() != null ?
			selectedAccount.getOpeningBalance() : BigDecimal.ZERO;
		
		Ledger ledger = company.getLedger();
		
		if (ledger.getTransactions() != null)
		{
			// Sort all transactions once by date for calculating opening balance correctly
			List<AccountingTransaction> allTransactionsSorted = ledger.getTransactions().stream()
				.filter(Objects::nonNull)
				.filter(tx -> tx.getDate() != null && !tx.getDate().trim().isEmpty())
				.sorted(Comparator.comparing(AccountingTransaction::getDate)
					.thenComparingLong(AccountingTransaction::getBookingDateTimestamp))
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
							
							if (entry.getAccount() != null && entry.getAccount().getAccountNumber()
								.equals(selectedAccount.getAccountNumber()))
							{
								BigDecimal amount =
									entry.getAmount() != null ? entry.getAmount() : BigDecimal.ZERO;
								
								if (entry.getAccountSide() == AccountSide.DEBIT)
								{
									runningBalance = runningBalance.add(amount);
								}
								else
								{
									runningBalance = runningBalance.subtract(amount);
								}
								
							}
							
						}
						
					}
					
				}
				catch (java.time.format.DateTimeParseException e)
				{
					System.err
						.println("Could not parse transaction date for opening balance calc: " +
							tx.getDate() + " for TX ID: " + tx.getBookingDateTimestamp());
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
							
							if (entry.getAccount() != null && entry.getAccount().getAccountNumber()
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
					// Logged during opening balance or handle again if necessary
				}
				
			}
			
			// Sort only the period transactions if not using the pre-sorted full list
			periodTransactions.sort(Comparator.comparing(AccountingTransaction::getDate)
				.thenComparingLong(AccountingTransaction::getBookingDateTimestamp));
			
			for (AccountingTransaction tx : periodTransactions)
			{
				
				for (AccountingEntry entry : tx.getEntries())
				{
					
					if (entry.getAccount() != null && entry.getAccount().getAccountNumber()
						.equals(selectedAccount.getAccountNumber()))
					{
						BigDecimal debitAmount = BigDecimal.ZERO;
						BigDecimal creditAmount = BigDecimal.ZERO;
						BigDecimal entryAmount =
							entry.getAmount() != null ? entry.getAmount() : BigDecimal.ZERO;
						
						if (entry.getAccountSide() == AccountSide.DEBIT)
						{
							debitAmount = entryAmount;
							runningBalance = runningBalance.add(debitAmount);
							periodDebitTotal = periodDebitTotal.add(debitAmount);
						}
						else
						{
							creditAmount = entryAmount;
							runningBalance = runningBalance.subtract(creditAmount);
							periodCreditTotal = periodCreditTotal.add(creditAmount);
						}
						
						displayRows.add(new TransactionDisplayRow(
							tx.getDate(),
							String.valueOf(tx.getBookingDateTimestamp()),
							tx.getMemo() != null ? tx.getMemo() : "",
							debitAmount,
							creditAmount,
							new BigDecimal(runningBalance.toString())));
					}
					
				}
				
			}
			
		}
		
		transactionDataList.addAll(displayRows);
		
		totalDebitsLabel.setText("Total Debits (Period): " + periodDebitTotal.toPlainString());
		totalCreditsLabel.setText("Total Credits (Period): " + periodCreditTotal.toPlainString());
		BigDecimal netChange = periodDebitTotal.subtract(periodCreditTotal);
		netChangeLabel.setText("Net Change (Period): " + netChange.toPlainString());
		
		if (transactionDataList.isEmpty())
		{
			transactionsTable.setPlaceholder(
				new Label("No transactions found for the selected account and date range."));
		}
		else
		{
			transactionsTable.setPlaceholder(null);
		}
		
	}
	
	private void setupCompanyChangeListener()
	{
		companyChangeListener = new CompanyChangeListener()
		{
			@Override public void companyChange(boolean companyNowOpen)
			{
				transactionDataList.clear();
				transactionsTable.setPlaceholder(new Label(
					"Company changed. Select account and date range, then click 'Load Transactions'."));
				totalDebitsLabel.setText("Total Debits: 0.00");
				totalCreditsLabel.setText("Total Credits: 0.00");
				netChangeLabel.setText("Net Change: 0.00");
				
				accountSelectorComboBox.getItems().clear();
				accountSelectorComboBox.setValue(null);
				
				if (companyNowOpen)
				{
					Company company = CurrentCompany.getCompany();
					
					if (company != null && company.getChartOfAccounts() != null)
					{
						ChartOfAccounts coa = company.getChartOfAccounts();
						
						if (coa.getAccounts() != null)
						{
							List<Account> sortedAccounts = coa.getAccounts().stream()
								.filter(Objects::nonNull)
								.sorted(Comparator.comparing(Account::getName,
									String.CASE_INSENSITIVE_ORDER))
								.collect(Collectors.toList());
							accountSelectorComboBox
								.setItems(FXCollections.observableArrayList(sortedAccounts));
						}
						
					}
					
				}
				
				if (accountSelectorComboBox.getItems().isEmpty())
				{
					accountSelectorComboBox.setPlaceholder(new Label("No accounts in COA"));
				}
				else
				{
					accountSelectorComboBox.setPlaceholder(new Label("Select Account"));
				}
				
			}
			
		};
		CurrentCompany.CompanyListener.addCompanyListener(companyChangeListener);
	}
	
	public static class TransactionDisplayRow
	{
		private final StringProperty date;
		private final StringProperty transactionId;
		private final StringProperty description;
		private final ObjectProperty<BigDecimal> debit;
		private final ObjectProperty<BigDecimal> credit;
		private final ObjectProperty<BigDecimal> runningBalance;
		
		public TransactionDisplayRow(String date, String transactionId, String description,
			BigDecimal debit, BigDecimal credit, BigDecimal runningBalance)
		{
			this.date = new SimpleStringProperty(date);
			this.transactionId = new SimpleStringProperty(transactionId);
			this.description = new SimpleStringProperty(description);
			this.debit = new SimpleObjectProperty<>(debit);
			this.credit = new SimpleObjectProperty<>(credit);
			this.runningBalance = new SimpleObjectProperty<>(runningBalance);
		}
		
		public StringProperty dateProperty()
		{
			return date;
		}
		
		public StringProperty transactionIdProperty()
		{
			return transactionId;
		}
		
		public StringProperty descriptionProperty()
		{
			return description;
		}
		
		public ObjectProperty<BigDecimal> debitProperty()
		{
			return debit;
		}
		
		public ObjectProperty<BigDecimal> creditProperty()
		{
			return credit;
		}
		
		public ObjectProperty<BigDecimal> runningBalanceProperty()
		{
			return runningBalance;
		}
		
		public String getDate()
		{
			return date.get();
		}
		
		public String getTransactionId()
		{
			return transactionId.get();
		}
		
		public String getDescription()
		{
			return description.get();
		}
		
		public BigDecimal getDebit()
		{
			return debit.get();
		}
		
		public BigDecimal getCredit()
		{
			return credit.get();
		}
		
		public BigDecimal getRunningBalance()
		{
			return runningBalance.get();
		}
		
	}
	
}
