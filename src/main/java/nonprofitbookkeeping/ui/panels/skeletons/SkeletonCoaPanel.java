
package nonprofitbookkeeping.ui.panels.skeletons;

import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableView;
// TreeItemPropertyValueFactory is not used if we use lambdas for
// cellData.getValue().getValue()
// import javafx.scene.control.cell.TreeItemPropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType; // Explicit import for ButtonType

import nonprofitbookkeeping.model.Company;
import nonprofitbookkeeping.model.CurrentCompany;
import nonprofitbookkeeping.model.ChartOfAccounts;
import nonprofitbookkeeping.model.Account;
import nonprofitbookkeeping.model.AccountType;
import java.util.List;
import nonprofitbookkeeping.model.CurrentCompany.CompanyChangeListener;

public class SkeletonCoaPanel extends BorderPane
{
	
	private TreeTableView<Account> coaTreeTable;
	private TreeItem<Account> rootAccountsNode;
	private CompanyChangeListener companyChangeListener;
	
	private Button addAccountButton;
	private Button editAccountButton;
	private Button deleteAccountButton;
	private HBox crudButtonsHBox;
	
	public SkeletonCoaPanel()
	{
		setPadding(new Insets(15)); // Overall padding
		
		// Initialize TreeTableView and its root
		// The root's value can be null as it's not shown.
		// Or use a dummy account if preferred for clarity, but it won't be visible.
		rootAccountsNode = new TreeItem<>();
		coaTreeTable = new TreeTableView<>(rootAccountsNode);
		coaTreeTable.setShowRoot(false); // Hide the dummy root
		coaTreeTable
			.setPlaceholder(new Label("No Chart of Accounts data to display or company not open."));
		this.setCenter(coaTreeTable);
		
		// Action Buttons (Bottom)
		crudButtonsHBox = new HBox();
		crudButtonsHBox.setPadding(new Insets(10, 0, 0, 0)); // Top padding
		crudButtonsHBox.setSpacing(10);
		crudButtonsHBox.setAlignment(Pos.CENTER_LEFT);
		
		addAccountButton = new Button("Add Account");
		editAccountButton = new Button("Edit Account");
		deleteAccountButton = new Button("Delete Account");
		crudButtonsHBox.getChildren().addAll(addAccountButton, editAccountButton,
			deleteAccountButton);
		this.setBottom(crudButtonsHBox);
		
		// Setup columns and listeners
		setupTreeTableColumns();
		setupEventListenersAndRefresh();
	}
	
	private void setupTreeTableColumns()
	{
		coaTreeTable.getColumns().clear();
		
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
		// TreeTableColumn<Account, String> balanceCol = new
		// TreeTableColumn<>("Balance");
		// balanceCol.setCellValueFactory(cellData -> {
		// Account acc = cellData.getValue().getValue();
		// // Assuming Account has a getBalance() method returning BigDecimal or similar
		// return new SimpleStringProperty(acc != null && acc.getBalance() != null ?
		// acc.getBalance().toPlainString() : "0.00");
		// });
		// balanceCol.setPrefWidth(120);
		// balanceCol.setStyle("-fx-alignment: CENTER-RIGHT;");
		
		coaTreeTable.getColumns().addAll(numberCol, nameCol, typeCol); // Add balanceCol if
																		// implemented
	}
	
	private void loadCoaData()
	{
		rootAccountsNode.getChildren().clear();
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
					rootAccountsNode.getChildren().add(accountNode);
				}
				
			}
			
		}
		
		// coaTreeTable.refresh(); // Not always needed if root's children list
		// modification triggers update
	}
	
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
	
	private void setupEventListenersAndRefresh()
	{
		companyChangeListener = new CompanyChangeListener()
		{
			@Override public void companyChange(boolean companyNowOpen)
			{
				loadCoaData();
			}
			
		};
		CurrentCompany.CompanyListener.addCompanyListener(companyChangeListener);
		
		addAccountButton.setOnAction(e -> {
			System.out.println("Add Account clicked - Placeholder");
			// TODO: Implement dialog for adding new account
			// Example: new AddAccountDialog(getScene().getWindow(),
			// CurrentCompany.getCompany().getChartOfAccounts()).showAndWait();
			// Then: loadCoaData();
			Alert info = new Alert(Alert.AlertType.INFORMATION,
				"Add Account functionality not yet implemented.");
			info.setHeaderText("Placeholder");
			info.showAndWait();
		});
		
		editAccountButton.setOnAction(e -> {
			TreeItem<Account> selectedItem = coaTreeTable.getSelectionModel().getSelectedItem();
			
			if (selectedItem != null && selectedItem.getValue() != null)
			{
				Account selectedAccount = selectedItem.getValue();
				System.out.println(
					"Edit Account clicked for: " + selectedAccount.getName() + " - Placeholder");
				// TODO: Implement dialog for editing account
				// Example: new EditAccountDialog(getScene().getWindow(),
				// CurrentCompany.getCompany().getChartOfAccounts(),
				// selectedAccount).showAndWait();
				// Then: loadCoaData(); // or selective refresh
				Alert info = new Alert(Alert.AlertType.INFORMATION,
					"Edit Account for '" + selectedAccount.getName() + "' not yet implemented.");
				info.setHeaderText("Placeholder");
				info.showAndWait();
			}
			else
			{
				Alert error =
					new Alert(Alert.AlertType.WARNING, "No account selected for editing.");
				error.setHeaderText("Selection Missing");
				error.showAndWait();
			}
			
		});
		
		deleteAccountButton.setOnAction(e -> {
			TreeItem<Account> selectedItem = coaTreeTable.getSelectionModel().getSelectedItem();
			
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
					
					confirmation.showAndWait().ifPresent(response -> {
						
						if (response == ButtonType.YES)
						{
							boolean deleted = company.getChartOfAccounts()
								.removeAccount(selectedAccount.getAccountNumber()); // Assuming
																					// remove by ID
							
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
						
					});
				}
				
			}
			else
			{
				Alert error =
					new Alert(Alert.AlertType.WARNING, "No account selected for deletion.");
				error.setHeaderText("Selection Missing");
				error.showAndWait();
			}
			
		});
		loadCoaData(); // Initial data load
	}
	
	// AccountData inner class is removed
}
