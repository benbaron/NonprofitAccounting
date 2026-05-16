
package nonprofitbookkeeping.ui.panels;

import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javafx.beans.property.*;
import javafx.collections.*;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import nonprofitbookkeeping.model.*;
import nonprofitbookkeeping.model.CurrentCompany.CompanyChangeListener;
import nonprofitbookkeeping.ui.UiSpacing;

/**
 * A JavaFX {@link BorderPane} that serves as the main dashboard for displaying journal transactions.
 * It includes:
 * <ul>
 *   <li>A top banner showing the currently loaded company and a reload button.</li>
 *   <li>Filter controls for selecting an account and filtering transactions by date, memo, and amount.</li>
 *   <li>A {@link TableView} to display {@link AccountingTransaction} data, transformed into {@link Row} objects for display.</li>
 * </ul>
 * The panel listens to company changes via {@link CompanyChangeListener} to update its content.
 */
public class DashboardPanelFX extends BorderPane
{
	
	/* ── "company loaded" banner ─────────────────────────────── */
	/** Label to display the name of the currently loaded company. Defaults to "No company loaded". */
	private final Label companyLbl = new Label("No company loaded");
	/** Button to manually trigger a reload of the current company's data. */
	private final Button reloadBtn = new Button("Reload");
	
	/* account / filter controls */
	/** ComboBox for selecting an account to filter transactions. */
	private final ComboBox<String> accountSelector = new ComboBox<>();
	/** TextField for filtering transactions by date (expects "yyyy-mm-dd" or partial match). */
	private final TextField dateFilter = new TextField();
	/** TextField for filtering transactions by memo content (case-insensitive partial match). */
	private final TextField memoFilter = new TextField();
	/** TextField for filtering transactions by a specific amount. */
	private final TextField amountFilter = new TextField();
	private Button applyFiltersButton; // Made into a field
	
	/* data table */
	/** TableView to display the journal transactions. */
	private final TableView<Row> table = new TableView<>();
	/** ObservableList that backs the {@link #table}, containing {@link Row} objects representing transactions. */
	private final ObservableList<Row> rows = FXCollections.observableArrayList();
	/** Stores all {@link AccountingTransaction}s for the currently loaded company. Initialized as an empty list. */
	private List<AccountingTransaction> allTxns = List.of();
	/** Stores the parsed {@link BigDecimal} value from the {@link #amountFilter} field. Null if filter is empty or invalid. */
	BigDecimal amtF = null;
	/** Listener instance to handle changes in the {@link CurrentCompany}. */
	private DashboardListener listener = new DashboardListener(this);
	
	
	/**
	 * Constructs a new {@code DashboardPanelFX}.
	 * Initializes the UI layout, including the top banner, filter controls, and the main transaction table.
	 * It also registers a {@link CompanyChangeListener} to react to company load/close events.
	 * The table is initially empty or shows data based on the company loaded at application start (if any).
	 */
	public DashboardPanelFX()
	{
		setPadding(PanelChrome.PANEL_PADDING);
		
		buildTopBanner();
		buildTopFilters();
		buildTable();
		setCenter(new TitledPane("Journal Transactions", this.table)
		{
			{
				setCollapsible(false);
			}
			
		});
		
		CurrentCompany.CompanyListener.addCompanyListener(this.listener);
		// Set initial state based on current company status
		loadCompany(CurrentCompany.isOpen() ? CurrentCompany.getCompany() : null);
	}
	
	/**
	 * Builds and configures the top banner of the dashboard.
	 * The banner includes a label for the current company's name and a "Reload" button.
	 * This banner is then set as the top component of this {@link BorderPane}.
	 */
	private void buildTopBanner()
	{
		this.companyLbl.getStyleClass().add("company-indicator");
		this.reloadBtn.setOnAction(e -> loadCompany(CurrentCompany.getCompany()));
		
		HBox banner = new HBox(UiSpacing.SECTION_SPACING, new Label("Current Company:"), this.companyLbl, this.reloadBtn);
		
		banner.setPadding(new Insets(UiSpacing.SECTION_SPACING));
		banner.getStyleClass().add("dashboard-banner");
		setTop(banner);
	}
	
	/**
	 * Builds and configures the filter controls area of the dashboard.
	 * This area includes:
	 * <ul>
	 *   <li>An account selector ComboBox.</li>
	 *   <li>Textfields for filtering by date, memo, and amount.</li>
	 *   <li>An "Apply" button to trigger the filtering process.</li>
	 * </ul>
	 * This filter control area is added below the top banner.
	 */
	private void buildTopFilters()
	{
		/* selector */
		HBox selectorBox = new HBox(UiSpacing.SECTION_SPACING, new Label("Account:"), this.accountSelector);
		selectorBox.setPadding(new Insets(UiSpacing.SECTION_SPACING));
		selectorBox.getStyleClass().add("dashboard-filter-box");
		
		this.applyFiltersButton = new Button("Apply"); // Assign to field
		this.applyFiltersButton.setOnAction(e -> refresh());
		
		HBox filterBox =
			new HBox(UiSpacing.SECTION_SPACING, new Label("Date (yyyy-mm-dd):"), this.dateFilter, new Label("Memo:"),
				this.memoFilter, new Label("Amount:"), this.amountFilter, this.applyFiltersButton); // Use
																									// field
		filterBox.setPadding(new Insets(UiSpacing.SECTION_SPACING));
		filterBox.getStyleClass().add("dashboard-filter-box");
		
		VBox topControls = new VBox(selectorBox, filterBox);
		setMargin(topControls, UiSpacing.actionBarTopMargin());
		
		Node currentTop = getTop(); // This will be the banner
		VBox newTopStructure = PanelChrome.topSection("Dashboard", currentTop, topControls);
		setTop(newTopStructure);
	}
	
	/**
	 * Builds and configures the {@link TableView} ({@link #table}) for displaying journal transactions.
	 * It defines columns for Date, Description, Amount, Balance, and Memo using the {@link #mkCol} helper method.
	 * The table is bound to the {@link #rows} observable list and a column resize policy is set.
	 * The {@code @SuppressWarnings({ "unchecked", "deprecation" })} is likely due to the use of varargs
	 * in {@code getColumns().addAll()} and possibly type inference issues with older JavaFX versions or specific
	 * patterns with cell value factories, though modern JavaFX with lambdas (as used in {@code mkCol})
	 * is generally type-safe. "deprecation" might relate to direct field name usage in PropertyValueFactory if that
	 * were used instead of lambdas.
	 */
	@SuppressWarnings(
	{ "unchecked", "deprecation" }) private void buildTable()
	{
		TableColumn<Row, Object> dateCol = mkCol("Date", r -> r.date);
		TableColumn<Row, Object> descCol = mkCol("Description", r -> r.desc);
		TableColumn<Row, Object> amtCol = mkCol("Amount", r -> r.amount);
		TableColumn<Row, Object> balCol = mkCol("Balance", r -> r.balance);
		TableColumn<Row, Object> memoCol = mkCol("Memo", r -> r.memo);
		
		this.table.getColumns().addAll(dateCol, descCol, amtCol, balCol, memoCol);
		this.table.setItems(this.rows);
		this.table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
	}
	
	/**
	 * Utility method to create a {@link TableColumn} for the transaction table.
	 * 
	 * @param <T> The type of the data to be displayed in this column's cells.
	 * @param n The title of the column (to be displayed in the header).
	 * @param f A {@link Function} that takes a {@link Row} object (the value of the TableView row)
	 *           and returns the value of type {@code T} to be displayed in the cell for that row.
	 * @return A configured {@link TableColumn} for displaying data of type {@code T} from a {@link Row}.
	 */
	private static <T> TableColumn<Row, T> mkCol(String n, Function<Row, T> f)
	{
		TableColumn<Row, T> c = new TableColumn<>(n);
		c.setCellValueFactory(cd -> new ReadOnlyObjectWrapper<>(f.apply(cd.getValue())));
		return c;
	}
	
	/** 
	 * Loads or unloads company data into the dashboard.
	 * This method is called when the current company changes (e.g., a file is opened or closed).
	 * If {@code cdf} is null (company closed), it clears the UI elements (company label, account selector, table).
	 * If {@code cdf} is not null, it updates the company label, loads all transactions from the company's ledger,
	 * populates the account selector (though this part is missing in the current `loadCompany` method,
	 * it should ideally populate {@link #accountSelector} based on {@code cdf.getChartOfAccounts()}),
	 * and then calls {@link #refresh()} to display the transactions.
	 *
	 * @param cdf The {@link Company} to load. If null, indicates that the current company has been closed.
	 */
	private void loadCompany(Company cdf)
	{
		boolean companyIsOpen = (cdf != null);
		
		// Manage UI component states
		this.reloadBtn.setDisable(!companyIsOpen);
		this.accountSelector.setDisable(!companyIsOpen);
		this.dateFilter.setDisable(!companyIsOpen);
		this.memoFilter.setDisable(!companyIsOpen);
		this.amountFilter.setDisable(!companyIsOpen);
		
		if (this.applyFiltersButton != null)
		{ // Ensure applyFiltersButton is initialized
			this.applyFiltersButton.setDisable(!companyIsOpen);
		}
		
		if (!companyIsOpen)
		{
			this.companyLbl.setText("None");
			this.accountSelector.getItems().clear();
			this.accountSelector.setPlaceholder(new Label("No company open"));
			this.rows.clear();
			this.allTxns = List.of();
			this.dateFilter.clear();
			this.memoFilter.clear();
			this.amountFilter.clear();
			// reloadBtn.setOnAction(null); // Or keep old action, it will just reload null
		}
		else
		{
			this.companyLbl.setText(cdf.getCompanyProfile().getCompanyName());
			
			// Populate accountSelector
			this.accountSelector.getItems().clear();
			
			if (cdf.getChartOfAccounts() != null && cdf.getChartOfAccounts().getAccounts() != null)
			{
				List<String> accountNames =
					cdf.getChartOfAccounts().getAccounts().stream().map(Account::getName)
						.filter(Objects::nonNull).sorted().collect(Collectors.toList());
				this.accountSelector.getItems().addAll(accountNames);
				
				if (!accountNames.isEmpty())
				{
					this.accountSelector.getSelectionModel().selectFirst();
				}
				else
				{
					this.accountSelector.setPlaceholder(new Label("No accounts in COA"));
				}
				
			}
			else
			{
				this.accountSelector.setPlaceholder(new Label("COA not available"));
			}
			
			this.allTxns = (cdf.getLedger() != null && cdf.getLedger().getTransactions() != null) ?
				cdf.getLedger().getTransactions() : List.of();
			
			this.reloadBtn.setOnAction(e -> loadCompany(CurrentCompany.getCompany()));
			refresh(); // Refresh table content
		}
		
	}
	
	/**
	 * Refreshes the transaction table ({@link #table}) based 
	 * on the current filter settings
	 * (selected account, date, memo, and amount).
	 * It first retrieves the selected account and checks if transactions are loaded.
	 * Then, it parses the filter criteria from the text fields.
	 * A predicate is constructed to filter {@link #allTxns}.
	 * The filtered transactions are converted into {@link Row} 
	 * objects, a running balance is calculated,
	 * and the table is updated with these rows.
	 * If no account is selected or no transactions are available, the table is cleared.
	 */
	private void refresh()
	{
		
		// Only proceed if a company is considered open and account selector has a value
		if (!CurrentCompany.isOpen() || this.accountSelector.getValue() == null ||
			this.allTxns.isEmpty())
		{
			
			if (CurrentCompany.isOpen() && this.accountSelector.getValue() == null &&
				!this.accountSelector.getItems().isEmpty())
			{
				this.rows.clear();
			}
			else if (!CurrentCompany.isOpen())
			{
				this.rows.clear(); // Ensure table is clear if no company
			}
			
		}
		
		String acct = this.accountSelector.getValue();
		
		if (acct == null && CurrentCompany.isOpen() && !this.accountSelector.getItems().isEmpty())
		{
			this.rows.clear();
			return;
		}
		else if (acct == null)
		{ // No account selected, and either no company or no accounts.
			this.rows.clear();
			return;
		}
		
		String dateFText = this.dateFilter.getText().trim();
		String memoFText = this.memoFilter.getText().trim().toLowerCase();
		
		this.amtF = null; // Reset before parsing amount filter
		
		if (!this.amountFilter.getText().isBlank())
		{
			
			try
			{
				this.amtF = new BigDecimal(this.amountFilter.getText().trim());
			}
			catch (NumberFormatException ignore)
			{
				// Optionally provide user feedback about invalid amount format
			}
			
		}
		
		Predicate<AccountingTransaction> p = t -> t != null &&
			t.getEntries().stream().anyMatch(e -> Objects.equals(e.getAccountName(), acct)) &&
			(dateFText.isEmpty() || (t.getDate() != null && t.getDate().contains(dateFText))) &&
			(memoFText.isEmpty() ||
				(t.getMemo() != null && t.getMemo().toLowerCase().contains(memoFText))) &&
			(this.amtF == null ||
				(t.getTotalAmount() != null && t.getTotalAmount().compareTo(this.amtF) == 0));
		
		List<AccountingTransaction> list =
			this.allTxns.stream().filter(p).collect(Collectors.toList());
		
		this.rows.clear();
		BigDecimal running = BigDecimal.ZERO;
		
		for (var t : list)
		{
			BigDecimal amt = t.getTotalAmount();
			running = running.add(amt);
			this.rows.add(new Row(t, amt, running));
		}
		
	}
	
	/**
	 * Represents a single row of data to be displayed in the dashboard's transaction table.
	 * This class uses JavaFX properties ({@link StringProperty}, {@link ObjectProperty}) to enable
	 * data binding with {@link TableView} columns. It wraps an {@link AccountingTransaction}
	 * and includes calculated amount and running balance relevant to the displayed context.
	 */
	private static class Row
	{
		/** The date of the transaction. */
		final StringProperty date = new SimpleStringProperty();
		/** The description of the transaction. */
		final StringProperty desc = new SimpleStringProperty();
		/** The amount of this transaction entry relevant to the current view (e.g., total debit/credit). */
		final ObjectProperty<BigDecimal> amount = new SimpleObjectProperty<>();
		/** The running balance after this transaction. */
		final ObjectProperty<BigDecimal> balance = new SimpleObjectProperty<>();
		/** The memo associated with the transaction. */
		final StringProperty memo = new SimpleStringProperty();
		
		/**
		 * Constructs a new {@code Row} for display in the transaction table.
		 * 
		 * @param t The source {@link AccountingTransaction} from which to derive row data.
		 * @param amt The specific amount to display for this transaction in the current context
		 *            (e.g., total amount of the transaction).
		 * @param bal The running balance calculated up to and including this transaction.
		 */
		Row(AccountingTransaction t, BigDecimal amt, BigDecimal bal)
		{
			this.date.set(t.getDate());
			this.desc.set(t.getDescription());
			this.amount.set(amt);
			this.balance.set(bal);
			this.memo.set(t.getMemo());
		}
		
	}
	
	
	/**
	 * A {@link CompanyChangeListener} implementation that listens for changes in the
	 * {@link CurrentCompany} (e.g., when a company file is opened or closed).
	 * When a change occurs, it triggers the {@link DashboardPanelFX#loadCompany(Company)}
	 * method to update the dashboard's display accordingly.
	 */
	class DashboardListener implements CompanyChangeListener
	{
		/** Reference to the {@link DashboardPanelFX} that this listener will update. */
		DashboardPanelFX dashboardPanelFX = null;
		
		public DashboardListener(DashboardPanelFX dashboardPanelFX)
		{
			this.dashboardPanelFX = dashboardPanelFX;
		}
		
		@Override public void companyChange(boolean companyNowOpen)
		{
			// Pass the actual company object or null to loadCompany
			this.dashboardPanelFX.loadCompany(companyNowOpen ? CurrentCompany.getCompany() : null);
		}
		
	}


	/**
	 * 
	 */
	public void reloadData()
	{
		// TODO Auto-generated method stub
		
		
	}
	
}
