
package nonprofitbookkeeping.reports.datasource.scareports;

import java.math.BigDecimal;

public class AdvertisingExpenseRow
{
	private String code;
	private String organizationOrPeriodical;
	private BigDecimal amount;
	
	public AdvertisingExpenseRow()
	{
	
	}
	
	public AdvertisingExpenseRow(String code, String organizationOrPeriodical, BigDecimal amount)
	{
		this.code = code;
		this.organizationOrPeriodical = organizationOrPeriodical;
		this.amount = amount;
		
	}
	
	public String getCode()
	{
		return code;
		
	}
	
	public void setCode(String code)
	{
		this.code = code;
		
	}
	
	public String getOrganizationOrPeriodical()
	{
		return organizationOrPeriodical;
		
	}
	
	public void setOrganizationOrPeriodical(String organizationOrPeriodical)
	{
		this.organizationOrPeriodical = organizationOrPeriodical;
		
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
