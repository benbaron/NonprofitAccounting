
package nonprofitbookkeeping.ui.panels;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import java.util.function.Consumer;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.ToolBar;
import javafx.scene.control.Separator;
import javafx.scene.control.cell.ComboBoxTableCell;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.util.converter.BigDecimalStringConverter;
import javafx.util.converter.DefaultStringConverter;

import nonprofitbookkeeping.model.Account;
import nonprofitbookkeeping.model.AccountSide;
import nonprofitbookkeeping.model.AccountingEntry;
import nonprofitbookkeeping.model.AccountingTransaction;
import nonprofitbookkeeping.model.ChartOfAccounts;
import nonprofitbookkeeping.model.CurrentCompany;
import nonprofitbookkeeping.ui.helpers.AlertBox;
import nonprofitbookkeeping.ui.helpers.FocusCommitTextFieldTableCell;

/**
 * JavaFX panel for creating a new general journal transaction.
 * <p>
 * Each row represents an account entry with columns for the account,
 * debit amount and credit amount. Debit and credit totals are shown at
 * the bottom and must balance when saving.
 */
public class GeneralJournalEntryPanelFX extends BorderPane
{
	
	/** Model for a single entry row. */
	public static final class Line
	{
		public StringProperty account = new SimpleStringProperty("");
		public ObjectProperty<BigDecimal> debit =
				new SimpleObjectProperty<>(BigDecimal.ZERO);
		public ObjectProperty<BigDecimal> credit =
				new SimpleObjectProperty<>(BigDecimal.ZERO);
		
	}
	
	private final ObservableList<Line> lines = FXCollections.observableArrayList();
	private final TableView<Line> table = new TableView<>(this.lines);
	private final DatePicker datePicker = new DatePicker(LocalDate.now());
	private final TextArea memoArea = new TextArea();
	private final TextField toFromField = new TextField();
	private final TextField checkNumberField = new TextField();
	private final TextField clearBankField = new TextField();
	private final TextField budgetTrackingField = new TextField();
	private final TextField associatedFundNameField = new TextField();
	private final Button saveBtn = new Button("Save");
	private final Label debitTotalLbl = new Label();
	private final Label creditTotalLbl = new Label();
	private final ChartOfAccounts coa =
			CurrentCompany.getCompany().getChartOfAccounts();
	private final Consumer<AccountingTransaction> onSave;
	private AccountingTransaction original;
	
	/**
	 * Creates a new panel with a save callback.
	 *
	 * @param onSave consumer invoked with the created transaction when the
	 *               user clicks save
	 */
	public GeneralJournalEntryPanelFX(Consumer<AccountingTransaction> onSave)
	{
		this.onSave = onSave;
		setPadding(new Insets(10));
		buildUI();
		this.lines.addListener((ListChangeListener<Line>) c -> recalcTotals());
		recalcTotals();
		
	}
	
	/**
	 * Constructs a panel for editing an existing transaction.
	 *
	 * @param existing the transaction to edit; may be {@code null} for a new
	 *                  transaction
	 * @param onSave   callback invoked with the updated transaction when saved
	 */
	public GeneralJournalEntryPanelFX(AccountingTransaction existing,
			Consumer<AccountingTransaction> onSave)
	{
		this(onSave);
		this.original = existing;
		
		if (existing != null)
		{
			loadFromTransaction(existing);
		}
		
	}
	
	/** Convenience constructor printing the transaction to stdout. */
	public GeneralJournalEntryPanelFX()
	{
		this(tx -> System.out.println(tx));
		
	}
	
	/**
	 * buildUI
	 */
	@SuppressWarnings("unchecked") private void buildUI()
	{
		this.table.getColumns().addAll(accountCol(),
				amtCol("Debit", l -> l.debit),
				amtCol("Credit", l -> l.credit));
		this.table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
		this.table.setEditable(true);
		this.table.setRowFactory(tv -> {
			TableRow<Line> row = new TableRow<>();
			row.setOnMouseClicked(e -> {
				
				if (e.getClickCount() == 1 && !row.isEmpty())
				{
					this.table.edit(row.getIndex(), this.table.getColumns().get(0));
				}
				
			});
			return row;
		});
		
		Button add = new Button("+ Entry");
		add.setOnAction(e -> {
			Line l = new Line();
			this.lines.add(l);
			watch(l);
		});
		
		Button del = new Button("Remove");
		del.setOnAction(e -> {
			Line sel = this.table.getSelectionModel().getSelectedItem();
			
			if (sel != null)
			{
				this.lines.remove(sel);
			}
			
		});
		
		this.saveBtn.setOnAction(e -> persist());
		
		GridPane top = new GridPane();
		top.setHgap(10);
		top.setVgap(8);
		top.addRow(0, new Label("Date:"), this.datePicker);
		top.addRow(1, new Label("Memo:"), this.memoArea);
		top.addRow(2, new Label("To/From:"), this.toFromField);
		top.addRow(3, new Label("Check #:"), this.checkNumberField);
		top.addRow(4, new Label("Clear Bank:"), this.clearBankField);
		top.addRow(5, new Label("Budget Tracking:"), this.budgetTrackingField);
		top.addRow(6, new Label("Fund Name:"), this.associatedFundNameField);
		
		setTop(top);
		setCenter(this.table);
		ToolBar bottom = new ToolBar(	add, del, new Separator(), this.saveBtn,
										new Separator(), new Label("Debit:"), this.debitTotalLbl,
										new Label("Credit:"), this.creditTotalLbl);
		setBottom(bottom);
		
	}
	
	private TableColumn<Line, String> accountCol()
	{
		ObservableList<String> choices = FXCollections.observableArrayList(
				this.coa.createAccountNumberMap().asMap().values().stream()
						.map(Account::getName).sorted().toList());
		Map<String, Account> byName = this.coa.createAccountNumberMap().asMap().values()
				.stream().collect(Collectors.toMap(Account::getName, a -> a,
						(a, b) -> a, LinkedHashMap::new));
		
		TableColumn<Line, String> col = new TableColumn<>("Account");
		col.setCellValueFactory(cd -> cd.getValue().account);
		col.setCellFactory(ComboBoxTableCell.forTableColumn(new DefaultStringConverter(),
				choices));
		col.setEditable(true);
		col.setOnEditCommit(ev -> {
			Line row = ev.getRowValue();
			String newName = ev.getNewValue();
			row.account.set(newName);
			Account acc = byName.get(newName);
			
			if (acc != null)
			{
				
				// Auto-set natural side: debit field for debit accounts etc.
				if (acc.getIncreaseSide() == AccountSide.DEBIT &&
						row.credit.get().signum() != 0 &&
						row.debit.get().signum() == 0)
				{
					row.debit.set(row.credit.get());
					row.credit.set(BigDecimal.ZERO);
				}
				else if (acc.getIncreaseSide() == AccountSide.CREDIT &&
						row.debit.get().signum() != 0 &&
						row.credit.get().signum() == 0)
				{
					row.credit.set(row.debit.get());
					row.debit.set(BigDecimal.ZERO);
				}
				
			}
			
		});
		return col;
		
	}
	
	private static TableColumn<Line, BigDecimal> amtCol(String title,
														javafx.util.Callback<Line,
																Property<BigDecimal>> prop)
	{
		TableColumn<Line, BigDecimal> c = new TableColumn<>(title);
		c.setCellValueFactory(cell -> prop.call(cell.getValue()));
		c.setCellFactory(
				param -> new FocusCommitTextFieldTableCell<>(new BigDecimalStringConverter()));
		return c;
		
	}
	
	private void recalcTotals()
	{
		BigDecimal debit = BigDecimal.ZERO;
		BigDecimal credit = BigDecimal.ZERO;
		
		for (Line l : this.lines)
		{
			
			if (l.debit.get() != null)
			{
				debit = debit.add(l.debit.get());
			}
			
			if (l.credit.get() != null)
			{
				credit = credit.add(l.credit.get());
			}
			
		}
		
		this.debitTotalLbl.setText(debit.toPlainString());
		this.creditTotalLbl.setText(credit.toPlainString());
		
	}
	
	/**
	 * Loads an existing transaction into the UI for editing.
	 */
	private void loadFromTransaction(AccountingTransaction tx)
	{
		this.datePicker.setValue(LocalDate.parse(tx.getDate()));
		this.memoArea.setText(tx.getDescription() != null ? tx.getDescription() : tx.getMemo());
		this.toFromField.setText(tx.getToFrom());
		this.checkNumberField.setText(tx.getCheckNumber());
		this.clearBankField.setText(tx.getClearBank());
		this.budgetTrackingField.setText(tx.getBudgetTracking());
		this.associatedFundNameField.setText(tx.getAssociatedFundName());
		this.lines.clear();
		
		for (AccountingEntry e : tx.getEntries())
		{
			Line l = new Line();
			Account a = this.coa.getAccount(e.getAccountNumber());
			l.account.set(a != null ? a.getName() : e.getAccountNumber());
			
			if (e.getAccountSide() == AccountSide.DEBIT)
			{
				l.debit.set(e.getAmount());
			}
			else
			{
				l.credit.set(e.getAmount());
			}
			
			this.lines.add(l);
			watch(l);
		}
		
		recalcTotals();
		
	}
	
	private void persist()
	{
		BigDecimal debit = BigDecimal.ZERO;
		BigDecimal credit = BigDecimal.ZERO;
		Set<AccountingEntry> entries = new LinkedHashSet<>();
		
		for (Line l : this.lines)
		{
			String name = l.account.get();
			
			if (name == null || name.isBlank())
			{
				AlertBox.showError(getScene() == null ? null : getScene().getWindow(),
						"Account name required");
				return;
			}
			
			Account account = this.coa.getAccountByName(name);
			
			if (account == null)
			{
				AlertBox.showError(getScene() == null ? null : getScene().getWindow(),
						"Account not found: " + name);
				return;
			}
			
			String acctNum = account.getAccountNumber();
			String acctName = account.getName();
			
			if (l.debit.get().signum() > 0)
			{
				entries.add(new AccountingEntry(l.debit.get(), acctNum,
												AccountSide.DEBIT, acctName));
				debit = debit.add(l.debit.get());
			}
			
			if (l.credit.get().signum() > 0)
			{
				entries.add(new AccountingEntry(l.credit.get(), acctNum,
												AccountSide.CREDIT, acctName));
				credit = credit.add(l.credit.get());
			}
			
		}
		
		if (debit.signum() == 0 || debit.compareTo(credit) != 0)
		{
			AlertBox.showError(getScene() == null ? null : getScene().getWindow(),
					"Transaction is not balanced");
			return;
		}
		
		AccountingTransaction tx = new AccountingTransaction(	new Account(), entries,
																Map.of(),
																this.original != null ?
																		this.original
																				.getBookingDateTimestamp() :
																		Instant.now()
																				.toEpochMilli());
		
		if (this.original != null)
		{
			tx.setId(this.original.getId());
		}
		
		tx.setDate(this.datePicker.getValue().toString());
		tx.setDescription(this.memoArea.getText());
		tx.setToFrom(this.toFromField.getText());
		tx.setCheckNumber(this.checkNumberField.getText());
		tx.setClearBank(this.clearBankField.getText());
		tx.setBudgetTracking(this.budgetTrackingField.getText());
		tx.setAssociatedFundName(this.associatedFundNameField.getText());
		
		this.onSave.accept(tx);
		
	}
	
	private void watch(Line l)
	{
		l.debit.addListener((obs, o, n) -> {
			adjustForAccountSide(l);
			recalcTotals();
		});
		l.credit.addListener((obs, o, n) -> {
			adjustForAccountSide(l);
			recalcTotals();
		});
		l.account.addListener((obs, o, n) -> {
			adjustForAccountSide(l);
		});
		
	}
	
	private void adjustForAccountSide(Line l)
	{
		Account acc = this.coa.getAccountByName(l.account.get());
		
		if (acc == null)
		{
			return;
		}
		
		if (acc.getIncreaseSide() == AccountSide.DEBIT &&
				l.credit.get().signum() > 0 && l.debit.get().signum() == 0)
		{
			l.debit.set(l.credit.get());
			l.credit.set(BigDecimal.ZERO);
		}
		else if (acc.getIncreaseSide() == AccountSide.CREDIT &&
				l.debit.get().signum() > 0 && l.credit.get().signum() == 0)
		{
			l.credit.set(l.debit.get());
			l.debit.set(BigDecimal.ZERO);
		}
		
	}
	
}
