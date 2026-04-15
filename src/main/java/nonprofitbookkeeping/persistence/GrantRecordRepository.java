package nonprofitbookkeeping.persistence;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import nonprofitbookkeeping.core.Database;
import nonprofitbookkeeping.model.Grant;
import nonprofitbookkeeping.model.GrantTraceabilityRow;

import java.io.IOException;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
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
			SELECT grant_record_id, grant_id, grantor, amount, date_awarded_text, purpose, status,
			       restriction_class, compliance_status, next_report_due, details
			FROM grant_record
			WHERE journal_txn_id IS NULL
			  AND canonical_txn_id IS NULL
			  AND donor_id IS NULL
			  AND person_id IS NULL
			  AND counterparty_id IS NULL
			  AND fund_id IS NULL
			  AND activity_id IS NULL
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
				if (grant.getGrantor() == null || grant.getGrantor().isBlank())
				{
					grant.setGrantor(rs.getString("grantor"));
				}
				if (grant.getAmount() == null)
				{
					grant.setAmount(rs.getBigDecimal("amount"));
				}
				if (grant.getDateAwarded() == null || grant.getDateAwarded().isBlank())
				{
					grant.setDateAwarded(rs.getString("date_awarded_text"));
				}
				if (grant.getPurpose() == null || grant.getPurpose().isBlank())
				{
					grant.setPurpose(rs.getString("purpose"));
				}
				if (grant.getStatus() == null || grant.getStatus().isBlank())
				{
					grant.setStatus(rs.getString("status"));
				}
				if (grant.getRestrictionClass() == null || grant.getRestrictionClass().isBlank())
				{
					grant.setRestrictionClass(rs.getString("restriction_class"));
				}
				if (grant.getComplianceStatus() == null || grant.getComplianceStatus().isBlank())
				{
					grant.setComplianceStatus(rs.getString("compliance_status"));
				}
				if (grant.getNextReportDue() == null || grant.getNextReportDue().isBlank())
				{
					Date due = rs.getDate("next_report_due");
					grant.setNextReportDue(due == null ? "" : due.toString());
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
			  AND canonical_txn_id IS NULL
			  AND donor_id IS NULL
			  AND person_id IS NULL
			  AND counterparty_id IS NULL
			  AND fund_id IS NULL
			  AND activity_id IS NULL
			"""))
		{
			ps.executeUpdate();
		}
	}

	private void insertRows(Connection c, List<Grant> grants) throws SQLException
	{
			String upsert = """
				MERGE INTO grant_record(
				  grant_record_id, grant_id, grantor, amount, date_awarded_text, purpose, status,
				  restriction_class, compliance_status, next_report_due,
				  donor_id, person_id, counterparty_id, fund_id, activity_id, journal_txn_id, canonical_txn_id,
				  details, updated_at
				) KEY(grant_record_id)
				VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, NULL, NULL, NULL, NULL, NULL, NULL, NULL, ?, CURRENT_TIMESTAMP)
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
					ps.setString(3, grant.getGrantor());
					ps.setBigDecimal(4, grant.getAmount());
					ps.setString(5, grant.getDateAwarded());
					ps.setString(6, grant.getPurpose());
					ps.setString(7, grant.getStatus());
					ps.setString(8, defaultIfBlank(grant.getRestrictionClass(), "RESTRICTED"));
					ps.setString(9, defaultIfBlank(grant.getComplianceStatus(), "IN_GOOD_STANDING"));
					String due = trimToNull(grant.getNextReportDue());
					if (due == null)
					{
						ps.setDate(10, null);
					}
					else
					{
						try
						{
							ps.setDate(10, Date.valueOf(due));
						}
						catch (IllegalArgumentException ex)
						{
							ps.setDate(10, null);
						}
					}
					ps.setString(11, toPayload(grant));
					ps.addBatch();
				}
			ps.executeBatch();
		}
	}

	private static String defaultIfBlank(String value, String defaultValue)
	{
		return value == null || value.isBlank() ? defaultValue : value;
	}

	private static String trimToNull(String value)
	{
		if (value == null)
		{
			return null;
		}
		String trimmed = value.trim();
		return trimmed.isEmpty() ? null : trimmed;
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

	/**
	 * Returns grant traceability rows from {@code v_grant_restriction_reporting}.
	 */
	public List<GrantTraceabilityRow> listTraceabilityRows() throws SQLException
	{
		List<GrantTraceabilityRow> rows = new ArrayList<>();
		String sql = """
			SELECT grant_record_id, grant_id, grant_reference_number, status, compliance_status,
			       restriction_class, fund_code, fund_name, activity_code, activity_name,
			       donor_or_contact, awarded_amount, recognized_amount, deferred_amount,
			       unrecognized_balance, next_report_due
			FROM v_grant_restriction_reporting
			ORDER BY next_report_due NULLS LAST, grant_id
			""";
		try (Connection c = Database.get().getConnection();
		     PreparedStatement ps = c.prepareStatement(sql);
		     ResultSet rs = ps.executeQuery())
		{
			while (rs.next())
			{
				rows.add(toTraceabilityRow(rs));
			}
		}
		return rows;
	}

	/**
	 * Returns compliance alert rows (late/at-risk or due before provided date).
	 *
	 * @param asOf cutoff date used to identify due grants
	 */
	public List<GrantTraceabilityRow> listComplianceAlerts(LocalDate asOf) throws SQLException
	{
		List<GrantTraceabilityRow> rows = new ArrayList<>();
		String sql = """
			SELECT grant_record_id, grant_id, grant_reference_number, status, compliance_status,
			       restriction_class, fund_code, fund_name, activity_code, activity_name,
			       donor_or_contact, awarded_amount, recognized_amount, deferred_amount,
			       unrecognized_balance, next_report_due
			FROM v_grant_restriction_reporting
			WHERE compliance_status IN ('LATE_REPORT','AT_RISK')
			   OR (next_report_due IS NOT NULL AND next_report_due < ?)
			ORDER BY next_report_due NULLS LAST, grant_id
			""";
		try (Connection c = Database.get().getConnection();
		     PreparedStatement ps = c.prepareStatement(sql))
		{
			ps.setDate(1, Date.valueOf(asOf));
			try (ResultSet rs = ps.executeQuery())
			{
				while (rs.next())
				{
					rows.add(toTraceabilityRow(rs));
				}
			}
		}
		return rows;
	}

	private static GrantTraceabilityRow toTraceabilityRow(ResultSet rs) throws SQLException
	{
		GrantTraceabilityRow row = new GrantTraceabilityRow();
		row.setGrantRecordId(rs.getString("grant_record_id"));
		row.setGrantId(rs.getString("grant_id"));
		row.setGrantReferenceNumber(rs.getString("grant_reference_number"));
		row.setStatus(rs.getString("status"));
		row.setComplianceStatus(rs.getString("compliance_status"));
		row.setRestrictionClass(rs.getString("restriction_class"));
		row.setFundCode(rs.getString("fund_code"));
		row.setFundName(rs.getString("fund_name"));
		row.setActivityCode(rs.getString("activity_code"));
		row.setActivityName(rs.getString("activity_name"));
		row.setDonorOrContact(rs.getString("donor_or_contact"));
		row.setAwardedAmount(rs.getBigDecimal("awarded_amount"));
		row.setRecognizedAmount(rs.getBigDecimal("recognized_amount"));
		row.setDeferredAmount(rs.getBigDecimal("deferred_amount"));
		row.setUnrecognizedBalance(rs.getBigDecimal("unrecognized_balance"));
		Date nextDue = rs.getDate("next_report_due");
		row.setNextReportDue(nextDue == null ? null : nextDue.toLocalDate());
		return row;
	}
}
