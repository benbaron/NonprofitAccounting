package nonprofitbookkeeping.schema;

import nonprofitbookkeeping.core.Database;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PeopleCounterpartyRepairBehaviorValidationTest
{
    @TempDir
    Path tempDir;

    @Test
    void flywayRepairsDonorExternalIdsBlankPersonTypesAndCounterparties() throws Exception
    {
        Database.init(tempDir.resolve("people-counterparty-repair-behavior"));
        Database database = Database.get();

        migrateWithFlyway(database, "14");
        seedPeopleAndCounterpartyScenario(database);

        migrateWithFlyway(database);

        assertPeopleAndCounterpartyRepairs(database);

        database.ensureSchema();
        database.ensureSchema();

        assertPeopleAndCounterpartyRepairs(database);
    }

    private static void migrateWithFlyway(Database database)
    {
        migrateWithFlyway(database, null);
    }

    private static void migrateWithFlyway(Database database, String targetVersion)
    {
        var configuration = Flyway.configure()
            .dataSource(database.getJdbcUrl(), database.getUser(), database.getPass())
            .locations("classpath:db/migration")
            .baselineOnMigrate(true)
            .baselineVersion("0");
        if (targetVersion != null)
        {
            configuration.target(targetVersion);
        }
        configuration.load().migrate();
    }

    private static void seedPeopleAndCounterpartyScenario(Database database) throws SQLException
    {
        try (Connection connection = DriverManager.getConnection(database.getJdbcUrl(), database.getUser(), database.getPass());
             Statement st = connection.createStatement())
        {
            st.execute("INSERT INTO schema_migration_history(migration_key) VALUES ('reconciled-backfill-v1')");
            st.execute("INSERT INTO schema_migration_history(migration_key) VALUES ('operational-link-backfill-v1')");
            st.execute("INSERT INTO donor(name) VALUES ('Missing External')");
            st.execute("INSERT INTO donor(external_id, name) VALUES ('existing-id', 'Already External')");
            st.execute("INSERT INTO donor(name) VALUES ('No Name')");
            st.execute("UPDATE donor SET name = NULL WHERE name = 'No Name'");
            st.execute("INSERT INTO person(name) VALUES ('Default Type')");
            st.execute("INSERT INTO person(name, type) VALUES ('Blank Type', '   ')");
            st.execute("INSERT INTO person(name, type) VALUES ('Already Typed', 'VOLUNTEER')");
        }
    }

    private static void assertPeopleAndCounterpartyRepairs(Database database) throws SQLException
    {
        assertEquals("Missing External", queryString(database,
            "SELECT external_id FROM donor WHERE name = 'Missing External'"));
        assertEquals("existing-id", queryString(database,
            "SELECT external_id FROM donor WHERE name = 'Already External'"));
        assertEquals(0, queryInt(database,
            "SELECT COUNT(*) FROM donor WHERE name IS NULL AND external_id IS NOT NULL"));
        assertEquals("DONOR", queryString(database,
            "SELECT type FROM person WHERE name = 'Default Type'"));
        assertEquals("DONOR", queryString(database,
            "SELECT type FROM person WHERE name = 'Blank Type'"));
        assertEquals("VOLUNTEER", queryString(database,
            "SELECT type FROM person WHERE name = 'Already Typed'"));
        assertEquals(1, queryInt(database,
            "SELECT COUNT(*) FROM counterparty WHERE display_name = 'Default Type' AND kind = 'DONOR'"));
        assertEquals(1, queryInt(database,
            "SELECT COUNT(*) FROM counterparty WHERE display_name = 'Blank Type' AND kind = 'DONOR'"));
        assertEquals(1, queryInt(database,
            "SELECT COUNT(*) FROM counterparty WHERE display_name = 'Already Typed' AND kind = 'VOLUNTEER'"));
        assertEquals(1, queryInt(database,
            "SELECT COUNT(*) FROM counterparty WHERE display_name = 'Missing External' AND kind = 'DONOR'"));
        assertEquals(1, queryInt(database,
            "SELECT COUNT(*) FROM counterparty WHERE display_name = 'Already External' AND kind = 'DONOR'"));
    }

    private static int queryInt(Database database, String sql) throws SQLException
    {
        try (Connection connection = DriverManager.getConnection(database.getJdbcUrl(), database.getUser(), database.getPass());
             PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery())
        {
            rs.next();
            return rs.getInt(1);
        }
    }

    private static String queryString(Database database, String sql) throws SQLException
    {
        try (Connection connection = DriverManager.getConnection(database.getJdbcUrl(), database.getUser(), database.getPass());
             PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery())
        {
            rs.next();
            return rs.getString(1);
        }
    }
}
