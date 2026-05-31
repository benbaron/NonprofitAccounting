package nonprofitbookkeeping.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import nonprofitbookkeeping.core.Database;
import nonprofitbookkeeping.persistence.DocumentRepository;

class FundNameLookupTest
{
	@TempDir
	Path tempDir;

	@Test
	void listActiveFundNamesIncludesLegacyFundsDocument() throws Exception
	{
		Database.init(this.tempDir.resolve("fund-lookup"));
		Database.get().ensureSchema();

		try (Connection connection = Database.get().getConnection();
			PreparedStatement statement = connection.prepareStatement(
				"INSERT INTO fund(code, name, fund_type, is_active) VALUES (?, ?, 'UNRESTRICTED', TRUE)"))
		{
			statement.setString(1, "OPERATIONS");
			statement.setString(2, "Operations");
			statement.executeUpdate();
		}
		new DocumentRepository().upsert("funds", """
			[
			  {"name":"General Fund","balance":0},
			  {"name":"Building Fund","balance":0},
			  {"name":"Missions Fund","balance":0}
			]
			""");

		assertEquals(List.of("Building Fund", "General Fund", "Missions Fund", "Operations"),
			FundNameLookup.listActiveFundNames());
	}
}
