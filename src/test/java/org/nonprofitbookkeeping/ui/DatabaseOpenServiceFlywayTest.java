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

/** Verifies that the shared database-open service runs Flyway for both UI shells. */
class DatabaseOpenServiceFlywayTest
{
    @TempDir
    Path tempDir;

    @Test
    void openDatabaseRunsFlywayAndCompatibilitySchema() throws Exception
    {
        Path dbBase = tempDir.resolve("shared-open-flyway");

        DatabaseOpenService.OpenResult result = DatabaseOpenService.openDatabase(dbBase);

        assertTrue(result.successMessage().contains(dbBase.toAbsolutePath().toString()));
        try (Connection connection = Database.get().getConnection())
        {
            assertTrue(tableExists(connection, "FLYWAY_SCHEMA_HISTORY"),
                "shared open path should create Flyway schema history");
            assertTrue(successfulFlywayVersionExists(connection, "1"),
                "shared open path should record successful Flyway V001 migration");
            assertTrue(successfulFlywayVersionExists(connection, "2"),
                "shared open path should record successful Flyway V002 migration");
            assertTrue(successfulFlywayVersionExists(connection, "3"),
                "shared open path should record successful Flyway V003 migration");
            assertTrue(tableExists(connection, "TXN"),
                "Flyway/compatibility path should create canonical txn table");
            assertTrue(tableExists(connection, "JOURNAL_TRANSACTION"),
                "Flyway/compatibility path should preserve legacy journal table for current runtime");
            assertTrue(tableExists(connection, "IMPORTED_ORGANIZATION_RECORD"),
                "Flyway V003 should create imported organization staging table");
            assertTrue(tableExists(connection, "IMPORTED_FUND_RECORD"),
                "Flyway V003 should create imported fund staging table");
            assertTrue(tableExists(connection, "IMPORTED_EVENT_RECORD"),
                "Flyway V003 should create imported event staging table");
            assertTrue(tableExists(connection, "IMPORTED_DOCUMENT_RECORD"),
                "Flyway V003 should create imported document staging table");
        }
    }

    private static boolean tableExists(Connection connection, String tableName) throws SQLException
    {
        try (ResultSet rs = connection.getMetaData().getTables(null, "PUBLIC", tableName, new String[] {"TABLE"}))
        {
            return rs.next();
        }
    }

    private static boolean successfulFlywayVersionExists(Connection connection, String version) throws SQLException
    {
        try (PreparedStatement ps = connection.prepareStatement(
            "SELECT COUNT(*) FROM flyway_schema_history WHERE version = ? AND success = TRUE"))
        {
            ps.setString(1, version);
            try (ResultSet rs = ps.executeQuery())
            {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }
}
