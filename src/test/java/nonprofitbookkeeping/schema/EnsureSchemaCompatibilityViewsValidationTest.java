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

class EnsureSchemaCompatibilityViewsValidationTest
{
    private static final List<String> READ_MODEL_TABLES = List.of(
        "RM_DONATION_SUMMARY",
        "RM_GRANT_SUMMARY",
        "RM_FUND_SUMMARY",
        "RM_RECONCILIATION_SUMMARY",
        "RM_DEPRECIATION_SUMMARY"
    );

    private static final List<String> COMPATIBILITY_VIEWS = List.of(
        "V_JOURNAL_TRANSACTION",
        "V_JOURNAL_ENTRY"
    );

    @TempDir
    Path tempDir;

    @Test
    void ensureSchemaDoesNotChangeFlywayOwnedReadModelTablesOrCompatibilityViews() throws Exception
    {
        Database.init(tempDir.resolve("compatibility-view-validation"));
        Database database = Database.get();

        migrateWithFlyway(database);
        Map<String, Set<String>> flywayTableColumns = objectColumns(database, READ_MODEL_TABLES);
        Map<String, Set<String>> flywayViewColumns = objectColumns(database, COMPATIBILITY_VIEWS);
        Set<String> flywayViews = views(database, COMPATIBILITY_VIEWS);

        database.ensureSchema();
        Map<String, Set<String>> postEnsureSchemaTableColumns = objectColumns(database, READ_MODEL_TABLES);
        Map<String, Set<String>> postEnsureSchemaViewColumns = objectColumns(database, COMPATIBILITY_VIEWS);
        Set<String> postEnsureSchemaViews = views(database, COMPATIBILITY_VIEWS);

        assertEquals(flywayTableColumns, postEnsureSchemaTableColumns,
            "read-model table columns should be Flyway-owned; ensureSchema must not add or remove columns");
        assertEquals(flywayViewColumns, postEnsureSchemaViewColumns,
            "compatibility view columns should be Flyway-owned; ensureSchema must not add or remove columns");
        assertEquals(flywayViews, postEnsureSchemaViews,
            "compatibility views should be Flyway-owned; ensureSchema must not add or remove views");
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

    private static Map<String, Set<String>> objectColumns(Database database, List<String> objectNames) throws SQLException
    {
        Map<String, Set<String>> out = new LinkedHashMap<>();
        for (String objectName : objectNames)
        {
            out.put(objectName, objectColumns(database, objectName));
        }
        return out;
    }

    private static Set<String> objectColumns(Database database, String objectName) throws SQLException
    {
        Set<String> columns = new TreeSet<>();
        try (Connection connection = DriverManager.getConnection(database.getJdbcUrl(), database.getUser(), database.getPass()))
        {
            DatabaseMetaData meta = connection.getMetaData();
            try (ResultSet rs = meta.getColumns(null, "PUBLIC", objectName, "%"))
            {
                while (rs.next())
                {
                    columns.add(normalize(rs.getString("COLUMN_NAME")));
                }
            }
        }
        return columns;
    }

    private static Set<String> views(Database database, List<String> viewNames) throws SQLException
    {
        Set<String> views = new TreeSet<>();
        try (Connection connection = DriverManager.getConnection(database.getJdbcUrl(), database.getUser(), database.getPass()))
        {
            DatabaseMetaData meta = connection.getMetaData();
            for (String viewName : viewNames)
            {
                try (ResultSet rs = meta.getTables(null, "PUBLIC", viewName, new String[] { "VIEW" }))
                {
                    while (rs.next())
                    {
                        views.add(normalize(rs.getString("TABLE_NAME")));
                    }
                }
            }
        }
        return views;
    }

    private static String normalize(String value)
    {
        return value == null ? "" : value.toUpperCase(Locale.ROOT);
    }
}
