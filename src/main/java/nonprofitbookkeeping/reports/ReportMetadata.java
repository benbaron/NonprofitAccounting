package nonprofitbookkeeping.reports;

/**
 * Holds metadata associated with a generated report.
 * This includes information such as the report's name, creation timestamp,
 * and the file path where the report is stored.
 */
public class ReportMetadata
{
	private final String reportName;
	private final String created;
	private final String filePath;

	public ReportMetadata(String reportName, String created, String filePath)
	{
		this.reportName = reportName;
		this.created = created;
		this.filePath = filePath;
	}

	public ReportMetadata(String reportName, String created, String filePath,
		String ignoredDescription)
	{
		this(reportName, created, filePath);
	}

	public String getReportName()
	{
		return this.reportName;
	}

	public String getCreated()
	{
		return this.created;
	}

	public String getFilePath()
	{
		return this.filePath;
	}
}
