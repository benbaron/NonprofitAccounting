
package nonprofitbookkeeping.ui.panels;

import java.math.BigDecimal;
import java.util.List;

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
	private final ObservableList<AccountRow> rows = FXCollections.observableArrayList();

	/**
     * Constructs a new {@code AccountsPanelFX}.
     * Initializes the panel layout, builds the table structure, sets up control buttons
     * (Add, Edit, Delete), and performs an initial refresh of account data from the
     * provided {@link AccountService}.
     *
     * @param service The {@link AccountService} used to fetch and potentially manage account data.
     *                The {@code @SuppressWarnings("unused")} suggests it might not be fully utilized
     *                if all operations are static on {@code AccountService} or if some features are pending.
     *                Currently, {@code AccountService.getAllAccounts()} is used in {@link #refresh()}.
     */
	public AccountsPanelFX(@SuppressWarnings("unused") AccountService service) // service param is used in refresh() via static call
	{
		setPadding(new Insets(10));
		buildTable();
		setCenter(new TitledPane("Chart of Accounts", this.table)
		{
			{
				setCollapsible(false); // TitledPane is not collapsible
			}
			
		});
		setBottom(buildControls());
		refresh();
	}
	
	/**
     * Builds and configures the columns for the accounts {@link TableView}.
     * Columns include Account Code, Account Name, Type, Parent Account, Currency, and Opening Balance.
     * It uses {@link PropertyValueFactory} to bind columns to the properties of {@link AccountRow}.
     * The {@code @SuppressWarnings({ "unchecked", "deprecation" })} might be related to raw type usage
     * with PropertyValueFactory if not using type-safe cell value factories, or if some property names
     * don't strictly follow Java bean conventions (though "opening" for "openingBalance" is common).
     */
	@SuppressWarnings({ "unchecked", "deprecation" }) // PropertyValueFactory can lead to these if not careful with property names
	private void buildTable()
	{
		TableColumn<AccountRow, String> codeCol = col("Account Code", "code");
		TableColumn<AccountRow, String> nameCol = col("Account Name", "name");
		TableColumn<AccountRow, String> typeCol = col("Type", "type"); // Will use AccountType.toString()
		TableColumn<AccountRow, String> parentCol = col("Parent Account", "parent"); // Will use Account.toString() or name
		TableColumn<AccountRow, String> curCol = col("Currency", "currency");
		TableColumn<AccountRow, BigDecimal> balCol = new TableColumn<>("Opening Balance");
		balCol.setCellValueFactory(new PropertyValueFactory<>("opening")); // Assumes AccountRow has getOpening()
		
		this.table.getColumns().addAll(codeCol, nameCol, 
			typeCol, parentCol, 
			curCol, balCol);
		this.table.setItems(this.rows);
		this.table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY); // Ensures columns fit width
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
		HBox box = new HBox(10);
		box.setPadding(new Insets(8));
		Button add = new Button("Add Account");
		Button edit = new Button("Edit Account");
		Button delete = new Button("Delete Account");
		
		add.setOnAction(e -> this.rows.add(new AccountRow())); // Adds a new default row
		edit.setOnAction(e -> {
			
			if (this.table.getSelectionModel().isEmpty())
			{
				alert("Please select an account to edit.");
			}
			else
			{
				// Start editing the first column of the selected row
				this.table.edit(this.table.getSelectionModel().getSelectedIndex(), this.table.getColumns().get(0));
			}
			
		});
		delete.setOnAction(e -> {
			int idx = this.table.getSelectionModel().getSelectedIndex();
			if (idx >= 0) { // Check if a row is actually selected
				this.rows.remove(idx);
            } else {
                alert("Please select an account to delete.");
            }
		});
		box.getChildren().addAll(add, edit, delete);
		return box;
	}
	
	/**
     * Displays a simple informational alert dialog.
     *
     * @param msg The message to display in the alert.
     */
	private static void alert(String msg)
	{
		new Alert(Alert.AlertType.INFORMATION, msg, ButtonType.OK).showAndWait();
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
		List<Account> accounts = AccountService.getAllAccounts(); // Static call
		if (accounts != null) { // Guard against null from service
		    accounts.forEach(a -> this.rows.add(new AccountRow(a)));
		}
	}
	
	/**
     * Represents a single row in the accounts table ({@link AccountsPanelFX#table}).
     * This class acts as a data bean for JavaFX's {@link PropertyValueFactory}
     * to populate table cells. It typically wraps an {@link Account} object or holds
     * data for a new/editable account row.
     */
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
		private Account parent;
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
         * Constructs an {@code AccountRow} populated with data from an existing {@link Account} object.
         *
         * @param a The {@link Account} object to represent as a table row. Must not be null.
         * @throws NullPointerException if {@code a} is null.
         */
		public AccountRow(Account a)
		{
			Objects.requireNonNull(a, "Account cannot be null for AccountRow construction.");
			this.code = a.getAccountCode();
			this.name = a.getName();
			this.type = a.getAccountType();
			this.parent = a.getParentAccount();
			this.currency = a.getCurrency();
			this.opening = a.getOpeningBalance();
		}
		
		// Getters and Setters for TableView PropertyValueFactory binding

		/**
		 * Gets the account code.
		 * @return The account code as a String.
		 */
		public String getCode() { return this.code; }
		/**
		 * Sets the account code.
		 * @param s The new account code. If null, it might be stored as null or an empty string depending on desired behavior.
		 */
		public void setCode(String s) { this.code = s; }
		
		/**
		 * Gets the account name.
		 * @return The account name as a String.
		 */
		public String getName() { return this.name; }
		/**
		 * Sets the account name.
		 * @param s The new account name. If null, it might be stored as null or an empty string.
		 */
		public void setName(String s) { this.name = s; }
		
		/**
		 * Gets the account type.
		 * @return The {@link AccountType}, or {@code null} if not set.
		 */
		public AccountType getType() { return this.type; }
		/**
		 * Sets the account type.
		 * @param s The new {@link AccountType}. Can be {@code null}.
		 */
		public void setType(AccountType s) { this.type = s; }
		
		/**
		 * Gets the parent account.
		 * @return The parent {@link Account}, or {@code null} if this is a top-level account or not set.
		 */
		public Account getParent() { return this.parent; }
		/**
		 * Sets the parent account.
		 * @param s The new parent {@link Account}. Can be {@code null}.
		 */
		public void setParent(Account s) { this.parent = s; }
		
		/**
		 * Gets the currency code for the account.
		 * @return The currency code as a String (e.g., "USD").
		 */
		public String getCurrency() { return this.currency; }
		/**
		 * Sets the currency code for the account.
		 * @param s The new currency code. Standard ISO 4217 codes are recommended (e.g., "USD", "EUR").
		 */
		public void setCurrency(String s) { this.currency = s; }
		
		/**
		 * Gets the opening balance of the account.
		 * @return The opening balance as a {@link BigDecimal}. Defaults to {@link BigDecimal#ZERO}.
		 */
		public BigDecimal getOpening() { return this.opening; }
		/**
		 * Sets the opening balance of the account.
		 * If the provided BigDecimal is {@code null}, the balance is set to {@link BigDecimal#ZERO}.
		 * @param b The new opening balance.
		 */
		public void setOpening(BigDecimal b) { this.opening = (b != null) ? b : BigDecimal.ZERO; }
		
	}
	
}
