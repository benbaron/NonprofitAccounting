
package nonprofitbookkeeping.reports.datasource.scareports;

import java.math.BigDecimal;

/**
 * Row representing a single deferred revenue item. Each entry corresponds to
 * an event for which revenue has been received but not yet earned.
 */

public class DeferredRevenueRow implements SupplementalRecord
{
	private String eventName;
	private BigDecimal priorAmount;
	private BigDecimal currentAmount;
	
	public DeferredRevenueRow()
	{
	
	}
	
	public DeferredRevenueRow(String eventName, BigDecimal priorAmount,
		BigDecimal currentAmount)
	{
		this.eventName = eventName;
		this.priorAmount = priorAmount;
		this.currentAmount = currentAmount;
		
	}
	
	public String getEventName()
	{
		return eventName;
		
	}
	
	public void setEventName(String eventName)
	{
		this.eventName = eventName;
		
	}
	
	public BigDecimal getPriorAmount()
	{
		return priorAmount;
		
	}
	
	public void setPriorAmount(BigDecimal priorAmount)
	{
		this.priorAmount = priorAmount;
		
	}
	
	public BigDecimal getCurrentAmount()
	{
		return currentAmount;
		
	}
	
	public void setCurrentAmount(BigDecimal currentAmount)
	{
		this.currentAmount = currentAmount;
		
	}
	
}

