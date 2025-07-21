
package nonprofitbookkeeping.reports.datasource;

/**
 * Bean used for the Chart of Accounts report.
 */
public class ChartOfAccountsRowBean
{
	private String accountNumber;
	private String accountName;
	private String type;
	
	public ChartOfAccountsRowBean(String accountNumber, String accountName, String type)
	{
		this.accountNumber = accountNumber;
		this.accountName = accountName;
		this.type = type;
	}
	
	public String getAccountNumber()
	{
		return this.accountNumber;
	}
	
	public void setAccountNumber(String accountNumber)
	{
		this.accountNumber = accountNumber;
	}
	
	public String getAccountName()
	{
		return this.accountName;
	}
	
	public void setAccountName(String accountName)
	{
		this.accountName = accountName;
	}
	
	public String getType()
	{
		return this.type;
	}
	
	public void setType(String type)
	{
		this.type = type;
	}
	
	// ------------------------------------------------------------------
	// Convenience getters matching field names in ChartOfAccounts*.jrxml
	// ------------------------------------------------------------------
	
	/**
	 * Alias for {@link #getAccountNumber()} matching the JRXML field
	 * name {@code account_number}.
	 *
	 * @return the account number
	 */
	public String getAccount_number()
	{
		return getAccountNumber();
	}
	
	/**
	 * Alias for {@link #getAccountName()} matching the JRXML field
	 * name {@code account_name}.
	 *
	 * @return the account name
	 */
	public String getAccount_name()
	{
		return getAccountName();
	}
	
	/**
	 * Alias for {@link #getAccountNumber()} matching the uppercase JRXML field
	 * name {@code ACCOUNTNUM} used by older templates.
	 *
	 * @return the account number
	 */
	public String getACCOUNTNUM()
	{
		return getAccountNumber();
	}
	
	/**
	 * Alias for {@link #getAccountName()} matching the uppercase JRXML field
	 * name {@code ACCOUNTNAME} used by older templates.
	 *
	 * @return the account name
	 */
	public String getACCOUNTNAME()
	{
		return getAccountName();
	}
	
	/**
	 * Alias for {@link #getType()} matching the uppercase JRXML field
	 * name {@code ACCOUNTTYPE} used by older templates.
	 *
	 * @return the account type
	 */
	public String getACCOUNTTYPE()
	{
		return getType();
	}
	
}
