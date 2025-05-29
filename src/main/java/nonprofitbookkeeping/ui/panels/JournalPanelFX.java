
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
import nonprofitbookkeeping.service.JournalService;

/** Shows transactions and lets user add / edit / delete them. */
public class JournalPanelFX extends BorderPane
{
	
	private final JournalService service = new JournalService();
	private final ObservableList<AccountingTransaction> rows =
		FXCollections.observableArrayList();
	private final TableView<AccountingTransaction> table = new TableView<>(this.rows);
	
	public JournalPanelFX()
	{
		setPadding(new Insets(10));
		buildTable();
		setCenter(this.table);
		setBottom(toolBar());
		refresh();
	}
	
	/* -------- Table -------- */
	private void buildTable()
	{
		this.table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
		this.table.getColumns().addAll(
			col("ID", "id"),
			col("Date", "date"),
			col("Account", "accountName"),
			num("Debit", "debit"),
			num("Credit", "credit"),
			col("Memo", "memo"));
	}
	
	private static TableColumn<AccountingTransaction, String> col(String t, String p)
	{
		TableColumn<AccountingTransaction, String> c = new TableColumn<>(t);
		c.setCellValueFactory(new PropertyValueFactory<>(p));
		return c;
	}
	
	private static TableColumn<AccountingTransaction, BigDecimal> num(String t, String p)
	{
		TableColumn<AccountingTransaction, BigDecimal> c = new TableColumn<>(t);
		c.setCellValueFactory(new PropertyValueFactory<>(p));
		return c;
	}
	
	/* -------- Toolbar -------- */
	private Node toolBar()
	{
		Button add = new Button("New");
		Button edit = new Button("Edit");
		Button del = new Button("Delete");
		
		add.setOnAction(e -> openEditor(null));
		edit.setOnAction(e -> {
			AccountingTransaction sel = this.table.getSelectionModel().getSelectedItem();
			if (sel != null)
			{
				openEditor(sel);
			}
		});
		del.setOnAction(e -> {
			AccountingTransaction sel = this.table.getSelectionModel().getSelectedItem();
			
			if (sel != null)
			{
				this.service.delete(sel.getId());
				refresh();
			}
			
		});
		return new ToolBar(add, edit, del);
	}
	
	/* -------- CRUD -------- */
	private void openEditor(AccountingTransaction existing) {

	    NewTransactionPanelFX pane =
	        (existing == null)
	        ? new NewTransactionPanelFX(          // create
	              (AccountingTransaction tx) -> {
	                  this.service.add(tx);
	                  refresh();
	              })
	        : new NewTransactionPanelFX(existing,
	            (Consumer<AccountingTransaction>) tx ->  {
	                  this.service.update(tx);
	                  refresh();
	              });

	    Dialog<Void> d = new Dialog<>();
	    d.setTitle(existing == null ? "New Transaction" : "Edit Transaction");
	    d.getDialogPane().setContent(pane);
	    d.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
	    d.showAndWait();
	}

	
	private void refresh()
	{
		this.rows.setAll(this.service.list());
	}
	
}
