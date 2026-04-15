package nonprofitbookkeeping.tools;

import nonprofitbookkeeping.core.Database;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.*;

class H2SchemaMigratorTest
{
    @TempDir
    Path tempDir;

    @Test
    void normalizeDbPath_stripsH2Suffixes()
    {
        Path base = Path.of("/tmp/company");
        assertEquals(base, H2SchemaMigrator.normalizeDbPath(Path.of("/tmp/company.mv.db")));
        assertEquals(base, H2SchemaMigrator.normalizeDbPath(Path.of("/tmp/company.trace.db")));
        assertEquals(base, H2SchemaMigrator.normalizeDbPath(base));
    }


    @Test
    void backupPathFor_appendsCorruptBackupSuffix()
    {
        Path backup = H2SchemaMigrator.backupPathFor(Path.of("/tmp/company.mv.db"));
        assertTrue(backup.getFileName().toString().startsWith("company.mv.db.corrupt-"));
        assertTrue(backup.getFileName().toString().contains(".corrupt-"));
        assertTrue(backup.getFileName().toString().endsWith(".bak"));
    }
    @Test
    void migrate_upgradesSchemaAndOptionallyWritesScript() throws Exception
    {
        Path dbPath = tempDir.resolve("legacy-company");
        Path scriptPath = tempDir.resolve("after-migration.sql");

        Database.init(dbPath);
        try (Connection c = Database.get().getConnection(); Statement st = c.createStatement())
        {
            st.execute("CREATE TABLE IF NOT EXISTS legacy_probe(id INT PRIMARY KEY)");
            st.execute("INSERT INTO legacy_probe(id) VALUES (1)");
        }

        H2SchemaMigrator.migrate(dbPath.resolveSibling("legacy-company.mv.db"), scriptPath);

        Database.init(dbPath);
        try (Connection c = Database.get().getConnection(); Statement st = c.createStatement())
        {
            try (ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM schema_migration_history"))
            {
                assertTrue(rs.next());
                assertTrue(rs.getInt(1) >= 0);
            }
            try (ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM legacy_probe"))
            {
                assertTrue(rs.next());
                assertEquals(1, rs.getInt(1));
            }
            assertDoesNotThrow(() -> st.executeQuery("SELECT COUNT(*) FROM ledger_record"));
            assertDoesNotThrow(() -> st.executeQuery("SELECT COUNT(*) FROM bank_id_record"));
            assertDoesNotThrow(() -> st.executeQuery("SELECT COUNT(*) FROM banking_transaction_record"));
            assertDoesNotThrow(() -> st.executeQuery("SELECT match_status, source_fingerprint, anomaly_duplicate FROM banking_transaction_record"));
            assertDoesNotThrow(() -> st.executeQuery("SELECT period_start, period_end, status FROM bank_statement"));
            assertDoesNotThrow(() -> st.executeQuery("SELECT banking_record_id, match_group_id, link_status FROM ledger_record"));
            assertDoesNotThrow(() -> st.executeQuery("SELECT COUNT(*) FROM asset_record_detail"));
            assertDoesNotThrow(() -> st.executeQuery("SELECT asset_state, in_service_date, disposal_date, depreciable_basis, salvage_value, useful_life_months, posted_acquisition_txn_id, posted_disposal_txn_id FROM asset_record_detail"));
            assertDoesNotThrow(() -> st.executeQuery("SELECT COUNT(*) FROM inventory_asset_link"));
            assertDoesNotThrow(() -> st.executeQuery("SELECT link_type, is_primary_link, primary_asset_inventory_key FROM inventory_asset_link"));
            assertDoesNotThrow(() -> st.executeQuery("SELECT COUNT(*) FROM depreciation_run"));
            assertDoesNotThrow(() -> st.executeQuery("SELECT period_start, period_end, run_status, is_locked, locked_at, locked_by, posted_txn_id FROM depreciation_run"));
            assertDoesNotThrow(() -> st.executeQuery("SELECT COUNT(*) FROM depreciation_record"));
            assertDoesNotThrow(() -> st.executeQuery("SELECT period_start, period_end, sequence_in_run, posted_journal_txn_id, reversal_journal_txn_id FROM depreciation_record"));
            assertDoesNotThrow(() -> st.executeQuery("SELECT COUNT(*) FROM depreciation_run_event"));
            assertDoesNotThrow(() -> st.executeQuery("SELECT COUNT(*) FROM grant_record"));
            assertDoesNotThrow(() -> st.executeQuery("SELECT grantor, amount, date_awarded_text, purpose, status FROM grant_record"));
            assertDoesNotThrow(() -> st.executeQuery("SELECT COUNT(*) FROM sale_record"));
            assertDoesNotThrow(() -> st.executeQuery("SELECT COUNT(*) FROM config_release"));
            assertDoesNotThrow(() -> st.executeQuery("SELECT COUNT(*) FROM statement_section"));
            assertDoesNotThrow(() -> st.executeQuery("SELECT COUNT(*) FROM account_statement_mapping"));
            assertDoesNotThrow(() -> st.executeQuery("SELECT COUNT(*) FROM schedule_requirement_rule"));
            assertDoesNotThrow(() -> st.executeQuery("SELECT COUNT(*) FROM config_change_event"));
        }

        assertTrue(Files.exists(scriptPath));
        assertTrue(Files.size(scriptPath) > 0L);
    }
}
