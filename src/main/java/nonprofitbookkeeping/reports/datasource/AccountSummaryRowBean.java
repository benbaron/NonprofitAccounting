package nonprofitbookkeeping.reports.datasource;

import java.math.BigDecimal;

/**
 * Bean describing a row in the Account Summary report.
 */
public class AccountSummaryRowBean
{
	private String accountName;
	private BigDecimal openingBalance;
	private BigDecimal closingBalance;
	
	public AccountSummaryRowBean()
	{
	}
	
	public AccountSummaryRowBean(String accountName,
		BigDecimal openingBalance, BigDecimal closingBalance)
	{
		this.accountName = accountName;
		this.openingBalance = openingBalance;
		this.closingBalance = closingBalance;
	}
	
	public String getAccountName()
	{
		return this.accountName;
	}
	
	public void setAccountName(String accountName)
	{
		this.accountName = accountName;
	}
	
	public BigDecimal getOpeningBalance()
	{
		return this.openingBalance;
	}
	
	public void setOpeningBalance(BigDecimal openingBalance)
	{
		this.openingBalance = openingBalance;
	}
	
	public BigDecimal getClosingBalance()
	{
		return this.closingBalance;
	}
	
	public void setClosingBalance(BigDecimal closingBalance)
	{
		this.closingBalance = closingBalance;
	}
}
