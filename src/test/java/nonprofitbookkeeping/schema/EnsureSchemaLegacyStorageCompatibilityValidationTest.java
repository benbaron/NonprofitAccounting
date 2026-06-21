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

class EnsureSchemaLegacyStorageCompatibilityValidationTest
{
    private static final List<String> LEGACY_STORAGE_TABLES = List.of(
        "UNDEPOSITED_FUNDS_ITEM",
        "DOCUMENT",
        "JSON_STORAGE",
        "COMPANY_STORE",
        "IMPORTED_ASSET_RECORD",
        "SALE_RECORD"
    );

    @TempDir
    Path tempDir;

    @Test
    void ensureSchemaDoesNotChangeFlywayOwnedLegacyStorageColumnsOrIndexes() throws Exception
    {
        Database.init(this.tempDir.resolve("legacy-storage-compatibility"));
        Database database = Database.get();

        migrateWithFlyway(database);
        Map<String, Set<String>> flywayColumns = tableColumns(database, LEGACY_STORAGE_TABLES);
        Map<String, Set<String>> flywayIndexes = tableIndexes(database, LEGACY_STORAGE_TABLES);

        database.ensureSchema();
        Map<String, Set<String>> postEnsureSchemaColumns = tableColumns(database, LEGACY_STORAGE_TABLES);
        Map<String, Set<String>> postEnsureSchemaIndexes = tableIndexes(database, LEGACY_STORAGE_TABLES);

        assertEquals(flywayColumns, postEnsureSchemaColumns,
            "legacy storage/sales columns should be Flyway-owned; ensureSchema must not add or remove columns");
        assertEquals(flywayIndexes, postEnsureSchemaIndexes,
            "legacy storage/sales indexes should be Flyway-owned; ensureSchema must not add or remove indexes");
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
}
