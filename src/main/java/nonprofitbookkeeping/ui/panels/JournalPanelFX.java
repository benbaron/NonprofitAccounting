
package nonprofitbookkeeping.ui.panels;

import java.math.BigDecimal;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import nonprofitbookkeeping.model.JournalEntry;
import nonprofitbookkeeping.service.JournalService;

/**
 * JavaFX port of {@code JournalPanel}. Shows journal entries and lets the user
 * create a new transaction (opens {@link NewTransactionPanelFX}), edit, or
 * delete selected entries.  Persisting goes through {@link JournalService}.
 */
public class JournalPanelFX extends BorderPane
{
	
	private final JournalService service;
	private final ObservableList<EntryRow> rows = FXCollections.observableArrayList();
	private final TableView<EntryRow> table = new TableView<>();
	
	/**
	 * 
	 * Constructor JournalPanelFX
	 */
	public JournalPanelFX()
	{
		this.service = new JournalService();
		setPadding(new Insets(10));
		buildTable();
		setCenter(this.table);
		setBottom(buttonBar());
		refresh();
	}
	
	/* ------------------------------------------------------------------ */
	@SuppressWarnings({ "unchecked", "deprecation" }) 
	private void buildTable()
	{
		TableColumn<EntryRow, String> idCol = col("ID", "id");
		TableColumn<EntryRow, String> dateCol = col("Date", "date");
		TableColumn<EntryRow, String> acctCol = col("Account", "account");
		TableColumn<EntryRow, BigDecimal> debitCol = new TableColumn<>("Debit");
		debitCol.setCellValueFactory(new PropertyValueFactory<>("debit"));
		TableColumn<EntryRow, BigDecimal> creditCol = new TableColumn<>("Credit");
		creditCol.setCellValueFactory(new PropertyValueFactory<>("credit"));
		TableColumn<EntryRow, String> memoCol = col("Memo", "memo");
		this.table.getColumns().addAll(idCol, dateCol, acctCol, debitCol, creditCol, memoCol);
		this.table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
		this.table.setItems(this.rows);
	}
	
	private static TableColumn<EntryRow, String> col(String title, String prop)
	{
		TableColumn<EntryRow, String> c = new TableColumn<>(title);
		c.setCellValueFactory(new PropertyValueFactory<>(prop));
		return c;
	}
	
	private ToolBar buttonBar()
	{
		Button add = new Button("New Transaction");
		Button edit = new Button("Edit");
		Button del = new Button("Delete");
		add.setOnAction(e -> newTransaction());
		edit.setOnAction(e -> {
			EntryRow sel = this.table.getSelectionModel().getSelectedItem();
			if (sel != null)
				editTransaction(sel);
		});
		del.setOnAction(e -> {
			EntryRow sel = this.table.getSelectionModel().getSelectedItem();
			
			if (sel != null)
			{
				this.service.deleteEntry(sel.id);
				refresh();
			}
			
		});
		return new ToolBar(add, edit, del);
	}
	
	/**
	 * 
	 */
	private void newTransaction()
	{
		NewTransactionPanelFX pane = new NewTransactionPanelFX(txn -> {
			this.service.addEntry(txn);
			refresh();
		});
		Dialog<Void> dlg = new Dialog<>();
		dlg.setTitle("New Transaction");
		dlg.getDialogPane().setContent(pane);
		dlg.getDialogPane().getButtonTypes().addAll(ButtonType.CLOSE);
		dlg.showAndWait();
	}
	
	/**
	 * 
	 * @param row
	 */
	private void editTransaction(EntryRow row)
	{
		JournalEntry entry = this.service.getEntry(row.id);
		NewTransactionPanelFX pane = new NewTransactionPanelFX(entry, txn -> {
			this.service.updateEntry(txn);
			refresh();
		});
		Dialog<Void> dlg = new Dialog<>();
		dlg.setTitle("Edit Transaction");
		dlg.getDialogPane().setContent(pane);
		dlg.getDialogPane().getButtonTypes().addAll(ButtonType.CLOSE);
		dlg.showAndWait();
	}
	
	private void refresh()
	{
		this.rows.clear();
		this.service.listEntries().forEach(e -> this.rows.add(new EntryRow(e)));
	}
	
	/* ------------------------------------------------------------------ */
	public static class EntryRow
	{
		String id, date, account, memo;
		BigDecimal debit, credit;
		
		/**
		 * 
		 * Constructor EntryRow
		 * @param e
		 */
		EntryRow(JournalEntry e)
		{
			this.id = e.getId();
			this.date = e.getDate();
			this.account = e.getAccount();
			this.debit = e.getDebit();
			this.credit = e.getCredit();
			this.memo = e.getMemo();
		}
		
		/**  
		 * Constructor EntryRow
		 * @param e
		 */
		public EntryRow(EntryRow e)
		{
			// TODO Auto-generated constructor stub
		}

		public String getId()
		{
			return this.id;
		}
		
		public String getDate()
		{
			return this.date;
		}
		
		public String getAccount()
		{
			return this.account;
		}
		
		public BigDecimal getDebit()
		{
			return this.debit;
		}
		
		public BigDecimal getCredit()
		{
			return this.credit;
		}
		
		public String getMemo()
		{
			return this.memo;
		}
		
	}
	
}
