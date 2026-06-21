package nonprofitbookkeeping.ui;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import nonprofitbookkeeping.persistence.records.GenericRecordCrudService;
import nonprofitbookkeeping.persistence.records.RecordSchemaService;
import nonprofitbookkeeping.persistence.records.TableColumnMetadata;
import nonprofitbookkeeping.service.PreferencesService;
import org.nonprofitbookkeeping.ui.AppPanel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.sql.Types;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

/**
 * Generic dynamic record editor panel backed by {@link GenericRecordCrudService}.
 */
public class GenericRecordEditorPanel implements AppPanel
{
    private static final Logger LOG = LoggerFactory.getLogger(GenericRecordEditorPanel.class);
    private static final String PENDING_ROW_CLASS = "pending-row";
    private static final String PENDING_ROW_TEXT_DARK_CLASS =
        "pending-row-text-dark";

    private final BorderPane root = new BorderPane();
    private final TableView<Map<String, Object>> table = new TableView<>();
    private final Label status = new Label("Ready");
    private final ScrollPane tableContainer = new ScrollPane();
    private final String panelTitle;
    private final String tableName;
    private final String primaryKeyColumn;
    private final Supplier<String> idSupplier;
    private final Set<String> hiddenColumnNames;
    private final RecordSchemaService schemaService;
    private final GenericRecordCrudService crudService;
    private final Set<Map<String, Object>> pendingNewRows = Collections.newSetFromMap(new IdentityHashMap<>());
    private final Map<String, Double> selectedColumnWidths = new LinkedHashMap<>();
    private final ObservableList<String> fundChoices = FXCollections.observableArrayList();
    private List<TableColumnMetadata> columns = List.of();

    public GenericRecordEditorPanel(String panelTitle, String tableName, String primaryKeyColumn, Supplier<String> idSupplier)
    {
        this(panelTitle, tableName, primaryKeyColumn, idSupplier, Set.of());
    }

    public GenericRecordEditorPanel(
        String panelTitle,
        String tableName,
        String primaryKeyColumn,
        Supplier<String> idSupplier,
        Set<String> hiddenColumnNames
    )
    {
        this(
            panelTitle,
            tableName,
            primaryKeyColumn,
            idSupplier,
            hiddenColumnNames,
            new RecordSchemaService(),
            new GenericRecordCrudService(new RecordSchemaService())
        );
    }

    GenericRecordEditorPanel(
        String panelTitle,
        String tableName,
        String primaryKeyColumn,
        Supplier<String> idSupplier,
        Set<String> hiddenColumnNames,
        RecordSchemaService schemaService,
        GenericRecordCrudService crudService
    )
    {
        this.panelTitle = panelTitle;
        this.tableName = tableName;
        this.primaryKeyColumn = primaryKeyColumn;
        this.idSupplier = idSupplier;
        this.hiddenColumnNames = normalizeHiddenColumns(hiddenColumnNames);
        this.schemaService = schemaService;
        this.crudService = crudService;

        this.root.setPadding(UiSpacing.pageInsets());
        Label title = new Label(panelTitle);
        title.getStyleClass().add("journal-entry-heading");

        Button add = new Button("+ Add Row");
        Button delete = new Button("Delete Selected");
        Button refresh = new Button("Refresh");
        Button save = new Button("Save");
        HBox actions = new HBox(UiSpacing.SECTION_SPACING, add, delete, refresh, save);
        this.root.setTop(new VBox(UiSpacing.SECTION_SPACING, title, actions, new Separator()));

        this.table.setEditable(true);
        this.table.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);
        this.table.setRowFactory(tv -> new TableRow<>()
        {
            @Override
            protected void updateItem(Map<String, Object> item, boolean empty)
            {
                super.updateItem(item, empty);
                getStyleClass().remove(PENDING_ROW_CLASS);
                if (empty || item == null)
                {
                    return;
                }
                if (GenericRecordEditorPanel.this.pendingNewRows.contains(item))
                {
                    getStyleClass().add(PENDING_ROW_CLASS);
                }
            }
        });
        this.tableContainer.setContent(this.table);
        this.tableContainer.setFitToWidth(true);
        this.tableContainer.setFitToHeight(true);
        this.tableContainer.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        this.tableContainer.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
        this.root.setCenter(this.tableContainer);
        this.root.setBottom(new VBox(new Separator(), this.status));

        add.setOnAction(e -> onAddRow());
        delete.setOnAction(e -> onDeleteSelected());
        refresh.setOnAction(e -> loadFromService());
        save.setOnAction(e -> onSave());
        loadFromService();
    }

    @Override
    public String title()
    {
        return this.panelTitle;
    }

    @Override
    public Node root()
    {
        return this.root;
    }

    @Override
    public void onSave()
    {
        try
        {
            int saved = 0;
            for (Map<String, Object> row : this.table.getItems())
            {
                this.crudService.upsert(this.tableName, toTypedRow(row));
                saved++;
            }
            this.status.setText("Saved " + saved + " row(s)");
            loadFromService();
        }
        catch (IllegalArgumentException ex)
        {
            this.status.setText("Validation error: " + ex.getMessage());
        }
        catch (SQLException | RuntimeException ex)
        {
            LOG.warn("Generic record save failed for table {}", this.tableName, ex);
            this.status.setText("Failed to save rows: " + ex.getMessage());
        }
    }

    private void onAddRow()
    {
        Map<String, Object> row = new LinkedHashMap<>();
        for (TableColumnMetadata column : this.columns)
        {
            row.put(column.columnName(), null);
        }
        if (this.primaryKeyColumn != null && !this.primaryKeyColumn.isBlank())
        {
            row.put(this.primaryKeyColumn, this.idSupplier == null ? null : this.idSupplier.get());
        }
        this.table.getItems().add(row);
        this.pendingNewRows.add(row);
        this.table.refresh();
        this.table.getSelectionModel().select(row);
        this.table.scrollTo(row);
        this.status.setText("Added new unsaved row. Press Save to persist.");
    }

    @Override
    public void onDelete()
    {
        onDeleteSelected();
    }

    @Override
    public void onCancel()
    {
        loadFromService();
    }

    private void onDeleteSelected()
    {
        Map<String, Object> selected = this.table.getSelectionModel().getSelectedItem();
        if (selected == null)
        {
            this.status.setText("Select a row to delete.");
            return;
        }

        if (this.pendingNewRows.contains(selected))
        {
            this.pendingNewRows.remove(selected);
            this.table.getItems().remove(selected);
            this.table.refresh();
            this.status.setText("Removed unsaved row.");
            return;
        }

        try
        {
            Map<String, Object> pk = primaryKeyValues(selected);
            int deleted = this.crudService.deleteByPrimaryKey(this.tableName, pk);
            this.table.getItems().remove(selected);
            this.table.refresh();
            this.status.setText(deleted > 0 ? "Deleted selected row." : "Removed unsaved row.");
        }
        catch (IllegalArgumentException ex)
        {
            this.status.setText("Validation error: " + ex.getMessage());
        }
        catch (SQLException | RuntimeException ex)
        {
            LOG.warn("Generic record delete failed for table {}", this.tableName, ex);
            this.status.setText("Failed to delete row: " + ex.getMessage());
        }
    }

    private void loadFromService()
    {
        try
        {
            this.columns = this.schemaService.columnsForTable(this.tableName).stream()
                .sorted((left, right) ->
                {
                    if (left.nullable() != right.nullable())
                    {
                        return left.nullable() ? 1 : -1;
                    }
                    return Integer.compare(left.ordinalPosition(), right.ordinalPosition());
                })
                .toList();
            configureColumns();
            List<Map<String, Object>> rows = new ArrayList<>(this.crudService.listAll(this.tableName));
            this.pendingNewRows.clear();
            this.table.setItems(FXCollections.observableArrayList(rows));
            this.table.refresh();
            ensureVerticalScrollBarVisible();
            this.status.setText("Loaded " + rows.size() + " row(s)");
        }
        catch (SQLException | RuntimeException ex)
        {
            LOG.warn("Generic record load failed for table {}", this.tableName, ex);
            this.status.setText("Failed to load rows: " + ex.getMessage());
        }
    }

    private void configureColumns()
    {
        captureColumnWidths();
        this.table.getColumns().clear();
        for (TableColumnMetadata column : this.columns)
        {
            String columnName = column.columnName();
            if (isHiddenColumn(columnName))
            {
                continue;
            }
            String displayTitle = toDisplayTitle(columnName);
            String headerTitle = column.nullable() ? displayTitle : displayTitle + " *";
            TableColumn<Map<String, Object>, String> tableColumn = new TableColumn<>(headerTitle);
            tableColumn.setUserData(columnName);
            tableColumn.setMinWidth(minWidthForTitle(headerTitle));
            tableColumn.setPrefWidth(this.selectedColumnWidths.getOrDefault(columnName, tableColumn.getMinWidth()));
            tableColumn.setCellValueFactory(cell -> new SimpleStringProperty(toDisplay(cell.getValue().get(columnName))));
            tableColumn.setCellFactory(col -> isFundNameColumn(columnName) ? new FundNameComboBoxCell(columnName) : new CommitOnFocusLossCell());
            tableColumn.setOnEditCommit(event ->
            {
                Map<String, Object> row = event.getRowValue();
                String value = event.getNewValue();
                row.put(columnName, value);
                this.status.setText("Updated " + displayTitle + " (pending save)");
            });
            this.table.getColumns().add(tableColumn);
        }
    }

    private void captureColumnWidths()
    {
        for (TableColumn<Map<String, Object>, ?> tableColumn : this.table.getColumns())
        {
            Object key = tableColumn.getUserData();
            if (key instanceof String columnName && !columnName.isBlank())
            {
                this.selectedColumnWidths.put(columnName, tableColumn.getWidth());
            }
        }
    }

    private void ensureVerticalScrollBarVisible()
    {
        Platform.runLater(() -> {
            Node node = this.table.lookup(".scroll-bar:vertical");
            if (node instanceof ScrollBar scrollBar)
            {
                scrollBar.setVisible(true);
                scrollBar.setManaged(true);
                scrollBar.setOpacity(1.0);
                scrollBar.setMaxWidth(12);
            }
        });
    }

    private void refreshFundChoices()
    {
        try
        {
            this.fundChoices.setAll(FundNameLookup.listActiveFundNames());
        }
        catch (SQLException ex)
        {
            this.fundChoices.clear();
            this.status.setText("Unable to load fund choices: " + ex.getMessage());
        }
    }

    private static boolean isFundNameColumn(String columnName)
    {
        if (columnName == null)
        {
            return false;
        }
        String normalized = columnName.trim().toLowerCase();
        return "fund_name".equals(normalized) || "associated_fund_name".equals(normalized);
    }

    private final class FundNameComboBoxCell extends TableCell<Map<String, Object>, String>
    {
        private final String columnName;
        private ComboBox<String> comboBox;

        private FundNameComboBoxCell(String columnName)
        {
            this.columnName = columnName;
        }

        @Override
        public void startEdit()
        {
            if (!isEmpty())
            {
                super.startEdit();
                createComboBox();
                setText(null);
                setGraphic(this.comboBox);
                this.comboBox.requestFocus();
                this.comboBox.show();
            }
        }

        @Override
        public void cancelEdit()
        {
            super.cancelEdit();
            setText(getItem());
            setGraphic(null);
        }

        @Override
        protected void updateItem(String item, boolean empty)
        {
            super.updateItem(item, empty);
            if (empty)
            {
                setText(null);
                setGraphic(null);
                getStyleClass().remove(PENDING_ROW_TEXT_DARK_CLASS);
            }
            else if (isEditing())
            {
                if (this.comboBox != null)
                {
                    this.comboBox.setValue(item);
                }
                setText(null);
                setGraphic(this.comboBox);
                applyPendingRowTextStyle();
            }
            else
            {
                setText(item);
                setGraphic(null);
                applyPendingRowTextStyle();
            }
        }

        @Override
        public void commitEdit(String value)
        {
            super.commitEdit(value);
            Map<String, Object> row = getTableRow() == null ? null : getTableRow().getItem();
            if (row != null)
            {
                row.put(this.columnName, value);
                GenericRecordEditorPanel.this.status.setText("Updated " + toDisplayTitle(this.columnName) + " (pending save)");
            }
        }

        private void createComboBox()
        {
            refreshFundChoices();
            this.comboBox = new ComboBox<>(GenericRecordEditorPanel.this.fundChoices);
            this.comboBox.setMinWidth(this.getWidth() - this.getGraphicTextGap() * 2);
            this.comboBox.setValue(getItem());
            this.comboBox.setOnAction(event -> commitEdit(this.comboBox.getValue()));
            this.comboBox.focusedProperty().addListener((obs, oldValue, hasFocus) -> {
                if (!hasFocus)
                {
                    commitEdit(this.comboBox.getValue());
                }
            });
        }

        private void applyPendingRowTextStyle()
        {
            TableRow<Map<String, Object>> row = getTableRow();
            getStyleClass().remove(PENDING_ROW_TEXT_DARK_CLASS);
            if (row != null && GenericRecordEditorPanel.this.pendingNewRows.contains(row.getItem()))
            {
                String preference = PreferencesService.getPendingRowTextColorPreference();
                if (preference == null || preference.isBlank() || "black".equalsIgnoreCase(preference))
                {
                    getStyleClass().add(PENDING_ROW_TEXT_DARK_CLASS);
                }
            }
        }
    }

    private final class CommitOnFocusLossCell extends TableCell<Map<String, Object>, String>
    {
        private TextField textField;

        @Override
        public void startEdit()
        {
            if (!isEmpty())
            {
                super.startEdit();
                createTextField();
                setText(null);
                setGraphic(this.textField);
                this.textField.selectAll();
                this.textField.requestFocus();
            }
        }

        @Override
        public void cancelEdit()
        {
            super.cancelEdit();
            setText(getItem());
            setGraphic(null);
        }

        @Override
        protected void updateItem(String item, boolean empty)
        {
            super.updateItem(item, empty);
            if (empty)
            {
                setText(null);
                setGraphic(null);
                getStyleClass().remove(PENDING_ROW_TEXT_DARK_CLASS);
            }
            else if (isEditing())
            {
                if (this.textField != null)
                {
                    this.textField.setText(item);
                }
                setText(null);
                setGraphic(this.textField);
                applyPendingRowTextStyle();
            }
            else
            {
                setText(item);
                setGraphic(null);
                applyPendingRowTextStyle();
            }
        }

        private void createTextField()
        {
            this.textField = new TextField(getItem());
            this.textField.setMinWidth(this.getWidth() - this.getGraphicTextGap() * 2);
            this.textField.setOnAction(event -> commitEdit(this.textField.getText()));
            this.textField.focusedProperty().addListener((obs, oldValue, hasFocus) -> {
                if (!hasFocus)
                {
                    commitEdit(this.textField.getText());
                }
            });
        }

        private void applyPendingRowTextStyle()
        {
            TableRow<Map<String, Object>> row = getTableRow();
            getStyleClass().remove(PENDING_ROW_TEXT_DARK_CLASS);
            if (row != null && GenericRecordEditorPanel.this.pendingNewRows.contains(row.getItem()))
            {
                if (isPendingRowTextBlack())
                {
                    getStyleClass().add(PENDING_ROW_TEXT_DARK_CLASS);
                }
            }
        }

        private boolean isPendingRowTextBlack()
        {
            String preference =
                PreferencesService.getPendingRowTextColorPreference();
            return preference == null || preference.isBlank() ||
                "black".equalsIgnoreCase(preference);
        }
    }

    private Map<String, Object> primaryKeyValues(Map<String, Object> row)
    {
        Map<String, Object> pk = new LinkedHashMap<>();
        for (TableColumnMetadata column : this.columns)
        {
            if (column.primaryKey())
            {
                Object raw = row.get(column.columnName());
                pk.put(column.columnName(), convertValue(raw, column, toDisplayTitle(column.columnName())));
            }
        }
        return pk;
    }

    private Map<String, Object> toTypedRow(Map<String, Object> row)
    {
        Map<String, Object> typed = new LinkedHashMap<>();
        for (TableColumnMetadata column : this.columns)
        {
            Object raw = row.get(column.columnName());
            typed.put(column.columnName(), convertValue(raw, column, toDisplayTitle(column.columnName())));
        }
        return typed;
    }

    private Object convertValue(Object raw, TableColumnMetadata column, String fieldName)
    {
        if (raw == null)
        {
            if (!column.nullable())
            {
                throw new IllegalArgumentException("Required field is blank: " + fieldName);
            }
            return null;
        }
        if (!(raw instanceof String value))
        {
            return raw;
        }
        String trimmed = value.trim();
        if (trimmed.isEmpty())
        {
            if (!column.nullable())
            {
                throw new IllegalArgumentException("Required field is blank: " + fieldName);
            }
            return null;
        }

        try
        {
            return switch (column.jdbcType())
            {
                case Types.INTEGER, Types.SMALLINT, Types.TINYINT -> Integer.parseInt(trimmed);
                case Types.BIGINT -> Long.parseLong(trimmed);
                case Types.DECIMAL, Types.NUMERIC -> new BigDecimal(trimmed);
                case Types.BOOLEAN, Types.BIT -> parseBoolean(trimmed, fieldName);
                case Types.DATE -> LocalDate.parse(trimmed);
                default -> trimmed;
            };
        }
        catch (NumberFormatException | DateTimeParseException ex)
        {
            throw new IllegalArgumentException("Invalid value for " + fieldName + ": " + value);
        }
    }

    private boolean parseBoolean(String raw, String fieldName)
    {
        if ("true".equalsIgnoreCase(raw) || "false".equalsIgnoreCase(raw))
        {
            return Boolean.parseBoolean(raw);
        }
        throw new IllegalArgumentException("Invalid boolean for " + fieldName + ": " + raw);
    }

    private String toDisplay(Object value)
    {
        return value == null ? "" : String.valueOf(value);
    }


    private String toDisplayTitle(String columnName)
    {
        if (columnName == null || columnName.isBlank())
        {
            return "";
        }
        String[] words = columnName.trim().split("[_\\s]+");
        StringBuilder title = new StringBuilder();
        for (String word : words)
        {
            if (word.isBlank())
            {
                continue;
            }
            if (!title.isEmpty())
            {
                title.append(' ');
            }
            String lower = word.toLowerCase();
            title.append(Character.toUpperCase(lower.charAt(0)));
            if (lower.length() > 1)
            {
                title.append(lower.substring(1));
            }
        }
        return title.toString();
    }

    private Set<String> normalizeHiddenColumns(Set<String> configuredHiddenColumns)
    {
        if (configuredHiddenColumns == null || configuredHiddenColumns.isEmpty())
        {
            return Set.of();
        }

        Set<String> normalized = new HashSet<>();
        for (String name : configuredHiddenColumns)
        {
            if (name != null && !name.isBlank())
            {
                normalized.add(name.trim().toLowerCase());
            }
        }
        return Set.copyOf(normalized);
    }

    private boolean isHiddenColumn(String columnName)
    {
        if (columnName == null)
        {
            return false;
        }
        return this.hiddenColumnNames.contains(columnName.toLowerCase());
    }

    private double minWidthForTitle(String title)
    {
        Text measure = new Text(title == null ? "" : title);
        return Math.max(80, measure.getLayoutBounds().getWidth() + 24);
    }
}
