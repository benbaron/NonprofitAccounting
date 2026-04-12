package nonprofitbookkeeping.persistence;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import nonprofitbookkeeping.core.Database;
import nonprofitbookkeeping.model.Grant;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Repository for grant rows persisted in {@code grant_record}.
 *
 * <p>This repository manages "service-owned" grants, which are rows without
 * donor/person/fund/journal linkage columns populated. Linked grant rows can
 * still coexist in the same table and are not touched by replace operations.</p>
 */
public class GrantRecordRepository
{
	private static final ObjectMapper MAPPER = new ObjectMapper()
		.enable(SerializationFeature.INDENT_OUTPUT);

	/**
	 * Replaces service-owned grant rows with the supplied list.
	 *
	 * @param grants grants to persist
	 * @throws SQLException if persistence fails
	 */
	public void replaceStandaloneGrants(List<Grant> grants) throws SQLException
	{
		try (Connection c = Database.get().getConnection())
		{
			c.setAutoCommit(false);
			try
			{
				deleteStandaloneRows(c);
				insertRows(c, grants == null ? List.of() : grants);
				c.commit();
			}
			catch (SQLException e)
			{
				c.rollback();
				throw e;
			}
			finally
			{
				c.setAutoCommit(true);
			}
		}
	}

	/**
	 * Lists service-owned grants from {@code grant_record}.
	 *
	 * @return stored grants
	 * @throws SQLException if the query fails
	 */
	public List<Grant> listStandaloneGrants() throws SQLException
	{
		List<Grant> rows = new ArrayList<>();
		String sql = """
			SELECT grant_record_id, grant_id, details
			FROM grant_record
			WHERE journal_txn_id IS NULL
			  AND donor_id IS NULL
			  AND person_id IS NULL
			  AND fund_id IS NULL
			ORDER BY grant_record_id
			""";
		try (Connection c = Database.get().getConnection();
		     PreparedStatement ps = c.prepareStatement(sql);
		     ResultSet rs = ps.executeQuery())
		{
			while (rs.next())
			{
				String payload = rs.getString("details");
				Grant grant = fromPayload(payload);
				if (grant.getGrantId() == null || grant.getGrantId().isBlank())
				{
					grant.setGrantId(rs.getString("grant_id"));
				}
				rows.add(grant);
			}
		}
		return rows;
	}

	private void deleteStandaloneRows(Connection c) throws SQLException
	{
		try (PreparedStatement ps = c.prepareStatement("""
			DELETE FROM grant_record
			WHERE journal_txn_id IS NULL
			  AND donor_id IS NULL
			  AND person_id IS NULL
			  AND fund_id IS NULL
			"""))
		{
			ps.executeUpdate();
		}
	}

	private void insertRows(Connection c, List<Grant> grants) throws SQLException
	{
		String upsert = """
			MERGE INTO grant_record(
			  grant_record_id, grant_id, donor_id, person_id, fund_id, journal_txn_id, details, updated_at
			) KEY(grant_record_id)
			VALUES (?, ?, NULL, NULL, NULL, NULL, ?, CURRENT_TIMESTAMP)
			""";
		try (PreparedStatement ps = c.prepareStatement(upsert))
		{
			for (Grant grant : grants)
			{
				if (grant == null || grant.getGrantId() == null || grant.getGrantId().isBlank())
				{
					continue;
				}
				ps.setString(1, grant.getGrantId());
				ps.setString(2, grant.getGrantId());
				ps.setString(3, toPayload(grant));
				ps.addBatch();
			}
			ps.executeBatch();
		}
	}

	private static String toPayload(Grant grant) throws SQLException
	{
		try
		{
			return MAPPER.writeValueAsString(grant);
		}
		catch (IOException e)
		{
			throw new SQLException("Failed to serialize grant payload", e);
		}
	}

	private static Grant fromPayload(String payload) throws SQLException
	{
		if (payload == null || payload.isBlank())
		{
			return new Grant();
		}
		try
		{
			return MAPPER.readValue(payload, Grant.class);
		}
		catch (IOException e)
		{
			throw new SQLException("Failed to deserialize grant payload", e);
		}
	}
}
