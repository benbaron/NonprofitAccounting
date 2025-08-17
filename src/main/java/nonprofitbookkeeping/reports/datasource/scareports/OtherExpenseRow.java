
package nonprofitbookkeeping.reports.datasource.scareports;

import java.math.BigDecimal;

public class OtherExpenseRow extends ScaRowBase {
	private String paidTo;
	private String reason;
	private BigDecimal amount;
	
	public OtherExpenseRow()
	{
		
	}
	
	public OtherExpenseRow(String paidTo, String reason, BigDecimal amount)
	{
		this.paidTo = paidTo;
		this.reason = reason;
		this.amount = amount;
		
	}
	
	public String getPaidTo()
	{
		return paidTo;
		
	}
	
	public void setPaidTo(String paidTo)
	{
		this.paidTo = paidTo;
		
	}
	
	public String getReason()
	{
		return reason;
		
	}
	
	public void setReason(String reason)
	{
		this.reason = reason;
		
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
