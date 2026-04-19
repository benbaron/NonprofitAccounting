package nonprofitbookkeeping.ui;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.ToolBar;
import javafx.scene.control.cell.ComboBoxTableCell;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import nonprofitbookkeeping.model.Account;
import nonprofitbookkeeping.model.AccountSide;
import nonprofitbookkeeping.model.AccountingEntry;
import nonprofitbookkeeping.model.AccountSide;
import nonprofitbookkeeping.model.AccountingTransaction;
import nonprofitbookkeeping.model.Company;
import nonprofitbookkeeping.model.Fund;
import nonprofitbookkeeping.persistence.CompanyDataRepository;
import nonprofitbookkeeping.model.records.BankingItemRecord;
import nonprofitbookkeeping.service.BankingItemRecordService;
import nonprofitbookkeeping.service.FundAccountingService;
import org.nonprofitbookkeeping.ui.AppPanel;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Transaction editor panel driven from selected ledger transaction.
 */
public class TransactionEditorPanel implements AppPanel
{

	private final BorderPane root = new BorderPane();
	private final TableView<SplitRow> splitTable = new TableView<>();
	private final ObservableList<SplitRow> splitRows = FXCollections.observableArrayList();
	private final TextField date = new TextField();
	private final TextField payee = new TextField();
	private final TextField memo = new TextField();
	private final TextField bank = new TextField();
	private final Label status = new Label("No transaction selected");
	private final CompanyDataRepository companyDataRepository = new CompanyDataRepository();
	private final BankingItemRecordService bankingItemRecordService = new BankingItemRecordService();
	private final Map<String, String> accountNameToNumber = new HashMap<>();
	private final ObservableList<String> accountOptions = FXCollections.observableArrayList();
	private final ObservableList<String> fundOptions = FXCollections.observableArrayList();

	/**
	 * Creates the transaction editor panel.
	 */
	public TransactionEditorPanel()
	{
		root.setPadding(new Insets(8));

		Label title = new Label("Transaction Editor");
		title.getStyleClass().add("panel-title");

		Button save = new Button("Save");
		Button post = new Button("Post / Validate");
		Button journal = new Button("Journal View");
		HBox actions = new HBox(8, save, post, journal);

		root.setTop(new VBox(6, title, actions, new Separator(), buildHeaderForm(), status));

		buildSplitTable();
		root.setCenter(buildSplitEditor());

		save.setOnAction(e -> onSave());
		post.setOnAction(e -> validateOrPost());
		journal.setOnAction(e -> showJournal());

		LedgerSelectionContext.selectedTransactionProperty().addListener((obs, oldValue, newValue) ->
			loadTransaction(newValue));
		reloadReferenceData();
		loadTransaction(LedgerSelectionContext.getSelectedTransaction());
	}

	private GridPane buildHeaderForm()
	{
		GridPane g = new GridPane();
		g.setHgap(8);
		g.setVgap(8);
		g.setPadding(new Insets(8, 0, 8, 0));

		int r = 0;
		g.add(new Label("Date"), 0, r);
		g.add(date, 1, r);
		g.add(new Label("Payee"), 2, r);
		g.add(payee, 3, r);
		r++;
		g.add(new Label("Memo"), 0, r);
		g.add(memo, 1, r, 3, 1);
		r++;
		g.add(new Label("Bank"), 0, r);
		g.add(bank, 1, r);

		g.getColumnConstraints().addAll(
			new ColumnConstraints(70),
			new ColumnConstraints(220),
			new ColumnConstraints(70),
			new ColumnConstraints(220)
		);
		g.getColumnConstraints().get(1).setHgrow(Priority.ALWAYS);
		g.getColumnConstraints().get(3).setHgrow(Priority.ALWAYS);

		return g;
	}

	private void buildSplitTable()
	{
		splitTable.setEditable(true);
		splitTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);

		TableColumn<SplitRow, String> accountCol = new TableColumn<>("Account");
		accountCol.setCellValueFactory(v -> v.getValue().accountProperty());
		accountCol.setCellFactory(ComboBoxTableCell.forTableColumn(accountOptions));
		accountCol.setOnEditCommit(e -> e.getRowValue().setAccount(e.getNewValue()));

		TableColumn<SplitRow, String> fundCol = new TableColumn<>("Fund");
		fundCol.setCellValueFactory(v -> v.getValue().fundProperty());
		fundCol.setCellFactory(ComboBoxTableCell.forTableColumn(fundOptions));
		fundCol.setOnEditCommit(e -> e.getRowValue().setFund(e.getNewValue()));

		TableColumn<SplitRow, String> amountCol = editableTextColumn("Amount",
			SplitRow::amountProperty, SplitRow::setAmount);

		TableColumn<SplitRow, String> sideCol = new TableColumn<>("Side");
		sideCol.setCellValueFactory(v -> v.getValue().sideProperty());
		sideCol.setCellFactory(ComboBoxTableCell.forTableColumn("DEBIT", "CREDIT"));
		sideCol.setOnEditCommit(e -> e.getRowValue().setSide(e.getNewValue()));

		TableColumn<SplitRow, String> merchantCol = editableTextColumn("Merchant",
			SplitRow::merchantProperty, SplitRow::setMerchant);
		TableColumn<SplitRow, String> nmrCol = editableTextColumn("NMR",
			SplitRow::nmrProperty, SplitRow::setNmr);
		TableColumn<SplitRow, String> notesCol = editableTextColumn("Notes",
			SplitRow::notesProperty, SplitRow::setNotes);

		splitTable.getColumns().setAll(accountCol, fundCol, amountCol, sideCol,
			merchantCol, nmrCol, notesCol);
		splitTable.setItems(splitRows);
	}

	private VBox buildSplitEditor()
	{
		Label lbl = new Label("Splits");
		lbl.getStyleClass().add("subheader");

		Button refresh = new Button("Refresh From Selection");
		Button addSplit = new Button("+ Split");
		Button removeSplit = new Button("- Split");
		ToolBar tb = new ToolBar(refresh, addSplit, removeSplit);
		refresh.setOnAction(e -> refreshFromSelection());
		addSplit.setOnAction(e -> splitRows.add(new SplitRow()));
		removeSplit.setOnAction(e -> {
			SplitRow selectedRow = splitTable.getSelectionModel().getSelectedItem();
			if (selectedRow != null)
			{
				splitRows.remove(selectedRow);
			}
		});

		VBox box = new VBox(6, lbl, tb, splitTable);
		VBox.setVgrow(splitTable, Priority.ALWAYS);
		return box;
	}

	private TableColumn<SplitRow, String> editableTextColumn(String name,
		java.util.function.Function<SplitRow, SimpleStringProperty> propertyFn,
		java.util.function.BiConsumer<SplitRow, String> setter)
	{
		TableColumn<SplitRow, String> column = new TableColumn<>(name);
		column.setCellValueFactory(v -> propertyFn.apply(v.getValue()));
		column.setCellFactory(TextFieldTableCell.forTableColumn());
		column.setOnEditCommit(e -> setter.accept(e.getRowValue(), e.getNewValue()));
		return column;
	}

	private void loadTransaction(AccountingTransaction transaction)
	{
		if (transaction == null)
		{
			date.setText("");
			payee.setText("");
			memo.setText("");
			bank.setText("");
			splitRows.clear();
			status.setText("No transaction selected");
			return;
		}

		date.setText(safe(transaction.getDate()));
		payee.setText(safe(transaction.getToFrom()));
		memo.setText(safe(transaction.getMemo()));
		bank.setText(safe(transaction.getBank()));

		List<SplitRow> rows = new ArrayList<>();
		Map<String, BankingItemRecord> bankingByLineId = loadBankingRecordsByLineId(transaction);
		List<String> splitIds = splitIdsFromTransaction(transaction);
		if (transaction.getEntries() != null)
		{
			int lineIndex = 0;
			for (AccountingEntry entry : transaction.getEntries())
			{
				lineIndex++;
				String splitId = lineIndex <= splitIds.size() ? splitIds.get(lineIndex - 1) :
					UUID.randomUUID().toString();
				String lineId = buildLineId(transaction, splitId);
				rows.add(SplitRow.fromEntry(entry, bankingByLineId.get(lineId), splitId));
			}
		}
		splitRows.setAll(rows);
		reloadReferenceData();
		status.setText("Loaded " + rows.size() + " split line(s) from selected transaction.");
	}

	private void validateOrPost()
	{
		AccountingTransaction selected = LedgerSelectionContext.getSelectedTransaction();
		if (selected == null)
		{
			status.setText("Validate/Post failed: no transaction selected.");
			return;
		}
		String validationError = validateTransaction(splitRows);
		if (validationError != null)
		{
			status.setText("Validate/Post failed: " + validationError);
			return;
		}
		if (saveTransaction())
		{
			status.setText("Validation passed and transaction saved.");
		}
		else
		{
			status.setText("Validation passed but save failed.");
		}
	}

	private void showJournal()
	{
		if (root.getScene() != null && root.getScene().getRoot() instanceof MainApplicationView view)
		{
			view.showPanel(MainApplicationView.PanelType.JOURNAL);
			status.setText("Opened Journal view for current transaction context.");
			return;
		}
		Alert a = new Alert(Alert.AlertType.INFORMATION,
			"Unable to navigate to Journal view in current context.");
		a.setHeaderText("Journal View");
		a.showAndWait();
	}

	@Override
	public String title()
	{
		return "Transaction Editor";
	}

	@Override
	public Node root()
	{
		return root;
	}

	@Override
	public void onSave()
	{
		saveTransaction();
	}

	private boolean saveTransaction()
	{
		try
		{
			reloadReferenceData();
			String validationError = validateTransaction(splitRows);
			if (validationError != null)
			{
				status.setText("Save failed: " + validationError);
				return false;
			}
			Company company = this.companyDataRepository.load();
			AccountingTransaction selected = LedgerSelectionContext.getSelectedTransaction();
			AccountingTransaction txToSave = buildFromForm(selected);

			List<AccountingTransaction> transactions =
				new ArrayList<>(company.getLedger().getJournal().getJournalTransactions());
			int index = findTransactionIndex(transactions, txToSave.getId());
			if (index >= 0)
			{
				transactions.set(index, txToSave);
			}
			else
			{
				transactions.add(txToSave);
			}

			company.getLedger().getJournal().replaceAllTransactions(transactions);
			this.companyDataRepository.persist(company);
			persistBankingItemsForSplits(txToSave);

			LedgerSelectionContext.setSelectedTransaction(txToSave);
			status.setText("Saved transaction " + txToSave.getId() + " to database.");
			return true;
		}
		catch (SQLException ex)
		{
			status.setText("Save failed: " + ex.getMessage());
		}
		catch (RuntimeException ex)
		{
			status.setText("Save failed: " + ex.getMessage());
		}
		return false;
	}

	private void refreshFromSelection()
	{
		AccountingTransaction selected = LedgerSelectionContext.getSelectedTransaction();
		if (selected == null)
		{
			loadTransaction(null);
			return;
		}
		try
		{
			List<AccountingTransaction> transactions =
				this.companyDataRepository.load().getLedger().getJournal()
					.getJournalTransactions();
			AccountingTransaction match = findMatchingTransaction(transactions, selected);
			LedgerSelectionContext.setSelectedTransaction(match);
			loadTransaction(match);
			status.setText("Refreshed transaction " + transactionRefreshLabel(match)
				+ " from journal.");
		}
		catch (SQLException ex)
		{
			status.setText("Refresh failed: " + ex.getMessage());
		}
	}

	private String transactionRefreshLabel(AccountingTransaction transaction)
	{
		if (transaction == null)
		{
			return "(none)";
		}
		if (transaction.getId() > 0)
		{
			return "#" + transaction.getId();
		}
		String dateValue = safe(transaction.getDate());
		if (!dateValue.isBlank())
		{
			return "dated " + dateValue;
		}
		return "(unidentified)";
	}

	private AccountingTransaction findMatchingTransaction(
		List<AccountingTransaction> transactions,
		AccountingTransaction selected)
	{
		if (transactions == null || transactions.isEmpty() || selected == null)
		{
			return selected;
		}
		if (selected.getId() > 0)
		{
			for (AccountingTransaction tx : transactions)
			{
				if (tx.getId() == selected.getId())
				{
					return tx;
				}
			}
		}
		if (selected.getBookingDateTimestamp() != null)
		{
			for (AccountingTransaction tx : transactions)
			{
				if (java.util.Objects.equals(tx.getBookingDateTimestamp(),
					selected.getBookingDateTimestamp()))
				{
					return tx;
				}
			}
		}
		return selected;
	}

	private String validateTransaction(List<SplitRow> entries)
	{
		if (safe(this.date.getText()).isBlank())
		{
			return "date is required.";
		}
		if (safe(this.payee.getText()).isBlank())
		{
			return "payee/payer is required.";
		}
		if (safe(this.memo.getText()).isBlank())
		{
			return "memo is required.";
		}
		if (entries == null || entries.isEmpty())
		{
			return "no split entries.";
		}
		BigDecimal debits = BigDecimal.ZERO;
		BigDecimal credits = BigDecimal.ZERO;
		for (SplitRow entry : entries)
		{
			if (entry == null || safe(entry.getAccount()).isBlank() ||
				safe(entry.getFund()).isBlank() || safe(entry.getAmount()).isBlank() ||
				safe(entry.getSide()).isBlank())
			{
				return "all split lines require account, amount, fund, and side.";
			}
			BigDecimal amountValue;
			try
			{
				amountValue = new BigDecimal(entry.getAmount().trim());
			}
			catch (RuntimeException ex)
			{
				return "split amounts must be valid numbers.";
			}
			if ("DEBIT".equalsIgnoreCase(entry.getSide()))
			{
				debits = debits.add(amountValue);
			}
			else if ("CREDIT".equalsIgnoreCase(entry.getSide()))
			{
				credits = credits.add(amountValue);
			}
			else
			{
				return "split side must be DEBIT or CREDIT.";
			}
		}
		if (debits.compareTo(credits) != 0)
		{
			return "debits (" + debits.toPlainString() + ") and credits ("
				+ credits.toPlainString() + ") must balance.";
		}
		return null;
	}

	private String safe(String value)
	{
		return value == null ? "" : value;
	}

	private AccountingTransaction buildFromForm(AccountingTransaction selected)
	{
		AccountingTransaction tx = new AccountingTransaction();
		int resolvedId = selected == null || selected.getId() <= 0 ? nextTransactionId() : selected.getId();
		tx.setId(resolvedId);
		tx.setBookingDateTimestamp(selected == null || selected.getBookingDateTimestamp() == null ||
			selected.getBookingDateTimestamp() == 0L ? System.currentTimeMillis() :
			selected.getBookingDateTimestamp());
		tx.setDate(safe(this.date.getText()));
		tx.setToFrom(safe(this.payee.getText()));
		tx.setMemo(safe(this.memo.getText()));
		tx.setBank(safe(this.bank.getText()));
		LinkedHashSet<AccountingEntry> entries = new LinkedHashSet<>();
		for (SplitRow row : splitRows)
		{
			if (row == null || safe(row.getAccount()).isBlank() || safe(row.getAmount()).isBlank())
			{
				continue;
			}
			AccountSide side = "CREDIT".equalsIgnoreCase(row.getSide()) ? AccountSide.CREDIT : AccountSide.DEBIT;
			String accountNumber = accountNameToNumber.getOrDefault(row.getAccount(), row.getAccount());
			AccountingEntry entry = new AccountingEntry(new BigDecimal(row.getAmount().trim()),
				accountNumber, side, row.getAccount());
			entry.setFundNumber(safe(row.getFund()));
			entries.add(entry);
		}
		tx.setEntries(entries);
		java.util.LinkedHashMap<String, String> info = selected == null || selected.getInfo() == null ?
			new java.util.LinkedHashMap<>() : new java.util.LinkedHashMap<>(selected.getInfo());
		info.put("ledger.split.ids", splitRows.stream()
			.map(SplitRow::getSplitId)
			.collect(Collectors.joining(",")));
		tx.setInfo(info);
		tx.setReconciled(selected != null && selected.isReconciled());
		return tx;
	}

	private void persistBankingItemsForSplits(AccountingTransaction tx) throws SQLException
	{
		String transactionId = String.valueOf(tx.getId());
		for (BankingItemRecord existing : bankingItemRecordService.listAll())
		{
			if (transactionId.equals(existing.transactionId()) &&
				"LEDGER_EDITOR".equals(existing.source()))
			{
				bankingItemRecordService.delete(existing.bankingItemId());
			}
		}
		for (int i = 0; i < splitRows.size(); i++)
		{
			SplitRow row = splitRows.get(i);
			String lineId = buildLineId(tx, row.getSplitId());
			BankingItemRecord record = new BankingItemRecord(
				"ledger-tx-" + transactionId + "-line-" + row.getSplitId(),
				"LEDGER_LINE",
				safe(bank.getText()),
				transactionId,
				List.of(lineId),
				null,
				parseAmountOrZero(row.getAmount()),
				safe(row.getNmr()),
				safe(row.getMerchant()),
				null,
				"",
				null,
				safe(row.getNotes()),
				"LEDGER_EDITOR",
				"DRAFT",
				null,
				null,
				Map.of(),
				null);
			bankingItemRecordService.save(record);
		}
	}

	private Map<String, BankingItemRecord> loadBankingRecordsByLineId(
		AccountingTransaction transaction)
	{
		Map<String, BankingItemRecord> byLineId = new HashMap<>();
		if (transaction == null || transaction.getId() <= 0)
		{
			return byLineId;
		}
		String transactionId = String.valueOf(transaction.getId());
		try
		{
			for (BankingItemRecord record : bankingItemRecordService.listAll())
			{
				if (!transactionId.equals(record.transactionId()))
				{
					continue;
				}
				for (String lineId : record.lineIds())
				{
					byLineId.put(lineId, record);
				}
			}
		}
		catch (SQLException ignore)
		{
			// leave map empty if unavailable
		}
		return byLineId;
	}

	private BigDecimal parseAmountOrZero(String amountText)
	{
		try
		{
			return new BigDecimal(safe(amountText).trim());
		}
		catch (RuntimeException ex)
		{
			return BigDecimal.ZERO;
		}
	}

	private String buildLineId(AccountingTransaction tx, String splitId)
	{
		return "tx-" + tx.getId() + "-split-" + splitId;
	}

	private List<String> splitIdsFromTransaction(AccountingTransaction transaction)
	{
		if (transaction == null || transaction.getInfo() == null)
		{
			return List.of();
		}
		String encoded = transaction.getInfo().get("ledger.split.ids");
		if (encoded == null || encoded.isBlank())
		{
			return List.of();
		}
		return java.util.Arrays.stream(encoded.split(","))
			.map(String::trim)
			.filter(s -> !s.isBlank())
			.toList();
	}

	private void reloadReferenceData()
	{
		accountNameToNumber.clear();
		accountOptions.clear();
		fundOptions.clear();
		try
		{
			Company company = this.companyDataRepository.load();
			for (Account account : company.getChartOfAccounts().getAccounts())
			{
				if (account == null || safe(account.getName()).isBlank())
				{
					continue;
				}
				accountNameToNumber.put(account.getName(), safe(account.getAccountNumber()));
				accountOptions.add(account.getName());
			}
		}
		catch (SQLException ignore)
		{
			// no-op
		}
		catch (RuntimeException ignore)
		{
			// no-op (e.g., no active company yet)
		}
		try
		{
			FundAccountingService service = new FundAccountingService();
			service.loadFunds(null);
			for (Fund fund : service.listFunds())
			{
				if (fund != null && !safe(fund.getName()).isBlank())
				{
					fundOptions.add(fund.getName());
				}
			}
		}
		catch (IOException ignore)
		{
			// no-op
		}
		catch (RuntimeException ignore)
		{
			// no-op (e.g., persistence layer not initialized yet)
		}
		if (fundOptions.isEmpty())
		{
			fundOptions.add("GENERAL");
		}
	}

	private int nextTransactionId()
	{
		try
		{
			return this.companyDataRepository.load().getLedger().getJournal()
				.getJournalTransactions().stream()
				.map(AccountingTransaction::getId)
				.max(Comparator.naturalOrder())
				.orElse(0) + 1;
		}
		catch (SQLException ex)
		{
			return 1;
		}
	}

	private int findTransactionIndex(List<AccountingTransaction> transactions, int id)
	{
		for (int i = 0; i < transactions.size(); i++)
		{
			if (transactions.get(i).getId() == id)
			{
				return i;
			}
		}
		return -1;
	}

	private static final class SplitRow
	{
		private final SimpleStringProperty account = new SimpleStringProperty("");
		private final SimpleStringProperty fund = new SimpleStringProperty("");
		private final SimpleStringProperty amount = new SimpleStringProperty("");
		private final SimpleStringProperty side = new SimpleStringProperty("DEBIT");
		private final SimpleStringProperty merchant = new SimpleStringProperty("");
		private final SimpleStringProperty nmr = new SimpleStringProperty("");
		private final SimpleStringProperty notes = new SimpleStringProperty("");
		private final SimpleStringProperty splitId =
			new SimpleStringProperty(UUID.randomUUID().toString());

		private static SplitRow fromEntry(AccountingEntry entry,
			BankingItemRecord bankingRecord,
			String persistedSplitId)
		{
			SplitRow row = new SplitRow();
			row.setSplitId(persistedSplitId);
			if (entry != null)
			{
				row.setAccount(entry.getAccountName());
				row.setFund(entry.getFundNumber());
				row.setAmount(entry.getAmount() == null ? "" : entry.getAmount().toPlainString());
				row.setSide(entry.getAccountSide() == null ? "DEBIT" : entry.getAccountSide().name());
			}
			if (bankingRecord != null)
			{
				row.setMerchant(bankingRecord.payee());
				row.setNmr(bankingRecord.checkNumber());
				row.setNotes(bankingRecord.memo());
			}
			return row;
		}

		public String getAccount() { return account.get(); }
		public void setAccount(String value) { account.set(value == null ? "" : value); }
		public SimpleStringProperty accountProperty() { return account; }
		public String getFund() { return fund.get(); }
		public void setFund(String value) { fund.set(value == null ? "" : value); }
		public SimpleStringProperty fundProperty() { return fund; }
		public String getAmount() { return amount.get(); }
		public void setAmount(String value) { amount.set(value == null ? "" : value); }
		public SimpleStringProperty amountProperty() { return amount; }
		public String getSide() { return side.get(); }
		public void setSide(String value) { side.set(value == null ? "" : value); }
		public SimpleStringProperty sideProperty() { return side; }
		public String getMerchant() { return merchant.get(); }
		public void setMerchant(String value) { merchant.set(value == null ? "" : value); }
		public SimpleStringProperty merchantProperty() { return merchant; }
		public String getNmr() { return nmr.get(); }
		public void setNmr(String value) { nmr.set(value == null ? "" : value); }
		public SimpleStringProperty nmrProperty() { return nmr; }
		public String getNotes() { return notes.get(); }
		public void setNotes(String value) { notes.set(value == null ? "" : value); }
		public SimpleStringProperty notesProperty() { return notes; }
		public String getSplitId() { return splitId.get(); }
		public void setSplitId(String value)
		{
			splitId.set(value == null || value.isBlank() ? UUID.randomUUID().toString() : value);
		}
		public SimpleStringProperty splitIdProperty() { return splitId; }
	}
}
