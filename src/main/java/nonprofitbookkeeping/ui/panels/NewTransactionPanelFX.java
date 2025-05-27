
package nonprofitbookkeeping.ui.panels;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import javafx.beans.property.*;
import javafx.collections.*;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.util.converter.BigDecimalStringConverter;
import javafx.util.converter.DefaultStringConverter; // Added import
import nonprofitbookkeeping.ui.helpers.FocusCommitTextFieldTableCell; // Added import

import nonprofitbookkeeping.model.*;

/**
 * Form that collects multiple accounting-entry lines until the transaction is
 * balanced; Save stays disabled until debits = credits and > 0.
 */
public class NewTransactionPanelFX extends BorderPane
{
	
	/* ===== static row model ===== */
	public static final class Line
	{
		public StringProperty account;
		public ObjectProperty<AccountSide> side;
		public ObjectProperty<BigDecimal> amount;
		
		/**
		 * Constructor Line
		 * @param a
		 * @param amount
		 */
		Line(Account acc, AccountSide a, BigDecimal amount)
		{
			this.account = new SimpleStringProperty(acc.getName());
			this.side = new SimpleObjectProperty<>(a);
			this.amount = new SimpleObjectProperty<>(amount);
		}
		
		/**  
		 * Constructor Line
		 */
		public Line()
		{
			this.account = new SimpleStringProperty("");
			this.side = new SimpleObjectProperty<>(AccountSide.DEBIT);
			this.amount = new SimpleObjectProperty<>(BigDecimal.ZERO);
		}
		
	}
	
	/* ------------------------------------------------------------------ */
	private final ObservableList<Line> lines = FXCollections.observableArrayList();
	private final TableView<Line> table = new TableView<>(this.lines);
	
	private final DatePicker datePicker = new DatePicker(LocalDate.now());
	private final TextArea memoArea = new TextArea();
	private Button saveBtn;
	private final Consumer<AccountingTransaction> onSave;
	
	public NewTransactionPanelFX(Consumer<AccountingTransaction> onSave)
	{
		this.onSave = onSave;
		setPadding(new Insets(10));
		buildUI();
		this.lines.addListener((ListChangeListener<Line>) c -> recalcTotals());
		recalcTotals();
	}
	
	/**  
	 * Constructor NewTransactionPanelFX
	 * @param existing
	 * @param consumer
	 */
	public NewTransactionPanelFX(AccountingTransaction existing,
		Consumer<AccountingTransaction> onSave)
	{
		this.onSave = onSave;
		setPadding(new Insets(10));
		buildUI(existing);
		this.lines.addListener((ListChangeListener<Line>) c -> recalcTotals());
		recalcTotals();
	}
	
	/**
	 * Populates the UI with an existing balanced transaction so the user can
	 * correct or extend entry lines.
	 */
	private void buildUI(AccountingTransaction existing)
	{
		buildUI(); 
		this.lines.forEach(this::watch);  
		
		/* 1. header fields */
		this.datePicker.setValue(LocalDate.parse(existing.getDate()));
		this.memoArea.setText(existing.getMemo());
		
		/* 2. entry lines */
		this.lines.clear();
		
		// Add the lines from the existing entries.
		for (AccountingEntry e : existing.getEntries())
		{
			Account stub = new Account();
			stub.setName(e.getAccountNumber());
			Line line = new Line(stub, e.getAccountSide(), e.getAmount());
			this.lines.add(line);
			watch(line);
		}
		
	}
	
	/* ===== UI build ===== */
	private void buildUI()
	{
		this.table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
		this.table.getColumns().addAll(
			strCol("Account", l -> l.account),
			sideCol(),
			amtCol("Amount", l -> l.amount));
		this.table.setEditable(true); // enable inline edits
		
		this.table.setRowFactory(tv -> { // double-click edit row
			TableRow<Line> row = new TableRow<>();
			row.setOnMouseClicked(ev -> {
				
				if (ev.getClickCount() == 2 && !row.isEmpty())
				{
					this.table.edit(row.getIndex(), this.table.getColumns().get(0)); // start edit
				}
				
			});
			return row;
		});
		
		Button add = new Button("+ Entry");
		add.setOnAction(e -> {
			Line line = new Line();
			this.lines.add(line);
			watch(line);
		});
		
		
		Button del = new Button("Remove");
		del.setOnAction(e -> {
			Line sel = this.table.getSelectionModel().getSelectedItem();
			if (sel != null)
			{
				this.lines.remove(sel);
			}
		});
		
		this.saveBtn = new Button("Save");
		this.saveBtn.setDisable(true); // enabled only when balanced
		this.saveBtn.setOnAction(e -> persist());
		
		GridPane top = new GridPane();
		top.setHgap(10);
		top.setVgap(8);
		top.addRow(0, new Label("Date:"), this.datePicker);
		top.addRow(1, new Label("Memo:"), this.memoArea);
		
		setTop(top);
		setCenter(this.table);
		setBottom(new ToolBar(add, del, new Separator(), this.saveBtn));
	}
	
	/* ===== build columns ===== */
	private static TableColumn<Line, String> strCol(String t,
													Function<Line, Property<String>> fx)
	{
		TableColumn<Line, String> c = new TableColumn<>(t);
		c.setCellValueFactory(cell -> fx.apply(cell.getValue()));
		// Use FocusCommitTextFieldTableCell with DefaultStringConverter
		c.setCellFactory(
			param -> new FocusCommitTextFieldTableCell<>(new DefaultStringConverter()));
		return c;
	}
	
	/**
	 * 
	 * @return
	 */
	private static TableColumn<Line, AccountSide> sideCol()
	{
		TableColumn<Line, AccountSide> c = new TableColumn<>("Side");
		c.setCellValueFactory(cell -> cell.getValue().side);
		c.setCellFactory(ChoiceBoxTableCell.forTableColumn(AccountSide.values()));
		return c;
	}
	
	/**
	 * 
	 * @param t
	 * @param fx
	 * @return
	 */
	private static TableColumn<Line, BigDecimal> amtCol(String t,
														Function<Line, Property<BigDecimal>> fx)
	{
		TableColumn<Line, BigDecimal> c = new TableColumn<>(t);
		c.setCellValueFactory(cell -> fx.apply(cell.getValue()));
		// Use FocusCommitTextFieldTableCell with BigDecimalStringConverter
		c.setCellFactory(
			param -> new FocusCommitTextFieldTableCell<>(new BigDecimalStringConverter()));
		return c;
	}
	
	/* ===== logic ===== */
	private void recalcTotals()
	{
		BigDecimal debit = BigDecimal.ZERO, credit = BigDecimal.ZERO;
		
		for (Line l : this.lines)
		{
			BigDecimal amt = l.amount.get() != null ? l.amount.get() : BigDecimal.ZERO;
			if (l.side.get() == AccountSide.DEBIT)
			{
				debit = debit.add(amt);
			}
			else
			{
				credit = credit.add(amt);
			}
		}
		
		this.saveBtn.setDisable(debit.signum() == 0 || debit.compareTo(credit) != 0);
	}
	
	/**
	 * 
	 */
	private void persist()
	{
		Set<AccountingEntry> entries = new LinkedHashSet<>();
		
		for (Line l : this.lines)
		{
			entries.add(new AccountingEntry(
				l.amount.get(), l.account.get(), l.side.get()));
		}
		
		AccountingTransaction tx = new AccountingTransaction(
			new Account(), entries, Map.of(), Instant.now().toEpochMilli());
		tx.setDate(this.datePicker.getValue().toString());
		tx.setDescription(this.memoArea.getText());
		this.onSave.accept(tx);
	}
	
	/* ── helper ────────────────────────────────────────────────────────── */
	private void watch(Line l)
	{
		l.amount.addListener((obs, o, n) -> recalcTotals());
		l.side.addListener((obs, o, n) -> recalcTotals());
		l.account.addListener((obs, o, n) -> {
			/* account text change doesn’t affect totals but keeps UI fresh */});
	}
	
}
