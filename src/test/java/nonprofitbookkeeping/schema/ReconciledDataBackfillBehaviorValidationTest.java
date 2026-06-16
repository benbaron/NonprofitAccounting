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

class ReconciledDataBackfillBehaviorValidationTest
{
    @TempDir
    Path tempDir;

    @Test
    void ensureSchemaBackfillsLegacyJournalRowsAndParsesLegacyDates() throws Exception
    {
        Database.init(tempDir.resolve("reconciled-data-backfill-behavior"));
        Database database = Database.get();

        migrateWithFlyway(database, "14");
        seedReconciledBackfillScenario(database);
        migrateWithFlyway(database);

        database.ensureSchema();
        database.ensureSchema();

        assertEquals(1, queryInt(database,
            "SELECT COUNT(*) FROM chart_of_accounts WHERE name = 'Default Legacy Chart' AND version = 'legacy'"));
        assertEquals(1, queryInt(database,
            "SELECT COUNT(*) FROM fund WHERE id = 1 AND code = 'GENERAL'"));
        assertEquals(2, queryInt(database,
            "SELECT COUNT(*) FROM account WHERE chart_id IS NOT NULL AND code = account_number AND normal_balance IS NOT NULL"));
        assertEquals("2026-06-15", queryString(database,
            "SELECT CAST(txn_date AS VARCHAR(10)) FROM txn WHERE id = 3001"));
        assertEquals(1, queryInt(database,
            "SELECT COUNT(*) FROM legacy_txn_map WHERE legacy_txn_id = 3001 AND canonical_txn_id = 3001"));
        assertEquals(1, queryInt(database, """
            SELECT COUNT(*)
            FROM txn_split ts
            JOIN account a ON a.id = ts.account_id
            WHERE ts.txn_id = 3001
              AND a.account_number = '1000'
              AND ts.amount_signed = 125.00
            """));
        assertEquals(1, queryInt(database, """
            SELECT COUNT(*)
            FROM txn_split ts
            JOIN account a ON a.id = ts.account_id
            WHERE ts.txn_id = 3001
              AND a.account_number = '4000'
              AND ts.amount_signed = -125.00
            """));
        assertEquals(1, queryInt(database,
            "SELECT COUNT(*) FROM counterparty WHERE display_name = 'Pat Donor' AND kind = 'DONOR'"));
        assertEquals(1, queryInt(database,
            "SELECT COUNT(*) FROM counterparty WHERE display_name = 'Pat Person' AND kind = 'DONOR'"));
        assertEquals(1, queryInt(database, """
            SELECT COUNT(*)
            FROM schema_migration_history
            WHERE migration_key = 'reconciled-backfill-v1'
            """));
    }

    @Test
    void flywaySeedsDefaultLegacyChartAndFundBeforeEnsureSchema() throws Exception
    {
        Database.init(tempDir.resolve("default-chart-fund-flyway-seed"));
        Database database = Database.get();

        migrateWithFlyway(database);

        assertEquals(1, queryInt(database,
            "SELECT COUNT(*) FROM chart_of_accounts WHERE name = 'Default Legacy Chart' AND version = 'legacy'"));
        assertEquals(1, queryInt(database,
            "SELECT COUNT(*) FROM fund WHERE id = 1 AND code = 'GENERAL'"));

        database.ensureSchema();

        assertEquals(1, queryInt(database,
            "SELECT COUNT(*) FROM chart_of_accounts WHERE name = 'Default Legacy Chart' AND version = 'legacy'"));
        assertEquals(1, queryInt(database,
            "SELECT COUNT(*) FROM fund WHERE id = 1 AND code = 'GENERAL'"));
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

    private static void seedReconciledBackfillScenario(Database database) throws SQLException
    {
        try (Connection connection = DriverManager.getConnection(database.getJdbcUrl(), database.getUser(), database.getPass());
             Statement st = connection.createStatement())
        {
            st.execute("INSERT INTO schema_migration_history(migration_key) VALUES ('operational-link-backfill-v1')");
            st.execute("INSERT INTO account(account_number, name, increase_side) VALUES ('1000', 'Cash', 'DEBIT')");
            st.execute("INSERT INTO account(account_number, name, increase_side) VALUES ('4000', 'Revenue', 'CREDIT')");
            st.execute("INSERT INTO donor(external_id, name, email, phone) VALUES ('donor-1', 'Pat Donor', 'pat@example.org', '555-0100')");
            st.execute("INSERT INTO person(name, email, phone) VALUES ('Pat Person', 'person@example.org', '555-0101')");
            st.execute("""
                INSERT INTO journal_transaction(id, date_text, to_from, memo)
                VALUES (3001, '6/15/2026', 'Pat Donor', 'legacy donation')
                """);
            st.execute("""
                INSERT INTO journal_entry(txn_id, amount, account_number, account_name, account_side)
                VALUES (3001, 125.00, '1000', 'Cash', 'DEBIT')
                """);
            st.execute("""
                INSERT INTO journal_entry(txn_id, amount, account_number, account_name, account_side)
                VALUES (3001, 125.00, '4000', 'Revenue', 'CREDIT')
                """);
        }
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
