
package nonprofitbookkeeping.reports.datasource;

/**
 * Bean representing a row in the Transaction Report.
 */
public class TransactionReportRowBean
{
	private String actId;
	private String invDate;
	private String commentAll;
	private String comment;
	private String fileInfo;
	private String regDate;
	private String accountNum;
	private String accountName;
	private String customer;
	private String debitFormat;
	private String creditFormat;
	
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
	
	public String getFileInfo()
	{
		return this.fileInfo;
	}
	
	public String getRegDate()
	{
		return this.regDate;
	}
	
	public String getAccountNum()
	{
		return this.accountNum;
	}
	
	public String getAccountName()
	{
		return this.accountName;
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
	
	// Convenience getters matching JRXML field names
	public String getact_id()
	{
		return this.actId;
	}
	
	public String getACT_ID()
	{
		return this.actId;
	}
	
	public String getinvdate()
	{
		return this.invDate;
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
	
	public String getfileinfo()
	{
		return this.fileInfo;
	}
	
	public String getFILEINFO()
	{
		return this.fileInfo;
	}
	
	public String getregdate()
	{
		return this.regDate;
	}
	
	public String getREGDATE()
	{
		return this.regDate;
	}
	
	public String getACCOUNTNUM()
	{
		return this.accountNum;
	}
	
	public String getACCOUNTNAME()
	{
		return this.accountName;
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
	
}
