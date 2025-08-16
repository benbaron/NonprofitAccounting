
package nonprofitbookkeeping.reports.datasource.scareports;

import java.math.BigDecimal;

/**
 * Row representing a single undeposited fund entry. Each row captures
 * the sending branch or reason for the cash being held and the amount
 * associated with it.
 */
public class UndepositedFundsRow
{
	private String sendingBranchOrReason;
	private BigDecimal amount;
	
	public UndepositedFundsRow()
	{
	
	}
	
	public UndepositedFundsRow(String sendingBranchOrReason, BigDecimal amount)
	{
		this.sendingBranchOrReason = sendingBranchOrReason;
		this.amount = amount;
		
	}
	
	public String getSendingBranchOrReason()
	{
		return sendingBranchOrReason;
		
	}
	
	public void setSendingBranchOrReason(String sendingBranchOrReason)
	{
		this.sendingBranchOrReason = sendingBranchOrReason;
		
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
