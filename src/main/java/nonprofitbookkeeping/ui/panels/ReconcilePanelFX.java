
package nonprofitbookkeeping.ui.panels;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import nonprofitbookkeeping.model.AccountingTransaction;
import nonprofitbookkeeping.service.ReconciliationService;

/**
 * JavaFX version of {@code ReconcilePanel}.  Provides a minimal workflow:
 * <ol>
 *   <li>Select account and statement ending balance</li>
 *   <li>Shows list of unreconciled transactions</li>
 *   <li>Mark items cleared, calculates difference</li>
 *   <li>Save reconciliation via {@link ReconciliationService#reconcile}</li>
 * </ol>
 */
public class ReconcilePanelFX extends BorderPane
{
	
	private final ReconciliationService service;
	private final ComboBox<String> accountBox = new ComboBox<>();
	private final DatePicker statementDate = new DatePicker(LocalDate.now());
	private final TextField endingBalField = new TextField();
	
	private final TableView<TxnRow> table = new TableView<>();
	private final ObservableList<TxnRow> rows = FXCollections.observableArrayList();
	private final Label diffLabel = new Label("Difference: 0.00");
	
	public ReconcilePanelFX(ReconciliationService svc)
	{
		this.service = svc;
		this.accountBox.getItems().addAll(svc.listReconcilableAccounts());
		this.accountBox.getSelectionModel().selectFirst();
		setPadding(new Insets(10));
		buildTop();
		buildTable();
		setCenter(this.table);
		setBottom(buildBottomBar());
		loadTransactions();
	}
	
	/* ------------------------------------------------------------------ */
	private void buildTop()
	{
		GridPane g = new GridPane();
		g.setHgap(10);
		g.setVgap(8);
		g.setPadding(new Insets(8));
		g.addRow(0, new Label("Account:"), this.accountBox);
		g.addRow(1, new Label("Statement Date:"), this.statementDate);
		g.addRow(2, new Label("Ending Balance:"), this.endingBalField);
		this.accountBox.setOnAction(e -> loadTransactions());
		setTop(g);
	}
	
	@SuppressWarnings({ "unchecked", "deprecation" }) private void buildTable()
	{
		TableColumn<TxnRow, Boolean> clrCol = new TableColumn<>("Cleared");
		clrCol.setCellValueFactory(new PropertyValueFactory<>("cleared"));
		clrCol.setCellFactory(CheckBoxTableCell.forTableColumn(clrCol));
		clrCol.setEditable(true);
		TableColumn<TxnRow, String> dateCol = col("Date", "date");
		TableColumn<TxnRow, String> memoCol = col("Memo", "memo");
		TableColumn<TxnRow, BigDecimal> amtCol = new TableColumn<>("Amount");
		amtCol.setCellValueFactory(new PropertyValueFactory<>("amount"));
		this.table.getColumns().addAll(clrCol, dateCol, memoCol, amtCol);
		this.table.setEditable(true);
		this.table.setItems(this.rows);
		this.table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
		clrCol.setOnEditCommit(e -> updateDifference());
	}
	
	private static TableColumn<TxnRow, String> col(String t, String p)
	{
		TableColumn<TxnRow, String> c = new TableColumn<>(t);
		c.setCellValueFactory(new PropertyValueFactory<>(p));
		return c;
	}
	
	private HBox buildBottomBar()
	{
		Button save = new Button("Save Reconciliation");
		save.setOnAction(e -> save());
		HBox box = new HBox(20, this.diffLabel, save);
		box.setPadding(new Insets(8));
		return box;
	}
	
	private void loadTransactions()
	{
		this.rows.clear();
		String selectedAccount = this.accountBox.getValue();
		if (selectedAccount == null) {
			// Or show a placeholder in the table, or disable table, etc.
			System.err.println("ReconcilePanelFX: No account selected.");
			updateDifference(); // Ensure difference is updated even if list is empty
			return;
		}
		List<AccountingTransaction> list = this.service.getUnreconciled(selectedAccount);

		if (list != null) { // Check if the list itself is null
			list.forEach(t -> {
				if (t != null) { // Check for null transactions within the list
					this.rows.add(new TxnRow(t));
				} else {
					System.err.println("ReconcilePanelFX: Service returned a null AccountingTransaction in the list for account: " + selectedAccount);
				}
			});
		} else {
			System.err.println("ReconcilePanelFX: Service returned a null list for unreconciled transactions for account: " + selectedAccount);
		}
		updateDifference();
	}
	
	private void updateDifference()
	{
		BigDecimal ending;
		
		try
		{
			ending = new BigDecimal(this.endingBalField.getText().trim());
		}
		catch (@SuppressWarnings("unused") Exception ex)
		{
			ending = BigDecimal.ZERO;
		}
		
		BigDecimal clearedSum = this.rows.stream()
			.filter(r -> r.getCleared().isSelected()) // checkbox state
			.map(TxnRow::getAmount) // BigDecimal value
			.reduce(BigDecimal.ZERO, BigDecimal::add);
		
		BigDecimal diff = ending.subtract(clearedSum);
		this.diffLabel.setText("Difference: " + diff);
		
		
	}
	
	private void save()
	{
		BigDecimal ending;
		
		try
		{
			ending = new BigDecimal(this.endingBalField.getText().trim());
		}
		catch (@SuppressWarnings("unused") Exception ex)
		{
			alert("Invalid ending balance");
			return;
		}
		
		List<Long> clearedIds = this.rows.stream()
			.filter(r -> r.getCleared().isSelected()) // was r.cleared.getValue()
			.map(TxnRow::getId) // method-reference for clarity
			.toList();
		
		this.service.reconcile(
			this.accountBox.getValue(),
			this.statementDate.getValue().toString(),
			ending,
			clearedIds);
		
		alert("Reconciliation saved.");
		loadTransactions();
	}
	
	private static void alert(String msg)
	{
		new Alert(Alert.AlertType.INFORMATION, msg, ButtonType.OK).showAndWait();
	}
	
	/* Row wrapper */
	public static class TxnRow
	{
		final long id;
		final String date, memo;
		final BigDecimal amount;
		final CheckBox cleared = new CheckBox();
		
		TxnRow(AccountingTransaction t)
		{
			this.id = t.getBookingDateTimestamp();
			this.date = t.getDate();
			this.memo = t.getMemo();
			this.amount = t.getTotalAmount() != null ? t.getTotalAmount() : BigDecimal.ZERO;
		}
		
		public CheckBox getCleared()
		{
			return this.cleared;
		}
		
		public String getDate()
		{
			return this.date;
		}
		
		public String getMemo()
		{
			return this.memo;
		}
		
		public BigDecimal getAmount()
		{
			return this.amount;
		}
		
		public long getId() 
		{ 
			return this.id; 
		}

		
	}
	
}
