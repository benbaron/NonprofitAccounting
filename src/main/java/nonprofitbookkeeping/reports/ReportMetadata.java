package nonprofitbookkeeping.reports;

/**
 * Compatibility wrapper for report metadata.
 */
public class ReportMetadata
	extends nonprofitbookkeeping.reports.jasper.runtime.ReportMetadata
{
	public ReportMetadata(String reportName, String created, String filePath)
	{
		super(reportName, created, filePath);
		
	}
	
	public ReportMetadata(String string, String format,
		String nonExistentFilePath, String string2)
	{
		super(string, format, nonExistentFilePath, string2);
		
	}
	
}
