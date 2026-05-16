package nonprofitbookkeeping.ui;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.control.Button;
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

        root.setPadding(UiSpacing.pageInsets());
        Label title = new Label(panelTitle);
        title.getStyleClass().add("journal-entry-heading");

        Button add = new Button("+ Add Row");
        Button delete = new Button("Delete Selected");
        Button refresh = new Button("Refresh");
        Button save = new Button("Save");
        HBox actions = new HBox(UiSpacing.SECTION_SPACING, add, delete, refresh, save);
        root.setTop(new VBox(UiSpacing.SECTION_SPACING, title, actions, new Separator()));

        table.setEditable(true);
        table.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);
        table.setRowFactory(tv -> new TableRow<>()
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
                if (pendingNewRows.contains(item))
                {
                    getStyleClass().add(PENDING_ROW_CLASS);
                }
            }
        });
        tableContainer.setContent(table);
        tableContainer.setFitToWidth(true);
        tableContainer.setFitToHeight(true);
        tableContainer.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        tableContainer.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
        root.setCenter(tableContainer);
        root.setBottom(new VBox(new Separator(), status));

        add.setOnAction(e -> onAddRow());
        delete.setOnAction(e -> onDeleteSelected());
        refresh.setOnAction(e -> loadFromService());
        save.setOnAction(e -> onSave());
        loadFromService();
    }

    @Override
    public String title()
    {
        return panelTitle;
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
            int saved = 0;
            for (Map<String, Object> row : table.getItems())
            {
                crudService.upsert(tableName, toTypedRow(row));
                saved++;
            }
            status.setText("Saved " + saved + " row(s)");
            loadFromService();
        }
        catch (IllegalArgumentException ex)
        {
            status.setText("Validation error: " + ex.getMessage());
        }
        catch (SQLException | RuntimeException ex)
        {
            LOG.warn("Generic record save failed for table {}", tableName, ex);
            status.setText("Failed to save rows: " + ex.getMessage());
        }
    }

    private void onAddRow()
    {
        Map<String, Object> row = new LinkedHashMap<>();
        for (TableColumnMetadata column : columns)
        {
            row.put(column.columnName(), null);
        }
        if (primaryKeyColumn != null && !primaryKeyColumn.isBlank())
        {
            row.put(primaryKeyColumn, idSupplier == null ? null : idSupplier.get());
        }
        table.getItems().add(row);
        pendingNewRows.add(row);
        table.refresh();
        table.getSelectionModel().select(row);
        table.scrollTo(row);
        status.setText("Added new unsaved row. Press Save to persist.");
    }

    private void onDeleteSelected()
    {
        Map<String, Object> selected = table.getSelectionModel().getSelectedItem();
        if (selected == null)
        {
            status.setText("Select a row to delete.");
            return;
        }

        if (pendingNewRows.contains(selected))
        {
            pendingNewRows.remove(selected);
            table.getItems().remove(selected);
            table.refresh();
            status.setText("Removed unsaved row.");
            return;
        }

        try
        {
            Map<String, Object> pk = primaryKeyValues(selected);
            int deleted = crudService.deleteByPrimaryKey(tableName, pk);
            table.getItems().remove(selected);
            table.refresh();
            status.setText(deleted > 0 ? "Deleted selected row." : "Removed unsaved row.");
        }
        catch (IllegalArgumentException ex)
        {
            status.setText("Validation error: " + ex.getMessage());
        }
        catch (SQLException | RuntimeException ex)
        {
            LOG.warn("Generic record delete failed for table {}", tableName, ex);
            status.setText("Failed to delete row: " + ex.getMessage());
        }
    }

    private void loadFromService()
    {
        try
        {
            this.columns = schemaService.columnsForTable(tableName).stream()
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
            List<Map<String, Object>> rows = new ArrayList<>(crudService.listAll(tableName));
            pendingNewRows.clear();
            table.setItems(FXCollections.observableArrayList(rows));
            table.refresh();
            ensureVerticalScrollBarVisible();
            status.setText("Loaded " + rows.size() + " row(s)");
        }
        catch (SQLException | RuntimeException ex)
        {
            LOG.warn("Generic record load failed for table {}", tableName, ex);
            status.setText("Failed to load rows: " + ex.getMessage());
        }
    }

    private void configureColumns()
    {
        captureColumnWidths();
        table.getColumns().clear();
        for (TableColumnMetadata column : columns)
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
            tableColumn.setPrefWidth(selectedColumnWidths.getOrDefault(columnName, tableColumn.getMinWidth()));
            tableColumn.setCellValueFactory(cell -> new SimpleStringProperty(toDisplay(cell.getValue().get(columnName))));
            tableColumn.setCellFactory(col -> new CommitOnFocusLossCell());
            tableColumn.setOnEditCommit(event ->
            {
                Map<String, Object> row = event.getRowValue();
                String value = event.getNewValue();
                row.put(columnName, value);
                status.setText("Updated " + displayTitle + " (pending save)");
            });
            table.getColumns().add(tableColumn);
        }
    }

    private void captureColumnWidths()
    {
        for (TableColumn<Map<String, Object>, ?> tableColumn : table.getColumns())
        {
            Object key = tableColumn.getUserData();
            if (key instanceof String columnName && !columnName.isBlank())
            {
                selectedColumnWidths.put(columnName, tableColumn.getWidth());
            }
        }
    }

    private void ensureVerticalScrollBarVisible()
    {
        Platform.runLater(() -> {
            Node node = table.lookup(".scroll-bar:vertical");
            if (node instanceof ScrollBar scrollBar)
            {
                scrollBar.setVisible(true);
                scrollBar.setManaged(true);
                scrollBar.setOpacity(1.0);
                scrollBar.setMaxWidth(12);
            }
        });
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
                setGraphic(textField);
                textField.selectAll();
                textField.requestFocus();
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
                if (textField != null)
                {
                    textField.setText(item);
                }
                setText(null);
                setGraphic(textField);
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
            textField = new TextField(getItem());
            textField.setMinWidth(this.getWidth() - this.getGraphicTextGap() * 2);
            textField.setOnAction(event -> commitEdit(textField.getText()));
            textField.focusedProperty().addListener((obs, oldValue, hasFocus) -> {
                if (!hasFocus)
                {
                    commitEdit(textField.getText());
                }
            });
        }

        private void applyPendingRowTextStyle()
        {
            TableRow<Map<String, Object>> row = getTableRow();
            getStyleClass().remove(PENDING_ROW_TEXT_DARK_CLASS);
            if (row != null && pendingNewRows.contains(row.getItem()))
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
        for (TableColumnMetadata column : columns)
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
        for (TableColumnMetadata column : columns)
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
        return hiddenColumnNames.contains(columnName.toLowerCase());
    }

    private double minWidthForTitle(String title)
    {
        Text measure = new Text(title == null ? "" : title);
        return Math.max(80, measure.getLayoutBounds().getWidth() + 24);
    }
}
