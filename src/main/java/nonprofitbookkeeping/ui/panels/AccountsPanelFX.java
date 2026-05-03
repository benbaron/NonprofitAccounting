
package nonprofitbookkeeping.ui.panels;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import nonprofitbookkeeping.model.Account;
import nonprofitbookkeeping.model.AccountType;
import nonprofitbookkeeping.service.AccountService;
import nonprofitbookkeeping.model.CurrentCompany; // Added for listener
import nonprofitbookkeeping.model.Company;
import nonprofitbookkeeping.ui.UiSpacing;


/**
 * JavaFX port of {@code AccountsPanel}. Shows the chart of accounts and basic
 * add/edit/delete row operations. It uses an {@link AccountService} to fetch account data
 * and displays it in a {@link TableView} using an inner {@link AccountRow} class for data binding.
 */
public class AccountsPanelFX extends BorderPane
{
	/** The TableView used to display account information. */
	private final TableView<AccountRow> table = new TableView<>();
	/** The ObservableList that backs the {@code table}, containing {@link AccountRow} objects. */
	private final ObservableList<AccountRow> rows =
		FXCollections.observableArrayList();
	
	/** The company listener. */
	private AccountsPanelCompanyListener companyListener;
	
	/** The action buttons box. */
	private HBox actionButtonsBox; // To store the controls
	
	/**
	 * Instantiates a new accounts panel FX.
	 *
	 * @param service the service
	 */
	public AccountsPanelFX(AccountService service)
	{
		// service param is kept for signature compatibility
		setPadding(UiSpacing.pageInsets());
		buildTable();
		setCenter(
			new TitledPane("Chart of Accounts", this.table)
			{
				{
					setCollapsible(false);
				}
				
			});
		
		this.actionButtonsBox = buildControls(); // New: call and store
		setBottom(this.actionButtonsBox); // New: set stored HBox
		
		this.companyListener = new AccountsPanelCompanyListener(this);
		CurrentCompany.CompanyListener.addCompanyListener(this.companyListener);
		
		handleCompanyChange(CurrentCompany.isOpen());
		
	}
	
	/**
	 * Builds and configures the columns for the accounts {@link TableView}.
	 * Columns include Account Code, Account Name, Type, Parent Account, Currency, and Opening Balance.
	 * It uses {@link PropertyValueFactory} to bind columns to the properties of {@link AccountRow}.
	 * The {@code @SuppressWarnings({ "unchecked", "deprecation" })} might be related to raw type usage
	 * with PropertyValueFactory if not using type-safe cell value factories, or if some property names
	 * don't strictly follow Java bean conventions (though "opening" for "openingBalance" is common).
	 */
	@SuppressWarnings(
	{ "unchecked", "deprecation" })
	
	private void buildTable()
	{
		TableColumn<AccountRow, String> codeCol = col("Account Code", "code");
		TableColumn<AccountRow, String> nameCol = col("Account Name", "name");
		TableColumn<AccountRow, String> typeCol = col("Type", "type");
		TableColumn<AccountRow, String> parentCol =
			col("Parent Account", "parent");
		TableColumn<AccountRow, String> curCol = col("Currency", "currency");
		TableColumn<AccountRow, BigDecimal> balCol =
			new TableColumn<>("Opening Balance");
		
		balCol.setCellValueFactory(new PropertyValueFactory<>("opening"));
		
		this.table.getColumns()
			.addAll(codeCol, nameCol,
				typeCol, parentCol,
				curCol, balCol);
		this.table.setItems(this.rows);
		this.table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
		
	}
	
	/**
	 * Utility method to create a {@link TableColumn} with a given title and property name
	 * for use with {@link PropertyValueFactory}.
	 *
	 * @param <T> The type of the data in the column.
	 * @param title The title of the column for the table header.
	 * @param prop The name of the property in {@link AccountRow} to bind this column to.
	 * @return A configured {@link TableColumn}.
	 */
	private static <T> TableColumn<AccountRow, T> col(String title, String prop)
	{
		TableColumn<AccountRow, T> c = new TableColumn<>(title);
		c.setCellValueFactory(new PropertyValueFactory<>(prop));
		return c;
		
	}
	
	/**
	 * Builds and returns an {@link HBox} containing control buttons for managing accounts
	 * (Add, Edit, Delete).
	 * <ul>
	 *   <li>"Add Account": Adds a new empty {@link AccountRow} to the table for inline editing (if table is editable).</li>
	 *   <li>"Edit Account": Puts the selected table row into editing mode (starts editing the first cell).
	 *       Shows an alert if no row is selected.</li>
	 *   <li>"Delete Account": Removes the selected row from the table.</li>
	 * </ul>
	 * @return An {@link HBox} with configured control buttons.
	 */
	private HBox buildControls()
	{
		HBox box = new HBox(UiSpacing.SECTION_SPACING);
		box.setPadding(new Insets(UiSpacing.SECTION_SPACING));
		Button add = new Button("Add Account");
		Button edit = new Button("Edit Account");
		Button delete = new Button("Delete Account");
		
		add.setOnAction(e -> this.rows.add(new AccountRow()));
		edit.setOnAction(e -> onEditAction());
		delete.setOnAction(e -> onDeleteAction());
		
		box.getChildren().addAll(add, edit, delete);
		return box;
		
	}
	
	/**
	 * onDeleteAction.
	 */
	void onDeleteAction()
	{
		int idx = this.table.getSelectionModel().getSelectedIndex();
		
		if (idx >= 0)
		{
			// Consider if AccountService needs to be notified of deletion
			this.rows.remove(idx);
		}
		else
		{
			alert("Please select an account to delete.");
		}
		
	}
	
	/**
	 * onEditAction.
	 */
	void onEditAction()
	{
		
		if (this.table.getSelectionModel().isEmpty())
		{
			alert("Please select an account to edit.");
		}
		else
		{
			this.table.edit(this.table.getSelectionModel().getSelectedIndex(),
				this.table.getColumns().get(0));
		}
		
	}
	
	/**
	 * Displays a simple informational alert dialog.
	 *
	 * @param msg The message to display in the alert.
	 */
	private static void alert(String msg)
	{
		new Alert(Alert.AlertType.INFORMATION, msg, ButtonType.OK)
			.showAndWait();
		
	}
	
	/**
	 * Refreshes the data displayed in the accounts table.
	 * It clears the current rows and repopulates them by fetching all accounts
	 * from {@link AccountService#getAllAccounts()} and converting each {@link Account}
	 * to an {@link AccountRow}.
	 */
	private void refresh()
	{
		this.rows.clear();
		
		if (!CurrentCompany.isOpen())
		{
			return;
		}
		
		Company company = CurrentCompany.getCompany();
		
		if (company != null && company.getChartOfAccounts() != null)
		{
			List<Account> accounts = company.getChartOfAccounts().getAccounts();
			
			if (accounts != null)
			{
				accounts.forEach(a -> this.rows.add(new AccountRow(a)));
			}
			
		}
		
	}
	
	
	/**
	 * New method to handle company state changes.
	 *
	 * @param companyIsOpen the company is open
	 */
	private void handleCompanyChange(boolean companyIsOpen)
	{
		
		if (companyIsOpen)
		{
			refresh();
			
			if (this.actionButtonsBox != null)
			{
				this.actionButtonsBox.getChildren().forEach(node -> {
					
					if (node instanceof Button)
					{
						((Button) node).setDisable(false);
					}
					
				});
			}
			
		}
		else
		{
			this.rows.clear();
			
			if (this.actionButtonsBox != null)
			{
				this.actionButtonsBox.getChildren().forEach(node -> {
					
					if (node instanceof Button)
					{
						((Button) node).setDisable(true);
					}
					
				});
			}
			
		}
		
	}
	
	/**
	 * Should be called when this panel is no longer needed. It unregisters
	 * the panel from {@link CurrentCompany.CompanyListener} to avoid memory
	 * leaks from dangling listeners.
	 */
	public void dispose()
	{
		
		if (this.companyListener != null)
		{
			CurrentCompany.CompanyListener
				.removeCompanyListener(this.companyListener);
			this.companyListener = null;
		}
		
	}
	
	
	/**
	 * AccountsPanelCompanyListener.
	 *
	 * @see AccountsPanelCompanyEvent
	 */
	private class AccountsPanelCompanyListener
		implements CurrentCompany.CompanyChangeListener
	{
		
		/** The panel. */
		private AccountsPanelFX panel;
		
		/**
		 * Instantiates a new accounts panel company listener.
		 *
		 * @param panel the panel
		 */
		public AccountsPanelCompanyListener(AccountsPanelFX panel)
		{
			this.panel = panel;
			
		}
		
		/**
		 * Override @see nonprofitbookkeeping.model.CurrentCompany.CompanyChangeListener#companyChange(boolean) 
		 */
		@Override
		public void companyChange(boolean isOpen)
		{
			this.panel.handleCompanyChange(isOpen);
			
		}
		
	}
	
	
	/////////////////////////////////////////////////////////
	/**
	 * The Class AccountRow.
	 */
	/* This class acts as a data bean for JavaFX's {@link PropertyValueFactory}
	 * to populate table cells. It typically wraps an {@link Account} object or
	 * holds data for a new/editable account row. */
	public static class AccountRow
	{
		/** The account code or number. Defaults to an empty string. */
		private String code = "";
		/** The name of the account. Defaults to an empty string. */
		private String name = "";
		/** The currency code for the account (e.g., "USD"). Defaults to "USD". */
		private String currency = "USD";
		/**
		 * The parent account if this is a sub-account.
		 * This can be {@code null} if it's a top-level account.
		 * Direct {@link Account} reference might be complex for direct TableView editing
		 * unless appropriate converters/cell factories are used.
		 */
		private String parentId;
		/** The opening balance of the account. Defaults to {@link BigDecimal#ZERO}. */
		private BigDecimal opening = BigDecimal.ZERO;
		/** The type of the account (e.g., Asset, Liability). This can be {@code null}. */
		private AccountType type;
		
		/**
		 * Default constructor for creating an empty {@code AccountRow},
		 * typically used when adding a new account via the UI.
		 * Initializes fields to default values.
		 */
		public AccountRow()
		{
		
		}
		
		/**
		 * Constructor AccountRow.
		 *
		 * @param a the a
		 */
		public AccountRow(Account a)
		{
			Objects.requireNonNull(a,
				"Account cannot be null for AccountRow construction.");
			this.code = a.getAccountCode();
			this.name = a.getName();
			this.type = a.getAccountType();
			this.parentId = a.getParentAccountId();
			this.currency = a.getCurrency();
			this.opening = a.getOpeningBalance();
			
		}
		
		/**
		 * Gets the code.
		 *
		 * @return the code
		 */
		public String getCode()
		{
			return this.code;
			
		}
		
		/**
		 * Sets the code.
		 *
		 * @param s the new code
		 */
		public void setCode(String s)
		{
			this.code = s;
			
		}
		
		/**
		 * Gets the name.
		 *
		 * @return the name
		 */
		public String getName()
		{
			return this.name;
			
		}
		
		/**
		 * Sets the name.
		 *
		 * @param s the new name
		 */
		public void setName(String s)
		{
			this.name = s;
			
		}
		
		/**
		 * Gets the type.
		 *
		 * @return the type
		 */
		public AccountType getType()
		{
			return this.type;
			
		}
		
		/**
		 * Sets the type.
		 *
		 * @param s the new type
		 */
		public void setType(AccountType s)
		{
			this.type = s;
			
		}
		
		/**
		 * Gets the parent id.
		 *
		 * @return the parent id
		 */
		public String getParentId()
		{
			return this.parentId;
			
		}
		
		/**
		 * Sets the parent id.
		 *
		 * @param s the new parent id
		 */
		public void setParentId(String s)
		{
			this.parentId = s;
			
		}
		
		/**
		 * Gets the currency.
		 *
		 * @return the currency
		 */
		public String getCurrency()
		{
			return this.currency;
			
		}
		
		/**
		 * Sets the currency.
		 *
		 * @param s the new currency
		 */
		public void setCurrency(String s)
		{
			this.currency = s;
			
		}
		
		/**
		 * Gets the opening.
		 *
		 * @return the opening
		 */
		public BigDecimal getOpening()
		{
			return this.opening;
			
		}
		
		/**
		 * Sets the opening.
		 *
		 * @param b the new opening
		 */
		public void setOpening(BigDecimal b)
		{
			this.opening = (b != null) ? b : BigDecimal.ZERO;
			
		}
		
	}
	
}
