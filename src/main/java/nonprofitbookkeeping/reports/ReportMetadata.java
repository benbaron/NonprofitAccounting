/**
 * nonprofit-scaledger-ribbon.zip_expanded
 * ReportMetadata.java
 * ReportMetadata
 */
package nonprofitbookkeeping.reports;

/**
 * Holds metadata associated with a generated report.
 * This includes information such as the report's name, creation timestamp,
 * and the file path where the report is stored.
 */
public class ReportMetadata
{
	private String reportName;
	private String created; // e.g., ISO date/time string
	private String filePath;

	/**
	 * Constructs a new ReportMetadata object.
	 *
	 * @param reportName The name of the report.
	 * @param created The creation timestamp of the report (e.g., as an ISO date/time string).
	 * @param filePath The file path where the report is stored.
	 */
	public ReportMetadata(String reportName, String created, String filePath) {
		this.reportName = reportName;
		this.created = created;
		this.filePath = filePath;
	}

	/**  
	 * Constructor ReportMetadata
	 * @param string
	 * @param format
	 * @param nonExistentFilePath
	 * @param string2
	 */
	public ReportMetadata(String string, String format, String nonExistentFilePath, String string2)
	{
		// TODO Auto-generated constructor stub
	}

	/**
	 * Gets the name of the report.
	 *
	 * @return The report name.
	 */
	public String getReportName()
	{
		return this.reportName;
	}

	/**
	 * Gets the creation timestamp of the report.
	 *
	 * @return The creation timestamp, typically as an ISO date/time string.
	 */
	public String getCreated()
	{
		return this.created;
	}

	/**
	 * Gets the file path where the report is stored.
	 *
	 * @return The file path of the report.
	 */
	public String getFilePath()
	{
		return this.filePath;
	}
	
}
