
package nonprofitbookkeeping.reports.datasource.scareports;

import java.math.BigDecimal;

/**
 * Row representing net income from advertising.
 */
public class NetAdvertisingIncomeRow extends ScaRowBase {
	private String description;
	private String itemsOrCount;
	private BigDecimal amount;
	
	public NetAdvertisingIncomeRow()
	{
	
	}
	
	public NetAdvertisingIncomeRow(String description, String itemsOrCount,
		BigDecimal amount)
	{
		this.description = description;
		this.itemsOrCount = itemsOrCount;
		this.amount = amount;
		
	}
	
	public String getDescription()
	{
		return description;
		
	}
	
	public void setDescription(String description)
	{
		this.description = description;
		
	}
	
	public String getItemsOrCount()
	{
		return itemsOrCount;
		
	}
	
	public void setItemsOrCount(String itemsOrCount)
	{
		this.itemsOrCount = itemsOrCount;
		
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
