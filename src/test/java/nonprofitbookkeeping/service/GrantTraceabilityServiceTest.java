package nonprofitbookkeeping.service;

import nonprofitbookkeeping.TestDatabase;
import nonprofitbookkeeping.core.Database;
import nonprofitbookkeeping.model.GrantTraceabilityRow;
import nonprofitbookkeeping.persistence.GrantRecordRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class GrantTraceabilityServiceTest
{
	@TempDir
	Path tempDir;

	@Test
	void listComplianceAlerts_returnsOverdueOrAtRiskRows() throws Exception
	{
		TestDatabase.reset(this.tempDir);
		seedGrant("g-1", "G-1", "AT_RISK", LocalDate.now().plusDays(30));
		seedGrant("g-2", "G-2", "IN_GOOD_STANDING", LocalDate.now().minusDays(1));

		GrantTraceabilityService service =
			new GrantTraceabilityService(new GrantRecordRepository());
		List<GrantTraceabilityRow> alerts = service.listComplianceAlerts(LocalDate.now());

		assertEquals(2, alerts.size());
		assertFalse(alerts.stream().noneMatch(r -> "G-1".equals(r.getGrantId())));
		assertFalse(alerts.stream().noneMatch(r -> "G-2".equals(r.getGrantId())));
	}

	private static void seedGrant(String recordId, String grantId,
		String complianceStatus, LocalDate due) throws Exception
	{
		try (Connection c = Database.get().getConnection();
		     PreparedStatement ps = c.prepareStatement("""
			INSERT INTO grant_record(
			  grant_record_id, grant_id, grantor, amount, date_awarded_text, purpose, status,
			  restriction_class, compliance_status, next_report_due
			) VALUES (?, ?, ?, ?, '2026-01-01', 'Testing', 'OPEN', 'RESTRICTED', ?, ?)
			"""))
		{
			ps.setString(1, recordId);
			ps.setString(2, grantId);
			ps.setString(3, "Grantor");
			ps.setBigDecimal(4, BigDecimal.TEN);
			ps.setString(5, complianceStatus);
			ps.setDate(6, java.sql.Date.valueOf(due));
			ps.executeUpdate();
		}
	}
}
