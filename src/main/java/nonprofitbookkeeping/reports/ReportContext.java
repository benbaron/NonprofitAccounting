
package nonprofitbookkeeping.reports;

import java.time.LocalDate;

/**
 * ReportContext
 */
public class ReportContext
{
	public String reportType;
	public LocalDate startDate;
	public LocalDate endDate;
	public String outputFormat;
	
	/**
	 * @return LocalDate
	 */
	public LocalDate getEndDate()
	{
		return this.endDate;
	}
	
	/**
	 * @return LocalDate
	 */
	public LocalDate getStartDate()
	{
		return this.startDate;
	}
	
}
