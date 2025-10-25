
package nonprofitbookkeeping.ui.panels;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

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
import nonprofitbookkeeping.model.Company;
import nonprofitbookkeeping.model.CurrentCompany;
import nonprofitbookkeeping.ui.helpers.AlertBox;
import nonprofitbookkeeping.ui.helpers.FocusCommitTextFieldTableCell;
import nonprofitbookkeeping.util.FormatUtils;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * JavaFX panel for creating a new general journal transaction.
 * <p>
 * Each row represents an account entry with columns for the account,
 * debit amount and credit amount. Debit and credit totals are shown at
 * the bottom and must balance when saving.
 */
public class GeneralJournalEntryPanelFX extends BorderPane
{
        private static final Logger LOGGER = LoggerFactory.getLogger(GeneralJournalEntryPanelFX.class);
	
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
        private final ChartOfAccounts coa;
        private final Consumer<AccountingTransaction> onSave;
        private AccountingTransaction original;
        private final Label validationLabel = new Label();

        /**
         * Creates a new panel with a save callback.
	 *
	 * @param onSave consumer invoked with the created transaction when the
	 *               user clicks save
	 */
        public GeneralJournalEntryPanelFX(Consumer<AccountingTransaction> onSave)
        {
                this.coa = resolveChartOfAccounts();
                this.onSave = onSave;
                setPadding(new Insets(10));
                buildUI();
                this.lines.addListener((ListChangeListener<Line>) change -> {

                        while (change.next())
                        {
                                if (change.wasAdded())
                                {
                                        change.getAddedSubList().forEach(this::watch);
                                }
                        }

                        recalcTotals();
                        updateSaveDisabled();

                });
                recalcTotals();
                updateSaveDisabled();
		
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
                this.saveBtn.setText(existing == null ? "Save" : "Update");

                if (existing != null)
                {
                        loadFromTransaction(existing);
                }
		
	}
	
        /** Convenience constructor logging the transaction. */
        public GeneralJournalEntryPanelFX()
        {
                this(tx -> LOGGER.debug("{}", tx));

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
                add.setOnAction(e -> this.lines.add(new Line()));
		
		Button del = new Button("Remove");
		del.setOnAction(e -> {
			Line sel = this.table.getSelectionModel().getSelectedItem();
			
			if (sel != null)
			{
				this.lines.remove(sel);
			}
			
		});
		
                this.saveBtn.setOnAction(e -> persist());
                this.validationLabel.getStyleClass().add("validation-error");
                this.validationLabel.setWrapText(true);
                this.validationLabel.managedProperty()
                                .bind(this.validationLabel.textProperty().isNotEmpty());
                this.validationLabel.visibleProperty()
                                .bind(this.validationLabel.managedProperty());
		
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
										new Label("Credit:"), this.creditTotalLbl,
										new Separator(), this.validationLabel);
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

                                BigDecimal debitAmount = amountOrZero(row.debit.get());
                                BigDecimal creditAmount = amountOrZero(row.credit.get());

                                // Auto-set natural side: debit field for debit accounts etc.
                                if (acc.getIncreaseSide() == AccountSide.DEBIT &&
                                                creditAmount.signum() != 0 &&
                                                debitAmount.signum() == 0)
                                {
                                        row.debit.set(creditAmount);
                                        row.credit.set(BigDecimal.ZERO);
                                }
                                else if (acc.getIncreaseSide() == AccountSide.CREDIT &&
                                                debitAmount.signum() != 0 &&
                                                creditAmount.signum() == 0)
                                {
                                        row.credit.set(debitAmount);
                                        row.debit.set(BigDecimal.ZERO);
                                }

                        }

                });
                return col;

        }
	
        private static TableColumn<Line, BigDecimal> amtCol(String title,
                        javafx.util.Callback<Line, Property<BigDecimal>> prop)
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
                        BigDecimal lineDebit = amountOrZero(l.debit.get());
                        BigDecimal lineCredit = amountOrZero(l.credit.get());

                        debit = debit.add(lineDebit);
                        credit = credit.add(lineCredit);

                }

               this.debitTotalLbl.setText(FormatUtils.formatCurrency(debit));
               this.creditTotalLbl.setText(FormatUtils.formatCurrency(credit));

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
		}
		
		recalcTotals();
		updateSaveDisabled();
		
	}
	
	private void persist()
	{
		String validationError = findValidationError();

		if (validationError != null)
		{
			AlertBox.showError(getScene() == null ? null : getScene().getWindow(), validationError);
			return;
		}

		Set<AccountingEntry> entries = new LinkedHashSet<>();

		for (Line l : this.lines)
		{
			Account account = this.coa.getAccountByName(l.account.get());

			if (account == null)
			{
				continue;
			}

			String acctNum = account.getAccountNumber();
			String acctName = account.getName();

			BigDecimal debitAmount = amountOrZero(l.debit.get());
			BigDecimal creditAmount = amountOrZero(l.credit.get());

			if (debitAmount.signum() > 0)
			{
				entries.add(new AccountingEntry(debitAmount, acctNum,
								AccountSide.DEBIT, acctName));
			}

			if (creditAmount.signum() > 0)
			{
				entries.add(new AccountingEntry(creditAmount, acctNum,
								AccountSide.CREDIT, acctName));
			}

		}

		Map<String, String> info = this.original != null ? this.original.getInfo() : Map.of();
		AccountingTransaction tx = new AccountingTransaction(	new Account(), entries,
								info,
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
		if (getScene() != null && getScene().getWindow() != null)
		{
			getScene().getWindow().hide();
		}

	}

	private void watch(Line l)
	{
		l.debit.addListener((obs, o, n) -> {
			if (n == null)
			{
				l.debit.set(BigDecimal.ZERO);
				return;
			}
			adjustForAccountSide(l);
			recalcTotals();
			updateSaveDisabled();
		});
		l.credit.addListener((obs, o, n) -> {
			if (n == null)
			{
				l.credit.set(BigDecimal.ZERO);
				return;
			}
			adjustForAccountSide(l);
			recalcTotals();
			updateSaveDisabled();
		});
		l.account.addListener((obs, o, n) -> {
			adjustForAccountSide(l);
			updateSaveDisabled();
		});

	}

	private void adjustForAccountSide(Line l)
	{
		Account acc = this.coa.getAccountByName(l.account.get());
		
		if (acc == null)
		{
			return;
		}
		
                BigDecimal debitAmount = amountOrZero(l.debit.get());
                BigDecimal creditAmount = amountOrZero(l.credit.get());

                if (acc.getIncreaseSide() == AccountSide.DEBIT &&
                                creditAmount.signum() > 0 && debitAmount.signum() == 0)
                {
                        l.debit.set(creditAmount);
                        l.credit.set(BigDecimal.ZERO);
                }
                else if (acc.getIncreaseSide() == AccountSide.CREDIT &&
                                debitAmount.signum() > 0 && creditAmount.signum() == 0)
                {
                        l.credit.set(debitAmount);
                        l.debit.set(BigDecimal.ZERO);
                }

        }

        private void updateSaveDisabled()
        {
                String error = findValidationError();
                this.saveBtn.setDisable(error != null);
                this.validationLabel.setText(error != null ? error : "");
        }

        private String findValidationError()
        {
                if (this.lines.isEmpty())
                {
                        return "Add at least one entry.";
                }

                BigDecimal debit = BigDecimal.ZERO;
                BigDecimal credit = BigDecimal.ZERO;
                boolean hasAmount = false;

                for (Line l : this.lines)
                {
                        String name = l.account.get();

                        if (name == null || name.isBlank())
                        {
                                return "Select an account for each line.";
                        }

                        Account account = this.coa.getAccountByName(name);

                        if (account == null)
                        {
                                return "Account not found: " + name;
                        }

                        BigDecimal debitAmount = amountOrZero(l.debit.get());
                        BigDecimal creditAmount = amountOrZero(l.credit.get());

                        if (debitAmount.signum() > 0 && creditAmount.signum() > 0)
                        {
                                return "Enter either a debit or credit for " + account.getName() + ", not both.";
                        }

                        if (debitAmount.signum() > 0)
                        {
                                debit = debit.add(debitAmount);
                                hasAmount = true;
                        }

                        if (creditAmount.signum() > 0)
                        {
                                credit = credit.add(creditAmount);
                                hasAmount = true;
                        }
                }

                if (!hasAmount)
                {
                        return "Enter at least one debit or credit amount.";
                }

                if (debit.compareTo(credit) != 0)
                {
                        return "Transaction is not balanced.";
                }

                return null;
        }

        private static ChartOfAccounts resolveChartOfAccounts()
        {
                Company company = CurrentCompany.getCompany();

                if (company == null)
                {
                        throw new IllegalStateException(
                                        "GeneralJournalEntryPanelFX requires an open company");
                }

                ChartOfAccounts chart = company.getChartOfAccounts();

                if (chart == null)
                {
                        throw new IllegalStateException(
                                        "Current company does not have a chart of accounts loaded");
                }

                return chart;
        }

        private static BigDecimal amountOrZero(BigDecimal value)
        {
                return value != null ? value : BigDecimal.ZERO;
        }

}
