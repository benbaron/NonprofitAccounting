
package nonprofitbookkeeping.ui.panels.skeletons;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;

import nonprofitbookkeeping.model.Company;
import nonprofitbookkeeping.model.CompanySummary;
import nonprofitbookkeeping.model.CurrentCompany;
import nonprofitbookkeeping.model.AccountingTransaction;
import nonprofitbookkeeping.model.Ledger;
import nonprofitbookkeeping.model.Account;
import nonprofitbookkeeping.model.AccountType;
import nonprofitbookkeeping.model.ChartOfAccounts;
import nonprofitbookkeeping.model.AccountingEntry;
import nonprofitbookkeeping.model.AccountSide;

import java.util.List;
import java.util.ArrayList;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import nonprofitbookkeeping.model.CurrentCompany.CompanyChangeListener;


public class SkeletonDashboardPanel extends BorderPane
{
	
	private final TableView<AccountingTransaction> recentTransactionsTable = new TableView<>();
	private final ObservableList<AccountingTransaction> transactionDataList =
		FXCollections.observableArrayList();
	
	private Label totalAssetsValueLabel = new Label(CompanySummary.getTotalAssets());
	private Label totalLiabilitiesValueLabel = new Label(CompanySummary.getTotalLiabilities());
	private Label equityValueLabel = new Label(CompanySummary.getTotalEquity());
	private Label ytdIncomeValueLabel = new Label(CompanySummary.getYtdIncomeValue());
	
	private final CompanyChangeListener companyChangeListener = new CompanyChangeListener()
	{
		@Override public void companyChange(boolean companyNowOpen)
		{
			loadData();
		}
		
	};
	
	public SkeletonDashboardPanel()
	{
		setPadding(new Insets(15));
		
		GridPane keyFiguresGrid = new GridPane();
		keyFiguresGrid.setPadding(new Insets(10));
		keyFiguresGrid.setHgap(20);
		keyFiguresGrid.setVgap(10);
		
		keyFiguresGrid.add(new Label("Total Assets:"), 0, 0);
		keyFiguresGrid.add(this.totalAssetsValueLabel, 1, 0);
		keyFiguresGrid.add(new Label("Total Liabilities:"), 0, 1);
		keyFiguresGrid.add(this.totalLiabilitiesValueLabel, 1, 1);
		keyFiguresGrid.add(new Label("Equity:"), 2, 0);
		keyFiguresGrid.add(this.equityValueLabel, 3, 0);
		keyFiguresGrid.add(new Label("YTD Income:"), 2, 1);
		keyFiguresGrid.add(this.ytdIncomeValueLabel, 3, 1);
		
		ScrollPane keyFiguresScrollPane = new ScrollPane();
		keyFiguresScrollPane.setContent(keyFiguresGrid);
		keyFiguresScrollPane.setFitToWidth(true);
		keyFiguresScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
		keyFiguresScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
		this.setTop(keyFiguresScrollPane);
		
		this.recentTransactionsTable.setPlaceholder(new Label("No recent transactions to display."));
		this.recentTransactionsTable.setItems(this.transactionDataList);
		
		TableColumn<AccountingTransaction, String> dateCol = new TableColumn<>("Date");
		dateCol.setCellValueFactory(
			cellData -> new SimpleStringProperty(cellData.getValue().getDate()));
		dateCol.setPrefWidth(100);
		
		TableColumn<AccountingTransaction, String> accountCol = new TableColumn<>("Account");
		accountCol.setCellValueFactory(
			cellData -> new SimpleStringProperty(cellData.getValue().getAccountName()));
		accountCol.setPrefWidth(150);
		
		TableColumn<AccountingTransaction, String> descriptionCol =
			new TableColumn<>("Description");
		descriptionCol.setCellValueFactory(cellData -> new SimpleStringProperty(
			cellData.getValue().getDescription() != null ? cellData.getValue().getDescription() :
				cellData.getValue().getMemo()));
		descriptionCol.setPrefWidth(300);
		
		TableColumn<AccountingTransaction, String> amountCol = new TableColumn<>("Amount");
		amountCol.setCellValueFactory(cellData -> {
			BigDecimal totalAmount = cellData.getValue().getTotalAmount();
			return new SimpleStringProperty(
				totalAmount != null ? totalAmount.toPlainString() : "0.00");
		});
		amountCol.setPrefWidth(100);
		amountCol.setStyle("-fx-alignment: CENTER-RIGHT;");
		
		this.recentTransactionsTable.getColumns().addAll(dateCol, accountCol, descriptionCol, amountCol);
		this.setCenter(this.recentTransactionsTable);
		BorderPane.setMargin(this.recentTransactionsTable, new Insets(10, 0, 10, 0));
		
		HBox actionButtonsBox = new HBox();
		actionButtonsBox.setPadding(new Insets(10, 0, 0, 0));
		actionButtonsBox.setSpacing(10);
		actionButtonsBox.setAlignment(Pos.CENTER_RIGHT);
		
		Button refreshButton = new Button("Refresh Dashboard");
		refreshButton.setOnAction(e -> loadData());
		actionButtonsBox.getChildren().add(refreshButton);
		this.setBottom(actionButtonsBox);
		
		loadData();
		CurrentCompany.CompanyListener.addCompanyListener(this.companyChangeListener);
	}
	
	private void loadData()
	{
		this.transactionDataList.clear();
		Company company = CurrentCompany.getCompany();
		
		BigDecimal totalAssets = BigDecimal.ZERO;
		BigDecimal totalLiabilities = BigDecimal.ZERO;
		BigDecimal totalEquity = BigDecimal.ZERO; // Represents sum of equity type accounts, not
													// necessarily total book equity
		BigDecimal totalIncome = BigDecimal.ZERO;
		BigDecimal totalExpenses = BigDecimal.ZERO;
		List<AccountingTransaction> transactions = null;
		
		
		if (company != null && company.getLedger() != null)
		{
			Ledger ledger = company.getLedger();
			transactions = ledger.getTransactions(); // Get all transactions for balance calculation
			ChartOfAccounts coa = company.getChartOfAccounts();
			
			if (coa != null && coa.getAccounts() != null)
			{
				Map<String, BigDecimal> accountBalances = new HashMap<>();
				
				for (Account acc : coa.getAccounts())
				{
					accountBalances.put(acc.getAccountNumber(), acc.getOpeningBalance() != null ?
						acc.getOpeningBalance() : BigDecimal.ZERO);
				}
				
				if (transactions != null)
				{
					
					for (AccountingTransaction tx : transactions)
					{
						
						if (tx.getEntries() != null)
						{
							
							for (AccountingEntry entry : tx.getEntries())
							{
								Account entryAccount = entry.getAccount(); // This should be the
																			// specific account from
																			// COA
								
								if (entryAccount != null &&
									entryAccount.getAccountNumber() != null &&
									entry.getAmount() != null)
								{
									BigDecimal currentBalance = accountBalances.getOrDefault(
										entryAccount.getAccountNumber(), BigDecimal.ZERO);
									BigDecimal entryAmount = entry.getAmount();
									// AccountType type = entryAccount.getAccountType(); // From COA
									// linked account
									
									// Use the type from the account object fetched from COA for
									// consistency
									Account coaAccountInstance =
										coa.getAccount(entryAccount.getAccountNumber());
									AccountType type = (coaAccountInstance != null) ?
										coaAccountInstance.getAccountType() : null;
									
									
									if (type != null)
									{
										
										if (entry.getAccountSide() == AccountSide.DEBIT)
										{
											
											if (type == AccountType.ASSET ||
												type == AccountType.EXPENSE)
											{
												currentBalance = currentBalance.add(entryAmount);
											}
											else
											{
												currentBalance =
													currentBalance.subtract(entryAmount);
											}
											
										}
										else
										{ // CREDIT entry
											
											if (type == AccountType.ASSET ||
												type == AccountType.EXPENSE)
											{
												currentBalance =
													currentBalance.subtract(entryAmount);
											}
											else
											{
												currentBalance = currentBalance.add(entryAmount);
											}
											
										}
										
									}
									
									accountBalances.put(entryAccount.getAccountNumber(),
										currentBalance);
								}
								
							}
							
						}
						
					}
					
				}
				
				for (Account acc : coa.getAccounts())
				{
					BigDecimal liveBalance = accountBalances.getOrDefault(acc.getAccountNumber(),
						acc.getOpeningBalance() != null ? acc.getOpeningBalance() :
							BigDecimal.ZERO);
					AccountType type = acc.getAccountType();
					
					if (type != null)
					{
						
						switch(type)
						{
							case ASSET:
								totalAssets = totalAssets.add(liveBalance);
								break;
								
							case LIABILITY:
								totalLiabilities = totalLiabilities.add(liveBalance);
								break;
								
							case EQUITY: // This sums up accounts of type EQUITY (e.g., Retained
											// Earnings, Common Stock)
								totalEquity = totalEquity.add(liveBalance);
								break;
								
							case INCOME:
								totalIncome = totalIncome.add(liveBalance); // Income increases this
																			// balance (typically
																			// credits)
								break;
								
							case EXPENSE:
								totalExpenses = totalExpenses.add(liveBalance); // Expenses increase
																				// this balance
																				// (typically
																				// debits)
								break;
						}
						
					}
					
				}
				
			}
			
			// Populate recent transactions list for UI (after all balance calculations)
			if (transactions != null && !transactions.isEmpty())
			{
				int limit = Math.min(transactions.size(), 10);
				List<AccountingTransaction> recent = new ArrayList<>();
				
				for (int i = transactions.size() - 1; i >= transactions.size() - limit; i--)
				{
					recent.add(transactions.get(i));
				}
				
				this.transactionDataList.addAll(recent);
			}
			
		}
		
		BigDecimal ytdIncome = totalIncome.subtract(totalExpenses); // Income is positive, Expenses
																	// are positive contributions to
																	// their types
		
		this.totalAssetsValueLabel.setText("$" + totalAssets.toPlainString());
		this.totalLiabilitiesValueLabel.setText("$" + totalLiabilities.toPlainString());
		this.equityValueLabel.setText("$" + totalEquity.toPlainString());
		this.ytdIncomeValueLabel.setText("YTD Net Income: $" + ytdIncome.toPlainString());
		
		
		if (this.transactionDataList.isEmpty())
		{
			this.recentTransactionsTable
				.setPlaceholder(new Label("No transactions found for the current company."));
		}
		else
		{
			// Keep default placeholder or set to null if items are present
			// recentTransactionsTable.setPlaceholder(new Label("No recent transactions to
			// display."));
		}
		
	}
	
}
