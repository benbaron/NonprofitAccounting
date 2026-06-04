package org.nonprofitbookkeeping.ui;

import nonprofitbookkeeping.core.Database;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Verifies that the default alternate-shell database-open path actually runs
 * Flyway before falling through to the legacy compatibility schema path.
 */
class AlternateDatabaseContextSwitcherFlywayTest
{
    @TempDir
    Path tempDir;

    @Test
    void openDatabaseRunsFlywayBaselineAndCompatibilitySchema() throws Exception
    {
        Path dbBase = tempDir.resolve("alternate-shell-flyway");

        new AlternateDatabaseContextSwitcher().openDatabase(dbBase);

        try (Connection connection = Database.get().getConnection())
        {
            assertTrue(tableExists(connection, "FLYWAY_SCHEMA_HISTORY"),
                "alternate database open should create Flyway schema history");
            assertTrue(successfulFlywayBaselineExists(connection),
                "alternate database open should record successful Flyway baseline migration");
            assertTrue(tableExists(connection, "TXN"),
                "Flyway/compatibility path should create canonical txn table");
            assertTrue(tableExists(connection, "JOURNAL_TRANSACTION"),
                "Flyway/compatibility path should preserve legacy journal table for current runtime");
        }
    }

    private static boolean tableExists(Connection connection, String tableName) throws SQLException
    {
        try (ResultSet rs = connection.getMetaData().getTables(null, "PUBLIC", tableName, new String[] {"TABLE"}))
        {
            return rs.next();
        }
    }

    private static boolean successfulFlywayBaselineExists(Connection connection) throws SQLException
    {
        try (PreparedStatement ps = connection.prepareStatement(
            "SELECT COUNT(*) FROM flyway_schema_history WHERE version = ? AND success = TRUE"))
        {
            ps.setString(1, "1");
            try (ResultSet rs = ps.executeQuery())
            {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }
}
