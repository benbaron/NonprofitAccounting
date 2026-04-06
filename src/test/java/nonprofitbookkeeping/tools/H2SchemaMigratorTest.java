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
        }

        assertTrue(Files.exists(scriptPath));
        assertTrue(Files.size(scriptPath) > 0L);
    }
}
