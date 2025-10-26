
package nonprofitbookkeeping.ui.panels;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Separator;
import javafx.scene.control.Tooltip;
import javafx.scene.control.cell.ComboBoxTableCell;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
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
import javafx.stage.Window;


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
        private static final String NEW_ENTRY_SUBTITLE =
                        "Record balanced debits and credits before saving the transaction.";
        private static final String EDIT_ENTRY_SUBTITLE =
                        "Review and adjust the transaction, keeping totals in balance.";
        private final Button saveBtn = new Button("Save");
        private final Button cancelBtn = new Button("Cancel");
        private final Label debitTotalLbl = new Label();
        private final Label creditTotalLbl = new Label();
        private final Label headerLabel = new Label();
        private final Label subtitleLabel = new Label(NEW_ENTRY_SUBTITLE);
        private final ChartOfAccounts coa;
        private final Consumer<AccountingTransaction> onSave;
        private AccountingTransaction original;
        private final Tooltip saveErrorTooltip = new Tooltip();

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
                configureMode(false);
                this.lines.addListener((ListChangeListener<Line>) change -> {
                        while (change.next())
                        {
                                if (change.wasAdded())
                                {
                                        change.getAddedSubList().forEach(this::watch);
                                }
                        }

                        recalcTotals();
                        updateSaveButtonState();
                });
                recalcTotals();
                updateSaveButtonState();
		
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

                configureMode(existing != null);

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
		getStyleClass().add("journal-entry-editor");
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
		this.table.setPlaceholder(
				new Label("Click \"Add Line\" to begin building the entry."));

		this.headerLabel.getStyleClass().add("journal-entry-editor__title");
		this.subtitleLabel.getStyleClass().add("journal-entry-editor__subtitle");

		this.memoArea.setPrefRowCount(3);
		this.memoArea.setWrapText(true);

		GridPane detailsGrid = new GridPane();
		detailsGrid.setHgap(12);
		detailsGrid.setVgap(10);

		Label dateLbl = new Label("Date");
		Label toFromLbl = new Label("To/From");
		Label memoLbl = new Label("Memo");
		Label checkLbl = new Label("Check #");
		Label clearBankLbl = new Label("Clear Bank");
		Label budgetLbl = new Label("Budget Tracking");
		Label fundNameLbl = new Label("Fund Name");

		detailsGrid.add(dateLbl, 0, 0);
		detailsGrid.add(this.datePicker, 1, 0);
		detailsGrid.add(toFromLbl, 2, 0);
		detailsGrid.add(this.toFromField, 3, 0);

		detailsGrid.add(memoLbl, 0, 1);
		detailsGrid.add(this.memoArea, 1, 1);
		GridPane.setColumnSpan(this.memoArea, 3);

		detailsGrid.add(checkLbl, 0, 2);
		detailsGrid.add(this.checkNumberField, 1, 2);
		detailsGrid.add(clearBankLbl, 2, 2);
		detailsGrid.add(this.clearBankField, 3, 2);

		detailsGrid.add(budgetLbl, 0, 3);
		detailsGrid.add(this.budgetTrackingField, 1, 3);
		detailsGrid.add(fundNameLbl, 2, 3);
		detailsGrid.add(this.associatedFundNameField, 3, 3);

		this.toFromField.setPromptText("Optional payee, vendor, or donor");
		this.memoArea.setPromptText("Describe the transaction for reporting purposes");
		this.checkNumberField.setPromptText("Optional check reference");
		this.clearBankField.setPromptText("Bank reconciliation note");
		this.budgetTrackingField.setPromptText("Budget tag or project code");
		this.associatedFundNameField.setPromptText("Fund or restriction name");

		VBox topContainer = new VBox(8, this.headerLabel, this.subtitleLabel, detailsGrid,
				new Separator());
		topContainer.setAlignment(Pos.TOP_LEFT);
		setTop(topContainer);

		Button add = new Button("Add Line");
		add.setOnAction(e -> {
			Line newLine = new Line();
			this.lines.add(newLine);
			this.table.getSelectionModel().select(newLine);
			this.table.scrollTo(newLine);
			this.table.edit(this.lines.indexOf(newLine), this.table.getColumns().get(0));
		});

		Button del = new Button("Remove Line");
		del.setOnAction(e -> {
			Line sel = this.table.getSelectionModel().getSelectedItem();

			if (sel != null)
			{
				this.lines.remove(sel);
			}

			ensureAtLeastOneLine();
		});

		Button duplicate = new Button("Duplicate");
		duplicate.setOnAction(e -> {
			Line sel = this.table.getSelectionModel().getSelectedItem();

			if (sel != null)
			{
				Line copy = copyOf(sel);
				int idx = this.lines.indexOf(sel);
				this.lines.add(idx + 1, copy);
				this.table.getSelectionModel().select(copy);
				this.table.scrollTo(copy);
			}
		});

		Button clearAll = new Button("Clear Lines");
		clearAll.setOnAction(e -> {
			this.lines.clear();
			ensureAtLeastOneLine();
		});

		HBox lineToolbar = new HBox(10, add, del, duplicate, clearAll);
		lineToolbar.setAlignment(Pos.CENTER_LEFT);
		lineToolbar.setPadding(new Insets(0, 0, 8, 0));

		BorderPane tableSection = new BorderPane(this.table);
		tableSection.setTop(lineToolbar);
		tableSection.setPadding(new Insets(10, 0, 10, 0));
		setCenter(tableSection);

		this.saveBtn.setOnAction(e -> persist());
		this.cancelBtn.setOnAction(e -> {
			Window window = getScene() != null ? getScene().getWindow() : null;

			if (window != null)
			{
				window.hide();
			}
		});

		VBox debitCard = createTotalCard("Debit Total", this.debitTotalLbl);
		VBox creditCard = createTotalCard("Credit Total", this.creditTotalLbl);

		HBox totalsRow = new HBox(20, debitCard, creditCard);
		totalsRow.setAlignment(Pos.CENTER_LEFT);

		Region spacer = new Region();
		HBox.setHgrow(spacer, Priority.ALWAYS);

		HBox actions = new HBox(10, spacer, this.cancelBtn, this.saveBtn);
		actions.setAlignment(Pos.CENTER_RIGHT);

		VBox bottomContainer = new VBox(12, new Separator(), new Label("Entry Summary"),
				totalsRow, actions);
		bottomContainer.setAlignment(Pos.TOP_LEFT);
		setBottom(bottomContainer);

		ensureAtLeastOneLine();

	}

	private VBox createTotalCard(String title, Label valueLabel)
	{
		Label heading = new Label(title);
		heading.getStyleClass().add("journal-entry-editor__total-heading");
		valueLabel.getStyleClass().add("journal-entry-editor__total-value");
		VBox box = new VBox(4, heading, valueLabel);
		box.getStyleClass().add("journal-entry-editor__total");
		box.setPadding(new Insets(10));
		box.setAlignment(Pos.CENTER_LEFT);
		return box;
	}

	private void ensureAtLeastOneLine()
	{
		if (this.lines.isEmpty())
		{
			this.lines.add(new Line());
		}
	}

	private Line copyOf(Line source)
	{
		Line copy = new Line();
		copy.account.set(source.account.get());
		copy.debit.set(amountOrZero(source.debit.get()));
		copy.credit.set(amountOrZero(source.credit.get()));
		return copy;
	}

        private void configureMode(boolean editing)
        {
                if (editing)
                {
                        this.headerLabel.setText("Edit Journal Entry");
                        this.saveBtn.setText("Update Entry");
                        this.subtitleLabel.setText(EDIT_ENTRY_SUBTITLE);
                }
                else
                {
                        this.headerLabel.setText("Record New Journal Entry");
                        this.saveBtn.setText("Save Entry");
                        this.subtitleLabel.setText(NEW_ENTRY_SUBTITLE);
                }
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
                col.setCellFactory(column -> new ComboBoxTableCell<>(new DefaultStringConverter(), choices)
                {
                        @Override public void updateItem(String item, boolean empty)
                        {
                                super.updateItem(item, empty);

                                if (empty)
                                {
                                        setStyle("");
                                        setTooltip(null);
                                        return;
                                }

                                Line line = getTableRow() != null ? getTableRow().getItem() : null;
                                Account account = line != null ? GeneralJournalEntryPanelFX.this.resolveAccount(line.account.get()) :
                                                GeneralJournalEntryPanelFX.this.resolveAccount(item);

                                boolean highlight = line != null && account == null &&
                                                (amountOrZero(line.debit.get()).signum() != 0 ||
                                                                amountOrZero(line.credit.get()).signum() != 0);

                                if (highlight)
                                {
                                        setStyle("-fx-background-color: rgba(255,0,0,0.2);");
                                        setTooltip(new Tooltip("Account not found in chart"));
                                }
                                else
                                {
                                        setStyle("");
                                        setTooltip(null);
                                }
                        }
                });
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
                updateSaveButtonState();

        }
	
	private void persist()
	{
		Optional<String> validationError = validateLines();

		if (validationError.isPresent())
		{
			AlertBox.showError(getScene() == null ? null : getScene().getWindow(),
					validationError.get());
			return;
		}

		BigDecimal debit = BigDecimal.ZERO;
		BigDecimal credit = BigDecimal.ZERO;
		Set<AccountingEntry> entries = new LinkedHashSet<>();

		for (Line l : this.lines)
		{
			BigDecimal debitAmount = amountOrZero(l.debit.get());
			BigDecimal creditAmount = amountOrZero(l.credit.get());

			if (debitAmount.signum() == 0 && creditAmount.signum() == 0)
			{
				continue;
			}

			Account account = resolveAccount(l.account.get());

			if (account == null)
			{
				continue;
			}

			String acctNum = account.getAccountNumber();
			String acctName = account.getName();

			if (debitAmount.signum() > 0)
			{
				entries.add(new AccountingEntry(debitAmount, acctNum,
							AccountSide.DEBIT, acctName));
				debit = debit.add(debitAmount);
			}

			if (creditAmount.signum() > 0)
			{
				entries.add(new AccountingEntry(creditAmount, acctNum,
							AccountSide.CREDIT, acctName));
				credit = credit.add(creditAmount);
			}

		}

		AccountingTransaction tx = new AccountingTransaction(new Account(), entries,
				Map.of(), this.original != null ?
						this.original.getBookingDateTimestamp() :
						Instant.now().toEpochMilli());

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

                Window window = getScene() != null ? getScene().getWindow() : null;

                if (window != null)
                {
                        window.hide();
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
			updateSaveButtonState();
		});
		l.credit.addListener((obs, o, n) -> {
			if (n == null)
			{
				l.credit.set(BigDecimal.ZERO);
				return;
			}
			adjustForAccountSide(l);
			recalcTotals();
			updateSaveButtonState();
		});
		l.account.addListener((obs, o, n) -> {
			adjustForAccountSide(l);
			updateSaveButtonState();
			this.table.refresh();
		});

	}

	private void adjustForAccountSide(Line l)
	{
		Account acc = resolveAccount(l.account.get());
		
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

	private Optional<String> validateLines()
	{
		BigDecimal debit = BigDecimal.ZERO;
		BigDecimal credit = BigDecimal.ZERO;
		List<String> missingAccounts = new ArrayList<>();
		boolean hasAmounts = false;

		for (Line l : this.lines)
		{
			BigDecimal debitAmount = amountOrZero(l.debit.get());
			BigDecimal creditAmount = amountOrZero(l.credit.get());
			boolean hasValue = debitAmount.signum() != 0 || creditAmount.signum() != 0;
			String accountToken = l.account.get();

			if (!hasValue && (accountToken == null || accountToken.isBlank()))
			{
				continue;
			}

			if (accountToken == null || accountToken.isBlank())
			{
				return Optional.of("Each amount must reference an account.");
			}

			if (debitAmount.signum() > 0 && creditAmount.signum() > 0)
			{
				return Optional.of("A line cannot have both debit and credit amounts.");
			}

			Account account = resolveAccount(accountToken);

			if (account == null)
			{
				missingAccounts.add(accountToken);
				continue;
			}

			hasAmounts |= hasValue;

			if (debitAmount.signum() > 0)
			{
				debit = debit.add(debitAmount);
			}

			if (creditAmount.signum() > 0)
			{
				credit = credit.add(creditAmount);
			}
		}

		if (!missingAccounts.isEmpty())
		{
			return Optional.of("Account not found: " + String.join(", ", missingAccounts));
		}

		if (!hasAmounts)
		{
			return Optional.of("Add at least one debit or credit amount.");
		}

		if (debit.compareTo(credit) != 0)
		{
			return Optional.of("Transaction is not balanced.");
		}

		return Optional.empty();
	}

	private void updateSaveButtonState()
	{
		Optional<String> error = validateLines();
		boolean invalid = error.isPresent();
		this.saveBtn.setDisable(invalid);

		if (invalid)
		{
			this.saveErrorTooltip.setText(error.get());
			this.saveBtn.setTooltip(this.saveErrorTooltip);
		}
		else
		{
			this.saveBtn.setTooltip(null);
		}

		this.table.refresh();
	}

	private Account resolveAccount(String accountToken)
	{
		if (accountToken == null || accountToken.isBlank())
		{
			return null;
		}

		Account byName = this.coa.getAccountByName(accountToken);

		if (byName != null)
		{
			return byName;
		}

		return this.coa.getAccount(accountToken);
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
