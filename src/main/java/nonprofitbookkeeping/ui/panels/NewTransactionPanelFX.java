
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
	/**
	 * Represents a single line (entry) in an accounting transaction.
	 * Each line has an account, an account side (Debit/Credit), and an amount.
	 * This class uses JavaFX properties to enable data binding with the TableView.
	 */
	public static final class Line
	{
		/** The name of the account for this transaction line. Represented as a StringProperty. */
		public StringProperty account;
		/** The side of the account (Debit or Credit) for this transaction line. Represented as an ObjectProperty. */
		public ObjectProperty<AccountSide> side;
		/** The monetary amount of this transaction line. Represented as an ObjectProperty of BigDecimal. */
		public ObjectProperty<BigDecimal> amount;
		
		/**
		 * Constructs a new {@code Line} with specified account, side, and amount.
		 *
		 * @param acc The {@link Account} object from which the account name is derived. Must not be null.
		 * @param accountSide The {@link AccountSide} (Debit or Credit) for this line.
		 * @param amount The monetary amount for this line.
		 */
		Line(Account acc, AccountSide accountSide, BigDecimal amount)
		{
			this.account = new SimpleStringProperty(acc.getName());
			this.side = new SimpleObjectProperty<>(accountSide);
			this.amount = new SimpleObjectProperty<>(amount);
		}
		
		/**  
		 * Constructs a new, empty {@code Line} with default values.
		 * Account name is empty, side defaults to Debit, and amount defaults to BigDecimal.ZERO.
		 * This is typically used for creating a new row in the UI for the user to fill.
		 */
		public Line()
		{
			this.account = new SimpleStringProperty("");
			this.side = new SimpleObjectProperty<>(AccountSide.DEBIT);
			this.amount = new SimpleObjectProperty<>(BigDecimal.ZERO);
		}
		
	}
	
	/* ------------------------------------------------------------------ */
	/** ObservableList that backs the {@link #table}, containing {@link Line} objects representing transaction entries. */
	private final ObservableList<Line> lines = FXCollections.observableArrayList();
	/** TableView used to display and edit the transaction lines (entries). */
	private final TableView<Line> table = new TableView<>(this.lines);
	
	/** DatePicker for selecting the transaction date. Defaults to the current date. */
	private final DatePicker datePicker = new DatePicker(LocalDate.now());
	/** TextArea for entering a memo or description for the transaction. */
	private final TextArea memoArea = new TextArea();
	/** Button to save the transaction. Enabled only when the transaction is balanced. */
	private Button saveBtn;
	/** Callback {@link Consumer} to be invoked when the transaction is saved. It receives the created {@link AccountingTransaction}. */
	private Consumer<AccountingTransaction> onSave;
	/** Reference to the current company's {@link ChartOfAccounts}, used for account selection. */
	private ChartOfAccounts coa;
	
	/**
	 * Constructs a new {@code NewTransactionPanelFX} for creating a new transaction.
	 * Initializes the UI components and sets up listeners to recalculate totals
	 * and enable/disable the save button based on whether the transaction is balanced.
	 * 
	 * @param onSave A {@link Consumer} callback that will be invoked with the successfully created
	 *               {@link AccountingTransaction} when the user saves. Must not be null.
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
	 * Constructs a new {@code NewTransactionPanelFX} for editing an existing transaction.
	 * Initializes the UI components, pre-fills them with data from the {@code existing} transaction,
	 * and sets up listeners to recalculate totals and enable/disable the save button.
	 *
	 * @param existing The {@link AccountingTransaction} to be edited. Its details will populate the panel. Must not be null.
	 * @param onSave A {@link Consumer} callback that will be invoked with the successfully updated
	 *               {@link AccountingTransaction} when the user saves. Must not be null.
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
	 * Populates the UI with data from an existing {@link AccountingTransaction}.
	 * This method is called when the panel is constructed for editing an existing transaction.
	 * It first calls the base {@link #buildUI()} method to set up the general structure,
	 * then sets the transaction date and memo from the {@code existing} transaction.
	 * Finally, it clears any default lines and populates the {@link #lines} list (and thus the table)
	 * with {@link Line} objects derived from the entries in the {@code existing} transaction.
	 *
	 * @param existing The {@link AccountingTransaction} whose data is to be loaded into the UI.
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
			Account acc = this.coa.getAccount(e.getAccountNumber());
			Account stub = new Account();
			stub.setName(acc != null ? acc.getName() : e.getAccountNumber());
			Line line = new Line(stub, e.getAccountSide(), e.getAmount());
			this.lines.add(line);
			watch(line);
		}
		
	}
	
	/**
	 * Builds the main user interface for the transaction panel.
	 * This includes setting up the {@link #table} with columns for Account, Side (Debit/Credit), and Amount.
	 * It configures cell factories for editable cells, including ComboBoxes for account and side selection.
	 * It also sets up buttons for adding/removing lines and saving the transaction.
	 * The general layout includes a top section for date and memo, the table in the center,
	 * and a toolbar with action buttons at the bottom.
	 * The {@code @SuppressWarnings("unchecked")} is used because {@code table.getColumns().addAll()}
	 * is a varargs method and can cause warnings with generic TableColumn types.
	 */
	@SuppressWarnings("unchecked") 
	private void buildUI()
	{
		this.table.getColumns().addAll(
			accountCol(), // new combo column
			sideCol(),
			amtCol("Amount", l -> l.amount));
		
		this.table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
		
		this.table.setEditable(true); // enable inline edits
		
		this.table.setRowFactory(tv -> { // double-click edit row
			TableRow<Line> row = new TableRow<>();
			row.setOnMouseClicked(ev -> {
				
				if (ev.getClickCount() == 1 && !row.isEmpty())
				{
					this.table.edit(row.getIndex(),
						this.table.getColumns().get(0)); // start edit
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
	
	
	/**
	 * Creates a {@link TableColumn} for displaying and editing String properties of a {@link Line}.
	 * This method is marked as unused, suggesting it might be a helper that was deprecated or replaced
	 * by more specific column creation methods like {@link #accountCol()}.
	 * It configures the column with a cell factory for text field-based editing.
	 * The {@code @SuppressWarnings("unused")} indicates that this method is not currently called.
	 * 
	 * @param t The title of the column for the table header.
	 * @param fx A {@link Function} that takes a {@link Line} object and returns the {@link Property}
	 *           (specifically a {@code StringProperty}) to be bound to this column's cells.
	 * @return A configured {@link TableColumn} for displaying and editing String data.
	 */
	@SuppressWarnings("unused") 
	private static TableColumn<Line, String> strCol(String t,
	                                                Function<Line,Property<String>> fx)
	{
		TableColumn<Line, String> c = new TableColumn<>(t);
		c.setCellValueFactory(cell -> fx.apply(cell.getValue()));
		
		// Use FocusCommitTextFieldTableCell with DefaultStringConverter
		c.setCellFactory(
			param -> new FocusCommitTextFieldTableCell<>(new DefaultStringConverter()));
		return c;
	}
	
	/**
	 * Creates and configures a {@link TableColumn} for selecting the {@link AccountSide} (Debit/Credit)
	 * for a transaction {@link Line}.
	 * The column uses a {@link ChoiceBoxTableCell} populated with {@code AccountSide.values()}
	 * to allow users to choose between Debit and Credit.
	 * 
	 * @return A configured {@link TableColumn} for {@link AccountSide} selection.
	 */
	private static TableColumn<Line, AccountSide> sideCol()
	{
		TableColumn<Line, AccountSide> c = new TableColumn<>("Side");
		c.setCellValueFactory(cell -> cell.getValue().side);
		c.setCellFactory(ChoiceBoxTableCell.forTableColumn(AccountSide.values()));
		return c;
	}
	
	/**
	 * Creates and configures a {@link TableColumn} for displaying and editing the monetary amount
	 * (as a {@link BigDecimal}) of a transaction {@link Line}.
	 * The column uses a {@link FocusCommitTextFieldTableCell} with a {@link BigDecimalStringConverter}
	 * to allow users to input and edit numeric amounts.
	 * 
	 * @param t The title of the column for the table header (e.g., "Amount").
	 * @param fx A {@link Function} that takes a {@link Line} object and returns the {@link Property}
	 *           (specifically an {@code ObjectProperty<BigDecimal>}) to be bound to this column's cells.
	 * @return A configured {@link TableColumn} for {@link BigDecimal} input.
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
	 * Creates and configures a {@link TableColumn} for selecting an account for a transaction {@link Line}.
	 * The column uses a {@link ComboBoxTableCell} populated with account names from the
	 * current company's {@link ChartOfAccounts} ({@link #coa}).
	 * When an account is selected or its name is edited in the cell, an edit commit handler
	 * updates the {@code Line}'s account name and attempts to automatically set the
	 * {@code AccountSide} (Debit/Credit) based on the selected account's natural increase side.
	 * 
	 * @return A configured {@link TableColumn} for account selection.
	 */
	private TableColumn<Line, String> accountCol()
	{
		
                ObservableList<String> choices =
                        FXCollections.observableArrayList(
                                this.coa.createAccountNumberMap()
                                        .asMap()
                                        .values()
                                        .stream()
                                        .map(Account::getName)
                                        .sorted()
                                        .toList());

                Map<String, Account> byName =
                        this.coa.createAccountNumberMap()
                                .asMap()
                                .values()
                                .stream()
                                .collect(Collectors.toMap(
                                        Account::getName, // key = name
					a -> a, // value = Account
					(a, b) -> a, // merge: keep the first duplicate
					LinkedHashMap::new // (optional) keep insertion order
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
	/**
	 * Recalculates the total debit and credit amounts from all {@link Line}s in the table.
	 * After calculating the totals, it updates the enabled state of the {@link #saveBtn}.
	 * The save button is enabled only if the total debits are greater than zero and
	 * total debits equal total credits (i.e., the transaction is balanced).
	 */
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
	 * Persists the current transaction.
	 * This method is called when the "Save" button is clicked. It constructs an
	 * {@link AccountingTransaction} object from the current UI state (date, memo, and all {@link Line} entries).
	 * A unique booking timestamp is generated for the transaction.
	 * The {@link #onSave} consumer (provided during panel construction) is then invoked with the
	 * newly created {@code AccountingTransaction}.
	 */
	private void persist()
	{
		Set<AccountingEntry> entries = new LinkedHashSet<>();
		BigDecimal debitTotal = BigDecimal.ZERO;
		BigDecimal creditTotal = BigDecimal.ZERO;
		
		for (Line l : this.lines)
		{
			String name = l.account.get();
			Account account = this.coa.getAccountByName(name);
			String acctNum = account != null ? account.getAccountNumber() : name;
			
                        String acctName = account != null ? account.getName() : name;
                        entries.add(new AccountingEntry(
                                l.amount.get(), acctNum, l.side.get(), acctName));
			
			BigDecimal amt = l.amount.get() != null ? l.amount.get() : BigDecimal.ZERO;
			
			if (l.side.get() == AccountSide.DEBIT)
			{
				debitTotal = debitTotal.add(amt);
			}
			else
			{
				creditTotal = creditTotal.add(amt);
			}
			
		}
		
		// Save the timestamp as transaction id
		AccountingTransaction tx = new AccountingTransaction(
			new Account(),
			entries,
			Map.of(),
			Instant.now().toEpochMilli());
		
		tx.setDate(this.datePicker.getValue().toString());
		tx.setDescription(this.memoArea.getText());
		
//		if (!this.lines.isEmpty())
//		{
//			tx.setAccountName(this.lines.get(0).account.get());
//		}
//		
//		tx.setDebit(debitTotal);
//		tx.setCredit(creditTotal);
		
		this.onSave.accept(tx);
	}
	
	/**
	 * Attaches change listeners to the properties of a given transaction {@link Line}.
	 * Specifically, it listens for changes to the line's {@code amount} and {@code side} properties.
	 * When these properties change, {@link #recalcTotals()} is called to update the transaction totals
	 * and the enabled state of the save button.
	 * It also includes a listener for the {@code account} property, though its body is currently empty,
	 * implying that account text changes do not directly affect totals but might be used for UI freshness.
	 * 
	 * @param l The {@link Line} object whose properties are to be observed for changes.
	 */
	private void watch(Line l)
	{
		l.amount.addListener((obs, o, n) -> recalcTotals());
		l.side.addListener((obs, o, n) -> recalcTotals());
		l.account.addListener((obs, o, n) -> {
			/* account text change doesn’t affect totals but keeps UI fresh */
		});
	}
	
	
}
