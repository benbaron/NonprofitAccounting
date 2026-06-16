package nonprofitbookkeeping.schema;

import nonprofitbookkeeping.core.Database;
import nonprofitbookkeeping.core.FlywayMigrationRunner;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.assertTrue;

class LegacyScheduleDefaultMigrationValidationTest
{
    @TempDir
    Path tempDir;

    @Test
    void v006AddsMissingIsRequiredColumnForLegacyScheduleDefaults() throws Exception
    {
        Database.init(tempDir.resolve("legacy-schedule-default-missing-required"));
        Database database = Database.get();
        createLegacyScheduleDefaultTableWithoutRequiredColumn(database);

        FlywayMigrationRunner.migrateCurrentDatabaseIfEnabled();

        try (Connection connection = database.getConnection())
        {
            assertTrue(columnExists(connection, "ACCOUNT_SUBTYPE_SCHEDULE_DEFAULT", "IS_REQUIRED"),
                "V006 should add is_required when a legacy table predates that column");
            assertTrue(successfulFlywayVersionExists(connection, "006"),
                "V006 should complete successfully after adding the missing compatibility column");
        }
    }

    private static void createLegacyScheduleDefaultTableWithoutRequiredColumn(Database database) throws SQLException
    {
        try (Connection connection = database.getConnection();
             Statement st = connection.createStatement())
        {
            st.execute("""
                CREATE TABLE account_subtype_schedule_default(
                    subtype VARCHAR(40) NOT NULL,
                    schedule_kind_id BIGINT NOT NULL,
                    PRIMARY KEY(subtype, schedule_kind_id)
                )
                """);
        }
    }

    private static boolean columnExists(Connection connection, String tableName, String columnName) throws SQLException
    {
        try (ResultSet rs = connection.getMetaData().getColumns(null, "PUBLIC", tableName, columnName))
        {
            return rs.next();
        }
    }

    private static boolean successfulFlywayVersionExists(Connection connection, String version) throws SQLException
    {
        try (PreparedStatement ps = connection.prepareStatement(
            "SELECT COUNT(*) FROM \"flyway_schema_history\" WHERE \"version\" = ? AND \"success\" = TRUE"))
        {
            ps.setString(1, version);
            try (ResultSet rs = ps.executeQuery())
            {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }
}
