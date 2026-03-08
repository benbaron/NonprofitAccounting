
package nonprofitbookkeeping.ui.panels;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;

import javafx.collections.*;
import javafx.geometry.Insets;
import javafx.geometry.Rectangle2D;
import javafx.stage.Screen;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.Button; // For casting items in toolbar
import javafx.scene.control.ToolBar; // For the actionToolBar field
import javafx.scene.layout.BorderPane;

import nonprofitbookkeeping.model.*;
import nonprofitbookkeeping.model.CurrentCompany; // Explicit import for inner class usage
import nonprofitbookkeeping.ui.helpers.AlertBox;

/**
 * JavaFX panel for displaying and managing journal transactions.
 * It provides a table view of {@link AccountingTransaction} objects and
 * allows users to add, edit, or delete these transactions via a toolbar
 * and a dialog ({@link GeneralJournalEntryPanelFX}).
 * Data is typically sourced from the {@link Journal} of the {@link CurrentCompany}.
 */
public class JournalPanelFX extends BorderPane
{
	
	/** The rows. */
	private final ObservableList<AccountingTransaction> rows = FXCollections.observableArrayList();
	
	/** The table. */
	private final TableView<AccountingTransaction> table = new TableView<>(this.rows);
	
	/** The company listener. */
	private JournalPanelCompanyListener companyListener;
	
	/** The action tool bar. */
	private ToolBar actionToolBar; // Field to store the toolbar
	
	/**
	 * Instantiates a new journal panel FX.
	 */
	public JournalPanelFX()
	{
		setPadding(new Insets(10));
		buildTable();
		setCenter(this.table);
		
		buildToolBar(); // Creates and assigns to this.actionToolBar
		setBottom(this.actionToolBar); // Sets the stored toolbar
		
		this.companyListener = new JournalPanelCompanyListener(this);
		CurrentCompany.CompanyListener.addCompanyListener(this.companyListener);
		
		handleCompanyChange(CurrentCompany.isOpen());
		
	}
	
	/* -------- Table -------- */
	/**
	 * Builds and configures the {@link TableView} ({@link #table}) for displaying journal transactions.
	 * Sets a column resize policy and defines columns for ID, Date, Account, Debit, Credit, and Memo
	 * using the helper methods {@link #col(String, String)} and {@link #num(String, String)}.
	 */
	@SuppressWarnings("unchecked") private void buildTable()
	{
		this.table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
		this.table.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
		this.table.getColumns().addAll(
				col("ID", "id"),
				col("Date", "date"),
				col("Account", "accountName"),
				num("Debit", "debit"),
				num("Credit", "credit"),
				col("Memo", "memo"),
				col("To/From", "toFrom"),
				col("Check #", "checkNumber"),
				col("Clear Bank", "clearBank"),
				col("Budget Tracking", "budgetTracking"),
				col("Fund Name", "associatedFundName"));

		this.table.setRowFactory(tv -> {
			TableRow<AccountingTransaction> row = new TableRow<>();

			row.setOnMouseClicked(event -> {
				if (event.getClickCount() == 2 && !row.isEmpty())
				{
					openEditor(row.getItem());
				}
			});

			return row;
		});
	}
	/**
	 * Utility method to create a {@link TableColumn} for displaying String properties
	 * from an {@link AccountingTransaction}.
	 *
	 * @param t The title of the column for the table header.
	 * @param p The name of the property in {@link AccountingTransaction} to bind this column to
	 *          (e.g., "date" for getDate()).
	 * @return A configured {@link TableColumn} for displaying String data.
	 */
	private static TableColumn<AccountingTransaction, String> col(String t, String p)
	{
		TableColumn<AccountingTransaction, String> c = new TableColumn<>(t);
		c.setCellValueFactory(new PropertyValueFactory<>(p));
		return c;
		
	}
	
	/**
	 * Utility method to create a {@link TableColumn} for displaying {@link BigDecimal} properties
	 * from an {@link AccountingTransaction}.
	 *
	 * @param t The title of the column for the table header.
	 * @param p The name of the property in {@link AccountingTransaction} to bind this column to
	 *          (e.g., "debit" for getDebit()).
	 * @return A configured {@link TableColumn} for displaying BigDecimal data.
	 */
	private static TableColumn<AccountingTransaction, BigDecimal> num(String t, String p)
	{
		TableColumn<AccountingTransaction, BigDecimal> c = new TableColumn<>(t);
		c.setCellValueFactory(new PropertyValueFactory<>(p));
		return c;
		
	}
	
	/* -------- Toolbar -------- */
	/**
	 * Builds and returns a {@link ToolBar} containing action buttons for
	 * managing journal transactions. The toolbar includes "New", "Edit",
	 * "Delete", and "Refresh" buttons. The first three open the journal
	 * entry editor or remove the selected entry. The refresh button reloads
	 * the table from the underlying journal.
	 *
	 * @return A {@link ToolBar} node populated with action buttons.
	 */
	private void buildToolBar()
	{
		Button add = new Button("New");
		Button edit = new Button("Edit");
		Button del = new Button("Delete");
		Button refreshBtn = new Button("Refresh");
		
		add.setOnAction(e -> openEditor(null));
		edit.setOnAction(e -> {
			AccountingTransaction sel = this.table.getSelectionModel().getSelectedItem();

			if (sel != null)
			{
				openEditor(sel);
			}
		});

		del.setOnAction(e -> {
			ObservableList<AccountingTransaction> selected =
					this.table.getSelectionModel().getSelectedItems();
			
			if (selected != null && !selected.isEmpty())
			{
				
				if (CurrentCompany.isOpen() && CurrentCompany.getCompany() != null &&
						CurrentCompany.getCompany().getLedger() != null)
				{
					Journal journal = CurrentCompany.getCompany().getLedger().getJournal();
					
					if (journal != null)
					{
						ArrayList<AccountingTransaction> toDelete = new ArrayList<>(selected);
						
						for (AccountingTransaction tx : toDelete)
						{
							journal.deleteTransaction(tx.getBookingDateTimestamp());
						}
						
						try
						{
							CurrentCompany.persist();
						}
						catch (IOException ex)
						{
							AlertBox.showError(getScene() == null ? null : getScene().getWindow(),
								"Unable to save deleted transactions. Please try again.");
							return;
						}
						
						refresh();
					}
					
				}
				
			}
			
		});
		
		refreshBtn.setOnAction(e -> refresh());
		
		this.actionToolBar = new ToolBar(add, edit, del, refreshBtn);
		
	}
	
	/* -------- CRUD -------- */
	/**
	 * Opens a dialog (using {@link GeneralJournalEntryPanelFX}) for creating a new journal transaction
	 * or editing an existing one.
	 * If {@code existing} is null, the dialog is configured for adding a new transaction.
	 * Otherwise, it's configured for editing the provided {@code existing} transaction.
	 * The dialog handles the save/update logic via callbacks that interact with the
	 * main {@link Journal} from the {@link CurrentCompany}.
	 * The table is refreshed upon successful save or update.
	 * If the main journal is not available from {@code CurrentCompany}, an error is printed,
	 * and the dialog is not shown.
	 *
	 * @param existing The {@link AccountingTransaction} to edit. If null, a new transaction will be created.
	 */
	private void openEditor(AccountingTransaction existing)
	{
		
		if (!CurrentCompany.isOpen() || CurrentCompany.getCompany() == null ||
				CurrentCompany.getCompany().getLedger() == null)
		{
			System.err.println(
					"Error: Cannot open transaction editor. No active company or journal.");
			return;
		}
		
		Journal mainJournal = CurrentCompany.getCompany().getLedger().getJournal();
		
		if (mainJournal == null)
		{
			System.err.println("Error: Journal not available in CurrentCompany.");
			return;
		}
		
		BorderPane pane = new GeneralJournalEntryPanelFX(existing, tx -> {
			
			if (existing == null)
			{
				mainJournal.addTransaction(tx);
			}
			else
			{
				mainJournal.updateTransaction(tx);
			}
			
			try
			{
				CurrentCompany.persist();
			}
			catch (IOException ex)
			{
				AlertBox.showError(getScene() == null ? null : getScene().getWindow(),
					"Unable to save the transaction. Please try again.");
				return;
			}
			
			refresh();
		});
		
		Dialog<Void> d = new Dialog<>();
		d.setTitle(existing == null ? "New Transaction" : "Edit Transaction");
		d.getDialogPane().setContent(pane);
		d.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
		d.setResizable(true);
		
		Rectangle2D bounds = Screen.getPrimary().getVisualBounds();
		
		double prefW = Math.min(1200, bounds.getWidth() * 0.92);
		double prefH = Math.min(860, bounds.getHeight() * 0.90);
		
		double minW = Math.max(800, Math.min(980, bounds.getWidth() * 0.70));
		double minH = Math.max(600, Math.min(720, bounds.getHeight() * 0.70));
		
		d.getDialogPane().setPrefSize(prefW, prefH);
		d.getDialogPane().setMinSize(minW, minH);
		
		pane.setMinSize(
			Math.max(760, minW - 40),
			Math.max(520, minH - 80));
		pane.setPrefSize(
			Math.max(900, prefW - 20),
			Math.max(620, prefH - 40));
		
		pane.prefWidthProperty().bind(d.getDialogPane().widthProperty().subtract(24));
		pane.prefHeightProperty().bind(d.getDialogPane().heightProperty().subtract(80));
		
		d.showAndWait();
	}
	
	/**
	 * Refreshes the data displayed in the journal transaction {@link #table}.
	 * It retrieves the current list of transactions from the {@link Journal} of the
	 * {@link CurrentCompany}. If the journal or company is not available, the table is cleared.
	 * Otherwise, the table's backing list ({@link #rows}) is updated with the fetched transactions.
	 */
	void refresh()
	{
		
		if (CurrentCompany.isOpen() && CurrentCompany.getCompany() != null &&
				CurrentCompany.getCompany().getLedger() != null)
		{
			Journal journal = CurrentCompany.getCompany().getLedger().getJournal();
			
			if (journal != null)
			{
				this.rows.setAll(journal.getJournalTransactions());
			}
			else
			{
				this.rows.clear();
			}
			
		}
		else
		{
			this.rows.clear();
		}
		
	}
	
	/**
	 * Handle company change.
	 *
	 * @param isOpen the is open
	 */
	private void handleCompanyChange(boolean isOpen)
	{
		
		if (isOpen)
		{
			refresh();
			
			if (this.actionToolBar != null)
			{
				this.actionToolBar.getItems().forEach(item -> {
					
					if (item instanceof Button)
					{
						((Button) item).setDisable(false);
					}
					
				});
			}
			
		}
		else
		{
			this.rows.clear();
			
			if (this.actionToolBar != null)
			{
				this.actionToolBar.getItems().forEach(item -> {
					
					if (item instanceof Button)
					{
						((Button) item).setDisable(true);
					}
					
				});
			}
			
		}
		
	}
	
	/**
	 * The listener interface for receiving journalPanelCompany events.
	 * The class that is interested in processing a journalPanelCompany
	 * event implements this interface, and the object created
	 * with that class is registered with a component using the
	 * component's <code>addJournalPanelCompanyListener</code> method. When
	 * the journalPanelCompany event occurs, that object's appropriate
	 * method is invoked.
	 *
	 * @see JournalPanelCompanyEvent
	 */
	private class JournalPanelCompanyListener implements CurrentCompany.CompanyChangeListener
	{
		
		/** The panel. */
		private JournalPanelFX panel;
		
		/**
		 * Instantiates a new journal panel company listener.
		 *
		 * @param panel the panel
		 */
		public JournalPanelCompanyListener(JournalPanelFX panel)
		{
			this.panel = panel;
			
		}
		
		/**
		 * Override @see nonprofitbookkeeping.model.CurrentCompany.CompanyChangeListener#companyChange(boolean) 
		 */
		@Override public void companyChange(boolean isOpen)
		{
			this.panel.handleCompanyChange(isOpen);
			
		}
		
	}
	
}
