package nonprofitbookkeeping.persistence.records;

/**
 * JDBC metadata for a single table column.
 */
public record TableColumnMetadata(
    String tableName,
    String columnName,
    int jdbcType,
    String typeName,
    int columnSize,
    int decimalDigits,
    boolean nullable,
    boolean primaryKey,
    int ordinalPosition
) {}
