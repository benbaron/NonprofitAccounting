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

class OperationalLinkBackfillBehaviorValidationTest
{
    @TempDir
    Path tempDir;

    @Test
    void flywayBackfillsOperationalLinksAndQueuesUnmatchedRecordsBeforeEnsureSchema() throws Exception
    {
        Database.init(this.tempDir.resolve("operational-link-backfill-behavior"));
        Database database = Database.get();

        migrateWithFlyway(database, "17");
        seedOperationalBackfillScenario(database);
        migrateWithFlyway(database);

        database.ensureSchema();

        assertEquals(1001, queryInt(database,
            "SELECT journal_txn_id FROM donation_record WHERE donation_id = 'donation-matched'"));
        assertEquals(1, queryInt(database, """
            SELECT COUNT(*)
            FROM donation_journal_link
            WHERE donation_id = 'donation-matched'
              AND journal_txn_id = 1001
              AND link_role = 'ORIGINAL'
            """));
        assertEquals(1, queryInt(database, """
            SELECT COUNT(*)
            FROM operational_link_backfill_queue
            WHERE module_name = 'DONATION'
              AND domain_id = 'donation-unmatched'
              AND issue_code = 'NO_MATCHED_JOURNAL_TXN'
            """));
        assertEquals(2001, queryInt(database,
            "SELECT journal_txn_id FROM grant_record WHERE grant_record_id = 'grant-matched'"));
        assertEquals(1, queryInt(database, """
            SELECT COUNT(*)
            FROM operational_link_backfill_queue
            WHERE module_name = 'GRANT'
              AND domain_id = 'grant-unmatched'
              AND issue_code = 'NO_MATCHED_JOURNAL_TXN'
            """));
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

    private static void seedOperationalBackfillScenario(Database database) throws SQLException
    {
        try (Connection connection = DriverManager.getConnection(database.getJdbcUrl(), database.getUser(), database.getPass());
             Statement st = connection.createStatement())
        {
            st.execute("INSERT INTO account(account_number, name) VALUES ('1000', 'Cash')");
            st.execute("INSERT INTO account(account_number, name) VALUES ('4000', 'Revenue')");
            st.execute("INSERT INTO donor(external_id, name) VALUES ('donor-1', 'Matched Donor')");
            st.execute("INSERT INTO donor(external_id, name) VALUES ('donor-missing', 'Unmatched Donor')");
            st.execute("""
                INSERT INTO journal_transaction(id, date_text, to_from, memo)
                VALUES (1001, '2026-06-01', 'donor-1', 'matched donation')
                """);
            st.execute("""
                INSERT INTO journal_entry(txn_id, amount, account_number, account_side)
                VALUES (1001, 125.00, '4000', 'CREDIT')
                """);
            st.execute("""
                INSERT INTO donation_record(
                    donation_id, donor_external_id, donation_date, amount,
                    cash_account_number, revenue_account_number
                )
                VALUES ('donation-matched', 'donor-1', DATE '2026-06-01', 125.00, '1000', '4000')
                """);
            st.execute("""
                INSERT INTO donation_record(
                    donation_id, donor_external_id, donation_date, amount,
                    cash_account_number, revenue_account_number
                )
                VALUES ('donation-unmatched', 'donor-missing', DATE '2026-06-02', 75.00, '1000', '4000')
                """);
            st.execute("""
                INSERT INTO journal_transaction(id, date_text, memo)
                VALUES (2001, '2026-06-03', 'matched grant')
                """);
            st.execute("""
                INSERT INTO transaction_info(txn_id, k, v)
                VALUES (2001, 'domain_record_id', 'grant-matched')
                """);
            st.execute("""
                INSERT INTO grant_record(grant_record_id, grant_id, grantor, amount, status, details)
                VALUES ('grant-matched', 'G-1', 'Matched Grantor', 200.00, 'ACTIVE', '{}')
                """);
            st.execute("""
                INSERT INTO grant_record(grant_record_id, grant_id, grantor, amount, status, details)
                VALUES ('grant-unmatched', 'G-2', 'Unmatched Grantor', 50.00, 'ACTIVE', '{}')
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
}
