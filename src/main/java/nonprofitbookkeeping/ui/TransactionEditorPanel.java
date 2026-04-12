package nonprofitbookkeeping.ui;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
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
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import nonprofitbookkeeping.model.AccountingEntry;
import nonprofitbookkeeping.model.AccountingTransaction;
import nonprofitbookkeeping.model.Company;
import nonprofitbookkeeping.persistence.CompanyDataRepository;
import org.nonprofitbookkeeping.ui.AppPanel;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;

/**
 * Transaction editor panel driven from selected ledger transaction.
 */
public class TransactionEditorPanel implements AppPanel
{

	private final BorderPane root = new BorderPane();
	private final TableView<AccountingEntry> splitTable = new TableView<>();
	private final TextField date = new TextField();
	private final TextField payee = new TextField();
	private final TextField memo = new TextField();
	private final TextField bank = new TextField();
	private final Label status = new Label("No transaction selected");
	private final CompanyDataRepository companyDataRepository = new CompanyDataRepository();

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
		splitTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
		splitTable.getColumns().add(col("Account", e -> safe(e.getAccountName())));
		splitTable.getColumns().add(col("Fund", e -> safe(e.getFundNumber())));
		splitTable.getColumns().add(col("Amount", e -> e.getAmount() == null ? "" : e.getAmount().toPlainString()));
		splitTable.getColumns().add(col("Side", e -> e.getAccountSide() == null ? "" : e.getAccountSide().name()));
		splitTable.getColumns().add(col("Merchant", e -> ""));
		splitTable.getColumns().add(col("NMR", e -> ""));
		splitTable.getColumns().add(col("Notes", e -> ""));
	}

	private VBox buildSplitEditor()
	{
		Label lbl = new Label("Splits");
		lbl.getStyleClass().add("subheader");

		Button refresh = new Button("Refresh From Selection");
		ToolBar tb = new ToolBar(refresh);
		refresh.setOnAction(e -> loadTransaction(LedgerSelectionContext.getSelectedTransaction()));

		VBox box = new VBox(6, lbl, tb, splitTable);
		VBox.setVgrow(splitTable, Priority.ALWAYS);
		return box;
	}

	private TableColumn<AccountingEntry, String> col(String name,
		java.util.function.Function<AccountingEntry, String> getter)
	{
		TableColumn<AccountingEntry, String> c = new TableColumn<>(name);
		c.setCellValueFactory(v -> new SimpleStringProperty(getter.apply(v.getValue())));
		return c;
	}

	private void loadTransaction(AccountingTransaction transaction)
	{
		if (transaction == null)
		{
			date.setText("");
			payee.setText("");
			memo.setText("");
			bank.setText("");
			splitTable.setItems(FXCollections.observableArrayList());
			status.setText("No transaction selected");
			return;
		}

		date.setText(safe(transaction.getDate()));
		payee.setText(safe(transaction.getToFrom()));
		memo.setText(safe(transaction.getMemo()));
		bank.setText(safe(transaction.getBank()));

		List<AccountingEntry> entries = transaction.getEntries() == null ?
			new ArrayList<>() : new ArrayList<>(transaction.getEntries());
		splitTable.setItems(FXCollections.observableArrayList(entries));
		status.setText("Loaded " + entries.size() + " split line(s) from selected transaction.");
	}

	private void validateOrPost()
	{
		Alert a = new Alert(Alert.AlertType.INFORMATION,
			"Validate/Post not wired yet; showing live loaded transaction data only.");
		a.setHeaderText("Post / Validate");
		a.showAndWait();
	}

	private void showJournal()
	{
		Alert a = new Alert(Alert.AlertType.INFORMATION,
			"Journal view uses selected transaction with " + splitTable.getItems().size() + " split(s).");
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
		try
		{
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

			LedgerSelectionContext.setSelectedTransaction(txToSave);
			status.setText("Saved transaction " + txToSave.getId() + " to database.");
		}
		catch (SQLException ex)
		{
			status.setText("Save failed: " + ex.getMessage());
		}
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
		tx.setEntries(new LinkedHashSet<>(this.splitTable.getItems()));
		tx.setInfo(selected == null ? new java.util.LinkedHashMap<>() : selected.getInfo());
		tx.setReconciled(selected != null && selected.isReconciled());
		return tx;
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
}
