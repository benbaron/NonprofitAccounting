
package nonprofitbookkeeping.reports.datasource;

import java.math.BigDecimal;

/**
 * Simple JavaBean representing a row in the Balance Sheet report.
 */
public class BalanceSheetRowBean
{
	private String category;
	private String account;
	private BigDecimal amount;
	
	public BalanceSheetRowBean(String category, String account, BigDecimal amount)
	{
		this.category = category;
		this.account = account;
		this.amount = amount;
	}
	
	public String getCategory()
	{
		return category;
	}
	
	public void setCategory(String category)
	{
		this.category = category;
	}
	
	public String getAccount()
	{
		return account;
	}
	
	public void setAccount(String account)
	{
		this.account = account;
	}
	
	public BigDecimal getAmount()
	{
		return amount;
	}
	
	public void setAmount(BigDecimal amount)
	{
		this.amount = amount;
	}
	
}
