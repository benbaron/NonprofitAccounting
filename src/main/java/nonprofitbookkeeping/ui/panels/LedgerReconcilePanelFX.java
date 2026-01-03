
package nonprofitbookkeeping.ui.panels;

import java.io.File;
import java.io.FileInputStream;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import nonprofitbookkeeping.core.AccountingTransactionBuilder;
import nonprofitbookkeeping.model.AccountingTransaction;
import nonprofitbookkeeping.service.ReconciliationService;

import com.webcohesion.ofx4j.io.AggregateUnmarshaller;
import com.webcohesion.ofx4j.io.OFXParseException;

import com.webcohesion.ofx4j.domain.data.banking.BankingResponseMessageSet;
import com.webcohesion.ofx4j.domain.data.common.Transaction;
import com.webcohesion.ofx4j.domain.data.MessageSetType;
import com.webcohesion.ofx4j.domain.data.ResponseEnvelope;
import com.webcohesion.ofx4j.domain.data.banking.BankStatementResponseTransaction;

/**
 * LedgerReconcilePanelFX – lets the user:
 *  • pick an account
 *  • import bank transactions from an OFX file (via ofx4j 1.38)
 *  • add bank transactions manually
 *  • toggle "cleared" on any row
 *  • export uncleared items to CSV
 */
public class LedgerReconcilePanelFX extends BorderPane
{
	
	/** ComboBox for selecting the account to reconcile. */
	private final ComboBox<String> accountBox = new ComboBox<>();
	/** Label to display the current ledger balance for the selected account. */
	private final Label ledgerBalLbl = new Label();
	/** Label to display the total balance of cleared transactions. */
	private final Label clearedBalLbl = new Label();
	/** Label to display the difference between the ledger balance and cleared balance. */
	private final Label diffLbl = new Label();
	
	/** TableView to display merged ledger and bank transactions for reconciliation. */
	private final TableView<Row> table = new TableView<>();
	/** ObservableList that backs the {@link #table}, containing {@link Row} objects. */
	private final ObservableList<Row> rows = FXCollections.observableArrayList();
	
	/**
	 * Stores imported or manually added bank transactions.
	 * The map is keyed by account name (String), and the value is a list of {@link AccountingTransaction} objects
	 * representing transactions from the bank statement for that account.
	 */
	private final Map<String, List<AccountingTransaction>> bankTxns = new HashMap<>();
	
	/**
	 * Constructs a new {@code LedgerReconcilePanelFX}.
	 * Initializes the panel with the necessary {@link ReconciliationService} and builds the UI components,
	 * including account selection, balance displays, the main reconciliation table, and action buttons.
	 *
	 * @param svc The {@link ReconciliationService} to be used for accessing ledger data and account information. Must not be null.
	 */
	public LedgerReconcilePanelFX(ReconciliationService svc)
	{
		setPadding(new Insets(10));
		buildTop();
		buildTable();
		setCenter(this.table);
		setBottom(buildButtons());
		refreshAccountList();
	}
	
	/**
	 * Selects the given account in the account combo box if present and
	 * refreshes the table. This is useful when another panel wants to
	 * preselect an account before showing the reconciliation UI.
	 *
	 * @param account account name to select
	 */
	public void selectAccount(String account)
	{
		if (account == null)
			return;
		
		if (!this.accountBox.getItems().contains(account))
			return;
		
		this.accountBox.getSelectionModel().select(account);
		reloadRows();
	}
	
	/* ----------------------- UI builders ----------------------- */
	/**
	 * Builds the top section of the panel.
	 * This section includes a {@link ComboBox} ({@link #accountBox}) for account selection
	 * and labels ({@link #ledgerBalLbl}, {@link #clearedBalLbl}, {@link #diffLbl}) to display balance information.
	 * An action is set on the accountBox to reload rows when the selection changes.
	 */
	private void buildTop()
	{
		GridPane g = new GridPane();
		g.setHgap(10);
		g.setVgap(8);
		g.setPadding(new Insets(8));
		g.addRow(0, new Label("Account:"), this.accountBox);
		g.addRow(1, this.ledgerBalLbl, this.clearedBalLbl, this.diffLbl);
		this.accountBox.setOnAction(e -> reloadRows());
		setTop(g);
	}
	
	/**
	 * Builds and configures the main {@link TableView} ({@link #table}) for displaying reconciliation items.
	 * It defines columns for "Cleared" (CheckBox), Date, Description, Amount, and Source,
	 * using the {@link #col(String, Function)} helper for most columns.
	 * The "Cleared" column is editable and triggers {@link #updateTotals()} on commit.
	 * The table is bound to the {@link #rows} observable list and set to be editable.
	 * The {@code @SuppressWarnings({ "unchecked", "deprecation" })} is likely related to generic types
	 * with {@code TableColumn} and {@code PropertyValueFactory} or similar mechanisms if used internally
	 * by helper methods, especially with older JavaFX patterns.
	 */
	@SuppressWarnings(
	{ "unchecked", "deprecation" }) private void buildTable()
	{
		TableColumn<Row, Boolean> clrCol = new TableColumn<>("Cleared");
		clrCol.setCellValueFactory(r -> r.getValue().cleared);
		clrCol.setCellFactory(CheckBoxTableCell.forTableColumn(clrCol));
		clrCol.setEditable(true);
		
		this.table.getColumns().addAll(clrCol, col("Date", r -> r.date),
			col("Description", r -> r.desc), col("Amount", r -> r.amount),
			col("Source", r -> r.source));
		this.table.setItems(this.rows);
		this.table.setEditable(true);
		this.table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
		clrCol.setOnEditCommit(ev -> updateTotals());
	}
	
	/**
	 * Utility method to create a {@link TableColumn} for the reconciliation table.
	 *
	 * @param <T> The type of the data to be displayed in this column's cells.
	 * @param name The title of the column (to be displayed in the header).
	 * @param fn A {@link Function} that takes a {@link Row} object (the value of the TableView row)
	 *           and returns the value of type {@code T} to be displayed in the cell for that row.
	 * @return A configured {@link TableColumn} for displaying data of type {@code T} from a {@link Row}.
	 */
	private static <T> TableColumn<Row, T> col(String name, Function<Row, T> fn)
	{
		TableColumn<Row, T> tc = new TableColumn<>(name);
		tc.setCellValueFactory(cdf -> new ReadOnlyObjectWrapper<>(fn.apply(cdf.getValue())));
		return tc;
	}
	
	/**
	 * Builds and returns an {@link HBox} containing action buttons for the reconciliation panel:
	 * "Import OFX", "Add Bank Txn", and "Export Uncleared".
	 * Each button is configured with an appropriate event handler (e.g., {@link ImportOfxHandler}).
	 *
	 * @return An {@link HBox} populated with control buttons.
	 */
	private HBox buildButtons()
	{
		Button importBtn = new Button("Import OFX");
		importBtn.setOnAction(new ImportOfxHandler());
		
		Button addBtn = new Button("Add Bank Txn");
		addBtn.setOnAction(new ManualEntryHandler());
		
		Button exportBtn = new Button("Export Uncleared");
		exportBtn.setOnAction(new ExportUnclearedHandler(this));
		
		HBox box = new HBox(10, importBtn, addBtn, exportBtn);
		box.setPadding(new Insets(8));
		return box;
	}
	
	/* --------------------- data operations --------------------- */
	/**
	 * Refreshes the list of accounts available in the {@link #accountBox} ComboBox.
	 * It fetches reconcilable accounts from the {@link #ledgerSvc}, populates the ComboBox,
	 * selects the first account if the list is not empty, and then calls {@link #reloadRows()}
	 * to load data for the selected account.
	 */
	private void refreshAccountList()
	{
		this.accountBox.getItems().setAll(ReconciliationService.listReconcilableAccounts());
		if (!this.accountBox.getItems().isEmpty())
			this.accountBox.getSelectionModel().selectFirst();
		reloadRows();
	}
	
	/**
	 * Reloads the rows in the {@link #table} for the currently selected account in {@link #accountBox}.
	 * It fetches unreconciled transactions from the {@link #ledgerSvc} and merges them with
	 * any imported or manually added bank transactions (from {@link #bankTxns}) for that account
	 * using {@link Matcher#merge(List, List)}. The table is then updated, and totals are recalculated.
	 * If no account is selected, the method returns without action.
	 */
	private void reloadRows()
	{
		String acct = this.accountBox.getValue();
		if (acct == null)
			return;
		List<AccountingTransaction> ledger = ReconciliationService.getUnreconciled(acct);
		List<AccountingTransaction> bank = this.bankTxns.getOrDefault(acct, List.of());
		this.rows.setAll(Matcher.merge(ledger, bank));
		updateTotals();
	}
	
	/**
	 * Calculates and updates the ledger balance, cleared balance, and the difference
	 * displayed in the labels ({@link #ledgerBalLbl}, {@link #clearedBalLbl}, {@link #diffLbl}).
	 * Ledger balance is the sum of amounts from "Ledger" source rows.
	 * Cleared balance is the sum of amounts from rows marked as "cleared".
	 * The difference is ledger balance minus cleared balance.
	 */
	private void updateTotals()
	{
		BigDecimal ledgerBal = this.rows.stream().filter(r -> r.source.equals("Ledger"))
			.map(r -> r.amount).reduce(BigDecimal.ZERO, BigDecimal::add);
		
		BigDecimal clearedBal = this.rows.stream().filter(r -> r.cleared.get()).map(r -> r.amount)
			.reduce(BigDecimal.ZERO, BigDecimal::add);
		
		this.ledgerBalLbl.setText("Ledger Balance: " + ledgerBal);
		this.clearedBalLbl.setText("Cleared Balance: " + clearedBal);
		this.diffLbl.setText("Difference: " + ledgerBal.subtract(clearedBal));
	}
	
	/* -------------------------- models ------------------------- */
	/**
	 * Represents a single row in the reconciliation table ({@link #table}).
	 * Each row can originate either from the ledger or a bank statement and includes
	 * details like date, description, amount, source, and a "cleared" status.
	 * The "cleared" status is a {@link SimpleBooleanProperty} to support JavaFX table cell binding.
	 */
	private static class Row
	{
		/** Boolean property indicating if the transaction is marked as cleared. Bound to the CheckBox in the table. */
		final SimpleBooleanProperty cleared = new SimpleBooleanProperty(false);
		/** The date of the transaction, as a String. */
		final String date;
		/** The description or memo of the transaction. */
		final String desc;
		/** The source of the transaction (e.g., "Ledger" or "Bank"). */
		final String source;
		/** The monetary amount of the transaction. */
		final BigDecimal amount;
		
		/**
		 * Constructs a new {@code Row} for the reconciliation table.
		 *
		 * @param d The date of the transaction (String).
		 * @param m The description or memo of the transaction.
		 * @param a The amount of the transaction.
		 * @param s The source of the transaction (e.g., "Ledger", "Bank").
		 */
		Row(String d, String m, BigDecimal a, String s)
		{
			this.date = d;
			this.desc = m;
			this.amount = a;
			this.source = s;
		}
		
	}
	
	/* --------------------- OFX import via ofx4j ----------------- */
	/**
	 * Handles the "Import OFX" button action.
	 * Prompts the user to select an OFX file using a {@link FileChooser}.
	 * If a file is selected, it attempts to read and parse the OFX file using {@link #readOfx(Path)},
	 * stores the imported bank transactions in {@link #bankTxns} for the current account,
	 * reloads the table rows, and shows a confirmation or error alert.
	 */
	private class ImportOfxHandler implements EventHandler<ActionEvent>
	{
		/**
		 * {@inheritDoc}
		 * Opens a {@link FileChooser} for OFX files, processes the selected file,
		 * updates the internal bank transaction list, and refreshes the UI.
		 * Displays alerts for success or failure.
		 */
		@Override public void handle(ActionEvent e)
		{
			FileChooser fc = new FileChooser();
			fc.setTitle("Open OFX File");
			fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("OFX", "*.ofx"));
			File f = fc.showOpenDialog(getScene().getWindow());
			if (f == null)
				return;
			
			try
			{
				List<AccountingTransaction> bank = readOfx(f.toPath());
				LedgerReconcilePanelFX.this.bankTxns
					.put(LedgerReconcilePanelFX.this.accountBox.getValue(), bank);
				reloadRows();
				new Alert(Alert.AlertType.INFORMATION, "Imported " + bank.size() + " transactions.")
					.showAndWait();
			}
			catch (Exception ex)
			{
				new Alert(Alert.AlertType.ERROR, "OFX import failed:\n" + ex.getMessage())
					.showAndWait();
				ex.printStackTrace();
			}
			
		}
		
	}
	
	/**
	 * Parses an OFX file using the ofx4j library (version 1.38 or as per project dependency)
	 * and converts bank transactions found within the file into a list of
	 * {@link AccountingTransaction} model objects.
	 * <p>
	 * This method handles reading the OFX file, unmarshalling its content into
	 * OFX4J domain objects, and then mapping relevant transaction data (date, description/memo, amount)
	 * to new {@code AccountingTransaction} instances.
	 * </p>
	 * It specifically processes banking message sets and extracts transactions from the first statement response.
	 *
	 * @param path The {@link Path} to the OFX file to be read. Must not be null.
	 * @return A {@link List} of {@link AccountingTransaction} objects derived from the OFX file.
	 *         Returns an empty list if no transactions are found or if the relevant OFX structures are missing.
	 * @throws Exception if an error occurs during file reading (e.g., {@link java.io.IOException})
	 *                   or OFX parsing (e.g., {@link OFXParseException}).
	 */
	private static List<AccountingTransaction> readOfx(Path path) throws Exception
	{
		List<AccountingTransaction> out = new ArrayList<>();
		
		try (FileInputStream in = new FileInputStream(path.toFile()))
		{
			// QFX ≈ OFX v1 with Intuit headers
			
			// The unmarshaller walks the whole document and builds the Java model for you
			var unmarshaller = new AggregateUnmarshaller<>(ResponseEnvelope.class);
			ResponseEnvelope envelope = unmarshaller.unmarshal(in);
			
			// Most QFX files contain exactly one banking message-set
			
			BankingResponseMessageSet banking =
				(BankingResponseMessageSet) envelope.getMessageSet(MessageSetType.banking);
			BankStatementResponseTransaction stmt = banking.getStatementResponses().get(0); // first
																							// (and
																							// usually
																							// only)
																							// statement
			
			List<Transaction> l = stmt.getMessage().getTransactionList().getTransactions();
			
			for (Transaction tx : l)
			{
				AccountingTransaction at = new AccountingTransaction();
				at.setDate(tx.getDatePosted().toString());
				at.setDescription(tx.getName() != null ? tx.getName() : tx.getMemo());
				at.setEntries(new LinkedHashSet<>());
				out.add(at);
			}
			
			return out;
			
		}
		catch (OFXParseException ex)
		{
			throw new Exception("OFX parse error", ex);
		}
		
	}
	
	
	/* ------------------- manual entry dialog ------------------- */
	/**
	 * Handles the "Add Bank Txn" button action.
	 * Displays a dialog prompting the user to manually enter details for a bank transaction
	 * (Date, Description, Amount). If the user confirms and provides valid input,
	 * a new {@link AccountingTransaction} is created (though the creation logic in {@link #build()} is currently a stub),
	 * added to the {@link #bankTxns} list for the current account, and the table is reloaded.
	 */
	private class ManualEntryHandler implements EventHandler<ActionEvent>
	{
		/**
		 * {@inheritDoc}
		 * Shows a dialog for manual bank transaction entry.
		 * On OK, it attempts to build an {@link AccountingTransaction} using the {@link #build()} method
		 * (which is currently a stub and returns null). If a transaction were successfully built,
		 * it would be added to the current account's bank transactions, and the table would be reloaded.
		 */
		@Override public void handle(ActionEvent e)
		{
			Dialog<AccountingTransaction> dlg = new Dialog<>();
			dlg.setTitle("Add Bank Transaction");
			dlg.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
			
			DatePicker dateP = new DatePicker(LocalDate.now());
			TextField descF = new TextField();
			TextField amtF = new TextField();
			
			GridPane gp = new GridPane();
			gp.setHgap(10);
			gp.setVgap(8);
			gp.addRow(0, new Label("Date"), dateP);
			gp.addRow(1, new Label("Description"), descF);
			gp.addRow(2, new Label("Amount"), amtF);
			dlg.getDialogPane().setContent(gp);
			
			dlg.setResultConverter(btn -> btn == ButtonType.OK ? build(dateP, descF, amtF) : null);
			dlg.showAndWait().ifPresent(tx -> {
				LedgerReconcilePanelFX.this.bankTxns
					.computeIfAbsent(LedgerReconcilePanelFX.this.accountBox.getValue(),
						k -> new ArrayList<>())
					.add(tx);
				reloadRows();
			});
		}
		
		/**
		 * Builds an {@link AccountingTransaction} from the dialog's input fields.
		        * <p>
		        * Parses the provided date, description, and amount fields to build an
		        * {@link AccountingTransaction} using the selected account number. If any
		        * field is invalid or missing, this method returns {@code null}.
		        * </p>
		        *
		        * @return A new {@link AccountingTransaction} based on dialog inputs or
		        *         {@code null} if validation fails.
		        */
		private AccountingTransaction build(DatePicker dateP, TextField descF, TextField amtF)
		{
			
			if (dateP.getValue() == null)
			{
				return null;
			}
			
			BigDecimal amt;
			
			try
			{
				amt = new BigDecimal(amtF.getText());
			}
			catch (NumberFormatException ex)
			{
				return null; // Invalid amount
			}
			
			String accountNum = LedgerReconcilePanelFX.this.accountBox.getValue();
			
			if (accountNum == null || accountNum.isBlank())
			{
				return null;
			}
			
			AccountingTransactionBuilder builder = AccountingTransactionBuilder.create();
			builder.debit(amt, accountNum);
			
			// Use a generic clearing account for the credit side
			builder.credit(amt, "CLEARING");
			AccountingTransaction tx = builder.build();
			tx.setDate(dateP.getValue().toString());
			tx.setDescription(descF.getText());
			return tx;
		}
		
	}
	
	/* -------------------- export uncleared CSV ----------------- */
	/**
	 * Handles the "Export Uncleared" button action.
	 * Filters the current {@link #rows} in the table to find all transactions not marked as "cleared".
	 * If uncleared items exist, it prompts the user for a file location using a {@link FileChooser}
	 * (defaulting to CSV format) and writes the uncleared items to the selected file as CSV.
	 * Displays alerts for success, failure, or if no uncleared items are found.
	 */
	class ExportUnclearedHandler implements EventHandler<ActionEvent>
	{
		/** Reference to the parent {@link LedgerReconcilePanelFX} to access its rows and scene. */
		private final LedgerReconcilePanelFX p;
		
		/**
		 * Constructs a new {@code ExportUnclearedHandler}.
		 * @param p The instance of {@link LedgerReconcilePanelFX} from which to export data.
		 */
		ExportUnclearedHandler(LedgerReconcilePanelFX p)
		{
			this.p = p;
		}
		
		/**
		 * {@inheritDoc}
		 * Filters uncleared rows, prompts for a save file location (CSV),
		 * and writes the data. Shows alerts for outcomes.
		 */
		@Override public void handle(ActionEvent e)
		{
			List<Row> uncleared =
				this.p.rows.stream().filter(r -> !r.cleared.get()).collect(Collectors.toList());
			
			if (uncleared.isEmpty())
			{
				new Alert(Alert.AlertType.INFORMATION, "No uncleared items.").showAndWait();
				return;
			}
			
			FileChooser fc = new FileChooser();
			fc.setTitle("Save Uncleared CSV");
			fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV", "*.csv"));
			File f = fc.showSaveDialog(this.p.getScene().getWindow());
			if (f == null)
				return;
			
			try (java.io.PrintWriter pw = new java.io.PrintWriter(f))
			{
				pw.println("Date,Description,Amount,Source");
				uncleared.forEach(r -> pw.printf("%s,%s,%s,%s%n", r.date, r.desc.replace(',', ' '),
					r.amount, r.source));
				new Alert(Alert.AlertType.INFORMATION, "Saved.").showAndWait();
			}
			catch (Exception ex)
			{
				new Alert(Alert.AlertType.ERROR, "Write failed:\n" + ex.getMessage()).showAndWait();
			}
			
		}
		
	}
	
	/**
	 * Provides a simple merging strategy for ledger and bank transactions.
	 * This example class contains a static method to combine two lists of transactions
	 * into a single list of {@link Row} objects for display, marking their source.
	 */
	class Matcher
	{
		/**
		 * Merges two lists of {@link AccountingTransaction}s (one from the ledger, one from the bank)
		 * into a single list of {@link Row} objects for display in the reconciliation table.
		 * Each row is marked with its source ("Ledger" or "Bank").
		 * This is a simple merge; it does not perform any matching or duplicate detection.
		 *
		 * @param ledger A list of {@link AccountingTransaction}s from the ledger.
		 * @param bank A list of {@link AccountingTransaction}s from the bank statement.
		 * @return A new {@link List} of {@link Row} objects containing all transactions from both sources.
		 */
		static List<Row> merge(List<AccountingTransaction> ledger, List<AccountingTransaction> bank)
		{
			List<Row> out = new ArrayList<>();
			ledger.forEach(t -> out
				.add(new Row(t.getDate(), t.getDescription(), t.getTotalAmount(), "Ledger")));
			bank.forEach(
				t -> out.add(new Row(t.getDate(), t.getDescription(), t.getTotalAmount(), "Bank")));
			return out;
		}
		
	}
	
}
