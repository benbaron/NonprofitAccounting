package nonprofitbookkeeping.ui;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import nonprofitbookkeeping.core.Database;
import nonprofitbookkeeping.persistence.DocumentRepository;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Loads fund names from the active company database for fund-name selectors.
 */
public final class FundNameLookup
{
	private static final String SQL =
		"SELECT name FROM fund WHERE is_active = TRUE ORDER BY name";
	private static final String LEGACY_FUNDS_DOCUMENT = "funds";
	private static final ObjectMapper MAPPER = new ObjectMapper();

	private FundNameLookup()
	{
	}

	/**
	 * Returns active fund names in display order.
	 *
	 * @return active fund names, or an empty list when no database is open
	 * @throws SQLException when the active database cannot be queried
	 */
	public static List<String> listActiveFundNames() throws SQLException
	{
		if (!Database.isInitialized())
		{
			return List.of();
		}

		Set<String> names = new LinkedHashSet<>();
		loadNormalizedFundNames(names);
		loadLegacyDocumentFundNames(names);
		return names.stream().sorted(String.CASE_INSENSITIVE_ORDER).toList();
	}

	private static void loadNormalizedFundNames(Set<String> names)
		throws SQLException
	{
		try (Connection connection = Database.get().getConnection();
			PreparedStatement statement = connection.prepareStatement(SQL);
			ResultSet rs = statement.executeQuery())
		{
			while (rs.next())
			{
				addName(names, rs.getString("name"));
			}
		}
	}

	private static void loadLegacyDocumentFundNames(Set<String> names)
		throws SQLException
	{
		String payload = new DocumentRepository().find(LEGACY_FUNDS_DOCUMENT)
			.orElse(null);
		if (payload == null || payload.isBlank())
		{
			return;
		}

		try
		{
			JsonNode root = MAPPER.readTree(payload);
			if (!root.isArray())
			{
				return;
			}
			for (JsonNode fund : root)
			{
				addName(names, fund.path("name").asText(null));
			}
		}
		catch (IOException ex)
		{
			SQLException sqlException = new SQLException(
				"Unable to parse persisted fund choices.", ex);
			throw sqlException;
		}
	}

	private static void addName(Set<String> names, String name)
	{
		if (name != null && !name.isBlank())
		{
			names.add(name.trim());
		}
	}
}
