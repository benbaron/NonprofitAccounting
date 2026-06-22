package nonprofitbookkeeping.ui.panels;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.IntConsumer;

import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBase;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.ToolBar;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.Window;
import nonprofitbookkeeping.model.AccountSide;
import nonprofitbookkeeping.model.AccountingEntry;
import nonprofitbookkeeping.model.AccountingTransaction;
import nonprofitbookkeeping.model.CurrentCompany;
import nonprofitbookkeeping.model.Journal;
import nonprofitbookkeeping.model.supplemental.TxnSupplementalLineBase;
import nonprofitbookkeeping.service.PreferencesService;
import nonprofitbookkeeping.ui.LedgerNavigationContext;
import nonprofitbookkeeping.ui.helpers.AlertBox;
import nonprofitbookkeeping.util.FormatUtils;
import org.nonprofitbookkeeping.ui.AppPanelId;
import org.nonprofitbookkeeping.ui.MainWindow;

/**
 * Displays one selectable and sortable journal-entry group per accounting
 * transaction.
 *
 * <p>Every transaction row contains all posting lines in stored order. Header
 * fields that apply to the transaction as a whole are rendered as note lines
 * beneath those postings. Selection, sorting, editing, and deletion therefore
 * operate on complete transactions rather than individual debit or credit
 * lines.</p>
 */
public class JournalPanelFX extends BorderPane
{
    private static final double DATE_WIDTH = 120;
    private static final double ACCOUNT_WIDTH = 680;
    private static final double FUND_WIDTH = 210;
    private static final double CLEARED_WIDTH = 140;
    private static final double AMOUNT_WIDTH = 130;
    private static final double ID_WIDTH = 110;
    private static final double SUPPLEMENTAL_WIDTH = 150;
    private static final double CONTENT_WIDTH =
        DATE_WIDTH + ACCOUNT_WIDTH + FUND_WIDTH + CLEARED_WIDTH +
        (2 * AMOUNT_WIDTH) + ID_WIDTH + SUPPLEMENTAL_WIDTH + 80;
    private static final double CREDIT_INDENT = 28;
    private static final double TRANSACTION_GAP = 18;

    private enum SortKey
    {
        DATE,
        ACCOUNT,
        FUND,
        CLEARED,
        DEBIT,
        CREDIT,
        TRANSACTION_ID,
        SUPPLEMENTAL
    }

    private final ObservableList<AccountingTransaction> rows =
        FXCollections.observableArrayList();
    private final TableView<AccountingTransaction> table =
        new TableView<>(this.rows);
    private final IntConsumer ledgerNavigator;
    private final Map<SortKey, Label> sortHeaders =
        new EnumMap<>(SortKey.class);
    private final Map<SortKey, String> sortHeaderTitles =
        new EnumMap<>(SortKey.class);

    private SortKey activeSortKey;
    private boolean sortAscending = true;
    private JournalPanelCompanyListener companyListener;
    private ToolBar actionToolBar;

    /** Creates a journal panel that uses the owning main window for navigation. */
    public JournalPanelFX()
    {
        this(null);
    }

    /**
     * Creates a journal panel with an optional custom ledger-navigation
     * callback, primarily for embedding and tests.
     *
     * @param ledgerNavigator callback invoked when a transaction-ID link is
     *                        selected; {@code null} uses the owning main window
     */
    public JournalPanelFX(IntConsumer ledgerNavigator)
    {
        this.ledgerNavigator = ledgerNavigator;
        setPadding(PanelChrome.PANEL_PADDING);
        buildTable();
        setTop(PanelChrome.topSection("Journal"));
        setCenter(this.table);

        buildToolBar();
        setBottom(this.actionToolBar);

        this.companyListener = new JournalPanelCompanyListener(this);
        CurrentCompany.CompanyListener.addCompanyListener(this.companyListener);
        handleCompanyChange(CurrentCompany.isOpen());
    }

    /** Builds the one-row-per-transaction journal display. */
    private void buildTable()
    {
        this.table.getStyleClass().add("journal-transaction-table");
        this.table.setFixedCellSize(-1);
        this.table.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);
        this.table.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        this.table.setPlaceholder(new Label(
            "No journal transactions to display or no company is open."));
        this.table.setStyle(
            "-fx-fixed-cell-size: -1; -fx-table-cell-border-color: transparent;");

        TableColumn<AccountingTransaction, AccountingTransaction> blockColumn =
            new TableColumn<>();
        blockColumn.setGraphic(buildHeader());
        blockColumn.setCellValueFactory(
            value -> new javafx.beans.property.ReadOnlyObjectWrapper<>(
                value.getValue()));
        blockColumn.setCellFactory(ignored -> new TransactionBlockCell());
        blockColumn.setSortable(false);
        blockColumn.setReorderable(false);
        blockColumn.setResizable(true);
        blockColumn.setMinWidth(CONTENT_WIDTH);
        blockColumn.setPrefWidth(CONTENT_WIDTH);
        this.table.getColumns().setAll(blockColumn);

        this.table.setRowFactory(tv -> {
            TableRow<AccountingTransaction> row = new TableRow<>();
            row.getStyleClass().add("journal-transaction-row");
            row.setStyle(
                "-fx-background-insets: 0; -fx-border-color: transparent;");
            row.setOnMouseClicked(event -> {
                if (event.getButton() == MouseButton.PRIMARY &&
                    event.getClickCount() == 2 && !row.isEmpty() &&
                    !isInteractiveTarget(event.getTarget()))
                {
                    openEditor(row.getItem());
                }
            });
            return row;
        });
    }

    private boolean isInteractiveTarget(Object target)
    {
        Node node = target instanceof Node ? (Node) target : null;
        while (node != null)
        {
            if (node instanceof ButtonBase || node instanceof TextField)
            {
                return true;
            }
            node = node.getParent();
        }
        return false;
    }

    private Node buildHeader()
    {
        GridPane header = createJournalGrid();
        header.getStyleClass().add("journal-block-header");
        header.add(sortHeader("Date", SortKey.DATE, Pos.CENTER_LEFT), 0, 0);
        header.add(sortHeader("Account Title and Description",
            SortKey.ACCOUNT, Pos.CENTER_LEFT), 1, 0);
        header.add(sortHeader("Fund", SortKey.FUND, Pos.CENTER_LEFT), 2, 0);
        header.add(sortHeader("Cleared", SortKey.CLEARED,
            Pos.CENTER_LEFT), 3, 0);
        header.add(sortHeader("Debit", SortKey.DEBIT,
            Pos.CENTER_RIGHT), 4, 0);
        header.add(sortHeader("Credit", SortKey.CREDIT,
            Pos.CENTER_RIGHT), 5, 0);
        header.add(sortHeader("Transaction ID", SortKey.TRANSACTION_ID,
            Pos.CENTER_LEFT), 6, 0);
        header.add(sortHeader("Supplemental", SortKey.SUPPLEMENTAL,
            Pos.CENTER_LEFT), 7, 0);
        return header;
    }

    private Label sortHeader(String title, SortKey key, Pos alignment)
    {
        Label label = new Label(title);
        label.setMaxWidth(Double.MAX_VALUE);
        label.setAlignment(alignment);
        label.setPadding(new Insets(4, 6, 4, 6));
        label.setCursor(Cursor.HAND);
        label.setStyle("-fx-font-weight: bold;");
        label.setTooltip(new Tooltip(
            "Sort complete journal transactions by " + title));
        label.setOnMouseClicked(event -> sortBy(key));
        this.sortHeaders.put(key, label);
        this.sortHeaderTitles.put(key, title);
        return label;
    }

    private void sortBy(SortKey key)
    {
        if (this.activeSortKey == key)
        {
            this.sortAscending = !this.sortAscending;
        }
        else
        {
            this.activeSortKey = key;
            this.sortAscending = true;
        }
        applyActiveSort();
    }

    private void applyActiveSort()
    {
        if (this.activeSortKey == null)
        {
            return;
        }

        Comparator<AccountingTransaction> comparator =
            comparatorFor(this.activeSortKey);
        if (!this.sortAscending)
        {
            comparator = comparator.reversed();
        }
        FXCollections.sort(this.rows,
            comparator.thenComparingInt(AccountingTransaction::getId));
        updateSortHeaders();
        this.table.refresh();
    }

    private void updateSortHeaders()
    {
        for (Map.Entry<SortKey, Label> item : this.sortHeaders.entrySet())
        {
            String title = this.sortHeaderTitles.get(item.getKey());
            if (item.getKey() == this.activeSortKey)
            {
                title += this.sortAscending ? " ▲" : " ▼";
            }
            item.getValue().setText(title);
        }
    }

    private Comparator<AccountingTransaction> comparatorFor(SortKey key)
    {
        return switch (key)
        {
            case DATE -> (left, right) ->
                compareDates(left.getDate(), right.getDate());
            case ACCOUNT -> Comparator.comparing(
                this::firstAccountTitle,
                Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER));
            case FUND -> Comparator.comparing(
                this::firstFund,
                Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER));
            case CLEARED -> Comparator.comparing(
                this::clearedStatus,
                Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER));
            case DEBIT -> Comparator.comparing(
                AccountingTransaction::getDebit,
                Comparator.nullsLast(BigDecimal::compareTo));
            case CREDIT -> Comparator.comparing(
                AccountingTransaction::getCredit,
                Comparator.nullsLast(BigDecimal::compareTo));
            case TRANSACTION_ID ->
                Comparator.comparingInt(AccountingTransaction::getId);
            case SUPPLEMENTAL -> Comparator.comparingInt(
                transaction -> transaction.getSupplementalLines().size());
        };
    }

    private int compareDates(String left, String right)
    {
        LocalDate leftDate = parseDate(left);
        LocalDate rightDate = parseDate(right);
        if (leftDate != null && rightDate != null)
        {
            return leftDate.compareTo(rightDate);
        }
        if (leftDate != null)
        {
            return -1;
        }
        if (rightDate != null)
        {
            return 1;
        }
        return safe(left).compareToIgnoreCase(safe(right));
    }

    private LocalDate parseDate(String value)
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
            return null;
        }
    }

    private String firstAccountTitle(AccountingTransaction transaction)
    {
        List<AccountingEntry> entries = orderedEntries(transaction);
        return entries.isEmpty() ? "" : accountTitle(entries.get(0));
    }

    private String firstFund(AccountingTransaction transaction)
    {
        List<AccountingEntry> entries = orderedEntries(transaction);
        return entries.isEmpty() ? "" : safe(entries.get(0).getFundNumber());
    }

    private GridPane createJournalGrid()
    {
        GridPane grid = new GridPane();
        grid.setHgap(8);
        grid.setVgap(3);
        grid.setMinWidth(CONTENT_WIDTH);
        grid.setPrefWidth(CONTENT_WIDTH);
        grid.getColumnConstraints().setAll(
            fixedColumn(DATE_WIDTH),
            fixedColumn(ACCOUNT_WIDTH),
            fixedColumn(FUND_WIDTH),
            fixedColumn(CLEARED_WIDTH),
            fixedColumn(AMOUNT_WIDTH),
            fixedColumn(AMOUNT_WIDTH),
            fixedColumn(ID_WIDTH),
            fixedColumn(SUPPLEMENTAL_WIDTH));
        return grid;
    }

    private ColumnConstraints fixedColumn(double width)
    {
        ColumnConstraints column = new ColumnConstraints(width, width, width);
        column.setHgrow(Priority.NEVER);
        return column;
    }

    private final class TransactionBlockCell
        extends TableCell<AccountingTransaction, AccountingTransaction>
    {
        @Override
        protected void updateItem(AccountingTransaction transaction,
            boolean empty)
        {
            super.updateItem(transaction, empty);
            setText(null);
            if (empty || transaction == null)
            {
                setGraphic(null);
                setPadding(Insets.EMPTY);
                return;
            }
            setGraphic(buildTransactionBlock(transaction, getIndex()));
            setPadding(new Insets(4, 0, TRANSACTION_GAP, 0));
            setStyle(
                "-fx-border-color: transparent; -fx-background-color: transparent;");
        }
    }

    private Node buildTransactionBlock(AccountingTransaction transaction,
        int tableIndex)
    {
        GridPane grid = createJournalGrid();
        grid.setPadding(new Insets(4, 6, 0, 6));

        List<AccountingEntry> entries = orderedEntries(transaction);
        int row = 0;
        if (entries.isEmpty())
        {
            grid.add(valueLabel(safe(transaction.getDate()), Pos.CENTER_LEFT),
                0, row);
            Label noLines = valueLabel("(No posting lines)", Pos.CENTER_LEFT);
            noLines.setStyle(
                "-fx-font-style: italic; -fx-text-fill: derive(-fx-text-base-color, -30%);");
            grid.add(noLines, 1, row);
            grid.add(valueLabel(clearedStatus(transaction), Pos.CENTER_LEFT),
                3, row);
            grid.add(transactionLink(transaction, tableIndex), 6, row);
            Node supplemental = supplementalButton(transaction, tableIndex);
            if (supplemental != null)
            {
                grid.add(supplemental, 7, row);
            }
            row++;
        }
        else
        {
            for (int index = 0; index < entries.size(); index++)
            {
                AccountingEntry entry = entries.get(index);
                grid.add(valueLabel(index == 0
                    ? safe(transaction.getDate()) : "", Pos.CENTER_LEFT),
                    0, row);

                Label account = valueLabel(accountTitle(entry),
                    Pos.CENTER_LEFT);
                if (entry.getAccountSide() == AccountSide.CREDIT)
                {
                    account.setPadding(new Insets(2, 4, 2, CREDIT_INDENT));
                }
                grid.add(account, 1, row);
                grid.add(valueLabel(safe(entry.getFundNumber()),
                    Pos.CENTER_LEFT), 2, row);

                BigDecimal amount = entry.getAmount();
                grid.add(valueLabel(
                    entry.getAccountSide() == AccountSide.DEBIT
                        ? FormatUtils.formatCurrency(amount) : "",
                    Pos.CENTER_RIGHT), 4, row);
                grid.add(valueLabel(
                    entry.getAccountSide() == AccountSide.CREDIT
                        ? FormatUtils.formatCurrency(amount) : "",
                    Pos.CENTER_RIGHT), 5, row);

                if (index == 0)
                {
                    Label cleared = valueLabel(clearedStatus(transaction),
                        Pos.CENTER_LEFT);
                    String bankTiming = safe(transaction.getClearBank());
                    if (!bankTiming.isBlank())
                    {
                        cleared.setTooltip(new Tooltip(
                            "Bank timing: " + bankTiming));
                    }
                    grid.add(cleared, 3, row);
                    grid.add(transactionLink(transaction, tableIndex), 6, row);
                    Node supplemental =
                        supplementalButton(transaction, tableIndex);
                    if (supplemental != null)
                    {
                        grid.add(supplemental, 7, row);
                    }
                }
                row++;
            }
        }

        for (String noteLine : transactionNoteLines(transaction))
        {
            Label note = valueLabel(noteLine, Pos.CENTER_LEFT);
            note.setWrapText(false);
            note.setPadding(new Insets(1, 4, 1, 12));
            note.setStyle(
                "-fx-font-style: italic; -fx-text-fill: derive(-fx-text-base-color, -18%);");
            grid.add(note, 1, row, 7, 1);
            row++;
        }

        VBox wrapper = new VBox(grid);
        wrapper.setFillWidth(true);
        wrapper.setMinWidth(CONTENT_WIDTH);
        wrapper.setPrefWidth(CONTENT_WIDTH);
        return wrapper;
    }

    private String clearedStatus(AccountingTransaction transaction)
    {
        boolean bankEntry = transaction.isReconciled() ||
            !safe(transaction.getBank()).isBlank() ||
            !safe(transaction.getClearBank()).isBlank();
        if (!bankEntry)
        {
            return "";
        }
        return transaction.isReconciled() ? "Reconciled" : "Uncleared";
    }

    private Hyperlink transactionLink(AccountingTransaction transaction,
        int tableIndex)
    {
        Hyperlink link = new Hyperlink(String.valueOf(transaction.getId()));
        link.setPadding(new Insets(2, 4, 2, 4));
        link.setOnAction(event -> {
            selectTransaction(tableIndex);
            navigateToLedger(transaction.getId());
            event.consume();
        });
        link.setTooltip(new Tooltip(
            "Open this transaction in the Ledger Register"));
        return link;
    }

    private Node supplementalButton(AccountingTransaction transaction,
        int tableIndex)
    {
        int count = transaction.getSupplementalLines().size();
        if (count == 0)
        {
            return null;
        }

        Button button = new Button("Schedules (" + count + ")");
        button.setMaxWidth(Double.MAX_VALUE);
        button.setTooltip(new Tooltip(
            "View supplemental schedules for this transaction"));
        button.setOnAction(event -> {
            selectTransaction(tableIndex);
            showSupplementalDetails(transaction);
            event.consume();
        });
        return button;
    }

    private void selectTransaction(int tableIndex)
    {
        if (tableIndex >= 0 && tableIndex < this.table.getItems().size())
        {
            this.table.getSelectionModel().clearAndSelect(tableIndex);
        }
    }

    private void showSupplementalDetails(AccountingTransaction transaction)
    {
        List<TxnSupplementalLineBase> supplemental =
            transaction.getSupplementalLines();
        if (supplemental.isEmpty())
        {
            return;
        }

        TableView<TxnSupplementalLineBase> details =
            new TableView<>(FXCollections.observableArrayList(supplemental));
        details.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);
        details.getColumns().addAll(
            supplementalColumn("Schedule", 130,
                line -> line.getKind() == null ? "" :
                    line.getKind().toString()),
            supplementalColumn("Reference", 120,
                line -> safe(line.getReference())),
            supplementalColumn("Description", 260,
                line -> safe(line.getDescription())),
            supplementalColumn("Amount", 120,
                line -> line.getAmount() == null ? "" :
                    FormatUtils.formatCurrency(line.getAmount())),
            supplementalColumn("Dates", 190, this::supplementalDates),
            supplementalColumn("Notes", 260,
                line -> safe(line.getNotes())));
        details.setPrefHeight(360);

        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Supplemental Schedules — Transaction " +
            transaction.getId());
        dialog.getDialogPane().setContent(details);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.getDialogPane().setPrefSize(1000, 450);
        dialog.setResizable(true);
        if (getScene() != null && getScene().getWindow() != null)
        {
            dialog.initOwner(getScene().getWindow());
        }
        dialog.showAndWait();
    }

    private TableColumn<TxnSupplementalLineBase, String> supplementalColumn(
        String title, double width,
        java.util.function.Function<TxnSupplementalLineBase, String> getter)
    {
        TableColumn<TxnSupplementalLineBase, String> column =
            new TableColumn<>(title);
        column.setCellValueFactory(value ->
            new ReadOnlyStringWrapper(getter.apply(value.getValue())));
        column.setPrefWidth(width);
        return column;
    }

    private String supplementalDates(TxnSupplementalLineBase line)
    {
        List<String> dates = new ArrayList<>();
        if (line.getDueDate() != null)
        {
            dates.add("Due " + line.getDueDate());
        }
        if (line.getStartDate() != null)
        {
            dates.add("From " + line.getStartDate());
        }
        if (line.getEndDate() != null)
        {
            dates.add("To " + line.getEndDate());
        }
        return String.join(" · ", dates);
    }

    private void navigateToLedger(int transactionId)
    {
        if (this.ledgerNavigator != null)
        {
            this.ledgerNavigator.accept(transactionId);
            return;
        }

        LedgerNavigationContext.requestTransaction(transactionId);
        Window journalWindow = getScene() == null ? null :
            getScene().getWindow();
        Window ownerWindow = journalWindow;
        while (ownerWindow instanceof Stage stage && stage.getOwner() != null)
        {
            ownerWindow = stage.getOwner();
        }

        if (ownerWindow != null && ownerWindow.getScene() != null &&
            ownerWindow.getScene().getRoot() instanceof MainWindow mainWindow)
        {
            if (journalWindow instanceof Stage journalStage &&
                journalWindow != ownerWindow)
            {
                journalStage.close();
            }
            mainWindow.openPanel(AppPanelId.LEDGER_REGISTER);
        }
    }

    private Label valueLabel(String text, Pos alignment)
    {
        Label label = new Label(text == null ? "" : text);
        label.setMaxWidth(Double.MAX_VALUE);
        label.setAlignment(alignment);
        label.setPadding(new Insets(2, 4, 2, 4));
        return label;
    }

    private List<AccountingEntry> orderedEntries(
        AccountingTransaction transaction)
    {
        Set<AccountingEntry> source = transaction.getEntries();
        if (source == null || source.isEmpty())
        {
            return List.of();
        }
        List<AccountingEntry> result = new ArrayList<>();
        for (AccountingEntry entry : source)
        {
            if (entry != null)
            {
                result.add(entry);
            }
        }
        if (!PreferencesService.isJournalStoredLineOrderPreserved())
        {
            result.sort(Comparator.comparingInt(entry ->
                entry.getAccountSide() == AccountSide.DEBIT ? 0 :
                    entry.getAccountSide() == AccountSide.CREDIT ? 1 : 2));
        }
        return result;
    }

    private String accountTitle(AccountingEntry entry)
    {
        String name = safe(entry.getAccountName());
        return name.isBlank() ? safe(entry.getAccountNumber()) : name;
    }

    private List<String> transactionNoteLines(
        AccountingTransaction transaction)
    {
        List<String> lines = new ArrayList<>();
        addNote(lines, "Memo", transaction.getMemo());
        addCombinedNote(lines,
            labeledValue("Payee", transaction.getToFrom()),
            labeledValue("Check/Ref", transaction.getCheckNumber()));
        addCombinedNote(lines,
            labeledValue("Bank timing", transaction.getClearBank()),
            labeledValue("Bank", transaction.getBank()),
            labeledValue("Budget", transaction.getBudgetTracking()));
        return lines;
    }

    private void addNote(List<String> lines, String label, String value)
    {
        String formatted = labeledValue(label, value);
        if (!formatted.isBlank())
        {
            lines.add(formatted);
        }
    }

    private void addCombinedNote(List<String> lines, String... values)
    {
        StringBuilder line = new StringBuilder();
        for (String value : values)
        {
            if (value == null || value.isBlank())
            {
                continue;
            }
            if (line.length() > 0)
            {
                line.append("    ");
            }
            line.append(value);
        }
        if (line.length() > 0)
        {
            lines.add(line.toString());
        }
    }

    private String labeledValue(String label, String value)
    {
        String safeValue = safe(value);
        return safeValue.isBlank() ? "" : label + ": " + safeValue;
    }

    private String safe(String value)
    {
        return value == null ? "" : value;
    }

    private void buildToolBar()
    {
        Button add = new Button("New");
        Button edit = new Button("Edit");
        Button del = new Button("Delete");
        Button refreshBtn = new Button("Refresh");

        add.setOnAction(e -> openEditor(null));
        edit.setOnAction(e -> {
            AccountingTransaction selected =
                this.table.getSelectionModel().getSelectedItem();
            if (selected != null)
            {
                openEditor(selected);
            }
        });

        del.setOnAction(e -> {
            ObservableList<AccountingTransaction> selected =
                this.table.getSelectionModel().getSelectedItems();
            if (selected == null || selected.isEmpty() ||
                !CurrentCompany.isOpen() ||
                CurrentCompany.getCompany() == null ||
                CurrentCompany.getCompany().getLedger() == null)
            {
                return;
            }

            Journal journal =
                CurrentCompany.getCompany().getLedger().getJournal();
            if (journal == null)
            {
                return;
            }

            for (AccountingTransaction transaction :
                new ArrayList<>(selected))
            {
                journal.deleteTransaction(
                    transaction.getBookingDateTimestamp());
            }

            try
            {
                CurrentCompany.persist();
            }
            catch (IOException ex)
            {
                AlertBox.showError(getScene() == null ? null :
                    getScene().getWindow(),
                    "Unable to save deleted transactions. Please try again.");
                return;
            }
            refreshData();
        });

        refreshBtn.setOnAction(e -> refreshData());
        this.actionToolBar = new ToolBar(add, edit, del, refreshBtn);
    }

    private void openEditor(AccountingTransaction existing)
    {
        if (!CurrentCompany.isOpen() ||
            CurrentCompany.getCompany() == null ||
            CurrentCompany.getCompany().getLedger() == null)
        {
            return;
        }

        Journal mainJournal =
            CurrentCompany.getCompany().getLedger().getJournal();
        if (mainJournal == null)
        {
            return;
        }

        BorderPane pane = new GeneralJournalEntryPanelFX(existing,
            transaction -> {
                if (existing == null)
                {
                    mainJournal.addTransaction(transaction);
                }
                else
                {
                    mainJournal.updateTransaction(transaction);
                }

                try
                {
                    CurrentCompany.persist();
                }
                catch (IOException ex)
                {
                    AlertBox.showError(getScene() == null ? null :
                        getScene().getWindow(),
                        "Unable to save the transaction. Please try again.");
                    return;
                }
                refreshData();
            });

        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle(existing == null ? "New Transaction" :
            "Edit Transaction");
        dialog.getDialogPane().setContent(pane);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.setResizable(true);

        Rectangle2D bounds = Screen.getPrimary().getVisualBounds();
        double prefW = Math.min(1200, bounds.getWidth() * 0.92);
        double prefH = Math.min(860, bounds.getHeight() * 0.90);
        double minW = Math.max(800,
            Math.min(980, bounds.getWidth() * 0.70));
        double minH = Math.max(600,
            Math.min(720, bounds.getHeight() * 0.70));

        dialog.getDialogPane().setPrefSize(prefW, prefH);
        dialog.getDialogPane().setMinSize(minW, minH);
        pane.setMinSize(Math.max(760, minW - 40),
            Math.max(520, minH - 80));
        pane.setPrefSize(Math.max(900, prefW - 20),
            Math.max(620, prefH - 40));
        pane.prefWidthProperty().bind(
            dialog.getDialogPane().widthProperty().subtract(24));
        pane.prefHeightProperty().bind(
            dialog.getDialogPane().heightProperty().subtract(80));
        dialog.showAndWait();
    }

    /** Backward-compatible refresh entry point. */
    @Deprecated
    public void refresh()
    {
        refreshData();
    }

    /** Reloads whole transactions from the current company journal. */
    public void refreshData()
    {
        if (CurrentCompany.isOpen() &&
            CurrentCompany.getCompany() != null &&
            CurrentCompany.getCompany().getLedger() != null)
        {
            Journal journal =
                CurrentCompany.getCompany().getLedger().getJournal();
            if (journal != null)
            {
                this.rows.setAll(journal.getJournalTransactions());
                applyActiveSort();
                this.table.refresh();
                return;
            }
        }
        this.rows.clear();
    }

    private void handleCompanyChange(boolean isOpen)
    {
        if (isOpen)
        {
            refreshData();
        }
        else
        {
            this.rows.clear();
        }
        if (this.actionToolBar != null)
        {
            this.actionToolBar.getItems().forEach(item -> {
                if (item instanceof Button button)
                {
                    button.setDisable(!isOpen);
                }
            });
        }
    }

    private static final class JournalPanelCompanyListener
        implements CurrentCompany.CompanyChangeListener
    {
        private final JournalPanelFX panel;

        private JournalPanelCompanyListener(JournalPanelFX panel)
        {
            this.panel = panel;
        }

        @Override
        public void companyChange(boolean isOpen)
        {
            this.panel.handleCompanyChange(isOpen);
        }
    }
}
