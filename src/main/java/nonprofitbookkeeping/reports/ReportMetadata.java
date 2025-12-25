package nonprofitbookkeeping.reports;

/**
 * Compatibility wrapper around {@link nonprofitbookkeeping.reports.jasper.runtime.ReportMetadata}.
 */
public class ReportMetadata
	extends nonprofitbookkeeping.reports.jasper.runtime.ReportMetadata
{
	public ReportMetadata(String reportName, String created, String filePath)
	{
		super(reportName, created, filePath);
	}
	
	public ReportMetadata(String reportName, String created, String filePath,
		String unused)
	{
		super(reportName, created, filePath, unused);
	}
}
