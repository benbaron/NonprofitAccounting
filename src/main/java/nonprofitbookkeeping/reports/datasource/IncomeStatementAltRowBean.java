
package nonprofitbookkeeping.reports.datasource;

import java.math.BigDecimal;

/**
 * Simple bean for the alternate Income Statement report.
 */
public class IncomeStatementAltRowBean
{
	private String account;
	private BigDecimal amount;
	
	public IncomeStatementAltRowBean(String account, BigDecimal amount)
	{
		this.account = account;
		this.amount = amount;
	}
	
	public String getAccount()
	{
		return this.account;
	}
	
	public void setAccount(String account)
	{
		this.account = account;
	}
	
	public BigDecimal getAmount()
	{
		return this.amount;
	}
	
	public void setAmount(BigDecimal amount)
	{
		this.amount = amount;
	}
	
}
