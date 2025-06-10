
package nonprofitbookkeeping.ui.panels.skeletons;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;

import nonprofitbookkeeping.model.Company;
import nonprofitbookkeeping.model.CurrentCompany;
import nonprofitbookkeeping.model.AccountingTransaction;
import nonprofitbookkeeping.model.AccountingEntry;
import nonprofitbookkeeping.model.AccountSide;
import nonprofitbookkeeping.model.Journal;
import java.util.List;
import java.util.ArrayList;
import nonprofitbookkeeping.model.CurrentCompany.CompanyChangeListener;
import java.math.BigDecimal;

/**
 * A JavaFX panel that displays journal entries from the current company's ledger.
 * It provides a table view ({@link #journalDisplayTable}) for individual debit/credit entries
 * derived from {@link AccountingTransaction}s.
 * Includes filter controls for searching by description/account and date,
 * and action buttons for New, Edit, and Delete operations on journal entries
 * (though some actions like New/Edit and filtering are placeholders).
 * The panel listens for changes in the {@link CurrentCompany} to reload data.
 */
public class SkeletonJournalPanel extends BorderPane
{
	
	/** TableView to display journal entries, using {@link JournalDisplayEntry} as the row model. */
	private TableView<JournalDisplayEntry> journalDisplayTable;
	/** ObservableList that backs the {@link #journalDisplayTable}, containing {@link JournalDisplayEntry} objects. */
	private ObservableList<JournalDisplayEntry> journalDataList;
	/** Listener to react to changes in the {@link CurrentCompany}, triggering a reload of journal data. */
	private CompanyChangeListener companyChangeListener;
	
	/** TextField for entering search terms to filter journal entries by description or account. */
	private TextField searchFilterField;
	/** TextField for entering a date (YYYY-MM-DD) to filter journal entries. */
	private TextField dateFilterField;
	/** Button to apply the filters entered in {@link #searchFilterField} and {@link #dateFilterField}. (Currently placeholder) */
	private Button applyFilterButton;
	/** Button to initiate creating a new journal entry. (Currently placeholder) */
	private Button newEntryButton;
	/** Button to initiate editing the selected journal entry. (Currently placeholder) */
	private Button editEntryButton;
	/** Button to delete the selected journal entry's original transaction. */
	private Button deleteEntryButton;
	
	/** HBox container for the filter input controls. */
	private HBox filterControlsBox;
	/** ScrollPane to ensure filter controls are accessible if they overflow. */
	private ScrollPane filterScrollPane;
	/** HBox container for the CRUD action buttons (New, Edit, Delete). */
	private HBox crudButtonsHBox;
	
	/**
	 * Constructs a new {@code SkeletonJournalPanel}.
	 * Initializes the UI layout, including filter controls at the top,
	 * the main table for journal entries in the center, and action buttons at the bottom.
	 * Sets up table columns, event listeners, and performs an initial data load.
	 */
	public SkeletonJournalPanel()
	{
		setPadding(new Insets(15)); // Overall padding
		
		// Initialize collections
		this.journalDataList = FXCollections.observableArrayList();
		this.journalDisplayTable = new TableView<>(this.journalDataList);
		this.journalDisplayTable
			.setPlaceholder(new Label("No journal entries to display or company not open."));
		
		// Filter Controls (Top)
		this.filterControlsBox = new HBox();
		this.filterControlsBox.setPadding(new Insets(0, 0, 10, 0));
		this.filterControlsBox.setSpacing(10);
		this.filterControlsBox.setAlignment(Pos.CENTER_LEFT);
		
		Label filterLabel = new Label("Filter:");
		this.searchFilterField = new TextField();
		this.searchFilterField.setPromptText("Search description/account...");
		this.searchFilterField.setPrefWidth(200);
		this.dateFilterField = new TextField();
		this.dateFilterField.setPromptText("Date (YYYY-MM-DD)");
		this.dateFilterField.setPrefWidth(150);
		this.applyFilterButton = new Button("Apply Filter");
		this.filterControlsBox.getChildren().addAll(filterLabel, this.searchFilterField, this.dateFilterField,
			this.applyFilterButton);
		
		this.filterScrollPane = new ScrollPane(this.filterControlsBox);
		this.filterScrollPane.setFitToWidth(true);
		this.filterScrollPane.setFitToHeight(true);
		this.filterScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
		this.filterScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
		this.setTop(this.filterScrollPane);
		
		// Action Buttons (Bottom)
		this.crudButtonsHBox = new HBox();
		this.crudButtonsHBox.setPadding(new Insets(10, 0, 0, 0));
		this.crudButtonsHBox.setSpacing(10);
		this.crudButtonsHBox.setAlignment(Pos.CENTER_LEFT);
		
		this.newEntryButton = new Button("New Entry");
		this.editEntryButton = new Button("Edit Entry");
		this.deleteEntryButton = new Button("Delete Entry");
		this.crudButtonsHBox.getChildren().addAll(this.newEntryButton, this.editEntryButton, this.deleteEntryButton);
		this.setBottom(this.crudButtonsHBox);
		
		// Setup and initial load
		setupTableColumns();
		setCenter(this.journalDisplayTable); // Place table in center
		setupEventListenersAndRefresh();
	}
	
	/**
	 * Sets up the columns for the {@link #journalDisplayTable}.
	 * Defines columns for Date, Transaction ID, Account, Description, Debit, and Credit.
	 * Cell value factories are configured using {@link PropertyValueFactory} to bind to
	 * properties of the {@link JournalDisplayEntry} class.
	 */
	private void setupTableColumns()
	{
		this.journalDisplayTable.getColumns().clear();
		
		TableColumn<JournalDisplayEntry, String> dateCol = new TableColumn<>("Date");
		dateCol.setCellValueFactory(new PropertyValueFactory<>("date"));
		dateCol.setPrefWidth(90);
		
		TableColumn<JournalDisplayEntry, String> transIdCol = new TableColumn<>("Transaction ID");
		transIdCol.setCellValueFactory(new PropertyValueFactory<>("transactionId"));
		transIdCol.setPrefWidth(120);
		
		TableColumn<JournalDisplayEntry, String> accountCol = new TableColumn<>("Account");
		accountCol.setCellValueFactory(new PropertyValueFactory<>("accountName"));
		accountCol.setPrefWidth(150);
		
		TableColumn<JournalDisplayEntry, String> descCol = new TableColumn<>("Description");
		descCol.setCellValueFactory(new PropertyValueFactory<>("description"));
		descCol.setPrefWidth(220);
		
		TableColumn<JournalDisplayEntry, String> debitCol = new TableColumn<>("Debit");
		debitCol.setCellValueFactory(new PropertyValueFactory<>("debit"));
		debitCol.setStyle("-fx-alignment: CENTER-RIGHT;");
		debitCol.setPrefWidth(90);
		
		TableColumn<JournalDisplayEntry, String> creditCol = new TableColumn<>("Credit");
		creditCol.setCellValueFactory(new PropertyValueFactory<>("credit"));
		creditCol.setStyle("-fx-alignment: CENTER-RIGHT;");
		creditCol.setPrefWidth(90);
		
		this.journalDisplayTable.getColumns()
			.addAll(dateCol, transIdCol, 
				accountCol, descCol, debitCol,
				creditCol);
	}
	
	/**
	 * Loads journal entry data for the {@link CurrentCompany} and populates the {@link #journalDisplayTable}.
	 * It clears any existing items in the table. If a company is open and its journal is available,
	 * it iterates through each {@link AccountingTransaction} and then through each {@link AccountingEntry}
	 * within that transaction, creating a {@link JournalDisplayEntry} for each.
	 * These display entries are added to {@link #journalDataList}, which updates the table.
	 * Transactions are typically displayed in reverse chronological order (newest first).
	 * If no company is open or no entries are found, a placeholder message is shown in the table.
	 */
        private void loadData()
        {
                this.journalDataList.clear();

                if (!CurrentCompany.isOpen() || CurrentCompany.getCompany() == null)
                {
                        this.journalDisplayTable
                                .setPlaceholder(new Label("No journal entries found or company not open."));
                        return;
                }

                Company company = CurrentCompany.getCompany();

                if (company != null && company.getLedger() != null &&
                        company.getLedger().getJournal() != null)
		{
			Journal journal = company.getLedger().getJournal();
			// Iterate in reverse to show newest transactions first at the top of the list
			List<AccountingTransaction> transactions = journal.getJournalTransactions();
			
			for (int i = transactions.size() - 1; i >= 0; i--)
			{
				AccountingTransaction tx = transactions.get(i);
				
				if (tx.getEntries() != null)
				{
					
					for (AccountingEntry entry : tx.getEntries())
					{
						this.journalDataList.add(new JournalDisplayEntry(tx, entry));
					}
					
				}
				
			}
			
		}
		
		if (this.journalDataList.isEmpty())
		{
			this.journalDisplayTable
				.setPlaceholder(new Label("No journal entries found or company not open."));
		}
		
		// No need for an 'else' to set placeholder to null, TableView handles it.
	}
	
	/**
	 * Sets up event listeners for UI components and performs an initial data refresh.
	 * This includes:
	 * <ul>
	 *   <li>Registering a {@link CompanyChangeListener} to reload journal data when the current company changes.</li>
	 *   <li>Setting action handlers for the "Apply Filter", "New Entry", "Edit Entry", and "Delete Entry" buttons.
	 *       (Note: Filter, New, and Edit actions are currently placeholders or have partial implementations.)</li>
	 *   <li>Performing an initial call to {@link #loadData()} to populate the table.</li>
	 * </ul>
	 */
	private void setupEventListenersAndRefresh()
	{
		this.companyChangeListener = new CompanyChangeListener()
		{
			@Override public void companyChange(boolean companyNowOpen)
			{
				loadData();
			}
			
		};
		CurrentCompany.CompanyListener.addCompanyListener(this.companyChangeListener);
		
		this.applyFilterButton.setOnAction(e -> {
			// TODO: Implement actual filtering logic based on searchFilterField and
			// dateFilterField
			// For now, just reload all data as a placeholder for filter action
			System.out.println("Filter button clicked. Search: " + this.searchFilterField.getText() +
				", Date: " + this.dateFilterField.getText());
			loadData();
		});
		
		this.newEntryButton.setOnAction(e -> {
			System.out.println("New Entry clicked - Placeholder");
			// TODO: Implement dialog/panel for new transaction entry
		});
		
		this.editEntryButton.setOnAction(e -> {
			JournalDisplayEntry selected =
				this.journalDisplayTable.getSelectionModel().getSelectedItem();
			
			if (selected != null)
			{
				AccountingTransaction originalTx = selected.getOriginalTransaction();
				System.out.println("Edit Entry clicked for TX ID: " +
					originalTx.getBookingDateTimestamp() + " - Placeholder");
				// TODO: Implement dialog/panel for editing, passing originalTx
			}
			else
			{
				System.out.println("No journal entry selected for editing.");
				// Consider AlertBox.showError(getScene().getWindow(), "No entry selected.");
			}
			
		});
		
		this.deleteEntryButton.setOnAction(e -> {
			JournalDisplayEntry selected =
				this.journalDisplayTable.getSelectionModel().getSelectedItem();
			
			if (selected != null)
			{
				AccountingTransaction originalTx = selected.getOriginalTransaction();
				Company company = CurrentCompany.getCompany();
				
				if (company != null && company.getLedger() != null &&
					company.getLedger().getJournal() != null)
				{
					boolean deleted = company.getLedger().getJournal()
						.deleteTransaction(originalTx.getBookingDateTimestamp());
					
					if (deleted)
					{
						loadData(); // Refresh table
						System.out.println(
							"Successfully deleted TX ID: " + originalTx.getBookingDateTimestamp());
					}
					else
					{
						System.out.println(
							"Failed to delete TX ID: " + originalTx.getBookingDateTimestamp());
						// Consider AlertBox.showError(getScene().getWindow(), "Deletion failed.");
					}
					
				}
				
			}
			else
			{
				System.out.println("No journal entry selected for deletion.");
				// Consider AlertBox.showError(getScene().getWindow(), "No entry selected for
				// deletion.");
			}
			
		});
		loadData(); // Initial data load
	}
	
	/**
	 * Represents a single displayable row in the journal table.
	 * Each {@code JournalDisplayEntry} corresponds to one {@link AccountingEntry}
	 * from an {@link AccountingTransaction}. It flattens the transaction data for table display,
	 * showing details like date, transaction ID, account name, description, and debit/credit amounts.
	 * It also holds a reference to the original {@link AccountingTransaction} for operations like editing or deleting.
	 */
	public static class JournalDisplayEntry
	{
		/** The date of the transaction. */
		private final SimpleStringProperty date;
		/** The unique ID of the transaction (typically the booking timestamp). */
		private final SimpleStringProperty transactionId;
		/** The name of the account associated with this specific journal entry line. */
		private final SimpleStringProperty accountName;
		/** The overall description or memo of the transaction. */
		private final SimpleStringProperty description;
		/** The debit amount for this entry line, as a string. Empty if it's a credit. */
		private final SimpleStringProperty debit;
		/** The credit amount for this entry line, as a string. Empty if it's a debit. */
		private final SimpleStringProperty credit;
		/** A reference to the original {@link AccountingTransaction} this display entry belongs to. */
		private final AccountingTransaction originalTransaction;
		
		/**
		 * Constructs a new {@code JournalDisplayEntry}.
		 *
		 * @param tx The source {@link AccountingTransaction}. Must not be null.
		 * @param entry The specific {@link AccountingEntry} within the transaction to display. Must not be null.
		 *              The entry's account and amount details are used to populate debit/credit columns.
		 */
		public JournalDisplayEntry(AccountingTransaction tx, AccountingEntry entry)
		{
			this.originalTransaction = tx;
			this.date = new SimpleStringProperty(tx.getDate());
			this.transactionId =
				new SimpleStringProperty(String.valueOf(tx.getBookingDateTimestamp()));
			this.description = new SimpleStringProperty(tx.getDescription() != null ?
				tx.getDescription() : (tx.getMemo() != null ? tx.getMemo() : ""));
			
			if (entry != null && entry.getAccount() != null)
			{
				this.accountName = new SimpleStringProperty(entry.getAccount().getName());
				BigDecimal amount = entry.getAmount() != null ? entry.getAmount() : BigDecimal.ZERO;
				
				if (entry.getAccountSide() == AccountSide.DEBIT)
				{
					this.debit = new SimpleStringProperty(amount.toPlainString());
					this.credit = new SimpleStringProperty("");
				}
				else
				{
					this.debit = new SimpleStringProperty("");
					this.credit = new SimpleStringProperty(amount.toPlainString());
				}
				
			}
			else
			{ // Fallback, should ideally not occur with valid data
				this.accountName = new SimpleStringProperty("Error: No Account");
				this.debit = new SimpleStringProperty("");
				this.credit = new SimpleStringProperty("");
			}
			
		}
		
		/**
		 * Gets the JavaFX property for the transaction date.
		 * @return The date property.
		 */
		public StringProperty dateProperty()
		{
			return this.date;
		}
		
		/**
		 * Gets the JavaFX property for the transaction ID.
		 * @return The transaction ID property.
		 */
		public StringProperty transactionIdProperty()
		{
			return this.transactionId;
		}
		
		/**
		 * Gets the JavaFX property for the account name of this entry line.
		 * @return The account name property.
		 */
		public StringProperty accountNameProperty()
		{
			return this.accountName;
		}
		
		/**
		 * Gets the JavaFX property for the transaction description/memo.
		 * @return The description property.
		 */
		public StringProperty descriptionProperty()
		{
			return this.description;
		}
		
		/**
		 * Gets the JavaFX property for the debit amount string.
		 * @return The debit amount property.
		 */
		public StringProperty debitProperty()
		{
			return this.debit;
		}
		
		/**
		 * Gets the JavaFX property for the credit amount string.
		 * @return The credit amount property.
		 */
		public StringProperty creditProperty()
		{
			return this.credit;
		}
		
		/**
		 * Gets the original {@link AccountingTransaction} from which this display entry was derived.
		 * This is useful for actions like editing or deleting the full transaction.
		 * @return The original {@link AccountingTransaction}.
		 */
		public AccountingTransaction getOriginalTransaction()
		{
			return this.originalTransaction;
		}
		
		/** Gets the transaction date string. @return The date. */
		public String getDate()
		{
			return this.date.get();
		}
		
		/** Gets the transaction ID string. @return The transaction ID. */
		public String getTransactionId()
		{
			return this.transactionId.get();
		}
		
		/** Gets the account name string for this entry line. @return The account name. */
		public String getAccountName()
		{
			return this.accountName.get();
		}
		
		/** Gets the transaction description/memo string. @return The description. */
		public String getDescription()
		{
			return this.description.get();
		}
		
		/** Gets the debit amount string for this entry line. Empty if it's a credit. @return The debit amount. */
		public String getDebit()
		{
			return this.debit.get();
		}
		
		/** Gets the credit amount string for this entry line. Empty if it's a debit. @return The credit amount. */
		public String getCredit()
		{
			return this.credit.get();
		}
		
	}
	
}
