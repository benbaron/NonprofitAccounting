
package nonprofitbookkeeping.reports.datasource;

/**
 * Bean representing a row in the Balance Report.
 */
public class BalanceReportRowBean
{
	private String accountNum;
	private String accountDesc;
	private String formattedIncomingAmount;
	private String formattedAmount;
	private String formattedOutgoingAmount;
	
	public String getAccountNum()
	{
		return this.accountNum;
	}
	
	public String getAccountDesc()
	{
		return this.accountDesc;
	}
	
	public String getFormattedIncomingAmount()
	{
		return this.formattedIncomingAmount;
	}
	
	public String getFormattedAmount()
	{
		return this.formattedAmount;
	}
	
	public String getFormattedOutgoingAmount()
	{
		return this.formattedOutgoingAmount;
	}
	
	// JRXML field convenience getters
	public String getACCOUNTNUM()
	{
		return this.accountNum;
	}
	
	public String getACCOUNTDESC()
	{
		return this.accountDesc;
	}
	
	public String getFORMATED_INCOMMINGAMOUNT()
	{
		return this.formattedIncomingAmount;
	}
	
	public String getFORMATED_AMOUNT()
	{
		return this.formattedAmount;
	}
	
	public String getFORMATED_OUTGOINGAMOUNT()
	{
		return this.formattedOutgoingAmount;
	}
	
}
