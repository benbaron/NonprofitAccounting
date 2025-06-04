
package nonprofitbookkeeping.ui.panels;

import java.math.BigDecimal;
import java.util.function.Consumer;

import javafx.collections.*;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;

import nonprofitbookkeeping.model.*;

/** Shows transactions and lets user add / edit / delete them. */
public class JournalPanelFX extends BorderPane
{
	private final ObservableList<AccountingTransaction> rows =
		FXCollections.observableArrayList();
	
	/** transaction table - rows of transactions */
	private final TableView<AccountingTransaction> transactionTableView = 
		new TableView<AccountingTransaction>(this.rows);
	
	/**
	 * 
	 * Constructor JournalPanelFX
	 */
	public JournalPanelFX()
	{
		setPadding(new Insets(10));
		buildTable();
		setCenter(this.transactionTableView);
		setBottom(toolBar());
		refresh();
	}
	
	/* -------- Table -------- */
	/**
	 * buildTable
	 */
	private void buildTable()
	{
		
		// FIXME : the property fields must be 
		// names of actual AccountingTransaction fields
		this.transactionTableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
		this.transactionTableView.getColumns().addAll(
			stringColumn("ID", "id"),
			stringColumn("Date", "date"),
			stringColumn("Account", "accountName"),
			bigDecimalColumn("Debit", "debit"),
			bigDecimalColumn("Credit", "credit"),
			stringColumn("Memo", "memo"));
	}
	
	/**
	 * Table Column of Accounting Transaction, string-type
	 * 
	 * @param tableColumn
	 * @param property
	 * @return
	 */
	private static
			TableColumn<AccountingTransaction, String>
			stringColumn(String tableColumn,
			             String property)
	{
		TableColumn<AccountingTransaction, String> c = 
			new TableColumn<AccountingTransaction, String>(tableColumn);
		c.setCellValueFactory(new PropertyValueFactory<>(property));
		return c;
	}
	
	/**
	 * Number column
	 * 
	 * @param tableColumn
	 * @param property
	 * 
	 * @return column
	 */
	private static
			TableColumn<AccountingTransaction, BigDecimal>
			bigDecimalColumn(String tableColumn,
			                 String property)
	{
		TableColumn<AccountingTransaction, BigDecimal> c = new TableColumn<>(tableColumn);
		c.setCellValueFactory(new PropertyValueFactory<>(property));
		return c;
	}
	
	/* -------- Toolbar -------- */
	private Node toolBar()
	{
		Button add = new Button("New");
		Button edit = new Button("Edit");
		Button del = new Button("Delete");
		
		// On add
		add.setOnAction(e -> openEditor(null));
		// On edit
		edit.setOnAction(e -> editAction());
		// On delete
		del.setOnAction(e -> deleteAction());
		
		return new ToolBar(add, edit, del);
	}
	
	
	/* -------- CRUD -------- */
	
	/**
	 * openEditor
	 * 
	 * @param existing
	 */
	private void openEditor(AccountingTransaction existing)
	{
		Journal mainJournal = CurrentCompany.getCompany().getLedger().getJournal();
		
		if (mainJournal == null)
		{
			// Optionally, show an error dialog if the main journal isn't available
			System.err.println("Error: Journal not available in CurrentCompany.");
			
			// You might want to disable add/edit buttons if mainJournal is null
			return;
		}
		
		NewTransactionPanelFX pane =
			(existing == null) ?
				new NewTransactionPanelFX( 			// Create new
					(AccountingTransaction tx) ->
					{ 
						// onSave consumer for new
						mainJournal.addTransaction(tx);
						refresh();
					}) :
				new NewTransactionPanelFX(existing, // Edit existing
					(Consumer<AccountingTransaction>) tx ->
					{ 
						// onSave consumer for edit
						mainJournal.updateTransaction(tx);
						refresh();
					});
					
		Dialog<Void> d = new Dialog<>();
		d.setTitle(existing == null ? "New Transaction" : "Edit Transaction");
		d.getDialogPane().setContent(pane);
		d.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
		d.showAndWait();
	}
	
	/**
	 * 
	 */
	void editAction()
	{
		AccountingTransaction sel = this.transactionTableView.getSelectionModel().getSelectedItem();
		
		if (sel != null)
		{
			openEditor(sel);
		}
		
	}
	
	/**
	 * 
	 */
	void deleteAction()
	{
		AccountingTransaction sel = this.transactionTableView.getSelectionModel().getSelectedItem();
		
		if (sel != null)
		{
			Journal journal = CurrentCompany.getCompany().getLedger().getJournal();
			
			if (journal != null)
			{
				journal.deleteTransaction(sel.getBookingDateTimestamp());
				refresh();
			}
			
		}
		
	}
	
	
	/**
	 * refresh
	 */
	private void refresh()
	{
		Journal journal = CurrentCompany.getCompany().getLedger().getJournal();
		
		if (journal != null)
		{
			this.rows.setAll(journal.getJournalTransactions());
		}
		else
		{
			this.rows.clear(); // Or handle as an error/empty state
		}
		
	}
	
}
