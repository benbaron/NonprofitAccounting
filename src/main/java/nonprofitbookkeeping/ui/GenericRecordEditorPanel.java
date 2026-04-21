package nonprofitbookkeeping.ui;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.text.Text;
import nonprofitbookkeeping.persistence.records.GenericRecordCrudService;
import nonprofitbookkeeping.persistence.records.RecordSchemaService;
import nonprofitbookkeeping.persistence.records.TableColumnMetadata;
import org.nonprofitbookkeeping.ui.AppPanel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.sql.Types;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Generic dynamic record editor panel backed by {@link GenericRecordCrudService}.
 */
public class GenericRecordEditorPanel implements AppPanel
{
    private static final Logger LOG = LoggerFactory.getLogger(GenericRecordEditorPanel.class);

    private final BorderPane root = new BorderPane();
    private final TableView<Map<String, Object>> table = new TableView<>();
    private final Label status = new Label("Ready");
    private final String panelTitle;
    private final String tableName;
    private final String primaryKeyColumn;
    private final Supplier<String> idSupplier;
    private final RecordSchemaService schemaService;
    private final GenericRecordCrudService crudService;
    private final Map<String, Double> selectedColumnWidths = new LinkedHashMap<>();
    private List<TableColumnMetadata> columns = List.of();

    public GenericRecordEditorPanel(String panelTitle, String tableName, String primaryKeyColumn, Supplier<String> idSupplier)
    {
        this(panelTitle, tableName, primaryKeyColumn, idSupplier, new RecordSchemaService(), new GenericRecordCrudService(new RecordSchemaService()));
    }

    GenericRecordEditorPanel(
        String panelTitle,
        String tableName,
        String primaryKeyColumn,
        Supplier<String> idSupplier,
        RecordSchemaService schemaService,
        GenericRecordCrudService crudService
    )
    {
        this.panelTitle = panelTitle;
        this.tableName = tableName;
        this.primaryKeyColumn = primaryKeyColumn;
        this.idSupplier = idSupplier;
        this.schemaService = schemaService;
        this.crudService = crudService;

        root.setPadding(new Insets(8));
        Label title = new Label(panelTitle);
        title.getStyleClass().add("panel-title");

        Button add = new Button("+ Add Row");
        Button delete = new Button("Delete Selected");
        Button refresh = new Button("Refresh");
        Button save = new Button("Save");
        HBox actions = new HBox(8, add, delete, refresh, save);
        root.setTop(new VBox(6, title, actions, new Separator()));

        table.setEditable(true);
        table.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);
        root.setCenter(table);
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
    }

    private void onDeleteSelected()
    {
        Map<String, Object> selected = table.getSelectionModel().getSelectedItem();
        if (selected == null)
        {
            status.setText("Select a row to delete.");
            return;
        }
        try
        {
            Map<String, Object> typed = toTypedRow(selected);
            Map<String, Object> pk = new LinkedHashMap<>();
            for (TableColumnMetadata column : columns)
            {
                if (column.primaryKey())
                {
                    pk.put(column.columnName(), typed.get(column.columnName()));
                }
            }
            int deleted = crudService.deleteByPrimaryKey(tableName, pk);
            table.getItems().remove(selected);
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
            table.setItems(FXCollections.observableArrayList(rows));
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
            String displayTitle = toDisplayTitle(columnName);
            String headerTitle = column.nullable() ? displayTitle : displayTitle + " *";
            TableColumn<Map<String, Object>, String> tableColumn = new TableColumn<>(headerTitle);
            tableColumn.setUserData(columnName);
            tableColumn.setMinWidth(minWidthForTitle(headerTitle));
            tableColumn.setPrefWidth(selectedColumnWidths.getOrDefault(columnName, tableColumn.getMinWidth()));
            tableColumn.setCellValueFactory(cell -> new SimpleStringProperty(toDisplay(cell.getValue().get(columnName))));
            tableColumn.setCellFactory(TextFieldTableCell.forTableColumn());
            tableColumn.setOnEditCommit(event ->
            {
                Map<String, Object> row = event.getRowValue();
                String value = event.getNewValue();
                row.put(columnName, value);
                try
                {
                    convertValue(value, column, displayTitle);
                    status.setText("Updated " + displayTitle);
                }
                catch (IllegalArgumentException ex)
                {
                    status.setText("Validation error: " + ex.getMessage());
                }
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
    private double minWidthForTitle(String title)
    {
        Text measure = new Text(title == null ? "" : title);
        return Math.max(80, measure.getLayoutBounds().getWidth() + 24);
    }
}
