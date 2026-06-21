package nonprofitbookkeeping.schema;

import nonprofitbookkeeping.core.Database;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EnsureSchemaLegacyJournalCompatibilityValidationTest
{
    private static final List<String> LEGACY_JOURNAL_TABLES = List.of(
        "ACCOUNT",
        "ACCOUNT_FUND",
        "JOURNAL_TRANSACTION",
        "JOURNAL_ENTRY",
        "TRANSACTION_INFO",
        "TXN_SUPPLEMENTAL_LINE",
        "LEGACY_TXN_MAP"
    );

    @TempDir
    Path tempDir;

    @Test
    void ensureSchemaDoesNotChangeFlywayOwnedLegacyJournalColumns() throws Exception
    {
        Database.init(this.tempDir.resolve("legacy-journal-compatibility"));
        Database database = Database.get();

        migrateWithFlyway(database);
        Map<String, Set<String>> flywayColumns = tableColumns(database, LEGACY_JOURNAL_TABLES);
        Map<String, Set<ForeignKeyDefinition>> flywayForeignKeys = importedForeignKeys(database, LEGACY_JOURNAL_TABLES);
        Map<String, Set<String>> flywayIndexes = tableIndexes(database, LEGACY_JOURNAL_TABLES);

        database.ensureSchema();
        Map<String, Set<String>> postEnsureSchemaColumns = tableColumns(database, LEGACY_JOURNAL_TABLES);
        Map<String, Set<ForeignKeyDefinition>> postEnsureSchemaForeignKeys =
            importedForeignKeys(database, LEGACY_JOURNAL_TABLES);
        Map<String, Set<String>> postEnsureSchemaIndexes = tableIndexes(database, LEGACY_JOURNAL_TABLES);

        assertEquals(flywayColumns, postEnsureSchemaColumns,
            "legacy journal/account columns should be Flyway-owned; ensureSchema must not add or remove columns");
        assertEquals(flywayForeignKeys, postEnsureSchemaForeignKeys,
            "legacy journal/account foreign keys should be Flyway-owned; ensureSchema must not add or remove foreign keys");
        assertEquals(flywayIndexes, postEnsureSchemaIndexes,
            "legacy journal/account indexes should be Flyway-owned; ensureSchema must not add or remove indexes");
    }

    private static void migrateWithFlyway(Database database)
    {
        Flyway.configure()
            .dataSource(database.getJdbcUrl(), database.getUser(), database.getPass())
            .locations("classpath:db/migration")
            .baselineOnMigrate(true)
            .baselineVersion("0")
            .load()
            .migrate();
    }

    private static Map<String, Set<String>> tableColumns(Database database, List<String> tableNames) throws SQLException
    {
        Map<String, Set<String>> out = new LinkedHashMap<>();
        for (String tableName : tableNames)
        {
            out.put(tableName, tableColumns(database, tableName));
        }
        return out;
    }

    private static Set<String> tableColumns(Database database, String tableName) throws SQLException
    {
        Set<String> columns = new TreeSet<>();
        try (Connection connection = DriverManager.getConnection(database.getJdbcUrl(), database.getUser(), database.getPass()))
        {
            DatabaseMetaData meta = connection.getMetaData();
            try (ResultSet rs = meta.getColumns(null, "PUBLIC", tableName, "%"))
            {
                while (rs.next())
                {
                    columns.add(normalize(rs.getString("COLUMN_NAME")));
                }
            }
        }
        return columns;
    }

    private static Map<String, Set<ForeignKeyDefinition>> importedForeignKeys(Database database, List<String> tableNames)
        throws SQLException
    {
        Map<String, Set<ForeignKeyDefinition>> out = new LinkedHashMap<>();
        for (String tableName : tableNames)
        {
            out.put(tableName, importedForeignKeys(database, tableName));
        }
        return out;
    }

    private static Set<ForeignKeyDefinition> importedForeignKeys(Database database, String tableName) throws SQLException
    {
        Set<ForeignKeyDefinition> foreignKeys = new TreeSet<>();
        try (Connection connection = DriverManager.getConnection(database.getJdbcUrl(), database.getUser(), database.getPass()))
        {
            DatabaseMetaData meta = connection.getMetaData();
            try (ResultSet rs = meta.getImportedKeys(null, "PUBLIC", tableName))
            {
                while (rs.next())
                {
                    foreignKeys.add(new ForeignKeyDefinition(
                        normalize(rs.getString("FK_NAME")),
                        normalize(rs.getString("FKCOLUMN_NAME")),
                        normalize(rs.getString("PKTABLE_NAME")),
                        normalize(rs.getString("PKCOLUMN_NAME"))
                    ));
                }
            }
        }
        return foreignKeys;
    }

    private static Map<String, Set<String>> tableIndexes(Database database, List<String> tableNames) throws SQLException
    {
        Map<String, Set<String>> out = new LinkedHashMap<>();
        for (String tableName : tableNames)
        {
            out.put(tableName, tableIndexes(database, tableName));
        }
        return out;
    }

    private static Set<String> tableIndexes(Database database, String tableName) throws SQLException
    {
        Set<String> indexes = new TreeSet<>();
        try (Connection connection = DriverManager.getConnection(database.getJdbcUrl(), database.getUser(), database.getPass()))
        {
            DatabaseMetaData meta = connection.getMetaData();
            try (ResultSet rs = meta.getIndexInfo(null, "PUBLIC", tableName, false, false))
            {
                while (rs.next())
                {
                    String indexName = normalize(rs.getString("INDEX_NAME"));
                    String columnName = normalize(rs.getString("COLUMN_NAME"));
                    if (!indexName.isBlank() && !columnName.isBlank())
                    {
                        indexes.add(indexName + "." + columnName);
                    }
                }
            }
        }
        return indexes;
    }

    private static String normalize(String value)
    {
        return value == null ? "" : value.toUpperCase(Locale.ROOT);
    }

    private record ForeignKeyDefinition(String name, String column, String referencedTable, String referencedColumn)
        implements Comparable<ForeignKeyDefinition>
    {
        @Override
        public int compareTo(ForeignKeyDefinition other)
        {
            int byName = this.name.compareTo(other.name);
            if (byName != 0)
            {
                return byName;
            }
            int byColumn = this.column.compareTo(other.column);
            if (byColumn != 0)
            {
                return byColumn;
            }
            int byReferencedTable = this.referencedTable.compareTo(other.referencedTable);
            if (byReferencedTable != 0)
            {
                return byReferencedTable;
            }
            return this.referencedColumn.compareTo(other.referencedColumn);
        }
    }
}
