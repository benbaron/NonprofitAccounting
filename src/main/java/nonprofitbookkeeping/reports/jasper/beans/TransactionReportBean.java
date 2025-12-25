package nonprofitbookkeeping.reports.jasper.beans;

public class TransactionReportBean
{
	private String actId;
	private String invDate;
	private String accountNum;
	private String accountName;
	private String customer;
	private String debitFormat;
	private String creditFormat;
	private String comment;
	
	public String getActId()
	{
		return this.actId;
		
	}
	
	public void setActId(String actId)
	{
		this.actId = actId;
		
	}
	
	public String getInvDate()
	{
		return this.invDate;
		
	}
	
	public void setInvDate(String invDate)
	{
		this.invDate = invDate;
		
	}
	
	public String getAccountNum()
	{
		return this.accountNum;
		
	}
	
	public void setAccountNum(String accountNum)
	{
		this.accountNum = accountNum;
		
	}
	
	public String getAccountName()
	{
		return this.accountName;
		
	}
	
	public void setAccountName(String accountName)
	{
		this.accountName = accountName;
		
	}
	
	public String getCustomer()
	{
		return this.customer;
		
	}
	
	public void setCustomer(String customer)
	{
		this.customer = customer;
		
	}
	
	public String getDebitFormat()
	{
		return this.debitFormat;
		
	}
	
	public void setDebitFormat(String debitFormat)
	{
		this.debitFormat = debitFormat;
		
	}
	
	public String getCreditFormat()
	{
		return this.creditFormat;
		
	}
	
	public void setCreditFormat(String creditFormat)
	{
		this.creditFormat = creditFormat;
		
	}
	
	public String getComment()
	{
		return this.comment;
		
	}
	
	public void setComment(String comment)
	{
		this.comment = comment;
		
	}
}
