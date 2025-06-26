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
import javafx.scene.control.ToolBar;
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
import nonprofitbookkeeping.ui.helpers.FocusCommitTextFieldTableCell;

/**
 * JavaFX panel for creating a new general journal transaction.
 * <p>
 * Each row represents an account entry with columns for the account,
 * debit amount and credit amount. The save button remains disabled
 * until the total debits equal the total credits and are greater than
 * zero.
 */
public class GeneralJournalEntryPanelFX extends BorderPane {

    /** Model for a single entry row. */
    public static final class Line {
        public StringProperty account = new SimpleStringProperty("");
        public ObjectProperty<BigDecimal> debit =
                new SimpleObjectProperty<>(BigDecimal.ZERO);
        public ObjectProperty<BigDecimal> credit =
                new SimpleObjectProperty<>(BigDecimal.ZERO);
    }

    private final ObservableList<Line> lines = FXCollections.observableArrayList();
    private final TableView<Line> table = new TableView<>(lines);
    private final DatePicker datePicker = new DatePicker(LocalDate.now());
    private final TextArea memoArea = new TextArea();
    private final Button saveBtn = new Button("Save");
    private final ChartOfAccounts coa =
            CurrentCompany.getCompany().getChartOfAccounts();
    private final Consumer<AccountingTransaction> onSave;

    /**
     * Creates a new panel with a save callback.
     *
     * @param onSave consumer invoked with the created transaction when the
     *               user clicks save
     */
    public GeneralJournalEntryPanelFX(Consumer<AccountingTransaction> onSave) {
        this.onSave = onSave;
        setPadding(new Insets(10));
        buildUI();
        lines.addListener((ListChangeListener<Line>) c -> recalcTotals());
        recalcTotals();
    }

    /** Convenience constructor printing the transaction to stdout. */
    public GeneralJournalEntryPanelFX() {
        this(tx -> System.out.println(tx));
    }

    @SuppressWarnings("unchecked")
    private void buildUI() {
        table.getColumns().addAll(accountCol(),
                amtCol("Debit", l -> l.debit),
                amtCol("Credit", l -> l.credit));
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        table.setEditable(true);
        table.setRowFactory(tv -> {
            TableRow<Line> row = new TableRow<>();
            row.setOnMouseClicked(e -> {
                if (e.getClickCount() == 1 && !row.isEmpty()) {
                    table.edit(row.getIndex(), table.getColumns().get(0));
                }
            });
            return row;
        });

        Button add = new Button("+ Entry");
        add.setOnAction(e -> {
            Line l = new Line();
            lines.add(l);
            watch(l);
        });

        Button del = new Button("Remove");
        del.setOnAction(e -> {
            Line sel = table.getSelectionModel().getSelectedItem();
            if (sel != null) {
                lines.remove(sel);
            }
        });

        saveBtn.setDisable(true);
        saveBtn.setOnAction(e -> persist());

        GridPane top = new GridPane();
        top.setHgap(10);
        top.setVgap(8);
        top.addRow(0, new Label("Date:"), datePicker);
        top.addRow(1, new Label("Memo:"), memoArea);

        setTop(top);
        setCenter(table);
        setBottom(new ToolBar(add, del, new Label(" "), saveBtn));
    }

    private TableColumn<Line, String> accountCol() {
        ObservableList<String> choices = FXCollections.observableArrayList(
                coa.createAccountNumberMap().asMap().values().stream()
                        .map(Account::getName).sorted().toList());
        Map<String, Account> byName = coa.createAccountNumberMap().asMap().values()
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
            if (acc != null) {
                // Auto-set natural side: debit field for debit accounts etc.
                if (acc.getIncreaseSide() == AccountSide.DEBIT &&
                        row.credit.get().signum() != 0 &&
                        row.debit.get().signum() == 0) {
                    row.debit.set(row.credit.get());
                    row.credit.set(BigDecimal.ZERO);
                } else if (acc.getIncreaseSide() == AccountSide.CREDIT &&
                        row.debit.get().signum() != 0 &&
                        row.credit.get().signum() == 0) {
                    row.credit.set(row.debit.get());
                    row.debit.set(BigDecimal.ZERO);
                }
            }
        });
        return col;
    }

    private static TableColumn<Line, BigDecimal> amtCol(String title,
            javafx.util.Callback<Line, Property<BigDecimal>> prop) {
        TableColumn<Line, BigDecimal> c = new TableColumn<>(title);
        c.setCellValueFactory(cell -> prop.call(cell.getValue()));
        c.setCellFactory(param ->
                new FocusCommitTextFieldTableCell<>(new BigDecimalStringConverter()));
        return c;
    }

    private void recalcTotals() {
        BigDecimal debit = BigDecimal.ZERO;
        BigDecimal credit = BigDecimal.ZERO;
        for (Line l : lines) {
            if (l.debit.get() != null) {
                debit = debit.add(l.debit.get());
            }
            if (l.credit.get() != null) {
                credit = credit.add(l.credit.get());
            }
        }
        saveBtn.setDisable(debit.signum() == 0 || debit.compareTo(credit) != 0);
    }

    private void persist() {
        Set<AccountingEntry> entries = new LinkedHashSet<>();
        for (Line l : lines) {
            String name = l.account.get();
            Account account = coa.getAccountByName(name);
            String acctNum = account != null ? account.getAccountNumber() : name;
            String acctName = account != null ? account.getName() : name;
            if (l.debit.get().signum() > 0) {
                entries.add(new AccountingEntry(l.debit.get(), acctNum,
                        AccountSide.DEBIT, acctName));
            }
            if (l.credit.get().signum() > 0) {
                entries.add(new AccountingEntry(l.credit.get(), acctNum,
                        AccountSide.CREDIT, acctName));
            }
        }
        AccountingTransaction tx = new AccountingTransaction(new Account(), entries,
                Map.of(), Instant.now().toEpochMilli());
        tx.setDate(datePicker.getValue().toString());
        tx.setDescription(memoArea.getText());

        onSave.accept(tx);
    }

    private void watch(Line l) {
        l.debit.addListener((obs, o, n) -> recalcTotals());
        l.credit.addListener((obs, o, n) -> recalcTotals());
    }
}
