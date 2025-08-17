
package nonprofitbookkeeping.reports.datasource.scareports;

import java.math.BigDecimal;

/**
 * Row representing adjusted gross income for a single event.
 */
public class AdjustedGrossEventIncomeRow extends ScaRowBase {
	private String eventName;
	private String itemsOrCount;
	private BigDecimal amount;
	
	public AdjustedGrossEventIncomeRow()
	{
	
	}
	
	public AdjustedGrossEventIncomeRow(String eventName, String itemsOrCount,
		BigDecimal amount)
	{
		this.eventName = eventName;
		this.itemsOrCount = itemsOrCount;
		this.amount = amount;
		
	}
	
	public String getEventName()
	{
		return eventName;
		
	}
	
	public void setEventName(String eventName)
	{
		this.eventName = eventName;
		
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
