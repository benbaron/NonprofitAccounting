
package nonprofitbookkeeping.ui.panels;

import java.io.File;
import java.io.FileInputStream;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
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
 *  • toggle “cleared” on any row
 *  • export uncleared items to CSV
 */
public class LedgerReconcilePanelFX extends BorderPane
{
	
	/* ----------------------------------------------------------- */
	private final ReconciliationService ledgerSvc;
	
	private final ComboBox<String> accountBox = new ComboBox<>();
	private final Label ledgerBalLbl = new Label();
	private final Label clearedBalLbl = new Label();
	private final Label diffLbl = new Label();
	
	private final TableView<Row> table = new TableView<>();
	private final ObservableList<Row> rows = FXCollections.observableArrayList();
	
	/** bankTxns keyed by account → list of imported/manual transactions */
	private final Map<String, List<AccountingTransaction>> bankTxns = new HashMap<>();
	
	public LedgerReconcilePanelFX(ReconciliationService svc)
	{
		this.ledgerSvc = svc;
		setPadding(new Insets(10));
		buildTop();
		buildTable();
		setCenter(this.table);
		setBottom(buildButtons());
		refreshAccountList();
	}
	
	/* ----------------------- UI builders ----------------------- */
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
	
	@SuppressWarnings({ "unchecked", "deprecation" }) private void buildTable()
	{
		TableColumn<Row, Boolean> clrCol = new TableColumn<>("Cleared");
		clrCol.setCellValueFactory(r -> r.getValue().cleared);
		clrCol.setCellFactory(CheckBoxTableCell.forTableColumn(clrCol));
		clrCol.setEditable(true);
		
		this.table.getColumns().addAll(
			clrCol,
			col("Date", r -> r.date),
			col("Description", r -> r.desc),
			col("Amount", r -> r.amount),
			col("Source", r -> r.source));
		this.table.setItems(this.rows);
		this.table.setEditable(true);
		this.table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
		clrCol.setOnEditCommit(ev -> updateTotals());
	}
	
	private static <T> TableColumn<Row, T> col(String name, Function<Row, T> fn)
	{
		TableColumn<Row, T> tc = new TableColumn<>(name);
		tc.setCellValueFactory(cdf -> new ReadOnlyObjectWrapper<>(fn.apply(cdf.getValue())));
		return tc;
	}
	
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
	private void refreshAccountList()
	{
		this.accountBox.getItems().setAll(this.ledgerSvc.listReconcilableAccounts());
		if (!this.accountBox.getItems().isEmpty())
			this.accountBox.getSelectionModel().selectFirst();
		reloadRows();
	}
	
	private void reloadRows()
	{
		String acct = this.accountBox.getValue();
		if (acct == null)
			return;
		List<AccountingTransaction> ledger = this.ledgerSvc.getUnreconciled(acct);
		List<AccountingTransaction> bank = this.bankTxns.getOrDefault(acct, List.of());
		this.rows.setAll(Matcher.merge(ledger, bank));
		updateTotals();
	}
	
	private void updateTotals()
	{
		BigDecimal ledgerBal = this.rows.stream()
			.filter(r -> r.source.equals("Ledger"))
			.map(r -> r.amount)
			.reduce(BigDecimal.ZERO, BigDecimal::add);
		
		BigDecimal clearedBal = this.rows.stream()
			.filter(r -> r.cleared.get())
			.map(r -> r.amount)
			.reduce(BigDecimal.ZERO, BigDecimal::add);
		
		this.ledgerBalLbl.setText("Ledger Balance: " + ledgerBal);
		this.clearedBalLbl.setText("Cleared Balance: " + clearedBal);
		this.diffLbl.setText("Difference: " + ledgerBal.subtract(clearedBal));
	}
	
	/* -------------------------- models ------------------------- */
	private static class Row
	{
		final SimpleBooleanProperty cleared = new SimpleBooleanProperty(false);
		final String date, desc, source;
		final BigDecimal amount;
		
		Row(String d, String m, BigDecimal a, String s)
		{
			this.date = d;
			this.desc = m;
			this.amount = a;
			this.source = s;
		}
		
	}
	
	/* --------------------- OFX import via ofx4j ----------------- */
	private class ImportOfxHandler implements EventHandler<ActionEvent>
	{
		@Override public void handle(ActionEvent e)
		{
			FileChooser fc = new FileChooser();
			fc.setTitle("Open OFX File");
			fc.getExtensionFilters().add(
				new FileChooser.ExtensionFilter("OFX", "*.ofx"));
			File f = fc.showOpenDialog(getScene().getWindow());
			if (f == null)
				return;
			
			try
			{
				List<AccountingTransaction> bank = readOfx(f.toPath());
				LedgerReconcilePanelFX.this.bankTxns.put(LedgerReconcilePanelFX.this.accountBox.getValue(), bank);
				reloadRows();
				new Alert(Alert.AlertType.INFORMATION,
					"Imported " + bank.size() + " transactions.")
					.showAndWait();
			}
			catch (Exception ex)
			{
				new Alert(Alert.AlertType.ERROR,
					"OFX import failed:\n" + ex.getMessage()).showAndWait();
				ex.printStackTrace();
			}
			
		}
		
	}
	
	/**
	 * Parses an OFX file with ofx4j 1.38 and converts each bank entry into our
	 * AccountingTransaction model.
	 * @param <BankTransactionList>
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
			BankStatementResponseTransaction stmt =
				banking.getStatementResponses().get(0); // first (and usually only) statement
			
			List<Transaction> l = stmt.getMessage().getTransactionList().getTransactions();
			
			for (Transaction tx : l)
			{
				AccountingTransaction at = new AccountingTransaction();
				at.setDate(tx.getDatePosted().toString());
				at.setDescription(
					tx.getName() != null ? tx.getName() : tx.getMemo());
				at.setTotalAmount(BigDecimal.valueOf(tx.getAmount()));
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
	private class ManualEntryHandler implements EventHandler<ActionEvent>
	{
		@Override public void handle(ActionEvent e)
		{
			Dialog<AccountingTransaction> dlg = new Dialog<>();
			dlg.setTitle("Add Bank Transaction");
			dlg.getDialogPane().getButtonTypes().addAll(
				ButtonType.OK, ButtonType.CANCEL);
			
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
			
			dlg.setResultConverter(btn -> btn == ButtonType.OK ? build() : null);
			dlg.showAndWait().ifPresent(tx -> {
				LedgerReconcilePanelFX.this.bankTxns.computeIfAbsent(LedgerReconcilePanelFX.this.accountBox.getValue(), k -> new ArrayList<>()).add(tx);
				reloadRows();
			});
			
			AccountingTransaction t = new AccountingTransaction();
			
			t.setDate(dateP.getValue().toString());
			t.setDescription(descF.getText());
			t.setTotalAmount(new BigDecimal(amtF.getText()));
			
			return;
		}
			
		/**
		 * @return
		 */
		public AccountingTransaction build()
		{
			// TODO Auto-generated method stub
			return null;
		}
		
	}
	
	/* -------------------- export uncleared CSV ----------------- */
	class ExportUnclearedHandler implements EventHandler<ActionEvent>
	{
		private final LedgerReconcilePanelFX p;
		
		ExportUnclearedHandler(LedgerReconcilePanelFX p)
		{
			this.p = p;
		}
		
		@Override public void handle(ActionEvent e)
		{
			List<Row> uncleared = this.p.rows.stream()
				.filter(r -> !r.cleared.get())
				.collect(Collectors.toList());
			
			if (uncleared.isEmpty())
			{
				new Alert(Alert.AlertType.INFORMATION,
					"No uncleared items.").showAndWait();
				return;
			}
			
			FileChooser fc = new FileChooser();
			fc.setTitle("Save Uncleared CSV");
			fc.getExtensionFilters().add(
				new FileChooser.ExtensionFilter("CSV", "*.csv"));
			File f = fc.showSaveDialog(this.p.getScene().getWindow());
			if (f == null)
				return;
			
			try (java.io.PrintWriter pw = new java.io.PrintWriter(f))
			{
				pw.println("Date,Description,Amount,Source");
				uncleared.forEach(r -> pw.printf("%s,%s,%s,%s%n",
					r.date, r.desc.replace(',', ' '), r.amount, r.source));
				new Alert(Alert.AlertType.INFORMATION, "Saved.").showAndWait();
			}
			catch (Exception ex)
			{
				new Alert(Alert.AlertType.ERROR,
					"Write failed:\n" + ex.getMessage()).showAndWait();
			}
			
		}
		
	}
	
	/* ------------------ simple matching example ---------------- */
	class Matcher
	{
		/** Merges two lists (ledger + bank) without duplicate matching rules. */
		static List<Row> merge(	List<AccountingTransaction> ledger,
								List<AccountingTransaction> bank)
		{
			List<Row> out = new ArrayList<>();
			ledger.forEach(t -> out.add(new Row(t.getDate(), t.getDescription(),
				t.getTotalAmount(), "Ledger")));
			bank.forEach(t -> out.add(new Row(t.getDate(), t.getDescription(),
				t.getTotalAmount(), "Bank")));
			return out;
		}
		
	}
	
}
