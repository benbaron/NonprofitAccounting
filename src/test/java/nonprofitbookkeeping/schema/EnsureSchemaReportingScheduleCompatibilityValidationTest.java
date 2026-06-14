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
import java.sql.Statement;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EnsureSchemaReportingScheduleCompatibilityValidationTest
{
    private static final List<String> REPORTING_SCHEDULE_TABLES = List.of(
        "SCHEDULE_KIND",
        "REPORT_SECTION",
        "ACCOUNT_REPORT_SECTION",
        "ACCOUNT_SCHEDULE_REQUIREMENT",
        "ACCOUNT_SUBTYPE_SCHEDULE_DEFAULT",
        "CONFIG_RELEASE",
        "STATEMENT_SECTION",
        "ACCOUNT_STATEMENT_MAPPING",
        "SCHEDULE_REQUIREMENT_RULE",
        "CONFIG_CHANGE_EVENT"
    );

    @TempDir
    Path tempDir;

    @Test
    void ensureSchemaDoesNotChangeFlywayOwnedReportingScheduleColumns() throws Exception
    {
        Database.init(tempDir.resolve("reporting-schedule-compatibility"));
        Database database = Database.get();

        migrateWithFlyway(database);
        Map<String, Set<String>> flywayColumns = tableColumns(database, REPORTING_SCHEDULE_TABLES);

        database.ensureSchema();
        Map<String, Set<String>> postEnsureSchemaColumns = tableColumns(database, REPORTING_SCHEDULE_TABLES);

        assertEquals(flywayColumns, postEnsureSchemaColumns,
            "reporting schedule columns should be Flyway-owned; ensureSchema must not add or remove columns");
    }

    @Test
    void flywayCreatesLegacyBaselineReportingScheduleRelease() throws Exception
    {
        Database.init(tempDir.resolve("reporting-schedule-baseline-release"));
        Database database = Database.get();

        migrateWithFlyway(database);

        try (Connection connection = DriverManager.getConnection(database.getJdbcUrl(), database.getUser(), database.getPass());
             Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery(
                 "SELECT COUNT(*) FROM config_release WHERE release_code = 'LEGACY_BASELINE_V1' AND status = 'ACTIVE'"))
        {
            assertTrue(rs.next());
            assertEquals(1, rs.getInt(1),
                "Flyway should seed the legacy baseline reporting schedule release without ensureSchema");
        }
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
