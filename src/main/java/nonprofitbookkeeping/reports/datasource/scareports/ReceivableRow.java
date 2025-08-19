
package nonprofitbookkeeping.reports.datasource.scareports;

import java.math.BigDecimal;

/**
 * Row representing a single receivable. Each receivable records who owes
 * the funds, the reason for the receivable, and the prior and current
 * amounts owed.
 */
<<<<<<< HEAD
public class ReceivableRow extends ScaRowBase {
=======
public class ReceivableRow implements SupplementalRecord
{
>>>>>>> refs/remotes/origin/codex/add-interface-and-extend-ledgerentry
	private String owedFrom;
	private String reason;
	private BigDecimal priorAmount;
	private BigDecimal currentAmount;
	
	public ReceivableRow()
	{
	
	}
	
	public ReceivableRow(String owedFrom, String reason, BigDecimal priorAmount,
		BigDecimal currentAmount)
	{
		this.owedFrom = owedFrom;
		this.reason = reason;
		this.priorAmount = priorAmount;
		this.currentAmount = currentAmount;
		
	}
	
	public String getOwedFrom()
	{
		return owedFrom;
		
	}
	
	public void setOwedFrom(String owedFrom)
	{
		this.owedFrom = owedFrom;
		
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
