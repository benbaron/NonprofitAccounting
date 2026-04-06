package nonprofitbookkeeping.tools;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import nonprofitbookkeeping.core.Database;

class H2ScriptCompanyExporterTest
{
	@Test
	void exportScriptWritesH2SqlFile(@TempDir Path tempDir) throws Exception
	{
		Path dbBase = tempDir.resolve("company-db");
		Database.init(dbBase);
		Database.get().ensureSchema();

		Path exportFile = tempDir.resolve("backup.sql");
		H2ScriptCompanyExporter.exportScript(exportFile);

		assertTrue(Files.exists(exportFile));
		String script = Files.readString(exportFile);
		assertTrue(script.contains("CREATE"));
	}

	@Test
	void extractDatabaseBasePathParsesJdbcUrl()
	{
		Path parsed = H2ScriptCompanyExporter.extractDatabaseBasePath(
			"jdbc:h2:file:C:/Users/benba/NonprofitBookkeeping/test10;AUTO_SERVER=TRUE;MODE=MySQL");
		assertEquals(Path.of("C:/Users/benba/NonprofitBookkeeping/test10").toAbsolutePath(), parsed);
	}

	@Test
	void extractDatabaseBasePathRejectsNonFileJdbcUrl()
	{
		assertThrows(IllegalStateException.class,
			() -> H2ScriptCompanyExporter.extractDatabaseBasePath("jdbc:h2:mem:test"));
	}

	@Test
	void isFileCorruptionDetectsKnownMarkers()
	{
		SQLException ex = new SQLException("File corrupted while reading record");
		assertTrue(H2ScriptCompanyExporter.isFileCorruption(ex));
	}
}
