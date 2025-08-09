/**
 * NonprofitAccounting
 * NewsletterRow.java
 * NewsletterRow
 */
package nonprofitbookkeeping.reports.datasource;

import java.math.BigDecimal;
import java.time.LocalDate;

/** One issue / subscription entry in the newsletter worksheet. */
public final class NewsletterRow extends IncomeRowBase
{
	
	private LocalDate issueDate; // date the issue was published / sold
	
	public NewsletterRow()
	{
	
	}
	
	public NewsletterRow(String issue,
		LocalDate issueDate,
		BigDecimal revenue)
	{
		super(issue, revenue);
		this.issueDate = issueDate;
		
	}
	
	public LocalDate getIssueDate()
	{
		return issueDate;
		
	}
	
	public void setIssueDate(LocalDate d)
	{
		this.issueDate = d;
		
	}
	
}
