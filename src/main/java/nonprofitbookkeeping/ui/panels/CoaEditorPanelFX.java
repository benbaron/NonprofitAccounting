
package nonprofitbookkeeping.ui.panels;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.Window;
// Added for listener
import nonprofitbookkeeping.model.*;
import nonprofitbookkeeping.service.ChartOfAccountsIOService;
import nonprofitbookkeeping.service.ChartOfAccountsService;
import nonprofitbookkeeping.ui.helpers.AlertBox;

/**
 * Interactive ladder view for editing a company's Chart of Accounts.
 *
 * <p>Usage:</p>
 * <pre>{@code
 * CoaEditorPanelFX editor = new CoaEditorPanelFX(
 *         company.getChartOfAccounts(),
 *         chart -> {                       // on-save callback
 *             company.setChartOfAccounts(chart);
 *             CompanyLoaderService.saveCompanyFile(companyFile, company);
 *         },
 *         () -> root.setCenter(previousView)   // on-close callback
 * );
 * root.setCenter(editor);
 * }</pre>
 * This panel uses a {@link TreeTableView} to display the hierarchical chart of accounts
 * and provides buttons for adding root accounts, sub-accounts, editing, deleting,
 * importing/exporting as XLSX, saving changes, and cancelling.
 */
public class CoaEditorPanelFX extends BorderPane
{
	private static final Logger LOGGER = LoggerFactory.getLogger(CoaEditorPanelFX.class);
	
	/** Service layer for Chart of Accounts operations. */
        private final ChartOfAccountsService svc;
	/** Callback to be executed when the "Save" button is clicked and changes are applied. Can be null. */
	private final Consumer<ChartOfAccounts> onSave;
	/** Callback to be executed when the panel is closed (e.g., via "Save" or "Cancel"). Can be null. */
	private final Runnable onClose;
	
        /** The TreeTableView used to display and interact with the chart of accounts. */
        private final TreeTableView<Account> tree = new TreeTableView<>();
        /** The root item for the {@link #tree}; it is hidden in the UI. */
        private final TreeItem<Account> rootItem = new TreeItem<>();

        /**
         * Returns the {@link TreeTableView} displaying the chart of accounts.
         * <p>
         * This is primarily exposed so embedding containers can adjust
         * properties such as the placeholder node when no company is open.
         *
         * @return the underlying {@code TreeTableView} component
         */
        public TreeTableView<Account> getTreeTable()
        {
                return this.tree;
        }
	
	/** Service for importing and exporting Chart of Accounts data to/from XLSX. */
	private final ChartOfAccountsIOService ioSvc = new ChartOfAccountsIOService();
	
	// Dialog fields - these are instance members because they are accessed by the
	// dialog's result converter lambda.
	/** TextField for account number input in the add/edit dialog. */
	private TextField numF;
	/** TextField for account name input in the add/edit dialog. */
	private TextField nameF;
	/** TextField for opening balance input in the add/edit dialog. */
	private TextField balF;
	/** ComboBox for selecting account type in the add/edit dialog. */
	private ComboBox<AccountType> typeBox;
	
	private CoaEditorPanelCompanyListener companyListener;
	private HBox actionButtonsBox;
	
	/** 
	 * Convenience constructor for {@code CoaEditorPanelFX} when no specific save or close callbacks are needed.
	 * Calls the main constructor with null for {@code onSave} and {@code onClose} callbacks.
	 * 
	 * @param chart The {@link ChartOfAccounts} to be edited. Must not be null.
	 */
	public CoaEditorPanelFX(ChartOfAccounts chart)
	{
		this(chart, null, null);
	}
	
	/**
	 * Constructs a new {@code CoaEditorPanelFX}.
	 * Initializes the panel with the given chart of accounts and sets up callbacks for save and close actions.
	 * The UI for the tree table view and control buttons is built and displayed.
	 *
	 * @param chart The {@link ChartOfAccounts} to be displayed and edited. Must not be null.
	 * @param onSave A {@link Consumer} callback that accepts the modified {@link ChartOfAccounts}
	 *               when the "Save" action is performed. Can be null if no save callback is needed.
	 * @param onClose A {@link Runnable} callback that is executed when the panel is closed,
	 *                either by saving or cancelling. Can be null if no close callback is needed.
	 * @throws NullPointerException if {@code chart} is null.
	 */
        public CoaEditorPanelFX(ChartOfAccounts chart,
                Consumer<ChartOfAccounts> onSave,
                Runnable onClose)
        {
                this.svc = new ChartOfAccountsService(
                        Objects.requireNonNull(chart, "ChartOfAccounts cannot be null."));
		this.onSave = onSave;
		this.onClose = onClose;
		
		setPadding(new Insets(10));
		
		// build the tree
		buildTree();
		
		setCenter(this.tree);
		
		this.actionButtonsBox = buildButtonsInternal();
		setBottom(this.actionButtonsBox);
		
		this.companyListener = new CoaEditorPanelCompanyListener(this);
		CurrentCompany.CompanyListener.addCompanyListener(this.companyListener);
		
                handleCompanyChange(CurrentCompany.isOpen());
        }

        /**
         * Replaces the currently displayed chart of accounts with the provided one
         * and refreshes the view.
         *
         * @param chart the new {@link ChartOfAccounts} to display
         */
        public void setChartOfAccounts(ChartOfAccounts chart)
        {
                Objects.requireNonNull(chart, "ChartOfAccounts cannot be null.");

                if (chart == this.svc.asChart())
                {
                        refresh();
                        return;
                }

                this.svc.replaceChart(chart);
                refresh();
        }
	
	
	/**
	 * Initializes and configures the {@link TreeTableView} component ({@link #tree}).
	 * Sets the root item (hidden), defines columns for Account Number, Name, Type, and Opening Balance,
	 * and sets a column resize policy.
	 * Column cell value factories are set up using the {@link #makeCol} helper method.
	 */
	@SuppressWarnings("unchecked") // For varargs in getColumns().addAll()
        private void buildTree()
        {
                this.tree.setShowRoot(false);
                // The actual root item is a dummy, its children are the
                // top-level accounts
                this.tree.setRoot(this.rootItem);

                this.tree.setPlaceholder(
                        new Label("No Chart of Accounts data to display or company not open."));
		
		this.tree.getColumns().addAll(
			makeCol("Number", Account::getAccountNumber),
			makeCol("Name", Account::getName),
			makeCol("Type", a -> (a.getAccountType() != null) ?
				a.getAccountType().toString() : ""),
			makeCol("Opening Balance", Account::getOpeningBalance));
		
		this.tree.setColumnResizePolicy(TreeTableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
	}
	
	/**
	 * Creates and configures an {@link HBox} containing action buttons for managing the chart of accounts.
	 * Buttons include: Add Root, Add Sub, Edit, Delete, Import XLSX, Export XLSX, Save, and Cancel.
	 * Event handlers are set for each button to trigger corresponding actions.
	 *
	 * @return An {@link HBox} populated with control buttons.
	 */
	private HBox buildButtonsInternal()
	{
		Button addRoot = new Button("Add Root");
		Button addSub = new Button("Add Sub-account");
		Button edit = new Button("Edit");
		Button del = new Button("Delete");
		Button saveBtn = new Button("Save");
		Button importBtn = new Button("Import XLSX");
		Button exportBtn = new Button("Export XLSX");
		Button cancel = new Button("Cancel");
		
		// button actions
		addRoot.setOnAction(e -> showDialog(null, null));
		addSub.setOnAction(e -> onSubAccountAction());
		edit.setOnAction(e -> onEditAction());
		del.setOnAction(e -> deleteSelected());
		importBtn.setOnAction(e -> importXlsx());
		exportBtn.setOnAction(e -> exportXlsx());
		saveBtn.setOnAction(e -> saveButtonAction());
		cancel.setOnAction(e -> closePanel());
		
		HBox hbox = new HBox(8, addRoot, addSub, edit, del,
			importBtn, exportBtn, saveBtn, cancel);
		hbox.setPadding(new Insets(6));
		return hbox;
	}
	
	/**
	 * onSubAccountAction
	 */
	void onSubAccountAction()
	{
		Account parent = selected();
		
		if (parent != null)
		{
			showDialog(parent, null);
		}
		
	}
	
	/**
	 * onEditAction
	 */
	void onEditAction()
	{
		Account sel = selected();
		
		if (sel != null)
		{
			showDialog(sel.getParentAccount(), sel);
		}
		
	}
	
	/**
	 * saveButtonAction
	 */
	void saveButtonAction()
	{
		
		if (this.onSave != null)
		{
			// On save, do this.
			this.onSave.accept(this.svc.asChart());
		}
		
		closePanel();
	}
	
	/**
	 * Displays a dialog for adding a new account or editing an existing one.
	 * The dialog includes fields for account number, name, type, and opening balance.
	 * <p>
	 * If {@code editing} is null, the dialog is configured for adding a new account.
	 * If {@code parent} is null in add mode, a root account is added. Otherwise, a sub-account to {@code parent} is added.
	 * If {@code editing} is not null, the dialog fields are pre-populated with its data.
	 * </p>
	 * <p>
	 * Input validation is performed for the account number (must be a positive integer and unique,
	 * unless editing the same account).
	 * Upon successful confirmation (OK button), the new or updated account details are processed:
	 * <ul>
	 *   <li>For edits, {@link ChartOfAccountsService#update} is called.</li>
	 *   <li>For new accounts, {@link ChartOfAccountsService#addRoot} or {@link ChartOfAccountsService#addChild} is called.</li>
	 * </ul>
	 * The tree view is then refreshed or updated accordingly.
	 * </p>
	 * 
	 * @param parent The parent {@link Account} if adding a sub-account; null if adding a root account or editing.
	 * @param editing The {@link Account} to edit; null if adding a new account.
	 */
	private void showDialog(Account parent, Account editing)
	{
		
		// Check if company is open before showing dialog that modifies COA
		if (!CurrentCompany.isOpen())
		{
			AlertBox.showError(null,
				"No Company Open",
				"Cannot perform this action as no company is currently open.");
			return;
		}
		
		// ... rest of the method remains the same
		boolean isEdit = editing != null;
		
		Dialog<Account> dlg = new Dialog<>();
		dlg.setTitle(
			isEdit ?
				"Edit Account" :
				(parent == null ?
					"Add Root Account" : "Add Sub-account to " + parent.getName()));
		dlg.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
		
		// Initialize dialog fields
		this.numF = new TextField(
			isEdit && editing.getAccountNumber() != null ? editing.getAccountNumber() : "");
		this.nameF = new TextField(isEdit && editing.getName() != null ? editing.getName() : "");
		this.typeBox = new ComboBox<>(FXCollections.observableArrayList(AccountType.values()));
		AccountType initialType = isEdit &&
			editing.getAccountType() != null ?
				editing.getAccountType() : AccountType.ASSET;
		this.typeBox.getSelectionModel().select(initialType);
		this.balF = new TextField(isEdit && editing.getOpeningBalance() != null ?
			editing.getOpeningBalance().toPlainString() : "0.00");
		
		GridPane gp = new GridPane();
		gp.setHgap(10);
		gp.setVgap(8);
		gp.addRow(0, new Label("Number"), this.numF);
		gp.addRow(1, new Label("Name"), this.nameF);
		gp.addRow(2, new Label("Type"), this.typeBox); // combo instead of text
		gp.addRow(3, new Label("Opening Balance"), this.balF);
		
		dlg.getDialogPane().setContent(gp);
		dlg.setResultConverter(btn -> {
			return resultConverterCallback(editing, btn);
		});
		dlg.showAndWait().ifPresent(det -> showAndWaitCallback(parent, editing, isEdit, det));
	}
	
	/**
	 * showAndWaitCallback
	 * @param parent
	 * @param editing
	 * @param isEdit
	 * @param det
	 */
	void showAndWaitCallback(Account parent, Account editing, boolean isEdit, Account det)
	{
		
		if (isEdit)
		{
			ChartOfAccountsService.update(editing,
				det.getName(),
				det.getAccountType(),
				det.getOpeningBalance());
			this.tree.refresh();
		}
		else
		{
			
			if (parent == null)
			{
				this.svc.addRoot(det);
			}
			else
			{
				this.svc.addChild(parent, det);
			}
			
			insertIntoTree(det, parent);
		}
		
	}
	
	/**
	 * resultConverterCallback
	 * @param editing
	 * @param btn
	 * @return
	 */
	Account resultConverterCallback(Account editing, ButtonType btn)
	{
		
		if (btn != ButtonType.OK)
			return null;
		
		/* 1) validate account number */
		String number = this.numF.getText().trim();
		
		if (!number.matches("\\d+"))
		{
			AlertBox.showError(null, "Account number must be a positive integer.");
			return null;
		}
		
		boolean duplicate = this.svc.findByNumber(number)
			.filter(acc -> !acc.equals(editing)) // ignore self when editing
			.isPresent();
		
		if (duplicate)
		{
			AlertBox.showError(null, "Account number already exists.");
			return null;
		}
		
		/* 2) build Account */
		return buildAccount(number); // pass number in
		
	}
	
	/**
	 * Deletes the currently selected account from the {@link TreeTableView} and the
	 * underlying {@link ChartOfAccountsService}.
	 * If no account is selected, this method does nothing.
	 * The corresponding node is removed from the tree view.
	 */
	private void deleteSelected()
	{
		
		if (!CurrentCompany.isOpen())
		{
			AlertBox.showError(null, "No Company Open",
				"Cannot perform this action as no company is currently open.");
			return;
		}
		
		// ... rest of the method
		Account sel = selected();
		
		if (sel == null)
		{
			AlertBox.showWarning(null, "No account selected for deletion."); // Provide feedback
			return;
		}
		
		this.svc.delete(sel); // Delete from the service/model
		TreeItem<Account> selectedTreeItem = this.tree.getSelectionModel().getSelectedItem();
		
		if (selectedTreeItem != null)
		{
			
			// Remove from tree:
			// If it's a root item's child:
			if (selectedTreeItem.getParent() == this.rootItem)
			{
				this.rootItem.getChildren().remove(selectedTreeItem);
			}
			// If it's a child of another account node:
			else if (selectedTreeItem.getParent() != null)
			{
				selectedTreeItem.getParent().getChildren().remove(selectedTreeItem);
			}
			
			this.tree.refresh(); // Refresh the tree view
		}
		
	}
	
	/**
	 * Initiates the process of importing a Chart of Accounts from an XLSX file.
	 * Displays a {@link FileChooser} for selecting the XLSX file. If a file is selected,
	 * it uses {@link ChartOfAccountsIOService#importFromXlsx(java.nio.file.Path)} to read the data,
	 * then {@link ChartOfAccountsService#replaceChart(ChartOfAccounts)} to update the current chart,
	 * and finally refreshes the tree view.
	 * Errors during import are displayed in an alert dialog.
	 */
	private void importXlsx()
	{
		
		if (!CurrentCompany.isOpen())
		{ // Check added
			AlertBox.showError(null, "No Company Open",
				"Cannot import Chart of Accounts as no company is currently open.");
			return;
		}
		
		// ... rest of the method
		FileChooser fc = new FileChooser();
		fc.setTitle("Import Chart of Accounts from XLSX");
		fc.getExtensionFilters()
			.add(new FileChooser.ExtensionFilter("Excel files (*.xlsx)", "*.xlsx"));
		File f = fc.showOpenDialog(getScene() != null ? getScene().getWindow() : null);
		
		if (f == null)
		{
			return; // User cancelled
		}
		
		try
		{
			ChartOfAccounts imported = this.ioSvc.importFromXlsx(f.toPath());
			this.svc.replaceChart(imported); // Replace current COA with imported one
			refresh(); // Refresh the tree view
			AlertBox.showInfo(null, "Chart of Accounts imported successfully from " +
				f.getName());
		}
		catch (IOException | NullPointerException ex) // Catch more specific exceptions
		{
			ex.printStackTrace(); // Log full stack trace for debugging
			AlertBox.showError(null, "Import failed: " + ex.getMessage());
		}
		
	}
	
	/**
	 * Initiates the process of exporting the current Chart of Accounts to an XLSX file.
	 * Displays a {@link FileChooser} (save dialog) for selecting the destination file.
	 * If a file path is chosen, it uses {@link ChartOfAccountsIOService#exportToXlsx(ChartOfAccounts, java.nio.file.Path)}
	 * to save the data.
	 * Success or error messages are displayed in alert dialogs.
	 */
	private void exportXlsx()
	{
		
		if (!CurrentCompany.isOpen() || this.svc.asChart().getAccounts().isEmpty())
		{ // Check added & ensure chart has content
			AlertBox.showError(null, "No Data to Export",
				"Cannot export Chart of Accounts as no company is open or the chart is empty.");
			return;
		}
		
		// ... rest of the method
		FileChooser fc = new FileChooser();
		fc.setTitle("Export Chart of Accounts to XLSX");
		fc.setInitialFileName("chart-of-accounts.xlsx");
		fc.getExtensionFilters()
			.add(new FileChooser.ExtensionFilter("Excel files (*.xlsx)", "*.xlsx"));
		File f = fc.showSaveDialog(getScene() != null ? getScene().getWindow() : null); // Set owner
		
		if (f == null)
		{
			return; // User cancelled
		}
		
		try
		{
			this.ioSvc.exportToXlsx(this.svc.asChart(), f.toPath());
			AlertBox.showInfo(null,
				"Chart of Accounts exported successfully to " + f.getAbsolutePath());
		}
		catch (IOException | NullPointerException ex) // Catch more specific exceptions
		{
			ex.printStackTrace(); // Log full stack trace
			AlertBox.showError(null, "Export failed: " + ex.getMessage());
		}
		
	}
	
	/**
	 * Closes this editor panel. If an {@code onClose} callback was provided during construction,
	 * it is executed. Otherwise, if this panel is hosted in its own {@link Stage} (e.g., as part of a dialog),
	 * that stage is closed.
	 */
	private void closePanel()
	{
		
		if (this.onClose != null)
		{
			this.onClose.run(); // Execute the provided callback
			// (e.g., caller restores previous
			// view)
		}
		else
		{
			// Fallback if no specific close handler: try to close the window this panel is
			// in.
			Window window = getScene() != null ? getScene().getWindow() : null;
			
			if (window instanceof Stage)
			{
				((Stage) window).close();
			}
			
		}
		
		// Notify other panels that the chart of accounts may have changed
		CurrentCompany.markCompanyOpen();
		
	}
	
	/**
	 * Refreshes the {@link TreeTableView} display.
	 * It clears all existing items from the root and rebuilds the tree structure
	 * by fetching root accounts from the {@link ChartOfAccountsService} and recursively
	 * adding their children using {@link #makeNode(Account)}.
	 */
	private void refresh()
	{
		this.rootItem.getChildren().clear();
		
		if (CurrentCompany.isOpen())
		{ // Only populate if a company is open
			this.svc.roots().forEach(r -> this.rootItem.getChildren().add(makeNode(r)));
		}
		
		this.tree.refresh();
	}
	
	/**
	 * Gets the {@link Account} object currently selected in the {@link TreeTableView}.
	 *
	 * @return The selected {@link Account}, or null if no item is selected or the selected item is null.
	 */
	private Account selected()
	{
		TreeItem<Account> ti = this.tree.getSelectionModel().getSelectedItem();
		return ti == null ? null : ti.getValue();
	}
	
	/**
	 * Constructs an {@link Account} object from the data entered in the add/edit dialog fields.
	 * This method is typically called from the dialog's result converter.
	 *
	 * @param number The account number string from the dialog's number field.
	 * @return A new {@link Account} populated with data from the dialog fields (name, type, opening balance).
	 *         Opening balance defaults to {@link BigDecimal#ZERO} if parsing fails.
	 */
	private Account buildAccount(String number)
	{
		Account a = new Account();
		a.setAccountNumber(number); // Assumes number is already validated
		a.setName(this.nameF.getText().trim());
		a.setAccountType(this.typeBox.getValue()); // Get selected AccountType from ComboBox
		
		try
		{
			a.setOpeningBalance(new BigDecimal(this.balF.getText().trim()));
		}
		catch (NumberFormatException ignore) // Or show an error to the user
		{
			a.setOpeningBalance(BigDecimal.ZERO); // Default if parsing fails
		}
		
		return a;
	}
	
	
	/**
	 * Recursively creates a {@link TreeItem} for the given {@link Account} ({@code acc})
	 * and all its children (obtained via {@link ChartOfAccountsService#childrenOf(Account)}).
	 * Each created {@code TreeItem} is set to be expanded by default.
	 *
	 * @param acc The {@link Account} for which to create a {@code TreeItem}.
	 * @return A {@link TreeItem<Account>} representing the given account and its descendants.
	 */
	private TreeItem<Account> makeNode(Account acc)
	{
		TreeItem<Account> ti = new TreeItem<>(acc);
		// Recursively add children
		this.svc.childrenOf(acc).forEach(child -> ti.getChildren().add(makeNode(child)));
		ti.setExpanded(true); // Expand nodes by default
		return ti;
	}
	
	/**
	 * Utility method to create a {@link TreeTableColumn} for the {@link TreeTableView}.
	 *
	 * @param <T> The type of the data to be displayed in this column's cells.
	 * @param name The title of the column (to be displayed in the header).
	 * @param fn A {@link Function} that takes an {@link Account} object (the value of the TreeItem)
	 *           and returns the value of type {@code T} to be displayed in the cell for that account.
	 * @return A configured {@link TreeTableColumn}.
	 */
	private static <T> TreeTableColumn<Account, T> makeCol(	String name,
															Function<Account, T> fn)
	{
		TreeTableColumn<Account, T> c = new TreeTableColumn<>(name);
		// CellValueFactory that extracts a value from the Account object wrapped by the
		// TreeItem
		c.setCellValueFactory(
			cd -> new ReadOnlyObjectWrapper<>(fn.apply(cd.getValue().getValue())));
		return c;
	}
	
	/**
	 * Inserts a new {@link TreeItem} representing the {@link Account} {@code det}
	 * into the {@link #tree}.
	 * If {@code parent} is null, the new item is added as a child of the root item.
	 * Otherwise, it attempts to find the {@code TreeItem} for the {@code parent} account
	 * and adds the new item as its child.
	 *
	 * @param det The {@link Account} for which the new {@link TreeItem} is to be inserted.
	 * @param parent The parent {@link Account} under which to insert the new item.
	 *               If null, {@code det} is added as a root-level account in the tree.
	 */
	private void insertIntoTree(Account det, Account parent)
	{
		
		if (parent == null)
		{
			this.rootItem.getChildren().add(makeNode(det)); // Add as a child of the (hidden) root
		}
		else
		{
			TreeItem<Account> parentItem = find(this.rootItem, parent); // Find the parent TreeItem
			
			if (parentItem != null)
			{
				parentItem.getChildren().add(makeNode(det));
				parentItem.setExpanded(true); // Ensure parent is expanded to show new child
			}
			else
			{
				// Should not happen if parent is valid and tree is consistent
				LOGGER.warn("Could not find parent TreeItem for account: " + parent.getName() +
					" when inserting child " + det.getName());
				// As a fallback, could add to root, but this indicates an issue.
				this.rootItem.getChildren().add(makeNode(det));
			}
			
		}
		
		this.tree.refresh(); // Refresh to show the new item
	}
	
	/**
	 * Recursively searches for a {@link TreeItem} within the subtree of {@code n}
	 * that wraps the specified {@link Account} {@code acc}.
	 *
	 * @param n The current {@link TreeItem} node to search from.
	 * @param acc The {@link Account} to find within the tree.
	 * @return The {@link TreeItem} that wraps {@code acc} if found; otherwise, null.
	 */
	private TreeItem<Account> find(TreeItem<Account> n, Account acc)
	{
		
		if (Objects.equals(n.getValue(), acc))
		{ // Use Objects.equals for null-safe comparison
			return n;
		}
		
		for (TreeItem<Account> c : n.getChildren())
		{
			TreeItem<Account> hit = find(c, acc);
			
			if (hit != null)
			{
				return hit;
			}
			
		}
		
		return null;
	}
	
	/**
	 * handleCompanyChange
	 * @param isOpen
	 */
        private void handleCompanyChange(boolean isOpen)
        {

                if (isOpen)
                {
			// If a company is opened, this panel should reflect the ChartOfAccounts
			// it was initially constructed with, assuming it's relevant to the
			// CurrentCompany.
			// The 'svc' (ChartOfAccountsService) holds the chart it was given.
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
                        this.rootItem.getChildren().clear();
                        this.tree.refresh();
                        this.tree.setPlaceholder(new Label("No company open."));

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
	 * CoaEditorPanelCompanyListener
	 */
	private class CoaEditorPanelCompanyListener implements CurrentCompany.CompanyChangeListener
	{
		private CoaEditorPanelFX panel;
		
		public CoaEditorPanelCompanyListener(CoaEditorPanelFX panel)
		{
			this.panel = panel;
		}
		
		@Override public void companyChange(boolean isOpen)
		{
			panel.handleCompanyChange(isOpen);
		}
		
	}
	
}
