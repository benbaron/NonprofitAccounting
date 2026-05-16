package nonprofitbookkeeping.ui;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import nonprofitbookkeeping.model.records.BankStatementRecord;
import nonprofitbookkeeping.service.BankStatementRecordService;
import org.nonprofitbookkeeping.ui.AppPanel;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Bank statement import records panel backed by {@link BankStatementRecordService}.
 */
public class BankStatementRecordsPanel implements AppPanel
{
    private final BorderPane root = new BorderPane();
    private final TableView<Row> table = new TableView<>();
    private final Label status = new Label("Ready");
    private final BankStatementRecordService service;

    public BankStatementRecordsPanel()
    {
        this(new BankStatementRecordService());
    }

    public BankStatementRecordsPanel(BankStatementRecordService service)
    {
        this.service = service;

        Label title = new Label("Bank Statement Records");
        title.getStyleClass().add("journal-entry-heading");
        Button add = new Button("+ Add Import Row");
        Button refresh = new Button("Refresh");
        Button delete = new Button("Delete Selected");
        Button save = new Button("Save");

        this.root.setTop(new VBox(6, title, new HBox(8, add, refresh, delete, save), new Separator()));
        configureTable();
        this.root.setCenter(this.table);
        this.root.setBottom(new VBox(new Separator(), this.status));

        add.setOnAction(e -> this.table.getItems().add(Row.empty()));
        refresh.setOnAction(e -> load());
        save.setOnAction(e -> onSave());
        delete.setOnAction(e -> onDeleteSelected());

        load();
    }

    private void configureTable()
    {
        this.table.setEditable(true);
        this.table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        this.table.getColumns().setAll(
            col("Import ID", Row::importIdProperty, Row::setImportId),
            col("Bank ID", Row::bankIdProperty, Row::setBankId),
            col("Account ID", Row::accountIdProperty, Row::setAccountId),
            col("Currency", Row::currencyProperty, Row::setCurrency),
            col("Start", Row::startProperty, Row::setStart),
            col("End", Row::endProperty, Row::setEnd),
            col("Ledger Balance", Row::ledgerBalanceProperty, Row::setLedgerBalance)
        );
    }

    private TableColumn<Row, String> col(String name,
        java.util.function.Function<Row, SimpleStringProperty> getter,
        java.util.function.BiConsumer<Row, String> setter)
    {
        TableColumn<Row, String> col = new TableColumn<>(name);
        col.setCellValueFactory(v -> getter.apply(v.getValue()));
        col.setCellFactory(c -> new FocusCommitTextFieldTableCell<>());
        col.setOnEditCommit(event -> setter.accept(event.getRowValue(), event.getNewValue()));
        return col;
    }

    private void load()
    {
        try
        {
            List<Row> rows = this.service.listAll().stream().map(Row::fromRecord).toList();
            this.table.setItems(FXCollections.observableArrayList(rows));
            this.status.setText("Loaded " + rows.size() + " bank statement row(s).");
        }
        catch (SQLException ex)
        {
            this.table.getItems().clear();
            this.status.setText("Failed to load bank statement rows: " + ex.getMessage());
        }
    }

    @Override
    public void onSave()
    {
        try
        {
            for (Row row : this.table.getItems())
            {
                this.service.save(row.toRecord());
            }
            this.status.setText("Saved " + this.table.getItems().size() + " bank statement row(s).");
        }
        catch (IllegalArgumentException ex)
        {
            this.status.setText("Validation failed: " + ex.getMessage());
        }
        catch (SQLException | RuntimeException ex)
        {
            this.status.setText("Failed to save bank statement rows: " + ex.getMessage());
        }
    }

    private void onDeleteSelected()
    {
        Row selected = this.table.getSelectionModel().getSelectedItem();
        if (selected == null)
        {
            this.status.setText("Select a row to delete.");
            return;
        }
        try
        {
            int deleted = this.service.delete(selected.importIdProperty().get());
            this.table.getItems().remove(selected);
            this.status.setText(deleted > 0 ? "Deleted import row " + selected.importIdProperty().get() : "Removed unsaved row.");
        }
        catch (SQLException ex)
        {
            this.status.setText("Failed to delete row: " + ex.getMessage());
        }
    }

    @Override
    public String title()
    {
        return "Bank Statement Records";
    }

    @Override
    public Node root()
    {
        return this.root;
    }

    static final class Row
    {
        private final SimpleStringProperty importId;
        private final SimpleStringProperty bankId;
        private final SimpleStringProperty accountId;
        private final SimpleStringProperty currency;
        private final SimpleStringProperty start;
        private final SimpleStringProperty end;
        private final SimpleStringProperty ledgerBalance;

        Row(String importId, String bankId, String accountId, String currency,
            String start, String end, String ledgerBalance)
        {
            this.importId = new SimpleStringProperty(importId);
            this.bankId = new SimpleStringProperty(bankId);
            this.accountId = new SimpleStringProperty(accountId);
            this.currency = new SimpleStringProperty(currency);
            this.start = new SimpleStringProperty(start);
            this.end = new SimpleStringProperty(end);
            this.ledgerBalance = new SimpleStringProperty(ledgerBalance);
        }

        static Row empty()
        {
            return new Row("import-" + UUID.randomUUID(), "", "", "USD", LocalDate.now().toString(), LocalDate.now().toString(), "0.00");
        }

        static Row fromRecord(BankStatementRecord record)
        {
            return new Row(
                record.importId(),
                record.bankAccount() == null ? "" : record.bankAccount().bankId(),
                record.bankAccount() == null ? "" : record.bankAccount().accountId(),
                record.currency(),
                record.statementStart() == null ? "" : record.statementStart().toString(),
                record.statementEnd() == null ? "" : record.statementEnd().toString(),
                record.ledgerBalance() == null || record.ledgerBalance().amount() == null
                    ? "0.00"
                    : record.ledgerBalance().amount().toPlainString());
        }

        BankStatementRecord toRecord()
        {
            LocalDate startDate = parseDate(this.start.get(), "start");
            LocalDate endDate = parseDate(this.end.get(), "end");
            BigDecimal ledger = parseAmount(this.ledgerBalance.get(), "ledgerBalance");
            return new BankStatementRecord(
                this.importId.get(),
                BankStatementRecord.SourceFormat.OTHER,
                "manual",
                BankStatementRecord.StatementKind.BANK,
                new BankStatementRecord.BankAccountRef(this.bankId.get(), this.accountId.get(), "CHECKING"),
                this.currency.get(),
                startDate,
                endDate,
                new BankStatementRecord.BalanceSnapshot(ledger, null),
                null,
                null,
                Map.of(),
                null);
        }

        private static LocalDate parseDate(String value, String fieldName)
        {
            if (value == null || value.isBlank())
            {
                return null;
            }
            try
            {
                return LocalDate.parse(value);
            }
            catch (DateTimeParseException ex)
            {
                throw new IllegalArgumentException(
                    "Invalid " + fieldName + " date '" + value + "' (expected YYYY-MM-DD).");
            }
        }

        private static BigDecimal parseAmount(String value, String fieldName)
        {
            if (value == null || value.isBlank())
            {
                return BigDecimal.ZERO;
            }
            try
            {
                return new BigDecimal(value.trim());
            }
            catch (NumberFormatException ex)
            {
                throw new IllegalArgumentException(
                    "Invalid " + fieldName + " amount '" + value + "'.");
            }
        }

        SimpleStringProperty importIdProperty(){ return this.importId; }
        SimpleStringProperty bankIdProperty(){ return this.bankId; }
        SimpleStringProperty accountIdProperty(){ return this.accountId; }
        SimpleStringProperty currencyProperty(){ return this.currency; }
        SimpleStringProperty startProperty(){ return this.start; }
        SimpleStringProperty endProperty(){ return this.end; }
        SimpleStringProperty ledgerBalanceProperty(){ return this.ledgerBalance; }
        void setImportId(String value){ this.importId.set(value); }
        void setBankId(String value){ this.bankId.set(value); }
        void setAccountId(String value){ this.accountId.set(value); }
        void setCurrency(String value){ this.currency.set(value); }
        void setStart(String value){ this.start.set(value); }
        void setEnd(String value){ this.end.set(value); }
        void setLedgerBalance(String value){ this.ledgerBalance.set(value); }
    }
}
