
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
import nonprofitbookkeeping.util.FormatUtils;
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
	
	/** Service layer for reconciliation operations. */
	private final ReconciliationService service;
	/** ComboBox for selecting the account to be reconciled. */
	private final ComboBox<String> accountBox = new ComboBox<>();
	/** DatePicker for selecting the statement ending date. */
	private final DatePicker statementDate = new DatePicker(LocalDate.now());
	/** TextField for entering the statement ending balance. */
	private final TextField endingBalField = new TextField();
	
	/** TableView to display unreconciled transactions. */
	private final TableView<TxnRow> table = new TableView<>();
	/** ObservableList that backs the {@link #table}, containing {@link TxnRow} objects. */
	private final ObservableList<TxnRow> rows = FXCollections.observableArrayList();
	/** Label to display the calculated difference between statement ending balance and sum of cleared transactions. */
       private final Label diffLabel = new Label("Difference: " + FormatUtils.formatCurrency(BigDecimal.ZERO));
	
	/**
	 * Constructs a new {@code ReconcilePanelFX}.
	 * Initializes the panel with the necessary {@link ReconciliationService} and builds the UI,
	 * including account selection, statement details input, a table for transactions,
	 * and a bar for showing the difference and saving the reconciliation.
	 *
	 * @param svc The {@link ReconciliationService} to be used for reconciliation operations. Must not be null.
	 */
	public ReconcilePanelFX(ReconciliationService svc)
	{
		this.service = svc;
		this.accountBox.getItems().addAll(ReconciliationService.listReconcilableAccounts());
		this.accountBox.getSelectionModel().selectFirst();
		setPadding(new Insets(10));
		buildTop();
		buildTable();
		setCenter(this.table);
		setBottom(buildBottomBar());
		loadTransactions();
	}
	
	/* ------------------------------------------------------------------ */
	/**
	 * Builds the top section of the panel.
	 * This section includes a {@link ComboBox} ({@link #accountBox}) for selecting the account,
	 * a {@link DatePicker} ({@link #statementDate}) for the statement date,
	 * and a {@link TextField} ({@link #endingBalField}) for the statement ending balance.
	 * An action is set on the accountBox to reload transactions when the selection changes.
	 */
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
	
	/**
	 * Builds and configures the {@link TableView} ({@link #table}) for displaying unreconciled transactions.
	 * It defines columns for "Cleared" (CheckBox), Date, Memo, and Amount.
	 * The "Cleared" column is editable and triggers {@link #updateDifference()} on commit.
	 * The table is bound to the {@link #rows} observable list and set to be editable.
	 * The {@code @SuppressWarnings({ "unchecked", "deprecation" })} is used because {@link PropertyValueFactory}
	 * (used directly or via the {@code col} helper) uses reflection and can lead to type safety warnings.
	 * "deprecation" might relate to older patterns of using PropertyValueFactory.
	 */
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
	
	/**
	 * Utility method to create a {@link TableColumn} for displaying String properties in the transactions table.
	 *
	 * @param t The title of the column for the table header.
	 * @param p The name of the property in {@link TxnRow} to bind this column to (e.g., "date" for getDate()).
	 * @return A configured {@link TableColumn} for displaying String data from a {@link TxnRow}.
	 */
	private static TableColumn<TxnRow, String> col(String t, String p)
	{
		TableColumn<TxnRow, String> c = new TableColumn<>(t);
		c.setCellValueFactory(new PropertyValueFactory<>(p));
		return c;
	}
	
	/**
	 * Builds and returns an {@link HBox} for the bottom of the panel.
	 * This bar contains the {@link #diffLabel} to show the reconciliation difference
	 * and a "Save Reconciliation" button that triggers the {@link #save()} action.
	 *
	 * @return A configured {@link HBox} for the bottom bar.
	 */
	private HBox buildBottomBar()
	{
		Button save = new Button("Save Reconciliation");
		save.setOnAction(e -> save());
		HBox box = new HBox(20, this.diffLabel, save);
		box.setPadding(new Insets(8));
		return box;
	}
	
	/**
	 * Loads unreconciled transactions for the currently selected account (from {@link #accountBox})
	 * into the {@link #table}.
	 * It clears existing rows, fetches unreconciled transactions from the {@link #service},
	 * converts them to {@link TxnRow} objects, and populates the table.
	 * It also calls {@link #updateDifference()} to refresh the difference calculation.
	 * If no account is selected or the service returns nulls, appropriate error messages are logged.
	 */
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
		List<AccountingTransaction> list = ReconciliationService.getUnreconciled(selectedAccount);

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
	
	/**
	 * Calculates and updates the {@link #diffLabel} to show the difference between
	 * the statement ending balance (from {@link #endingBalField}) and the sum of amounts
	 * of all transactions marked as "cleared" in the {@link #table}.
	 * If the ending balance field contains non-numeric text, it defaults to BigDecimal.ZERO for calculation.
	 */
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
               this.diffLabel.setText("Difference: " + FormatUtils.formatCurrency(diff));
		
		
	}
	
	/**
	 * Saves the current reconciliation state.
	 * It retrieves the statement ending balance, collects the IDs of all transactions marked as "cleared",
	 * and then calls the {@link ReconciliationService#reconcile(String, String, BigDecimal, List)} method.
	 * An alert is shown on success, and transactions are reloaded.
	 * If the ending balance is invalid, an error alert is shown.
	 */
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
	
	/**
	 * Displays a simple informational alert dialog with an OK button.
	 *
	 * @param msg The message to be displayed in the alert dialog.
	 */
	private static void alert(String msg)
	{
		new Alert(Alert.AlertType.INFORMATION, msg, ButtonType.OK).showAndWait();
	}
	
	/* Row wrapper */
	/**
	 * Represents a single transaction row in the reconciliation table.
	 * This class wraps an {@link AccountingTransaction} and includes a {@link CheckBox}
	 * for marking the transaction as "cleared". It provides properties suitable for JavaFX table binding.
	 */
	public static class TxnRow
	{
		/** The unique ID (booking timestamp) of the underlying transaction. */
		final long id;
		/** The date of the transaction, as a String. */
		final String date;
		/** The memo or description of the transaction. */
		final String memo;
		/** The monetary amount of the transaction. */
		final BigDecimal amount;
		/** CheckBox to indicate if this transaction is cleared. This is directly used by the TableCell. */
		final CheckBox cleared = new CheckBox();
		
		/**
		 * Constructs a {@code TxnRow} from an {@link AccountingTransaction} object.
		 * Initializes the row's properties based on the transaction data.
		 * The 'cleared' status defaults to false (unchecked).
		 *
		 * @param t The {@link AccountingTransaction} to represent as a table row. Must not be null.
		 */
		TxnRow(AccountingTransaction t)
		{
			this.id = t.getBookingDateTimestamp();
			this.date = t.getDate();
			this.memo = t.getMemo();
			this.amount = t.getTotalAmount() != null ? t.getTotalAmount() : BigDecimal.ZERO;
		}
		
		/**
		 * Gets the {@link CheckBox} instance used to represent the "cleared" status.
		 * This is typically used by the {@link CheckBoxTableCell} for display and interaction.
		 * To get the boolean cleared status, use {@code getCleared().isSelected()}.
		 * @return The {@link CheckBox} for the cleared status.
		 */
		public CheckBox getCleared()
		{
			return this.cleared;
		}
		
		/**
		 * Gets the date of the transaction.
		 * @return The transaction date as a String.
		 */
		public String getDate()
		{
			return this.date;
		}
		
		/**
		 * Gets the memo or description of the transaction.
		 * @return The transaction memo.
		 */
		public String getMemo()
		{
			return this.memo;
		}
		
		/**
		 * Gets the amount of the transaction.
		 * @return The transaction amount as a {@link BigDecimal}.
		 */
		public BigDecimal getAmount()
		{
			return this.amount;
		}
		
		/**
		 * Gets the unique ID (booking timestamp) of the transaction.
		 * @return The transaction ID.
		 */
		public long getId() 
		{ 
			return this.id; 
		}

		
	}
	
}
