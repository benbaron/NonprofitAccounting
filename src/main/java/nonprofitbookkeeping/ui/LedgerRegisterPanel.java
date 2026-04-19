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
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import nonprofitbookkeeping.model.AccountingEntry;
import nonprofitbookkeeping.model.AccountingTransaction;
import nonprofitbookkeeping.model.supplemental.TxnSupplementalLineBase;
import nonprofitbookkeeping.persistence.CompanyDataRepository;
import org.nonprofitbookkeeping.ui.AppPanel;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Ledger register panel backed by persisted journal transactions.
 */
public class LedgerRegisterPanel implements AppPanel
{
	private static final int SUBRECORD_PREVIEW_MAX = 80;

	private final BorderPane root = new BorderPane();
	private final TableView<LedgerViewRow> txnTable = new TableView<>();
	private final ObservableList<AccountingTransaction> allTransactions =
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
		Button refresh = new Button("Refresh");
		HBox actions = new HBox(8, newTxn, refresh);

		root.setTop(new VBox(6, title, range, actions, status, new Separator()));

		buildTable();
		root.setCenter(txnTable);

		newTxn.setOnAction(e -> onNew());
		refresh.setOnAction(e -> loadLiveData());

		txnTable.setRowFactory(tv -> {
			TableRow<LedgerViewRow> r = new TableRow<>();
			r.setOnMouseClicked(e -> {
				if (r.isEmpty())
				{
					return;
				}
				if (e.getClickCount() == 2 && e.getButton() == MouseButton.PRIMARY)
				{
					openRow(r.getItem().transaction());
				}
				if (e.getButton() == MouseButton.SECONDARY)
				{
					ContextMenu cm = new ContextMenu();
					MenuItem details = new MenuItem("Show Details");
					details.setOnAction(ev -> showDetails(r.getItem().transaction()));
					cm.getItems().add(details);
					r.setContextMenu(cm);
				}
			});
			return r;
		});

		DateRangeContext.selectedProperty().addListener((obs, oldRange, newRange) ->
			applyDateRangeFilter(newRange));
		LedgerSelectionContext.selectedSubpanelProperty().addListener((obs, oldPanel, newPanel) -> {
			if (newPanel == LedgerSelectionContext.LedgerSubpanel.REGISTER)
			{
				loadLiveData();
			}
		});
		loadLiveData();
	}

	private void buildTable()
	{
		txnTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
		txnTable.getColumns().add(col("Date", row -> safe(row.transaction().getDate())));
		txnTable.getColumns().add(col("Payee", row -> safe(row.transaction().getToFrom())));
		txnTable.getColumns().add(col("Memo", row -> safe(row.transaction().getMemo())));
		txnTable.getColumns().add(col("Bank", row -> safe(row.transaction().getBank())));
		txnTable.getColumns().add(col("Account",
			row -> row.entry() == null ? "" : safe(row.entry().getAccountName())));
		txnTable.getColumns().add(col("Fund",
			row -> row.entry() == null ? "" : safe(row.entry().getFundNumber())));
		txnTable.getColumns().add(subrecordColumn());
		txnTable.getColumns().add(col("Status",
			row -> row.transaction().isReconciled() ? "Reconciled" : "Unreconciled"));
	}

	private void loadLiveData()
	{
		try
		{
			List<AccountingTransaction> transactions =
				this.companyDataRepository.load().getLedger().getTransactions();
			this.allTransactions.setAll(transactions);
			applyDateRangeFilter(DateRangeContext.get());
			status.setText("Loaded " + transactions.size() + " transaction(s), "
				+ txnTable.getItems().size() + " row(s) from database.");
		}
		catch (SQLException | IllegalStateException ex)
		{
			this.allTransactions.clear();
			this.txnTable.getItems().clear();
			status.setText("Unable to load live ledger data: " + ex.getMessage());
		}
	}

	private void applyDateRangeFilter(DateRange range)
	{
		DateRange effectiveRange = range == null ? DateRange.ALL : range;
		List<LedgerViewRow> rows = new ArrayList<>();
		for (AccountingTransaction transaction : allTransactions)
		{
			if (!isWithinRange(transaction.getDate(), effectiveRange))
			{
				continue;
			}
			rows.addAll(asLedgerRows(transaction));
		}
		txnTable.getItems().setAll(rows);
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

	private TableColumn<LedgerViewRow, String> col(String name,
		java.util.function.Function<LedgerViewRow, String> getter)
	{
		TableColumn<LedgerViewRow, String> c = new TableColumn<>(name);
		c.setCellValueFactory(v -> new SimpleStringProperty(getter.apply(v.getValue())));
		return c;
	}

	private TableColumn<LedgerViewRow, String> subrecordColumn()
	{
		TableColumn<LedgerViewRow, String> column =
			col("Subrecords", row -> abbreviateSubrecordSummary(row.subrecordSummary()));
		column.setCellFactory(ignored -> new TableCell<>()
		{
			@Override
			protected void updateItem(String item, boolean empty)
			{
				super.updateItem(item, empty);
				if (empty)
				{
					setText(null);
					setTooltip(null);
					return;
				}
				LedgerViewRow row = getTableRow() == null ? null :
					getTableRow().getItem();
				String fullSummary = row == null ? null : row.subrecordSummary();
				setText(item);
				if (fullSummary == null || fullSummary.isBlank() ||
					fullSummary.length() <= SUBRECORD_PREVIEW_MAX)
				{
					setTooltip(null);
					return;
				}
				setTooltip(new Tooltip(fullSummary));
			}
		});
		return column;
	}

	private void openSelected()
	{
		LedgerViewRow sel = txnTable.getSelectionModel().getSelectedItem();
		if (sel != null)
		{
			openRow(sel.transaction());
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

	private List<LedgerViewRow> asLedgerRows(AccountingTransaction transaction)
	{
		if (transaction.getEntries() == null || transaction.getEntries().isEmpty())
		{
			return List.of(new LedgerViewRow(transaction, null,
				buildSubrecordSummary(transaction)));
		}
		List<LedgerViewRow> rows = new ArrayList<>();
		String subrecordSummary = buildSubrecordSummary(transaction);
		for (AccountingEntry entry : transaction.getEntries())
		{
			rows.add(new LedgerViewRow(transaction, entry, subrecordSummary));
		}
		return rows;
	}

	private String buildSubrecordSummary(AccountingTransaction transaction)
	{
		if (transaction.getSupplementalLines() == null ||
			transaction.getSupplementalLines().isEmpty())
		{
			return "";
		}
		return transaction.getSupplementalLines().stream()
			.map(this::formatSupplementalLine)
			.filter(Objects::nonNull)
			.filter(s -> !s.isBlank())
			.collect(Collectors.joining("; "));
	}

	private String formatSupplementalLine(TxnSupplementalLineBase line)
	{
		if (line == null)
		{
			return null;
		}
		String kind = line.getKind() == null ? "SUPPLEMENTAL" : line.getKind().name();
		StringBuilder summary = new StringBuilder(kind);
		if (line.getReference() != null && !line.getReference().isBlank())
		{
			summary.append("#").append(line.getReference().trim());
		}
		if (line.getDescription() != null && !line.getDescription().isBlank())
		{
			summary.append(" ").append(line.getDescription().trim());
		}
		if (line.getAmount() != null)
		{
			summary.append(" $").append(line.getAmount().stripTrailingZeros().toPlainString());
		}
		return summary.toString();
	}

	private String abbreviateSubrecordSummary(String summary)
	{
		if (summary == null || summary.length() <= SUBRECORD_PREVIEW_MAX)
		{
			return summary;
		}
		return summary.substring(0, SUBRECORD_PREVIEW_MAX - 1) + "…";
	}

	private record LedgerViewRow(
		AccountingTransaction transaction,
		AccountingEntry entry,
		String subrecordSummary)
	{
	}
}
