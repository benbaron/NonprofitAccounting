package nonprofitbookkeeping.ui.panels;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.ToolBar;
import javafx.scene.control.Tooltip;
import javafx.scene.control.cell.ComboBoxTableCell;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.util.Callback;
import javafx.util.StringConverter;

import nonprofitbookkeeping.model.Account;
import nonprofitbookkeeping.model.AccountSide;
import nonprofitbookkeeping.model.AccountingEntry;
import nonprofitbookkeeping.model.AccountingTransaction;
import nonprofitbookkeeping.model.ChartOfAccounts;
import nonprofitbookkeeping.model.Company;
import nonprofitbookkeeping.model.CurrentCompany;
import nonprofitbookkeeping.model.SettingsModel;
import nonprofitbookkeeping.model.supplemental.SupplementalLineKind;
import nonprofitbookkeeping.model.supplemental.TxnSupplementalLineBase;
import nonprofitbookkeeping.service.SettingsService;
import nonprofitbookkeeping.core.Database;
import java.io.IOException;
import java.util.EnumMap;
import java.util.EnumSet;
import nonprofitbookkeeping.ui.helpers.AlertBox;
import nonprofitbookkeeping.ui.helpers.FocusCommitTextFieldTableCell;
import nonprofitbookkeeping.ui.javafx.supplemental.EntryRef;
import nonprofitbookkeeping.ui.javafx.supplemental.PersonRef;
import nonprofitbookkeeping.ui.javafx.supplemental.SupplementalLineRow;
import nonprofitbookkeeping.ui.javafx.supplemental.SupplementalLinesEditor;
import nonprofitbookkeeping.ui.javafx.supplemental.SupplementalLinesFxAdapter;
import nonprofitbookkeeping.ui.javafx.supplemental.SupplementalLinesTabs;
import nonprofitbookkeeping.util.FormatUtils;
import nonprofitbookkeeping.service.PersonService;
import nonprofitbookkeeping.model.Person;

// TODO: Auto-generated Javadoc
/**
 * Shared workspace UI for creating or editing general journal entries.
 *
 * <p>The legacy code base exposed two very similar panels for "new" and
 * "edit" flows. This class re-imagines that experience with a single,
 * modernised workspace that emphasises a real-time balance summary and a
 * structured metadata editor. Both the old {@code GeneralJournalEntryPanelFX}
 * and {@code NewTransactionPanelFX} now delegate to this class.</p>
 */
public class JournalEntryWorkspaceFX extends BorderPane
{
        /** Public row model so existing tests can reason about the table state. */
        public static class Line
        {
                
                /** The account. */
                public final StringProperty account = new SimpleStringProperty("");
                
                /** The debit. */
                public final ObjectProperty<BigDecimal> debit =
                                new SimpleObjectProperty<>(BigDecimal.ZERO);
                
                /** The credit. */
                public final ObjectProperty<BigDecimal> credit =
                                new SimpleObjectProperty<>(BigDecimal.ZERO);
        }

        /** The lines. */
        private final ObservableList<Line> lines = FXCollections.observableArrayList();
        
        /** The table. */
        private final TableView<Line> table = new TableView<>(this.lines);
        
        /** The date picker. */
        private final DatePicker datePicker = new DatePicker(LocalDate.now());
        
        /** The memo area. */
        private final TextArea memoArea = new TextArea();
        
        /** The to from field. */
        private final TextField toFromField = new TextField();
        
        /** The check number field. */
        private final TextField checkNumberField = new TextField();
        
        /** The clear bank field. */
        private final TextField clearBankField = new TextField();
        
        /** The budget tracking field. */
        private final TextField budgetTrackingField = new TextField();
        
        /** The associated fund name field. */
        private final TextField associatedFundNameField = new TextField();

        /** The debit total label. */
        private final Label debitTotalLabel = new Label();
        
        /** The credit total label. */
        private final Label creditTotalLabel = new Label();
        
        /** The difference label. */
        private final Label differenceLabel = new Label();
        
        /** The status badge. */
        private final Label statusBadge = new Label();
        
        /** The validation message. */
        private final Label validationMessage = new Label();

        /** The save button. */
        private final Button saveButton = new Button("Save");
        
        /** The add line button. */
        private final Button addLineButton = new Button("Add Line");
        
        /** The duplicate line button. */
        private final Button duplicateLineButton = new Button("Duplicate");
        
        /** The remove line button. */
        private final Button removeLineButton = new Button("Remove");
        
        /** The save error tooltip. */
        private final Tooltip saveErrorTooltip = new Tooltip();

        /** The on save. */
        private final Consumer<AccountingTransaction> onSave;
        
        /** The chart of accounts. */
        private final ChartOfAccounts chartOfAccounts;
        
        /** The original. */
        private AccountingTransaction original;
        
        /** The accounts by name. */
        private final Map<String, Account> accountsByName;
        
        /** The supplemental tabs. */
        private final SupplementalLinesTabs supplementalTabs = new SupplementalLinesTabs();

        /** The heading text. */
        private final String headingText;

        /**
         * Convenience constructor used when wiring inside dialogs.
         *
         * @param onSave the on save
         */
        public JournalEntryWorkspaceFX(Consumer<AccountingTransaction> onSave)
        {
                this(null, onSave);
        }

        /** Convenience constructor for tests or logging only scenarios. */
        public JournalEntryWorkspaceFX()
        {
                this(tx -> { });
        }

        /**
         * Creates a workspace for either a new entry (when {@code existing} is
         * {@code null}) or for editing an existing transaction.
         *
         * @param existing the existing
         * @param onSave the on save
         */
        public JournalEntryWorkspaceFX(AccountingTransaction existing,
                        Consumer<AccountingTransaction> onSave)
        {
                this(existing, onSave, existing == null ? "New Journal Entry"
                                : "Edit Journal Entry");
        }

        /**
         * Internal constructor allowing a custom heading.
         *
         * @param existing the existing
         * @param onSave the on save
         * @param headingText the heading text
         */
        protected JournalEntryWorkspaceFX(AccountingTransaction existing,
                        Consumer<AccountingTransaction> onSave, String headingText)
        {
                if (!CurrentCompany.isOpen() || CurrentCompany.getCompany() == null)
                {
                        throw new IllegalStateException(
                                        "JournalEntryWorkspaceFX requires an open company");
                }

                this.chartOfAccounts = resolveChartOfAccounts();
                this.accountsByName = buildAccountsByName(this.chartOfAccounts);
                this.onSave = onSave != null ? onSave : tx -> { };
                this.original = existing;
                this.headingText = headingText;

                initialiseUi();
                attachListeners();

                if (existing != null)
                {
                        loadFromTransaction(existing);
                }
                else
                {
                        addLine();
                        applyDefaultAccountsFromSettings();
                        updateSupplementalTabAvailability();
                }

                recalcTotals();
                markValidationPending();
        }

        /**
         * Returns the backing list of lines. Primarily used by tests.
         *
         * @return the lines
         */
        public ObservableList<Line> getLines()
        {
                return this.lines;
        }

        /**
         * Returns the save button node for integration tests.
         *
         * @return the save button
         */
        public Button getSaveButton()
        {
                return this.saveButton;
        }

        /**
         * Exposes the add-line button primarily for UI tests.
         *
         * @return the adds the line button
         */
        public Button getAddLineButton()
        {
                return this.addLineButton;
        }

        /**
         * Exposes the remove-line button primarily for UI tests.
         *
         * @return the removes the line button
         */
        public Button getRemoveLineButton()
        {
                return this.removeLineButton;
        }

        /**
         * Exposes the duplicate-line button primarily for UI tests.
         *
         * @return the duplicate line button
         */
        public Button getDuplicateLineButton()
        {
                return this.duplicateLineButton;
        }

        /**
         * Returns the table of entry lines.
         *
         * @return the table
         */
        public TableView<Line> getTable()
        {
                return this.table;
        }

        /** Builds the UI skeleton. */
        private void initialiseUi()
        {
                setPadding(new Insets(18));

                VBox root = new VBox(18);
                root.getStyleClass().add("journal-entry-workspace");

                root.getChildren().add(buildHeader());
                root.getChildren().add(buildContent());
                root.getChildren().add(buildFooter());

                VBox.setVgrow(root.getChildren().get(1), Priority.ALWAYS);
                setCenter(root);

                this.supplementalTabs.setPersonRefs(loadPersonRefs());

                this.table.setId("entryTable");
                this.saveButton.setId("saveBtn");
                this.datePicker.setId("datePicker");
                this.memoArea.setId("memoArea");
        }

        /**
         * Builds the header.
         *
         * @return the node
         */
        private Node buildHeader()
        {
                Label heading = new Label(this.headingText);
                heading.getStyleClass().add("journal-entry-heading");
                heading.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

                Label subtitle = new Label(
                                "Capture debits and credits while monitoring balance status in real time.");
                subtitle.getStyleClass().add("journal-entry-subtitle");
                subtitle.setStyle("-fx-text-fill: -fx-text-base-color;");

                VBox box = new VBox(4, heading, subtitle);
                box.getStyleClass().add("journal-entry-header");
                return box;
        }

        /**
         * Builds the content.
         *
         * @return the node
         */
        private Node buildContent()
        {
                HBox container = new HBox(18);
                container.setAlignment(Pos.TOP_LEFT);
                VBox leftColumn = new VBox(12, buildTableSection(), buildSupplementalSection());
                leftColumn.setAlignment(Pos.TOP_LEFT);
                VBox.setVgrow(leftColumn.getChildren().get(0), Priority.ALWAYS);
                container.getChildren().addAll(leftColumn, buildDetailsSection());
                HBox.setHgrow(container.getChildren().get(0), Priority.ALWAYS);
                return container;
        }

        /**
         * Builds the table section.
         *
         * @return the node
         */
        private Node buildTableSection()
        {
                VBox section = new VBox(10);
                section.setAlignment(Pos.TOP_LEFT);

                Label title = new Label("Entry Lines");
                title.setStyle("-fx-font-weight: bold;");

                ToolBar toolbar = new ToolBar(this.addLineButton, this.duplicateLineButton,
                                this.removeLineButton);
                toolbar.setStyle("-fx-background-color: transparent;");

                this.addLineButton.setGraphic(null);
                this.addLineButton.getStyleClass().add("btn-add-line");
                this.addLineButton.setOnAction(e -> addLine());

                this.duplicateLineButton.setOnAction(e -> duplicateSelectedLine());
                this.removeLineButton.setOnAction(e -> removeSelectedLines());

                configureTable();
                VBox.setVgrow(this.table, Priority.ALWAYS);

                section.getChildren().addAll(title, toolbar, this.table);
                return section;
        }

        /**
         * Builds the supplemental section.
         *
         * @return the node
         */
        private Node buildSupplementalSection()
        {
                VBox section = new VBox(8);
                section.setAlignment(Pos.TOP_LEFT);

                Label title = new Label("Supplemental Schedules");
                title.setStyle("-fx-font-weight: bold;");

                section.getChildren().addAll(title, this.supplementalTabs);
                VBox.setVgrow(this.supplementalTabs, Priority.ALWAYS);

                return section;
        }

        /**
         * Builds the details section.
         *
         * @return the node
         */
        private Node buildDetailsSection()
        {
                VBox section = new VBox(12);
                section.setAlignment(Pos.TOP_LEFT);
                section.setPrefWidth(300);

                Label title = new Label("Details");
                title.setStyle("-fx-font-weight: bold;");

                GridPane grid = new GridPane();
                grid.setHgap(10);
                grid.setVgap(8);

                int row = 0;
                addDetailField(grid, row++, "Date", this.datePicker);
                addDetailField(grid, row++, "Memo", this.memoArea);
                addDetailField(grid, row++, "To / From", this.toFromField);
                addDetailField(grid, row++, "Check #", this.checkNumberField);
                addDetailField(grid, row++, "Clearing Bank", this.clearBankField);
                addDetailField(grid, row++, "Budget Tracking", this.budgetTrackingField);
                addDetailField(grid, row++, "Fund Name", this.associatedFundNameField);

                VBox.setVgrow(grid, Priority.ALWAYS);
                section.getChildren().addAll(title, grid);
                return section;
        }

        /**
         * Builds the footer.
         *
         * @return the node
         */
        private Node buildFooter()
        {
                HBox footer = new HBox(20);
                footer.setAlignment(Pos.CENTER_RIGHT);
                footer.setPadding(new Insets(12, 0, 0, 0));

                VBox totals = new VBox(6);
                totals.setAlignment(Pos.CENTER_LEFT);

                HBox row1 = new HBox(12, labelledValue("Debit", this.debitTotalLabel),
                                labelledValue("Credit", this.creditTotalLabel));
                HBox row2 = new HBox(12, labelledValue("Difference", this.differenceLabel),
                                this.statusBadge);
                row1.setAlignment(Pos.CENTER_LEFT);
                row2.setAlignment(Pos.CENTER_LEFT);

                this.statusBadge.getStyleClass().add("status-badge");
                this.statusBadge.setPadding(new Insets(4, 12, 4, 12));
                this.statusBadge.setStyle("-fx-background-color: #666; -fx-text-fill: white; -fx-background-radius: 12;");

                this.validationMessage.setWrapText(true);
                this.validationMessage.setStyle("-fx-text-fill: #cc3300;");

                totals.getChildren().addAll(row1, row2, this.validationMessage);

                Region spacer = new Region();
                HBox.setHgrow(spacer, Priority.ALWAYS);

                this.saveButton.setDefaultButton(true);
                this.saveButton.setOnAction(e -> persist());

                footer.getChildren().addAll(totals, spacer, this.saveButton);
                return footer;
        }

        /**
         * Labelled value.
         *
         * @param labelText the label text
         * @param valueLabel the value label
         * @return the h box
         */
        private static HBox labelledValue(String labelText, Label valueLabel)
        {
                Label label = new Label(labelText + ":");
                label.setStyle("-fx-font-weight: bold;");
                HBox box = new HBox(6, label, valueLabel);
                box.setAlignment(Pos.CENTER_LEFT);
                return box;
        }

        /**
         * Adds the detail field.
         *
         * @param grid the grid
         * @param row the row
         * @param labelText the label text
         * @param field the field
         */
        private void addDetailField(GridPane grid, int row, String labelText, Node field)
        {
                Label label = new Label(labelText);
                label.setStyle("-fx-font-weight: bold;");
                grid.add(label, 0, row);
                grid.add(field, 1, row);
                GridPane.setHgrow(field, Priority.ALWAYS);

                if (field instanceof TextArea ta)
                {
                        ta.setPrefRowCount(3);
                }

                if (field instanceof ComboBox<?> combo)
                {
                        combo.setMaxWidth(Double.MAX_VALUE);
                }

                if (field instanceof Region region)
                {
                        region.setMaxWidth(Double.MAX_VALUE);
                }
        }

        /**
         * Configure table.
         */
        private void configureTable()
        {
                this.table.setEditable(true);
                this.table.setColumnResizePolicy(
                                TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);

                TableColumn<Line, String> accountCol = new TableColumn<>("Account");
                accountCol.setCellValueFactory(cd -> cd.getValue().account);
                accountCol.setCellFactory(accountCellFactory());
                accountCol.setOnEditCommit(ev -> {
                        Line row = ev.getRowValue();
                        row.account.set(ev.getNewValue());
                        adjustForAccountSide(row);
                        refreshAfterEdit();
                });

                TableColumn<Line, BigDecimal> debitCol = amtCol("Debit", l -> l.debit);
                TableColumn<Line, BigDecimal> creditCol = amtCol("Credit", l -> l.credit);

                debitCol.setOnEditCommit(ev -> {
                        Line row = ev.getRowValue();
                        row.debit.set(amountOrZero(ev.getNewValue()));
                        if (amountOrZero(row.debit.get()).signum() != 0)
                        {
                                row.credit.set(BigDecimal.ZERO);
                        }
                        adjustForAccountSide(row);
                        refreshAfterEdit();
                });

                creditCol.setOnEditCommit(ev -> {
                        Line row = ev.getRowValue();
                        row.credit.set(amountOrZero(ev.getNewValue()));
                        if (amountOrZero(row.credit.get()).signum() != 0)
                        {
                                row.debit.set(BigDecimal.ZERO);
                        }
                        adjustForAccountSide(row);
                        refreshAfterEdit();
                });

                this.table.getColumns().setAll(accountCol, debitCol, creditCol);
                this.table.setRowFactory(tv -> {
                        TableRow<Line> row = new TableRow<>();
                        row.setOnMouseClicked(e -> {

                                if (e.getClickCount() == 1 && !row.isEmpty())
                                {
                                        this.table.edit(row.getIndex(), accountCol);
                                }

                        });
                        return row;
                });
        }

        /**
         * Account cell factory.
         *
         * @return the callback
         */
        private Callback<TableColumn<Line, String>, TableCell<Line, String>> accountCellFactory()
        {
                ObservableList<String> choices = FXCollections.observableArrayList(
                                this.accountsByName.keySet());

                StringConverter<String> converter = new StringConverter<>()
                {
                        @Override public String toString(String object)
                        {
                                return object;
                        }

                        @Override public String fromString(String string)
                        {
                                return string;
                        }
                };

                return column -> new ComboBoxTableCell<>(converter, choices)
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
                                Account account = line != null ? resolveAccount(line.account.get())
                                                : resolveAccount(item);

                                boolean highlight = line != null && account == null
                                                && (amountOrZero(line.debit.get()).signum() != 0
                                                                || amountOrZero(line.credit.get()).signum() != 0);

                                if (highlight)
                                {
                                        setStyle("-fx-background-color: rgba(204, 51, 0, 0.25);");
                                        setTooltip(new Tooltip(
                                                        "Account not found in the chart of accounts"));
                                }
                                else
                                {
                                        setStyle("");
                                        setTooltip(null);
                                }
                        }
                };
        }

        /**
         * Amt col.
         *
         * @param title the title
         * @param prop the prop
         * @return the table column
         */
        private TableColumn<Line, BigDecimal> amtCol(String title,
                        Callback<Line, Property<BigDecimal>> prop)
        {
                TableColumn<Line, BigDecimal> c = new TableColumn<>(title);
                c.setCellValueFactory(cell -> prop.call(cell.getValue()));
                c.setCellFactory(param -> new FocusCommitTextFieldTableCell<>(
                                createCurrencyConverter()));
                c.setEditable(true);
                c.setStyle("-fx-alignment: CENTER-RIGHT;");
                return c;
        }

        /**
         * Creates the currency converter.
         *
         * @return the string converter
         */
        private static StringConverter<BigDecimal> createCurrencyConverter()
        {
                return new StringConverter<>()
                {
                        @Override public String toString(BigDecimal value)
                        {
                                if (value == null)
                                {
                                        return "";
                                }
                                return FormatUtils.formatCurrency(value);
                        }

                        @Override public BigDecimal fromString(String value)
                        {
                                if (value == null || value.isBlank())
                                {
                                        return null;
                                }
                                BigDecimal parsed = FormatUtils.parseCurrency(value);
                                if (parsed == null)
                                {
                                        throw new IllegalArgumentException(
                                                        "Invalid currency amount: " + value);
                                }
                                return parsed;
                        }
                };
        }

        /**
         * Attach listeners.
         */
        private void attachListeners()
        {
                this.lines.addListener((ListChangeListener<Line>) change -> {
                        while (change.next())
                        {
                                if (change.wasAdded())
                                {
                                        change.getAddedSubList().forEach(this::watchLine);
                                }
                        }
                        refreshAfterEdit();
                });

                this.memoArea.textProperty().addListener((obs, o, n) -> markValidationPending());
        }

        /**
         * Watch line.
         *
         * @param line the line
         */
        private void watchLine(Line line)
        {
                line.account.addListener((obs, o, n) -> {
                        adjustForAccountSide(line);
                        refreshAfterEdit();
                });
                line.debit.addListener((obs, o, n) -> refreshAfterEdit());
                line.credit.addListener((obs, o, n) -> refreshAfterEdit());
        }

        /**
         * Refresh after edit.
         */
        private void refreshAfterEdit()
        {
                recalcTotals();
                markValidationPending();
                this.table.refresh();
                updateSupplementalTabAvailability();
        }

        /**
         * Load person refs.
         *
         * @return the list
         */
        private List<PersonRef> loadPersonRefs()
        {
                if (!Database.isInitialized())
                {
                        return List.of();
                }

                PersonService personService = new PersonService();
                List<PersonRef> refs = new ArrayList<>();
                for (Person person : personService.listPeople())
                {
                        refs.add(new PersonRef(person.getId(), person.getName()));
                }
                return refs;
        }

        /**
         * Load entry refs.
         *
         * @param txnId the txn id
         * @return the list
         */
        private List<EntryRef> loadEntryRefs(long txnId)
        {
                if (!Database.isInitialized() || txnId <= 0)
                {
                        return List.of();
                }

                String sql =
                        "SELECT id, amount, account_name, account_number, account_side " +
                        "FROM journal_entry WHERE txn_id = ? ORDER BY id";

                List<EntryRef> refs = new ArrayList<>();

                try (Connection c = Database.get().getConnection();
                        PreparedStatement ps = c.prepareStatement(sql))
                {
                        ps.setLong(1, txnId);
                        try (ResultSet rs = ps.executeQuery())
                        {
                                while (rs.next())
                                {
                                        String accountName = rs.getString("account_name");
                                        if (accountName == null || accountName.isBlank())
                                        {
                                                accountName = rs.getString("account_number");
                                        }
                                        AccountSide side = AccountSide.fromString(rs.getString("account_side"));
                                        boolean debit = side == AccountSide.DEBIT;
                                        refs.add(new EntryRef(
                                                rs.getLong("id"),
                                                accountName,
                                                debit,
                                                rs.getBigDecimal("amount")));
                                }
                        }
                }
                catch (SQLException ex)
                {
                        return Collections.emptyList();
                }

                return refs;
        }

        /**
         * Load entry amounts by id.
         *
         * @return the map
         */
        private Map<Long, BigDecimal> loadEntryAmountsById()
        {
                if (!Database.isInitialized())
                {
                        return Map.of();
                }

                Set<Long> entryIds = new LinkedHashSet<>();
                for (SupplementalLineKind kind : SupplementalLineKind.values())
                {
                        SupplementalLinesEditor editor = this.supplementalTabs.editor(kind);
                        if (editor == null)
                        {
                                continue;
                        }
                        for (SupplementalLineRow row : editor.getRows())
                        {
                                Long entryId = row.getEntryId();
                                if (entryId != null)
                                {
                                        entryIds.add(entryId);
                                }
                        }
                }

                if (entryIds.isEmpty())
                {
                        return Map.of();
                }

                String placeholders = entryIds.stream()
                        .map(id -> "?")
                        .collect(Collectors.joining(", "));
                String sql = "SELECT id, amount FROM journal_entry WHERE id IN (" + placeholders + ")";

                Map<Long, BigDecimal> amounts = new HashMap<>();
                try (Connection c = Database.get().getConnection();
                        PreparedStatement ps = c.prepareStatement(sql))
                {
                        int index = 1;
                        for (Long entryId : entryIds)
                        {
                                ps.setLong(index++, entryId);
                        }

                        try (ResultSet rs = ps.executeQuery())
                        {
                                while (rs.next())
                                {
                                        amounts.put(rs.getLong("id"), rs.getBigDecimal("amount"));
                                }
                        }
                }
                catch (SQLException ex)
                {
                        return Map.of();
                }

                return amounts;
        }

        /**
         * Update supplemental tab availability.
         */
        private void updateSupplementalTabAvailability()
        {
                Set<SupplementalLineKind> enabledKinds = EnumSet.noneOf(SupplementalLineKind.class);

                for (Line line : this.lines)
                {
                        Account account = resolveAccount(line.account.get());
                        if (account == null)
                        {
                                continue;
                        }
                        enabledKinds.addAll(kindsForAccount(account));
                }

                for (SupplementalLineKind kind : SupplementalLineKind.values())
                {
                        boolean enabled = enabledKinds.contains(kind);
                        this.supplementalTabs.setEnabled(kind, enabled);
                }
        }

        /**
         * Kinds for account.
         *
         * @param account the account
         * @return the sets the
         */
        private Set<SupplementalLineKind> kindsForAccount(Account account)
        {
                if (account == null || account.getSupplementalLineKinds() == null
                        || account.getSupplementalLineKinds().isEmpty())
                {
                        return EnumSet.noneOf(SupplementalLineKind.class);
                }
                return EnumSet.copyOf(account.getSupplementalLineKinds());
        }

        /**
         * Apply default accounts from settings.
         */
        private void applyDefaultAccountsFromSettings()
        {
                if (!Database.isInitialized())
                {
                        return;
                }

                SettingsService settingsService = new SettingsService();

                try
                {
                        settingsService.loadSettings(null);
                }
                catch (IOException ex)
                {
                        return;
                }

                SettingsModel settings = settingsService.getSettings();

                if (settings == null || this.lines.isEmpty())
                {
                        return;
                }

                if (settings.getDefaultExpenseAccount() != null
                        && !settings.getDefaultExpenseAccount().isBlank())
                {
                        this.lines.get(0).account.set(settings.getDefaultExpenseAccount());
                }

                if (settings.getDefaultIncomeAccount() != null
                        && !settings.getDefaultIncomeAccount().isBlank())
                {
                        if (this.lines.size() == 1)
                        {
                                addLine();
                        }

                        if (this.lines.size() >= 2)
                        {
                                this.lines.get(1).account.set(settings.getDefaultIncomeAccount());
                        }
                }

                if (!this.lines.isEmpty())
                {
                        this.table.getSelectionModel().select(this.lines.get(0));
                }
        }

        /**
         * Adds the line.
         */
        private void addLine()
        {
                Line line = new Line();
                this.lines.add(line);
                this.table.getSelectionModel().select(line);
                this.table.scrollTo(line);
        }

        /**
         * Duplicate selected line.
         */
        private void duplicateSelectedLine()
        {
                Line selected = this.table.getSelectionModel().getSelectedItem();

                if (selected == null)
                {
                        return;
                }

                Line copy = new Line();
                copy.account.set(selected.account.get());
                copy.debit.set(amountOrZero(selected.debit.get()));
                copy.credit.set(amountOrZero(selected.credit.get()));
                this.lines.add(this.lines.indexOf(selected) + 1, copy);
                this.table.getSelectionModel().select(copy);
        }

        /**
         * Removes the selected lines.
         */
        private void removeSelectedLines()
        {
                List<Line> selected = new ArrayList<>(this.table.getSelectionModel().getSelectedItems());

                if (selected.isEmpty())
                {
                        return;
                }

                this.lines.removeAll(selected);

                if (this.lines.isEmpty())
                {
                        addLine();
                }
        }

        /**
         * Recalc totals.
         */
        private void recalcTotals()
        {
                BigDecimal debit = BigDecimal.ZERO;
                BigDecimal credit = BigDecimal.ZERO;

                for (Line l : this.lines)
                {
                        debit = debit.add(amountOrZero(l.debit.get()));
                        credit = credit.add(amountOrZero(l.credit.get()));
                }

                BigDecimal diff = debit.subtract(credit);

                this.debitTotalLabel.setText(FormatUtils.formatCurrency(debit));
                this.creditTotalLabel.setText(FormatUtils.formatCurrency(credit));
                this.differenceLabel.setText(FormatUtils.formatCurrency(diff.abs()));
        }

        /**
         * Mark validation pending.
         */
        private void markValidationPending()
        {
                this.saveButton.setDisable(false);
                this.validationMessage.setText("Press Save to validate the entry totals.");
                this.saveButton.setTooltip(null);
                this.statusBadge.setText("Pending check");
                this.statusBadge.setStyle("-fx-background-color: #666666; -fx-text-fill: white; -fx-background-radius: 12;");
        }

        /**
         * Show validation error.
         *
         * @param message the message
         */
        private void showValidationError(String message)
        {
                this.saveButton.setDisable(false);
                this.validationMessage.setText(message);
                this.saveErrorTooltip.setText(message);
                this.saveButton.setTooltip(this.saveErrorTooltip);
                this.statusBadge.setText("Needs attention");
                this.statusBadge.setStyle("-fx-background-color: #cc3300; -fx-text-fill: white; -fx-background-radius: 12;");
        }

        /**
         * Show balanced state.
         */
        private void showBalancedState()
        {
                this.saveButton.setDisable(false);
                this.validationMessage.setText("");
                this.saveButton.setTooltip(null);
                this.statusBadge.setText("Balanced");
                this.statusBadge.setStyle("-fx-background-color: #2e7d32; -fx-text-fill: white; -fx-background-radius: 12;");
        }

        /**
         * Validate lines.
         *
         * @return the optional
         */
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

                        if (debitAmount.signum() != 0 && creditAmount.signum() != 0)
                        {
                                return Optional.of(
                                                "A line cannot contain both a debit and a credit amount.");
                        }

                        Account account = resolveAccount(accountToken);

                        if (account == null)
                        {
                                missingAccounts.add(accountToken);
                                continue;
                        }

                        hasAmounts |= hasValue;

                        if (debitAmount.signum() != 0)
                        {
                                debit = debit.add(debitAmount);
                        }

                        if (creditAmount.signum() != 0)
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

        /**
         * Validate supplemental lines.
         *
         * @return the optional
         */
        private Optional<String> validateSupplementalLines()
        {
                List<String> errors = new ArrayList<>();
                Map<Long, BigDecimal> entryAmounts = loadEntryAmountsById();

                for (SupplementalLineKind kind : SupplementalLineKind.values())
                {
                        SupplementalLinesEditor editor = this.supplementalTabs.editor(kind);
                        if (editor == null)
                        {
                                continue;
                        }
                        if (!editor.validateAndDisplay())
                        {
                                errors.add("Fix validation errors in the " + editor.getConfig().tabTitle
                                                + " tab.");
                        }

                        if (!entryAmounts.isEmpty())
                        {
                                errors.addAll(editor.validateEntryLinkSums(entryAmounts::get));
                        }

                        BigDecimal expected = expectedAmountForKind(kind);
                        BigDecimal actual = sumSupplementalAmount(kind);
                        if (expected.signum() != 0 || actual.signum() != 0)
                        {
                                if (expected.compareTo(actual) != 0)
                                {
                                        errors.add(editor.getConfig().tabTitle + " total "
                                                + actual + " must match entry total " + expected + ".");
                                }
                        }
                }

                if (errors.isEmpty())
                {
                        return Optional.empty();
                }
                return Optional.of(String.join("\n", errors));
        }

        /**
         * Expected amount for kind.
         *
         * @param kind the kind
         * @return the big decimal
         */
        private BigDecimal expectedAmountForKind(SupplementalLineKind kind)
        {
                BigDecimal total = BigDecimal.ZERO;
                for (Line line : this.lines)
                {
                        Account account = resolveAccount(line.account.get());
                        if (account == null)
                        {
                                continue;
                        }
                        if (!kindsForAccount(account).contains(kind))
                        {
                                continue;
                        }

                        BigDecimal debitAmount = amountOrZero(line.debit.get());
                        BigDecimal creditAmount = amountOrZero(line.credit.get());

                        if (isAssetKind(kind))
                        {
                                total = total.add(debitAmount);
                        }
                        else
                        {
                                total = total.add(creditAmount);
                        }
                }
                return total;
        }

        /**
         * Sum supplemental amount.
         *
         * @param kind the kind
         * @return the big decimal
         */
        private BigDecimal sumSupplementalAmount(SupplementalLineKind kind)
        {
                SupplementalLinesEditor editor = this.supplementalTabs.editor(kind);
                if (editor == null)
                {
                        return BigDecimal.ZERO;
                }
                BigDecimal total = BigDecimal.ZERO;
                for (SupplementalLineRow row : editor.getRows())
                {
                        BigDecimal amount = row.getAmount();
                        if (amount != null)
                        {
                                total = total.add(amount);
                        }
                }
                return total;
        }

        /**
         * Checks if is asset kind.
         *
         * @param kind the kind
         * @return true, if is asset kind
         */
        private boolean isAssetKind(SupplementalLineKind kind)
        {
                return kind == SupplementalLineKind.RECEIVABLE
                        || kind == SupplementalLineKind.PREPAID_EXPENSE
                        || kind == SupplementalLineKind.OTHER_ASSET;
        }

        /**
         * Collect supplemental lines.
         *
         * @return the list
         */
        private List<TxnSupplementalLineBase> collectSupplementalLines()
        {
                List<TxnSupplementalLineBase> beans = new ArrayList<>();
                for (SupplementalLineKind kind : SupplementalLineKind.values())
                {
                        SupplementalLinesEditor editor = this.supplementalTabs.editor(kind);
                        if (editor == null)
                        {
                                continue;
                        }
                        beans.addAll(SupplementalLinesFxAdapter.toBeans(kind,
                                new ArrayList<>(editor.getRows())));
                }
                return beans;
        }

        /**
         * Load supplemental lines.
         *
         * @param tx the tx
         */
        private void loadSupplementalLines(AccountingTransaction tx)
        {
                Map<SupplementalLineKind, List<SupplementalLineRow>> grouped =
                        new EnumMap<>(SupplementalLineKind.class);
                for (SupplementalLineKind kind : SupplementalLineKind.values())
                {
                        grouped.put(kind, new ArrayList<>());
                }

                if (tx.getSupplementalLines() != null)
                {
                        for (TxnSupplementalLineBase line : tx.getSupplementalLines())
                        {
                                grouped.get(line.getKind()).add(
                                        SupplementalLinesFxAdapter.toRow(line));
                        }
                }

                for (SupplementalLineKind kind : SupplementalLineKind.values())
                {
                        SupplementalLinesEditor editor = this.supplementalTabs.editor(kind);
                        if (editor != null)
                        {
                                editor.setRows(grouped.get(kind));
                        }
                }
        }

        /**
         * Adjust for account side.
         *
         * @param line the line
         */
        private void adjustForAccountSide(Line line)
        {
                Account account = resolveAccount(line.account.get());

                if (account == null)
                {
                        return;
                }

                BigDecimal debitAmount = amountOrZero(line.debit.get());
                BigDecimal creditAmount = amountOrZero(line.credit.get());

                if (account.getIncreaseSide() == AccountSide.DEBIT && creditAmount.signum() != 0
                                && debitAmount.signum() == 0)
                {
                        line.debit.set(creditAmount);
                        line.credit.set(BigDecimal.ZERO);
                }
                else if (account.getIncreaseSide() == AccountSide.CREDIT && debitAmount.signum() != 0
                                && creditAmount.signum() == 0)
                {
                        line.credit.set(debitAmount);
                        line.debit.set(BigDecimal.ZERO);
                }
        }

        /**
         * Persist.
         */
        private void persist()
        {
                Optional<String> validationError = validateLines();

                if (validationError.isPresent())
                {
                        showValidationError(validationError.get());
                        AlertBox.showError(getScene() == null ? null : getScene().getWindow(),
                                        validationError.get());
                        return;
                }

                Optional<String> supplementalError = validateSupplementalLines();
                if (supplementalError.isPresent())
                {
                        AlertBox.showError(getScene() == null ? null : getScene().getWindow(),
                                        supplementalError.get());
                        return;
                }

                showBalancedState();

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

                        if (debitAmount.signum() != 0)
                        {
                                entries.add(new AccountingEntry(debitAmount, acctNum,
                                                        AccountSide.DEBIT, acctName));
                                debit = debit.add(debitAmount);
                        }

                        if (creditAmount.signum() != 0)
                        {
                                entries.add(new AccountingEntry(creditAmount, acctNum,
                                                        AccountSide.CREDIT, acctName));
                                credit = credit.add(creditAmount);
                        }
                }

                AccountingTransaction tx = new AccountingTransaction(new Account(), entries, Map.of(),
                                this.original != null ? this.original.getBookingDateTimestamp()
                                                : Instant.now().toEpochMilli());

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
                tx.setSupplementalLines(collectSupplementalLines());

                this.onSave.accept(tx);
        }

        /**
         * Load from transaction.
         *
         * @param tx the tx
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

                if (tx.getEntries() != null)
                {
                        for (AccountingEntry entry : tx.getEntries())
                        {
                                Line line = new Line();
                                Account account = this.chartOfAccounts.getAccount(entry.getAccountNumber());
                                line.account.set(account != null ? account.getName() : entry.getAccountNumber());

                                if (entry.getAccountSide() == AccountSide.DEBIT)
                                {
                                        line.debit.set(entry.getAmount());
                                }
                                else
                                {
                                        line.credit.set(entry.getAmount());
                                }

                                this.lines.add(line);
                        }
                }

                if (this.lines.isEmpty())
                {
                        addLine();
                }

                this.supplementalTabs.setEntryRefs(loadEntryRefs(tx.getId()));
                loadSupplementalLines(tx);
                updateSupplementalTabAvailability();
        }

        /**
         * Resolve account.
         *
         * @param token the token
         * @return the account
         */
        private Account resolveAccount(String token)
        {
                if (token == null || token.isBlank())
                {
                        return null;
                }

                Account byName = this.accountsByName.get(token);

                if (byName != null)
                {
                                return byName;
                }

                return this.chartOfAccounts.getAccount(token);
        }

        /**
         * Resolve chart of accounts.
         *
         * @return the chart of accounts
         */
        private static ChartOfAccounts resolveChartOfAccounts()
        {
                Company company = CurrentCompany.getCompany();

                if (company == null)
                {
                        throw new IllegalStateException(
                                        "JournalEntryWorkspaceFX requires an open company");
                }

                ChartOfAccounts chart = company.getChartOfAccounts();

                if (chart == null)
                {
                        throw new IllegalStateException(
                                        "Current company does not have a chart of accounts loaded");
                }

                return chart;
        }

        /**
         * Builds the accounts by name.
         *
         * @param chart the chart
         * @return the map
         */
        private static Map<String, Account> buildAccountsByName(ChartOfAccounts chart)
        {
                return chart.createAccountNumberMap().asMap().values().stream().collect(
                                Collectors.toMap(Account::getName, a -> a, (a, b) -> a,
                                                LinkedHashMap::new));
        }

        /**
         * Amount or zero.
         *
         * @param value the value
         * @return the big decimal
         */
        private static BigDecimal amountOrZero(BigDecimal value)
        {
                return value != null ? value : BigDecimal.ZERO;
        }
}
