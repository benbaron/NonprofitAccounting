
package nonprofitbookkeeping.ui.panels.skeletons;

import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType; // Explicit import for ButtonType
import javafx.stage.Stage;
import javafx.scene.Scene;
import nonprofitbookkeeping.ui.helpers.AlertBox;
import nonprofitbookkeeping.ui.panels.CoaEditorPanelFX;

import nonprofitbookkeeping.model.Company;
import nonprofitbookkeeping.model.CurrentCompany;
import nonprofitbookkeeping.model.ChartOfAccounts;
import nonprofitbookkeeping.model.Account;
import nonprofitbookkeeping.model.AccountType;
import java.util.List;
import nonprofitbookkeeping.model.CurrentCompany.CompanyChangeListener;

/**
 * A JavaFX panel that displays a company's Chart of Accounts (COA) in a {@link TreeTableView}.
 * It provides basic CRUD (Create, Read, Update, Delete) functionalities for managing accounts,
 * though some actions (Add, Edit) are currently placeholders showing informational alerts.
 * The panel listens for changes in the {@link CurrentCompany} and reloads the COA data accordingly.
 */
public class SkeletonCoaPanel extends BorderPane
{
	
	/** The TreeTableView used to display the hierarchical chart of accounts. */
	private TreeTableView<Account> coaTreeTable;
	/** The root {@link TreeItem} for the {@link #coaTreeTable}; it is hidden in the UI. */
	private TreeItem<Account> rootAccountsNode;
	/** Listener to react to changes in the {@link CurrentCompany}, triggering a reload of COA data. */
	private CompanyChangeListener companyChangeListener;
	
	/** Button to initiate adding a new account (currently a placeholder). */
	private Button addAccountButton;
	/** Button to initiate editing a selected account (currently a placeholder). */
	private Button editAccountButton;
	/** Button to delete a selected account and its sub-accounts after confirmation. */
	private Button deleteAccountButton;
	/** HBox container for the CRUD action buttons. */
	private HBox crudButtonsHBox;
	
	/**
	 * Constructs a new {@code SkeletonCoaPanel}.
	 * Initializes the UI components, including the {@link TreeTableView} for the Chart of Accounts,
	 * action buttons for Add, Edit, and Delete operations, and sets up event listeners.
	 * It also performs an initial load of the COA data.
	 */
	public SkeletonCoaPanel()
	{
		setPadding(new Insets(15)); // Overall padding
		
		this.rootAccountsNode = new TreeItem<>();
		this.coaTreeTable = new TreeTableView<>(this.rootAccountsNode);
		this.coaTreeTable.setShowRoot(false); // Hide the dummy root
		this.coaTreeTable
			.setPlaceholder(new Label("No Chart of Accounts data to display or company not open."));
		this.setCenter(this.coaTreeTable);
		
		// Action Buttons (Bottom)
		this.crudButtonsHBox = new HBox();
		this.crudButtonsHBox.setPadding(new Insets(10, 0, 0, 0)); // Top padding
		this.crudButtonsHBox.setSpacing(10);
		this.crudButtonsHBox.setAlignment(Pos.CENTER_LEFT);
		
		this.addAccountButton = new Button("Add Account");
		this.editAccountButton = new Button("Edit Account");
		this.deleteAccountButton = new Button("Delete Account");
		this.crudButtonsHBox.getChildren().addAll(this.addAccountButton, this.editAccountButton,
			this.deleteAccountButton);
		this.setBottom(this.crudButtonsHBox);
		
		// Setup columns and listeners
		setupTreeTableColumns();
		setupEventListenersAndRefresh();
	}
	
	
	/**
	 * Sets up the columns for the {@link #coaTreeTable}.
	 * Defines columns for Account Number, Account Name, and Account Type.
	 * Cell value factories are configured using lambda expressions to extract data from {@link Account} objects.
	 * Placeholder comments suggest where a "Balance" column could be added if account balances were available.
	 */
	private void setupTreeTableColumns()
	{
		this.coaTreeTable.getColumns().clear();
		
		TreeTableColumn<Account, String> numberCol = new TreeTableColumn<>("Account Number");
		numberCol.setCellValueFactory(cellData -> {
			Account acc = cellData.getValue().getValue();
			return new SimpleStringProperty(acc != null ? acc.getAccountNumber() : "");
		});
		numberCol.setPrefWidth(150);
		
		TreeTableColumn<Account, String> nameCol = new TreeTableColumn<>("Account Name");
		nameCol.setCellValueFactory(cellData -> {
			Account acc = cellData.getValue().getValue();
			return new SimpleStringProperty(acc != null ? acc.getName() : "");
		});
		nameCol.setPrefWidth(280); // Increased width for longer names
		
		TreeTableColumn<Account, String> typeCol = new TreeTableColumn<>("Type");
		typeCol.setCellValueFactory(cellData -> {
			Account acc = cellData.getValue().getValue();
			AccountType accType = acc != null ? acc.getAccountType() : null;
			return new SimpleStringProperty(accType != null ? accType.toString() : "");
		});
		typeCol.setPrefWidth(150);
		
		// Example for a balance column (requires data in Account object or external
		// fetching)
		//
		// TreeTableColumn<Account, String> balanceCol = new
		// TreeTableColumn<>("Balance");
		// balanceCol.setCellValueFactory(cellData -> {
		// Account acc = cellData.getValue().getValue();
		//
		// // Assuming Account has a getBalance() method returning BigDecimal or similar
		//
		// return new SimpleStringProperty(acc != null && acc.getBalance() != null ?
		// acc.getBalance().toPlainString() : "0.00");
		// });
		// balanceCol.setPrefWidth(120);
		// balanceCol.setStyle("-fx-alignment: CENTER-RIGHT;");
		
		this.coaTreeTable.getColumns().addAll(numberCol,
			nameCol,
			typeCol); // Add balanceCol if // implemented
	}
	
	/**
	 * Loads the Chart of Accounts data for the {@link CurrentCompany} and populates the {@link #coaTreeTable}.
	 * It clears any existing items in the tree, retrieves the root-level accounts from the company's COA,
	 * and recursively builds the tree structure using {@link #createAccountTreeItem(Account, ChartOfAccounts)}.
	 * If no company is open or the COA is unavailable, the table will remain empty or show its placeholder.
	 */
	private void loadCoaData()
	{
		this.rootAccountsNode.getChildren().clear();
		
		if (!CurrentCompany.isOpen() || CurrentCompany.getCompany() == null)
		{
			this.coaTreeTable.setPlaceholder(new Label("No company open."));
			return;
		}
		
		Company company = CurrentCompany.getCompany();
		
		if (company != null && company.getChartOfAccounts() != null)
		{
			ChartOfAccounts coa = company.getChartOfAccounts();
			List<Account> rootLevelAccounts = coa.getRootAccounts(); // Assuming this method exists
			
			if (rootLevelAccounts != null)
			{
				
				for (Account acc : rootLevelAccounts)
				{
					TreeItem<Account> accountNode = createAccountTreeItem(acc, coa);
					this.rootAccountsNode.getChildren().add(accountNode);
				}
				
			}
			
		}
		
		// coaTreeTable.refresh(); // Not always needed if root's children list
		// modification triggers update
	}
	
	/**
	 * Recursively creates a {@link TreeItem} for a given {@link Account} and its children.
	 * Each created {@code TreeItem} is set to be expanded by default to show its children.
	 * 
	 * @param account The {@link Account} for which to create a {@code TreeItem}. Must not be null.
	 * @param coa The {@link ChartOfAccounts} instance, used to fetch child accounts. Must not be null.
	 * @return A {@link TreeItem<Account>} representing the given account and its descendants.
	 */
	private TreeItem<Account> createAccountTreeItem(Account account, ChartOfAccounts coa)
	{
		TreeItem<Account> item = new TreeItem<>(account);
		item.setExpanded(true); // Expand by default
		List<Account> children = coa.getChildren(account); // Assuming this method exists
		
		if (children != null)
		{
			
			for (Account child : children)
			{
				item.getChildren().add(createAccountTreeItem(child, coa));
			}
			
		}
		
		return item;
	}
	
	/**
	 * Sets up event listeners for UI components and performs an initial data refresh.
	 * This includes:
	 * <ul>
	 *   <li>Registering a {@link CompanyChangeListener} to reload COA data when the current company changes.</li>
	 *   <li>Setting action handlers for the "Add Account", "Edit Account", and "Delete Account" buttons.
	 *       (Note: Add and Edit actions currently show placeholder alerts.)</li>
	 *   <li>Performing an initial call to {@link #loadCoaData()} to populate the table.</li>
	 * </ul>
	 */
	private void setupEventListenersAndRefresh()
	{
		this.companyChangeListener = new CompanyChangeListener()
		{
			@Override public void companyChange(boolean companyNowOpen)
			{
				loadCoaData();
			}
			
		};
		CurrentCompany.CompanyListener.addCompanyListener(this.companyChangeListener);
		
		this.addAccountButton.setOnAction(e -> openEditor());		
		this.editAccountButton.setOnAction(e -> onEditAction());		
		this.deleteAccountButton.setOnAction(e -> onDeleteAction());
		
		loadCoaData(); // Initial data load
	}


	/**
	 * On Edit button
	 */
	void onEditAction()
	{
		TreeItem<Account> selectedItem =
			this.coaTreeTable.getSelectionModel().getSelectedItem();
		
		if (selectedItem != null && selectedItem.getValue() != null)
		{
			Account selectedAccount = selectedItem.getValue();
			openEditor();
		}
		else
		{
			Alert error =
				new Alert(Alert.AlertType.WARNING, "No account selected for editing.");
			error.setHeaderText("Selection Missing");
			error.showAndWait();
		}
	}


	/**
	 * On Delete Button
	 */
	void onDeleteAction()
	{
		TreeItem<Account> selectedItem =
			this.coaTreeTable.getSelectionModel().getSelectedItem();
		
		if (selectedItem != null && selectedItem.getValue() != null)
		{
			Account selectedAccount = selectedItem.getValue();
			Company company = CurrentCompany.getCompany();
			
			if (company != null && company.getChartOfAccounts() != null)
			{
				Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION,
					"Are you sure you want to delete account '" + selectedAccount.getName() +
						"' (" + selectedAccount.getAccountNumber() +
						") and all its sub-accounts (if any)? " +
						"This action cannot be undone and may affect existing transactions if not handled carefully by the backend.",
					ButtonType.YES, ButtonType.NO);
				confirmation.setHeaderText("Confirm Deletion");
				confirmation.setTitle("Delete Account");
				
				confirmation.showAndWait().ifPresent(
					response -> onConfirm(selectedAccount, company, response));
			}
			
		}
		else
		{
			Alert error = new Alert(Alert.AlertType.WARNING, 
				"No account selected for deletion.");
			error.setHeaderText("Selection Missing");
			error.showAndWait();
		}
	}


	/**
	 * @param selectedAccount
	 * @param company
	 * @param response
	 */
	void onConfirm(Account selectedAccount, Company company, ButtonType response)
	{		
		if (response == ButtonType.YES)
		{
			boolean deleted = company.getChartOfAccounts()
				.removeAccount(selectedAccount.getAccountNumber()); // Assuming remove by ID
			
			if (deleted)
			{
				loadCoaData();
				System.out.println("Deleted account: " + selectedAccount.getName());
			}
			else
			{
				Alert error = new Alert(Alert.AlertType.ERROR,
					"Failed to delete account '" + selectedAccount.getName() +
						"'. It might be in use or a core account.");
				error.setHeaderText("Deletion Failed");
				error.showAndWait();
			}
			
		}
		
	}
	
	/** 
	 * Opens the full Chart of Accounts editor in a new window. 
	 */
	private void openEditor()
	{
		Company company = CurrentCompany.getCompany();
		
		if (company == null || company.getChartOfAccounts() == null)
		{
			AlertBox.showError(getScene().getWindow(), "No company open.");
			return;
		}
		
		// Launch Editor Panel
		CoaEditorPanelFX editor = new CoaEditorPanelFX(
			company.getChartOfAccounts(),
			coa ->chartOfAccountsCallback(company, coa),
			null); // on close
			
		Stage s = new Stage();
		s.setTitle("Chart of Accounts Editor");
		s.initOwner(getScene().getWindow());
		s.setScene(new Scene(editor, 800, 600));
		s.showAndWait();
	}


	/**
	 * Chart of accounts callback
	 * 
	 * @param company
	 * @param coa
	 */
	void chartOfAccountsCallback(Company company, ChartOfAccounts coa)
	{
		company.setChartOfAccounts(coa);
		
		try
		{
			CurrentCompany.persist();
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
		
                CurrentCompany.markCompanyOpen();
		loadCoaData();
	}
	
}
