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
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EnsureSchemaCompatibilityValidationTest
{
    @TempDir
    Path tempDir;

    @Test
    void ensureSchemaDoesNotChangeFlywayOwnedCompanyProfileColumns() throws Exception
    {
        Database.init(tempDir.resolve("company-profile-compatibility"));
        Database database = Database.get();

        migrateWithFlyway(database);
        Set<String> flywayColumns = tableColumns(database, "COMPANY_PROFILE");

        database.ensureSchema();
        Set<String> postEnsureSchemaColumns = tableColumns(database, "COMPANY_PROFILE");

        assertEquals(flywayColumns, postEnsureSchemaColumns,
            "company_profile columns should be Flyway-owned; ensureSchema must not add or remove columns");
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
