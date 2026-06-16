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

class LegacyTxnMapBackfillBehaviorValidationTest
{
    @TempDir
    Path tempDir;

    @Test
    void ensureSchemaBackfillsLegacyTxnMapRowsAfterIdempotentTransactionMirroring() throws Exception
    {
        Database.init(tempDir.resolve("legacy-txn-map-backfill-behavior"));
        Database database = Database.get();

        migrateWithFlyway(database);
        seedLegacyTxnMapScenario(database);

        database.ensureSchema();
        database.ensureSchema();

        assertEquals(1, queryInt(database,
            "SELECT COUNT(*) FROM legacy_txn_map WHERE legacy_txn_id = 5001 AND canonical_txn_id = 5001"));
        assertEquals(1, queryInt(database,
            "SELECT COUNT(*) FROM legacy_txn_map WHERE legacy_txn_id = 5002 AND canonical_txn_id = 5002 AND checksum = 'already-mapped'"));
        assertEquals(1, queryInt(database,
            "SELECT COUNT(*) FROM legacy_txn_map WHERE legacy_txn_id = 5003 AND canonical_txn_id = 5003"));
        assertEquals(0, queryInt(database,
            "SELECT COUNT(*) FROM legacy_txn_map WHERE canonical_txn_id = 5004"));
        assertEquals(3, queryInt(database, "SELECT COUNT(*) FROM legacy_txn_map"));
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

    private static void seedLegacyTxnMapScenario(Database database) throws SQLException
    {
        try (Connection connection = DriverManager.getConnection(database.getJdbcUrl(), database.getUser(), database.getPass());
             Statement st = connection.createStatement())
        {
            st.execute("INSERT INTO journal_transaction(id, date_text, memo) VALUES (5001, '2026-06-15', 'matching legacy txn')");
            st.execute("INSERT INTO txn(id, txn_date, memo) VALUES (5001, DATE '2026-06-15', 'matching canonical txn')");
            st.execute("INSERT INTO journal_transaction(id, date_text, memo) VALUES (5002, '2026-06-16', 'pre-mapped legacy txn')");
            st.execute("INSERT INTO txn(id, txn_date, memo) VALUES (5002, DATE '2026-06-16', 'pre-mapped canonical txn')");
            st.execute("INSERT INTO legacy_txn_map(legacy_txn_id, canonical_txn_id, checksum) VALUES (5002, 5002, 'already-mapped')");
            st.execute("INSERT INTO journal_transaction(id, date_text, memo) VALUES (5003, '2026-06-17', 'legacy only txn')");
            st.execute("INSERT INTO txn(id, txn_date, memo) VALUES (5004, DATE '2026-06-18', 'canonical only txn')");
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
