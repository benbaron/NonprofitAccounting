package nonprofitbookkeeping.ui;

import nonprofitbookkeeping.core.Database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Loads fund names from the active company database for fund-name selectors.
 */
public final class FundNameLookup
{
	private static final String SQL =
		"SELECT name FROM fund WHERE is_active = TRUE ORDER BY name";

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

		List<String> names = new ArrayList<>();
		try (Connection connection = Database.get().getConnection();
			PreparedStatement statement = connection.prepareStatement(SQL);
			ResultSet rs = statement.executeQuery())
		{
			while (rs.next())
			{
				String name = rs.getString("name");
				if (name != null && !name.isBlank())
				{
					names.add(name);
				}
			}
		}
		return names;
	}
}
