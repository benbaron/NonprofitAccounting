
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

public class SkeletonJournalPanel extends BorderPane
{
	
	private TableView<JournalDisplayEntry> journalDisplayTable;
	private ObservableList<JournalDisplayEntry> journalDataList;
	private CompanyChangeListener companyChangeListener;
	
	private TextField searchFilterField;
	private TextField dateFilterField;
	private Button applyFilterButton;
	private Button newEntryButton, editEntryButton, deleteEntryButton;
	
	private HBox filterControlsBox;
	private ScrollPane filterScrollPane;
	private HBox crudButtonsHBox;
	
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
	
	private void loadData()
	{
		this.journalDataList.clear();
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
	
	public static class JournalDisplayEntry
	{
		private final SimpleStringProperty date;
		private final SimpleStringProperty transactionId; // Using timestamp as a unique ID for
															// display
		private final SimpleStringProperty accountName;
		private final SimpleStringProperty description;
		private final SimpleStringProperty debit;
		private final SimpleStringProperty credit;
		private final AccountingTransaction originalTransaction;
		
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
		
		public StringProperty dateProperty()
		{
			return this.date;
		}
		
		public StringProperty transactionIdProperty()
		{
			return this.transactionId;
		}
		
		public StringProperty accountNameProperty()
		{
			return this.accountName;
		}
		
		public StringProperty descriptionProperty()
		{
			return this.description;
		}
		
		public StringProperty debitProperty()
		{
			return this.debit;
		}
		
		public StringProperty creditProperty()
		{
			return this.credit;
		}
		
		public AccountingTransaction getOriginalTransaction()
		{
			return this.originalTransaction;
		}
		
		public String getDate()
		{
			return this.date.get();
		}
		
		public String getTransactionId()
		{
			return this.transactionId.get();
		}
		
		public String getAccountName()
		{
			return this.accountName.get();
		}
		
		public String getDescription()
		{
			return this.description.get();
		}
		
		public String getDebit()
		{
			return this.debit.get();
		}
		
		public String getCredit()
		{
			return this.credit.get();
		}
		
	}
	
}
