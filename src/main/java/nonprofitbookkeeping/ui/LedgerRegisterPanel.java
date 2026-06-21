package nonprofitbookkeeping.ui;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Alert;
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
import nonprofitbookkeeping.model.Company;
import nonprofitbookkeeping.model.CurrentCompany;
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
    private final ObservableList<AccountingTransaction> allTransactions = FXCollections.observableArrayList();
    private final Label status = new Label();
    private final CompanyDataRepository companyDataRepository = new CompanyDataRepository();

    public LedgerRegisterPanel()
    {
        root.setPadding(new Insets(16));

        Label title = new Label("Ledger Register");
        Label range = new Label();
        range.textProperty().bind(Bindings.createStringBinding(
            () -> "Date Range: " + DateRangeContext.get(),
            DateRangeContext.selectedProperty()));
        title.getStyleClass().add("journal-entry-heading");

        javafx.scene.control.Button refresh = new javafx.scene.control.Button("Refresh");
        HBox actions = new HBox(8, refresh);
        root.setTop(new VBox(6, title, range, actions, status, new Separator()));

        buildTable();
        root.setCenter(txnTable);
        refresh.setOnAction(e -> loadLiveData());

        txnTable.setRowFactory(tv -> {
            TableRow<LedgerViewRow> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (row.isEmpty())
                {
                    return;
                }
                if (event.getClickCount() == 2 && event.getButton() == MouseButton.PRIMARY)
                {
                    showDetails(row.getItem().transaction());
                }
                if (event.getButton() == MouseButton.SECONDARY)
                {
                    ContextMenu menu = new ContextMenu();
                    MenuItem details = new MenuItem("Show Details");
                    details.setOnAction(ignored -> showDetails(row.getItem().transaction()));
                    menu.getItems().add(details);
                    row.setContextMenu(menu);
                }
            });
            return row;
        });

        DateRangeContext.selectedProperty().addListener(
            (obs, oldRange, newRange) -> applyDateRangeFilter(newRange));
        LedgerNavigationContext.requestedTransactionIdProperty().addListener(
            (obs, oldId, newId) -> {
                if (newId != null)
                {
                    Platform.runLater(() -> handleNavigationRequest(newId));
                }
            });

        loadLiveData();
        Integer pendingId = LedgerNavigationContext.getRequestedTransactionId();
        if (pendingId != null)
        {
            Platform.runLater(() -> handleNavigationRequest(pendingId));
        }
    }

    private void buildTable()
    {
        txnTable.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);
        txnTable.getColumns().add(sizedCol("Date", 140, row -> safe(row.transaction().getDate())));
        txnTable.getColumns().add(sizedCol("Payee", 220, row -> safe(row.transaction().getToFrom())));
        txnTable.getColumns().add(sizedCol("Memo", 300, row -> safe(row.transaction().getMemo())));
        txnTable.getColumns().add(sizedCol("Bank", 180, row -> safe(row.transaction().getBank())));
        txnTable.getColumns().add(sizedCol("Account", 220,
            row -> row.entry() == null ? "" : safe(row.entry().getAccountName())));
        txnTable.getColumns().add(sizedCol("Fund", 180,
            row -> row.entry() == null ? "" : safe(row.entry().getFundNumber())));
        TableColumn<LedgerViewRow, String> subrecords = subrecordColumn();
        subrecords.setPrefWidth(240);
        txnTable.getColumns().add(subrecords);
        txnTable.getColumns().add(sizedCol("Status", 160,
            row -> row.transaction().isReconciled() ? "Reconciled" : "Unreconciled"));
    }

    private void loadLiveData()
    {
        try
        {
            List<AccountingTransaction> transactions =
                companyDataRepository.load().getLedger().getTransactions();
            allTransactions.setAll(transactions);
            applyDateRangeFilter(DateRangeContext.get());
            status.setText("Loaded " + transactions.size() + " transaction(s), "
                + txnTable.getItems().size() + " row(s) from database.");
        }
        catch (SQLException | IllegalStateException ex)
        {
            if (loadFromCurrentCompanyFallback())
            {
                return;
            }
            allTransactions.clear();
            txnTable.getItems().clear();
            status.setText("Unable to load live ledger data: " + ex.getMessage());
        }
    }

    private boolean loadFromCurrentCompanyFallback()
    {
        Company current = CurrentCompany.getCompany();
        if (current == null || current.getLedger() == null)
        {
            return false;
        }
        List<AccountingTransaction> transactions = current.getLedger().getTransactions();
        allTransactions.setAll(transactions);
        applyDateRangeFilter(DateRangeContext.get());
        status.setText("Loaded " + transactions.size() + " transaction(s), "
            + txnTable.getItems().size() + " row(s) from in-memory journal.");
        return true;
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

    public boolean selectTransactionById(int transactionId)
    {
        loadLiveData();
        int index = findVisibleTransactionRow(transactionId);
        if (index < 0 && allTransactions.stream().anyMatch(
            transaction -> transaction != null && transaction.getId() == transactionId))
        {
            DateRangeContext.set(DateRange.ALL);
            applyDateRangeFilter(DateRange.ALL);
            index = findVisibleTransactionRow(transactionId);
        }

        if (index < 0)
        {
            status.setText("Transaction " + transactionId + " was not found in the ledger register.");
            return false;
        }

        txnTable.getSelectionModel().clearAndSelect(index);
        txnTable.scrollTo(index);
        txnTable.requestFocus();
        status.setText("Selected transaction " + transactionId + ".");
        return true;
    }

    private void handleNavigationRequest(int transactionId)
    {
        if (selectTransactionById(transactionId))
        {
            LedgerNavigationContext.clearRequest(transactionId);
        }
    }

    private int findVisibleTransactionRow(int transactionId)
    {
        for (int index = 0; index < txnTable.getItems().size(); index++)
        {
            LedgerViewRow row = txnTable.getItems().get(index);
            if (row != null && row.transaction() != null
                && row.transaction().getId() == transactionId)
            {
                return index;
            }
        }
        return -1;
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
        TableColumn<LedgerViewRow, String> column = new TableColumn<>(name);
        column.setCellValueFactory(value -> new SimpleStringProperty(getter.apply(value.getValue())));
        return column;
    }

    private TableColumn<LedgerViewRow, String> sizedCol(String name, double width,
        java.util.function.Function<LedgerViewRow, String> getter)
    {
        TableColumn<LedgerViewRow, String> column = col(name, getter);
        column.setPrefWidth(width);
        return column;
    }

    private TableColumn<LedgerViewRow, String> subrecordColumn()
    {
        TableColumn<LedgerViewRow, String> column = col("Subrecords",
            row -> abbreviateSubrecordSummary(row.subrecordSummary()));
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
                LedgerViewRow row = getTableRow() == null ? null : getTableRow().getItem();
                String fullSummary = row == null ? null : row.subrecordSummary();
                setText(item);
                if (fullSummary == null || fullSummary.isBlank()
                    || fullSummary.length() <= SUBRECORD_PREVIEW_MAX)
                {
                    setTooltip(null);
                    return;
                }
                setTooltip(new Tooltip(fullSummary));
            }
        });
        return column;
    }

    private void showDetails(AccountingTransaction transaction)
    {
        Alert alert = new Alert(Alert.AlertType.INFORMATION,
            "Date: " + safe(transaction.getDate())
                + "\nPayee: " + safe(transaction.getToFrom())
                + "\nMemo: " + safe(transaction.getMemo())
                + "\nEntries: " + (transaction.getEntries() == null ? 0
                    : transaction.getEntries().size()));
        alert.setHeaderText("Details");
        alert.showAndWait();
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
        // Read-only register: no create action.
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
        if (transaction.getSupplementalLines() == null
            || transaction.getSupplementalLines().isEmpty())
        {
            return "";
        }
        return transaction.getSupplementalLines().stream()
            .map(this::formatSupplementalLine)
            .filter(Objects::nonNull)
            .filter(summary -> !summary.isBlank())
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
