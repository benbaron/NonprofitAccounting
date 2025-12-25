package nonprofitbookkeeping.reports.datasource;

import java.math.BigDecimal;

public class AccountSummaryRowBean
{
	private String accountName;
	private BigDecimal openingBalance;
	private BigDecimal closingBalance;
	
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
