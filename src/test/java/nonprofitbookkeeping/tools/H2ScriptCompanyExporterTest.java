package nonprofitbookkeeping.tools;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;

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
}

