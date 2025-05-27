
package nonprofitbookkeeping.ui.panels;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

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
	private Consumer<AccountingTransaction> onSave;
	private ChartOfAccounts coa; 
	
	/**
	 * 
	 * Constructor NewTransactionPanelFX
	 * @param onSave
	 */
	public NewTransactionPanelFX(Consumer<AccountingTransaction> onSave)
	{
		this.coa = CurrentCompany.getCompany().getChartOfAccounts();
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
		this.coa = CurrentCompany.getCompany().getChartOfAccounts();
		this.onSave = onSave;
		setPadding(new Insets(10));
		buildUI(existing);
		
		recalcTotals();
	}
	
	/**
	 * Populates the UI with an existing balanced transaction so the user can
	 * correct or extend entry lines.
	 * 
	 * @param existing
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
	
	/**
	 * buildUI
	 */
	@SuppressWarnings("unchecked") private void buildUI()
	{
		this.table.getColumns().addAll(
			accountCol(), // new combo column
			sideCol(),
			amtCol("Amount", l -> l.amount));
		
		this.table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
		
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

	/**
	 * strCol
	 * @param t
	 * @param fx
	 * 
	 * @return TableColumn
	 */
	@SuppressWarnings("unused") 
	private static TableColumn<Line, String> strCol
						(String t,
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
	 * sideCol
	 * 
	 * @return TableColumn
	 */
	private static TableColumn<Line, AccountSide> sideCol()
	{
		TableColumn<Line, AccountSide> c = new TableColumn<>("Side");
		c.setCellValueFactory(cell -> cell.getValue().side);
		c.setCellFactory(ChoiceBoxTableCell.forTableColumn(AccountSide.values()));
		return c;
	}
	
	/**
	 * amtCol
	 * 
	 * @param t
	 * @param fx
	 * 
	 * @return TableColumn
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
	
	/**
	 * accountCol
	 * 
	 * @return TableColumn
	 */
	private TableColumn<Line, String> accountCol()
	{
		
		ObservableList<String> choices =
			FXCollections.observableArrayList(
				this.coa.getAccountNumberToAccountDetails()
					.values()
					.stream()
					.map(Account::getName)
					.sorted()
					.toList());
		
		Map<String, Account> byName =
		    coa.getAccountNumberToAccountDetails()
		       .values()
		       .stream()
		       .collect(Collectors.toMap(
		           Account::getName,          // key  = name
		           a -> a,                    // value = Account
		           (a, b) -> a,               // merge: keep the first duplicate
		           LinkedHashMap::new         // (optional) keep insertion order
		       ));
		
		TableColumn<Line, String> col = new TableColumn<>("Account");
		col.setCellValueFactory(cd -> cd.getValue().account);
		
		/* editable ComboBox cells */
		col.setCellFactory(
			ComboBoxTableCell.forTableColumn(new DefaultStringConverter(), choices));
		col.setEditable(true);
		
		/* commit handler on the COLUMN, not the cell */
		col.setOnEditCommit(ev -> {
			Line row = ev.getRowValue();
			String newName = ev.getNewValue();
			row.account.set(newName);
			
			Account acc = byName.get(newName);
			
			if (acc != null)
			{
				row.side.set(acc.getIncreaseSide()); // auto-sync DEBIT/CREDIT
			}
			
		});
		
		return col;
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
	 * persist
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
	
	/**
	 * watch
	 * 
	 * @param l
	 */
	private void watch(Line l)
	{
		l.amount.addListener((obs, o, n) -> recalcTotals());
		l.side.addListener((obs, o, n) -> recalcTotals());
		l.account.addListener((obs, o, n) -> {
			/* account text change doesn’t 
			 * affect totals but keeps UI fresh */
		});
	}
	
	
}
