
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
import javafx.scene.layout.VBox;

import nonprofitbookkeeping.model.Company;
import nonprofitbookkeeping.model.CompanySummary;
import nonprofitbookkeeping.model.CurrentCompany;
import nonprofitbookkeeping.model.AccountingTransaction;
import nonprofitbookkeeping.model.AccountingEntry;
import nonprofitbookkeeping.model.Ledger;


import java.util.List;
import java.util.ArrayList;
import java.math.BigDecimal;
import nonprofitbookkeeping.util.FormatUtils;
import nonprofitbookkeeping.model.CurrentCompany.CompanyChangeListener;

/**
 * A JavaFX panel that serves as a dashboard, displaying key financial figures
 * (Total Assets, Total Liabilities, Equity, and Income) and a list of recent transactions.
 * The panel listens for changes in the {@link CurrentCompany} and reloads its data accordingly.
 * Key financial figures are calculated by iterating through accounts and transactions
 * from the current company's {@link Ledger} and {@link ChartOfAccounts}.
 */
public class SkeletonDashboardPanel extends BorderPane
{
	
	/** TableView to display a list of recent accounting transactions. */
	private final TableView<AccountingTransaction> recentTransactionsTable = new TableView<>();
	/** ObservableList that backs the {@link #recentTransactionsTable}, containing {@link AccountingTransaction} objects. */
	private final ObservableList<AccountingTransaction> transactionDataList =
		FXCollections.observableArrayList();
	
	/** Label to display the calculated total assets value. Initializes with a value from {@link CompanySummary}. */
	private Label totalAssetsValueLabel = new Label(CompanySummary.getTotalAssets());
	/** Label to display the calculated total liabilities value. Initializes with a value from {@link CompanySummary}. */
	private Label totalLiabilitiesValueLabel = new Label(CompanySummary.getTotalLiabilities());
	/** Label to display the calculated total equity value. Initializes with a value from {@link CompanySummary}. */
	private Label equityValueLabel = new Label(CompanySummary.getTotalEquity());
	/** Label to display the calculated net income value. Initializes with a value from {@link CompanySummary}. */
	private Label ytdIncomeValueLabel = new Label(CompanySummary.getYtdIncomeValue());
	
	/** Label showing the name of the currently open company. */
	private final Label companyNameLabel = new Label("No company loaded");
	
	/** Listener that triggers {@link #loadData()} when the {@link CurrentCompany} changes. */
	private final CompanyChangeListener companyChangeListener = new CompanyChangeListener()
	{
		@Override public void companyChange(boolean companyNowOpen)
		{
			loadData();
		}
		
	};
	
	/**
	 * Constructs a new {@code SkeletonDashboardPanel}.
	 * Initializes the UI layout, including a grid for key financial figures at the top,
	 * a table for recent transactions in the center, and a refresh button at the bottom.
	 * It also sets up a listener for company changes and performs an initial data load.
	 */
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
		keyFiguresGrid.add(new Label("Income:"), 2, 1);
		keyFiguresGrid.add(this.ytdIncomeValueLabel, 3, 1);
		
		ScrollPane keyFiguresScrollPane = new ScrollPane();
		keyFiguresScrollPane.setContent(keyFiguresGrid);
		keyFiguresScrollPane.setFitToWidth(true);
		keyFiguresScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
		keyFiguresScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
		
		Label panelTitle = new Label("Dashboard");
		panelTitle.getStyleClass().add("journal-entry-heading");
		this.companyNameLabel.setStyle("-fx-font-size: 1.5em; -fx-font-weight: bold;");
		VBox topBox = new VBox(5, panelTitle, this.companyNameLabel, keyFiguresScrollPane);
		this.setTop(topBox);
		
		this.recentTransactionsTable
			.setPlaceholder(new Label("No recent transactions to display."));
		this.recentTransactionsTable.setItems(this.transactionDataList);
		
		TableColumn<AccountingTransaction, String> dateCol = new TableColumn<>("Date");
		dateCol.setCellValueFactory(
			cellData -> new SimpleStringProperty(cellData.getValue().getDate()));
		dateCol.setPrefWidth(120);
		
		TableColumn<AccountingTransaction, String> accountCol = new TableColumn<>("Account");
                accountCol.setCellValueFactory(cellData -> {
                        AccountingTransaction tx = cellData.getValue();
                        String name = "";
                        if (tx != null && tx.getEntries() != null && !tx.getEntries().isEmpty())
                        {
                                AccountingEntry first = tx.getEntries().iterator().next();
                                name = first.getAccountName();
                        }
                        return new SimpleStringProperty(name);
                });
                accountCol.setPrefWidth(220);
		
		TableColumn<AccountingTransaction, String> descriptionCol =
			new TableColumn<>("Description");
		descriptionCol.setCellValueFactory(cellData -> new SimpleStringProperty(
			cellData.getValue().getDescription() != null ? cellData.getValue().getDescription() :
				cellData.getValue().getMemo()));
		descriptionCol.setPrefWidth(950);
		
		TableColumn<AccountingTransaction, String> amountCol = new TableColumn<>("Amount");
               amountCol.setCellValueFactory(cellData -> {
                       BigDecimal totalAmount = cellData.getValue().getTotalAmount();
                       return new SimpleStringProperty(
                               FormatUtils.formatCurrency(totalAmount != null ? totalAmount : BigDecimal.ZERO));
               });
		amountCol.setPrefWidth(140);
		amountCol.getStyleClass().add("table-column-numeric");
		
		this.recentTransactionsTable.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);
		this.recentTransactionsTable.getColumns()
			.addAll(dateCol,
				accountCol,
				descriptionCol,
				amountCol);
		
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
	
	/**
	 * Loads and processes data from the {@link CurrentCompany} to update the dashboard.
	 * This method performs the following steps:
	 * <ol>
	 *   <li>Clears the existing list of recent transactions.</li>
	 *   <li>Retrieves the current {@link Company}, {@link Ledger}, and {@link ChartOfAccounts}.</li>
	 *   <li>If company data is available, it calculates account balances by starting with opening balances
	 *       and applying all transaction entries.</li>
	 *   <li>Sums these account balances to determine Total Assets, Total Liabilities, and Total Equity
	 *       (sum of equity-type accounts), Total Income, and Total Expenses.</li>
	 *   <li>Calculates Net Income (Total Income - Total Expenses).</li>
	 *   <li>Updates the corresponding labels ({@link #totalAssetsValueLabel}, etc.) with these calculated figures.</li>
	 *   <li>Populates the {@link #recentTransactionsTable} with a limited number of the most recent transactions (up to 10).</li>
	 *   <li>Sets appropriate placeholder text for the recent transactions table if no data is available.</li>
	 * </ol>
	 * If no company is loaded, or essential data like the ledger or chart of accounts is missing,
	 * the key figures will typically default to zero or initial values, and the transaction list will be empty.
	 */
	private void loadData()
	{
		this.transactionDataList.clear();
		
		if (!CurrentCompany.isOpen() || CurrentCompany.getCompany() == null)
		{
			this.companyNameLabel.setText("No company loaded");
                       this.totalAssetsValueLabel.setText(FormatUtils.formatCurrency(BigDecimal.ZERO));
                       this.totalLiabilitiesValueLabel.setText(FormatUtils.formatCurrency(BigDecimal.ZERO));
                       this.equityValueLabel.setText(FormatUtils.formatCurrency(BigDecimal.ZERO));
                       this.ytdIncomeValueLabel.setText("Net Income: " + FormatUtils.formatCurrency(BigDecimal.ZERO));
			this.recentTransactionsTable
				.setPlaceholder(new Label("No company open."));
			return;
		}
		
		Company company = CurrentCompany.getCompany();
		this.companyNameLabel.setText(company.getCompanyProfile().getCompanyName());
		
		List<AccountingTransaction> transactions = null;
		
		if (company != null && company.getLedger() != null)
		{
			Ledger ledger = company.getLedger();
			transactions = ledger.getTransactions();
			
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
		
		BigDecimal totalAssets = new BigDecimal(CompanySummary.getTotalAssets());
		BigDecimal totalLiabilities = new BigDecimal(CompanySummary.getTotalLiabilities());
		BigDecimal totalEquity = new BigDecimal(CompanySummary.getTotalEquity());
		BigDecimal ytdIncome = new BigDecimal(CompanySummary.getYtdIncomeValue());
		
               this.totalAssetsValueLabel.setText(FormatUtils.formatCurrency(totalAssets));
               this.totalLiabilitiesValueLabel.setText(FormatUtils.formatCurrency(totalLiabilities));
               this.equityValueLabel.setText(FormatUtils.formatCurrency(totalEquity));
               this.ytdIncomeValueLabel.setText("Net Income: " + FormatUtils.formatCurrency(ytdIncome));
		
		if (this.transactionDataList.isEmpty())
		{
			this.recentTransactionsTable
				.setPlaceholder(new Label("No transactions found for the current company."));
		}
		else
		{
			// Keep default placeholder or set to null if items are present
			this.recentTransactionsTable.setPlaceholder(
				new Label("No recent transactions to display."));
		}
		
	}
	
}
