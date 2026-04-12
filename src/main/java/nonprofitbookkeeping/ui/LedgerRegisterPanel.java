package nonprofitbookkeeping.ui;

import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Separator;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import nonprofitbookkeeping.model.AccountingTransaction;
import nonprofitbookkeeping.persistence.CompanyDataRepository;
import org.nonprofitbookkeeping.ui.AppPanel;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

/**
 * Ledger register panel backed by persisted journal transactions.
 */
public class LedgerRegisterPanel implements AppPanel
{

	private final BorderPane root = new BorderPane();
	private final TableView<AccountingTransaction> txnTable = new TableView<>();
	private final ObservableList<AccountingTransaction> allRows =
		FXCollections.observableArrayList();
	private final Label status = new Label();
	private final CompanyDataRepository companyDataRepository =
		new CompanyDataRepository();

	/**
	 * Creates the ledger register panel.
	 */
	public LedgerRegisterPanel()
	{
		root.setPadding(new Insets(8));

		Label title = new Label("Ledger Register");
		Label range = new Label();
		range.textProperty().bind(Bindings.createStringBinding(
			() -> "Date Range: " + DateRangeContext.get(),
			DateRangeContext.selectedProperty()));
		title.getStyleClass().add("panel-title");

		Button newTxn = new Button("+ New Transaction");
		Button open = new Button("Open");
		Button refresh = new Button("Refresh");
		HBox actions = new HBox(8, newTxn, open, refresh);

		root.setTop(new VBox(6, title, range, actions, status, new Separator()));

		buildTable();
		root.setCenter(txnTable);

		newTxn.setOnAction(e -> onNew());
		open.setOnAction(e -> openSelected());
		refresh.setOnAction(e -> loadLiveData());

		txnTable.setRowFactory(tv -> {
			TableRow<AccountingTransaction> r = new TableRow<>();
			r.setOnMouseClicked(e -> {
				if (r.isEmpty())
				{
					return;
				}
				if (e.getClickCount() == 2 && e.getButton() == MouseButton.PRIMARY)
				{
					openRow(r.getItem());
				}
				if (e.getButton() == MouseButton.SECONDARY)
				{
					ContextMenu cm = new ContextMenu();
					MenuItem details = new MenuItem("Show Details");
					details.setOnAction(ev -> showDetails(r.getItem()));
					cm.getItems().add(details);
					r.setContextMenu(cm);
				}
			});
			return r;
		});

		DateRangeContext.selectedProperty().addListener((obs, oldRange, newRange) ->
			applyDateRangeFilter(newRange));
		loadLiveData();
	}

	private void buildTable()
	{
		txnTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
		txnTable.getColumns().add(col("Date", tx -> safe(tx.getDate())));
		txnTable.getColumns().add(col("Payee", tx -> safe(tx.getToFrom())));
		txnTable.getColumns().add(col("Memo", tx -> safe(tx.getMemo())));
		txnTable.getColumns().add(col("Bank", tx -> safe(tx.getBank())));
		txnTable.getColumns().add(col("Status", tx -> tx.isReconciled() ? "Reconciled" : "Unreconciled"));
	}

	private void loadLiveData()
	{
		try
		{
			List<AccountingTransaction> transactions =
				this.companyDataRepository.load().getLedger().getTransactions();
			this.allRows.setAll(transactions);
			applyDateRangeFilter(DateRangeContext.get());
			status.setText("Loaded " + transactions.size() + " transaction(s) from database.");
		}
		catch (SQLException | IllegalStateException ex)
		{
			this.allRows.clear();
			this.txnTable.getItems().clear();
			status.setText("Unable to load live ledger data: " + ex.getMessage());
		}
	}

	private void applyDateRangeFilter(DateRange range)
	{
		DateRange effectiveRange = range == null ? DateRange.ALL : range;
		txnTable.getItems().setAll(allRows.stream()
			.filter(row -> isWithinRange(row.getDate(), effectiveRange))
			.toList());
	}

	private boolean isWithinRange(String dateText, DateRange range)
	{
		if (range == null || range.isAll())
		{
			return true;
		}
		try
		{
			LocalDate rowDate = LocalDate.parse(dateText);
			if (range.startInclusive() != null && rowDate.isBefore(range.startInclusive()))
			{
				return false;
			}
			if (range.endInclusive() != null && rowDate.isAfter(range.endInclusive()))
			{
				return false;
			}
			return true;
		}
		catch (RuntimeException ex)
		{
			return true;
		}
	}

	private TableColumn<AccountingTransaction, String> col(String name,
		java.util.function.Function<AccountingTransaction, String> getter)
	{
		TableColumn<AccountingTransaction, String> c = new TableColumn<>(name);
		c.setCellValueFactory(v -> new SimpleStringProperty(getter.apply(v.getValue())));
		return c;
	}

	private void openSelected()
	{
		AccountingTransaction sel = txnTable.getSelectionModel().getSelectedItem();
		if (sel != null)
		{
			openRow(sel);
		}
	}

	private void openRow(AccountingTransaction row)
	{
		LedgerSelectionContext.setSelectedTransaction(row);
		LedgerSelectionContext.setSelectedSubpanel(LedgerSelectionContext.LedgerSubpanel.EDITOR);
	}

	private void showDetails(AccountingTransaction row)
	{
		Alert a = new Alert(Alert.AlertType.INFORMATION,
			"Date: " + safe(row.getDate()) + "\nPayee: " + safe(row.getToFrom())
				+ "\nMemo: " + safe(row.getMemo()) + "\nEntries: "
				+ (row.getEntries() == null ? 0 : row.getEntries().size()));
		a.setHeaderText("Details");
		a.showAndWait();
	}

	@Override
	public String title()
	{
		return "Ledger Register";
	}

	@Override
	public Node root()
	{
		return root;
	}

	@Override
	public void onNew()
	{
		AccountingTransaction draft = new AccountingTransaction();
		draft.setDate(LocalDate.now().toString());
		draft.setMemo("");
		draft.setToFrom("");
		LedgerSelectionContext.setSelectedTransaction(draft);
		LedgerSelectionContext.setSelectedSubpanel(LedgerSelectionContext.LedgerSubpanel.EDITOR);
	}

	private String safe(String value)
	{
		return value == null ? "" : value;
	}
}
