
package nonprofitbookkeeping.reports.datasource.scareports;

import java.math.BigDecimal;

/**
 * Row representing a miscellaneous liability that does not fall under
 * deferred revenue or standard payables. Similar structure to a payable,
 * tracking who is owed, the reason, and the prior/current amounts.
 */

public class OtherLiabilityRow implements SupplementalRecord
{
	private String owedTo;
	private String reason;
	private BigDecimal priorAmount;
	private BigDecimal currentAmount;
	
	public OtherLiabilityRow()
	{
	
	}
	
	public OtherLiabilityRow(String owedTo, String reason,
		BigDecimal priorAmount, BigDecimal currentAmount)
	{
		this.owedTo = owedTo;
		this.reason = reason;
		this.priorAmount = priorAmount;
		this.currentAmount = currentAmount;
		
	}
	
	public String getOwedTo()
	{
		return owedTo;
		
	}
	
	public void setOwedTo(String owedTo)
	{
		this.owedTo = owedTo;
		
	}
	
	public String getReason()
	{
		return reason;
		
	}
	
	public void setReason(String reason)
	{
		this.reason = reason;
		
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

