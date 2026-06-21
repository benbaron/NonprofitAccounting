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

class EnsureSchemaOperationalLinkageCompatibilityValidationTest
{
    private static final List<String> OPERATIONAL_LINKAGE_TABLES = List.of(
        "BANK_ID_RECORD",
        "BANKING_TRANSACTION_RECORD",
        "LEDGER_RECORD",
        "ASSET_RECORD_DETAIL",
        "INVENTORY_ASSET_LINK",
        "DEPRECIATION_RUN",
        "DEPRECIATION_RECORD",
        "GRANT_RECORD",
        "DONATION_RECORD",
        "DONATION_JOURNAL_LINK",
        "OPERATIONAL_LINK_BACKFILL_QUEUE"
    );

    @TempDir
    Path tempDir;

    @Test
    void ensureSchemaDoesNotChangeFlywayOwnedOperationalLinkageColumns() throws Exception
    {
        Database.init(this.tempDir.resolve("operational-linkage-compatibility"));
        Database database = Database.get();

        migrateWithFlyway(database);
        Map<String, Set<String>> flywayColumns = tableColumns(database, OPERATIONAL_LINKAGE_TABLES);

        database.ensureSchema();
        Map<String, Set<String>> postEnsureSchemaColumns = tableColumns(database, OPERATIONAL_LINKAGE_TABLES);

        assertEquals(flywayColumns, postEnsureSchemaColumns,
            "operational linkage columns should be Flyway-owned; ensureSchema must not add or remove columns");
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

    private static String normalize(String value)
    {
        return value == null ? "" : value.toUpperCase(Locale.ROOT);
    }
}
