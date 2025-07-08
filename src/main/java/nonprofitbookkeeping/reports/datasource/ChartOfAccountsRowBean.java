
package nonprofitbookkeeping.reports.datasource;

/**
 * Bean representing a row in the Chart of Accounts report.
 */
public class ChartOfAccountsRowBean
{
	private String accountNum;
	private String accountName;
	private String accountType;
	
	public String getAccountNum()
	{
		return this.accountNum;
	}
	
	public String getAccountName()
	{
		return this.accountName;
	}
	
	public String getAccountType()
	{
		return this.accountType;
	}
	
	// JRXML field convenience getters
	public String getACCOUNTNUM()
	{
		return this.accountNum;
	}
	
	public String getACCOUNTNAME()
	{
		return this.accountName;
	}
	
	public String getACCOUNTTYPE()
	{
		return this.accountType;
	}
	
}
