
package nonprofitbookkeeping.reports;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

class ReportMetadataTest
{
	
	@Test
	@DisplayName("Constructor and Getters: Test with valid reportName, created timestamp, and filePath")
	void testConstructorAndGetters_withValidInputs()
	{
		String reportName = "Monthly Financial Summary";
		String created = "2023-01-15T10:30:00Z";
		String filePath = "/reports/monthly_summary_jan_2023.pdf";
		
		ReportMetadata metadata =
			new ReportMetadata(reportName, created, filePath);
		
		assertEquals(reportName, metadata.getReportName(),
			"getReportName() should return the name provided in constructor.");
		assertEquals(created, metadata.getCreated(),
			"getCreated() should return the timestamp provided in constructor.");
		assertEquals(filePath, metadata.getFilePath(),
			"getFilePath() should return the path provided in constructor.");
		
	}
	
	@Test
	@DisplayName("Constructor and Getters: Test with null reportName")
	void testConstructorAndGetters_withNullReportName()
	{
		String created = "2023-02-20T11:00:00Z";
		String filePath = "/reports/annual_report_2022.docx";
		ReportMetadata metadata = new ReportMetadata(null, created, filePath);
		
		assertNull(metadata.getReportName(),
			"getReportName() should return null if null was passed for reportName.");
		assertEquals(created, metadata.getCreated());
		assertEquals(filePath, metadata.getFilePath());
		
	}
	
	@Test
	@DisplayName("Constructor and Getters: Test with null created timestamp")
	void testConstructorAndGetters_withNullCreated()
	{
		String reportName = "User Activity Log";
		String filePath = "/logs/user_activity_2023_Q1.log";
		ReportMetadata metadata =
			new ReportMetadata(reportName, null, filePath);
		
		assertEquals(reportName, metadata.getReportName());
		assertNull(metadata.getCreated(),
			"getCreated() should return null if null was passed for created timestamp.");
		assertEquals(filePath, metadata.getFilePath());
		
	}
	
	@Test
	@DisplayName("Constructor and Getters: Test with null filePath")
	void testConstructorAndGetters_withNullFilePath()
	{
		String reportName = "Inventory Stock Levels";
		String created = "2023-03-10T00:00:00Z";
		ReportMetadata metadata = new ReportMetadata(reportName, created, null);
		
		assertEquals(reportName, metadata.getReportName());
		assertEquals(created, metadata.getCreated());
		assertNull(metadata.getFilePath(),
			"getFilePath() should return null if null was passed for filePath.");
		
	}
	
	@Test
	@DisplayName("Constructor and Getters: Test with all parameters as null")
	void testConstructorAndGetters_withAllNull()
	{
		ReportMetadata metadata = new ReportMetadata(null, null, null);
		
		assertNull(metadata.getReportName(),
			"getReportName() should be null if all inputs are null.");
		assertNull(metadata.getCreated(),
			"getCreated() should be null if all inputs are null.");
		assertNull(metadata.getFilePath(),
			"getFilePath() should be null if all inputs are null.");
		
	}
	
	@Test
	@DisplayName("Constructor and Getters: Test with empty string for reportName")
	void testConstructorAndGetters_withEmptyReportName()
	{
		String created = "2023-04-01T12:00:00Z";
		String filePath = "/reports/data_export.csv";
		ReportMetadata metadata = new ReportMetadata("", created, filePath);
		
		assertEquals("", metadata.getReportName(),
			"getReportName() should return an empty string if an empty string was passed.");
		assertEquals(created, metadata.getCreated());
		assertEquals(filePath, metadata.getFilePath());
		
	}
	
	@Test
	@DisplayName("Constructor and Getters: Test with empty string for created timestamp")
	void testConstructorAndGetters_withEmptyCreated()
	{
		String reportName = "Audit Trail";
		String filePath = "/secure/audit_2023.txt";
		ReportMetadata metadata = new ReportMetadata(reportName, "", filePath);
		
		assertEquals(reportName, metadata.getReportName());
		assertEquals("", metadata.getCreated(),
			"getCreated() should return an empty string if an empty string was passed.");
		assertEquals(filePath, metadata.getFilePath());
		
	}
	
	@Test
	@DisplayName("Constructor and Getters: Test with empty string for filePath")
	void testConstructorAndGetters_withEmptyFilePath()
	{
		String reportName = "System Health Check";
		String created = "2023-05-25T18:30:00Z";
		ReportMetadata metadata = new ReportMetadata(reportName, created, "");
		
		assertEquals(reportName, metadata.getReportName());
		assertEquals(created, metadata.getCreated());
		assertEquals("", metadata.getFilePath(),
			"getFilePath() should return an empty string if an empty string was passed.");
		
	}
	
	@Test
	@DisplayName("Constructor and Getters: Test with all parameters as empty strings")
	void testConstructorAndGetters_withAllEmpty()
	{
		ReportMetadata metadata = new ReportMetadata("", "", "");
		
		assertEquals("", metadata.getReportName(),
			"getReportName() should be an empty string if all inputs are empty strings.");
		assertEquals("", metadata.getCreated(),
			"getCreated() should be an empty string if all inputs are empty strings.");
		assertEquals("", metadata.getFilePath(),
			"getFilePath() should be an empty string if all inputs are empty strings.");
		
	}
	
	@Test
	@DisplayName("Constructor and Getters: Test with mixed null and empty strings")
	void testConstructorAndGetters_withMixedNullAndEmpty()
	{
		ReportMetadata metadata = new ReportMetadata(null, "", "path/to/file");
		assertNull(metadata.getReportName());
		assertEquals("", metadata.getCreated());
		assertEquals("path/to/file", metadata.getFilePath());
		
	}
	
}
