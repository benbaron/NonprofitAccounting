
package nonprofitbookkeeping.reports.datasource;

/**
 * Simple bean representing a row in the Account Summary report.
 * Only minimal fields are provided to illustrate how data from
 * the application can be mapped to the fields defined in
 * {@code AccountSummary.jrxml}.
 */
public class AccountSummaryRowBean
{
	private String actId;
	private String invDate;
	private String commentAll;
	private String comment;
	private String customer;
	private String debitFormat;
	private String creditFormat;
	private String accountNum;
	private String accountName;
	
	// Standard getters
	public String getActId()
	{
		return this.actId;
	}
	
	public String getInvDate()
	{
		return this.invDate;
	}
	
	public String getCommentAll()
	{
		return this.commentAll;
	}
	
	public String getComment()
	{
		return this.comment;
	}
	
	public String getCustomer()
	{
		return this.customer;
	}
	
	public String getDebitFormat()
	{
		return this.debitFormat;
	}
	
	public String getCreditFormat()
	{
		return this.creditFormat;
	}
	
	public String getAccountNum()
	{
		return this.accountNum;
	}
	
	public String getAccountName()
	{
		return this.accountName;
	}
	
	// Convenience getters matching JRXML field names
	public String getACT_ID()
	{
		return this.actId;
	}
	
	public String getINVDATE()
	{
		return this.invDate;
	}
	
	public String getCOMMENTALL()
	{
		return this.commentAll;
	}
	
	public String getCOMMENT()
	{
		return this.comment;
	}
	
	public String getCUSTOMER()
	{
		return this.customer;
	}
	
	public String getDEBITFORMAT()
	{
		return this.debitFormat;
	}
	
	public String getCREDITFORMAT()
	{
		return this.creditFormat;
	}
	
	public String getACCOUNTNUM()
	{
		return this.accountNum;
	}
	
	public String getACCOUNTNAME()
	{
		return this.accountName;
	}
	
}
