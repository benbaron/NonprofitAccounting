package nonprofitbookkeeping.persistence.records;

import nonprofitbookkeeping.core.Database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Generic CRUD service for dynamic record editing panels.
 */
public class GenericRecordCrudService
{
    private final RecordSchemaService schemaService;

    public GenericRecordCrudService(RecordSchemaService schemaService)
    {
        this.schemaService = schemaService;
    }

    public List<Map<String, Object>> listAll(String tableName) throws SQLException
    {
        String normalizedTable = normalizeIdentifier(tableName, "tableName");
        String sql = "SELECT * FROM " + normalizedTable;
        try (Connection connection = Database.get().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery())
        {
            return toRows(rs);
        }
    }

    public int upsert(String tableName, Map<String, Object> rowValues) throws SQLException
    {
        String normalizedTable = normalizeIdentifier(tableName, "tableName");
        List<TableColumnMetadata> columns = schemaService.columnsForTable(normalizedTable);
        if (columns.isEmpty())
        {
            throw new IllegalArgumentException("Unknown table or no visible columns: " + normalizedTable);
        }

        Map<String, Object> normalizedValues = normalizeKeys(rowValues);
        List<TableColumnMetadata> participatingColumns = columns.stream()
            .filter(column -> normalizedValues.containsKey(normalizeKey(column.columnName())))
            .collect(Collectors.toCollection(ArrayList::new));
        if (participatingColumns.isEmpty())
        {
            throw new IllegalArgumentException("No row values matched table columns for " + normalizedTable);
        }

        List<TableColumnMetadata> keyColumns = participatingColumns.stream()
            .filter(TableColumnMetadata::primaryKey)
            .collect(Collectors.toCollection(ArrayList::new));
        if (keyColumns.isEmpty())
        {
            throw new IllegalArgumentException("Upsert requires at least one primary key value for " + normalizedTable);
        }

        String columnList = participatingColumns.stream()
            .map(TableColumnMetadata::columnName)
            .collect(Collectors.joining(", "));
        String keyList = keyColumns.stream()
            .map(TableColumnMetadata::columnName)
            .collect(Collectors.joining(", "));
        String placeholders = participatingColumns.stream()
            .map(column -> "?")
            .collect(Collectors.joining(", "));
        String sql = "MERGE INTO " + normalizedTable + "(" + columnList + ") KEY(" + keyList + ") VALUES(" + placeholders + ")";

        try (Connection connection = Database.get().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql))
        {
            int idx = 0;
            for (TableColumnMetadata column : participatingColumns)
            {
                Object value = normalizedValues.get(normalizeKey(column.columnName()));
                bindValue(statement, ++idx, value, column.jdbcType());
            }
            return statement.executeUpdate();
        }
    }

    public int deleteByPrimaryKey(String tableName, Map<String, Object> primaryKeyValues) throws SQLException
    {
        String normalizedTable = normalizeIdentifier(tableName, "tableName");
        List<TableColumnMetadata> columns = schemaService.columnsForTable(normalizedTable);
        Map<String, TableColumnMetadata> columnsByName = columns.stream()
            .collect(Collectors.toMap(column -> normalizeKey(column.columnName()), column -> column));
        Map<String, Object> normalizedPkValues = normalizeKeys(primaryKeyValues);
        List<String> keyNames = normalizedPkValues.keySet().stream()
            .filter(columnsByName::containsKey)
            .filter(key -> columnsByName.get(key).primaryKey())
            .collect(Collectors.toCollection(ArrayList::new));
        if (keyNames.isEmpty())
        {
            throw new IllegalArgumentException("No primary key values provided for " + normalizedTable);
        }

        String whereClause = keyNames.stream()
            .map(key -> columnsByName.get(key).columnName() + " = ?")
            .collect(Collectors.joining(" AND "));
        String sql = "DELETE FROM " + normalizedTable + " WHERE " + whereClause;

        try (Connection connection = Database.get().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql))
        {
            int idx = 0;
            for (String key : keyNames)
            {
                TableColumnMetadata column = columnsByName.get(key);
                bindValue(statement, ++idx, normalizedPkValues.get(key), column.jdbcType());
            }
            return statement.executeUpdate();
        }
    }

    private List<Map<String, Object>> toRows(ResultSet rs) throws SQLException
    {
        List<Map<String, Object>> rows = new ArrayList<>();
        ResultSetMetaData metaData = rs.getMetaData();
        int columnCount = metaData.getColumnCount();
        while (rs.next())
        {
            Map<String, Object> row = new LinkedHashMap<>();
            for (int i = 1; i <= columnCount; i++)
            {
                row.put(metaData.getColumnLabel(i), rs.getObject(i));
            }
            rows.add(row);
        }
        return rows;
    }

    private void bindValue(PreparedStatement statement, int index, Object value, int jdbcType) throws SQLException
    {
        if (value == null)
        {
            statement.setNull(index, jdbcType);
            return;
        }
        statement.setObject(index, value);
    }

    private Map<String, Object> normalizeKeys(Map<String, Object> values)
    {
        if (values == null || values.isEmpty())
        {
            return Map.of();
        }
        Map<String, Object> normalized = new LinkedHashMap<>();
        for (Map.Entry<String, Object> entry : values.entrySet())
        {
            normalized.put(normalizeKey(entry.getKey()), entry.getValue());
        }
        return normalized;
    }

    private String normalizeKey(String key)
    {
        return key == null ? "" : key.trim().toLowerCase(Locale.ROOT);
    }

    private String normalizeIdentifier(String value, String fieldName)
    {
        if (value == null || value.isBlank())
        {
            throw new IllegalArgumentException(fieldName + " is required.");
        }
        String trimmed = value.trim();
        if (!trimmed.matches("[A-Za-z0-9_]+"))
        {
            throw new IllegalArgumentException(fieldName + " contains unsupported characters: " + value);
        }
        return trimmed;
    }
}
