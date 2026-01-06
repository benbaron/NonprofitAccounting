
package nonprofitbookkeeping.reports.jasper.runtime;

/**
 * Optional hook for report generators that need access to the {@link ReportContext}
 * after instantiation.
 */
public interface ReportContextAware
{
	void setReportContext(ReportContext context);
	
}
