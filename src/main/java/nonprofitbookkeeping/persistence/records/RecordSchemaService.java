package nonprofitbookkeeping.persistence.records;

import nonprofitbookkeeping.core.Database;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Reads table/column metadata from JDBC {@link DatabaseMetaData}.
 */
public class RecordSchemaService
{
    private static final Pattern IDENTIFIER = Pattern.compile("[A-Za-z0-9_]+");

    public List<TableColumnMetadata> columnsForTable(String tableName) throws SQLException
    {
        String normalizedTableName = normalizeIdentifier(tableName, "tableName");
        try (Connection connection = Database.get().getConnection())
        {
            DatabaseMetaData metaData = connection.getMetaData();
            Set<String> primaryKeys = readPrimaryKeys(metaData, normalizedTableName);
            List<TableColumnMetadata> columns = new ArrayList<>();
            try (ResultSet rs = metaData.getColumns(null, null, normalizedTableName.toUpperCase(Locale.ROOT), null))
            {
                while (rs.next())
                {
                    String columnName = rs.getString("COLUMN_NAME");
                    int nullableCode = rs.getInt("NULLABLE");
                    columns.add(new TableColumnMetadata(
                        normalizedTableName,
                        columnName,
                        rs.getInt("DATA_TYPE"),
                        rs.getString("TYPE_NAME"),
                        rs.getInt("COLUMN_SIZE"),
                        rs.getInt("DECIMAL_DIGITS"),
                        nullableCode != DatabaseMetaData.columnNoNulls,
                        primaryKeys.contains(columnName.toUpperCase(Locale.ROOT)),
                        rs.getInt("ORDINAL_POSITION")
                    ));
                }
            }
            return columns;
        }
    }

    private Set<String> readPrimaryKeys(DatabaseMetaData metaData, String tableName) throws SQLException
    {
        Set<String> keys = new HashSet<>();
        try (ResultSet rs = metaData.getPrimaryKeys(null, null, tableName.toUpperCase(Locale.ROOT)))
        {
            while (rs.next())
            {
                keys.add(rs.getString("COLUMN_NAME").toUpperCase(Locale.ROOT));
            }
        }
        return keys;
    }

    private String normalizeIdentifier(String value, String fieldName)
    {
        if (value == null || value.isBlank())
        {
            throw new IllegalArgumentException(fieldName + " is required.");
        }
        if (!IDENTIFIER.matcher(value).matches())
        {
            throw new IllegalArgumentException(fieldName + " contains unsupported characters: " + value);
        }
        return value.trim();
    }
}
