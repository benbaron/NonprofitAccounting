package nonprofitbookkeeping.core;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.assertTrue;

class FlywayMigrationRunnerRepairTest
{
    @TempDir
    Path tempDir;

    @Test
    void migrateRepairsFailedMigrationHistoryAndContinues() throws Exception
    {
        Database.init(tempDir.resolve("failed-flyway-history-repair"));
        Database database = Database.get();

        migrateWithFlyway(database, "5");
        insertFailedVersionSixHistoryRow(database);

        FlywayMigrationRunner.migrateCurrentDatabaseIfEnabled();

        try (Connection connection = database.getConnection())
        {
            assertTrue(successfulFlywayVersionExists(connection, "006"),
                "runner should repair the failed V006 history row and rerun V006 successfully");
            assertTrue(successfulFlywayVersionExists(connection, "018"),
                "runner should continue migrating after the repaired failed history row");
        }
    }

    private static void migrateWithFlyway(Database database, String target)
    {
        Flyway.configure()
            .dataSource(database.getJdbcUrl(), database.getUser(), database.getPass())
            .locations("classpath:db/migration")
            .baselineOnMigrate(true)
            .baselineVersion("0")
            .target(target)
            .load()
            .migrate();
    }

    private static void insertFailedVersionSixHistoryRow(Database database) throws SQLException
    {
        try (Connection connection = database.getConnection();
             Statement st = connection.createStatement())
        {
            int nextRank;
            try (ResultSet rs = st.executeQuery("SELECT MAX(\"installed_rank\") + 1 FROM \"flyway_schema_history\""))
            {
                rs.next();
                nextRank = rs.getInt(1);
            }
            try (PreparedStatement ps = connection.prepareStatement("""
                INSERT INTO "flyway_schema_history"(
                    "installed_rank", "version", "description", "type", "script", "checksum",
                    "installed_by", "execution_time", "success")
                VALUES (?, '6', 'jpa validation compatibility', 'SQL',
                    'V006__jpa_validation_compatibility.sql', NULL, 'SA', 0, FALSE)
                """))
            {
                ps.setInt(1, nextRank);
                ps.executeUpdate();
            }
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
