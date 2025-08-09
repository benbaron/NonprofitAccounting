
package nonprofitbookkeeping.reports.datasource.scareports;

import java.math.BigDecimal;

public class Funds14Row
{
	private String fundName;
	private BigDecimal beginBalance;
	private BigDecimal receipts;
	private BigDecimal disbursements;
	private BigDecimal transfersIn;
	private BigDecimal transfersOut;
	private BigDecimal endBalance;
	
	public Funds14Row()
	{
		
	}
	
	public String getFundName()
	{
		return fundName;
		
	}
	
	public void setFundName(String v)
	{
		this.fundName = v;
		
	}
	
	public BigDecimal getBeginBalance()
	{
		return beginBalance;
		
	}
	
	public void setBeginBalance(BigDecimal v)
	{
		this.beginBalance = v;
		
	}
	
	public BigDecimal getReceipts()
	{
		return receipts;
		
	}
	
	public void setReceipts(BigDecimal v)
	{
		this.receipts = v;
		
	}
	
	public BigDecimal getDisbursements()
	{
		return disbursements;
		
	}
	
	public void setDisbursements(BigDecimal v)
	{
		this.disbursements = v;
		
	}
	
	public BigDecimal getTransfersIn()
	{
		return transfersIn;
		
	}
	
	public void setTransfersIn(BigDecimal v)
	{
		this.transfersIn = v;
		
	}
	
	public BigDecimal getTransfersOut()
	{
		return transfersOut;
		
	}
	
	public void setTransfersOut(BigDecimal v)
	{
		this.transfersOut = v;
		
	}
	
	public BigDecimal getEndBalance()
	{
		return endBalance;
		
	}
	
	public void setEndBalance(BigDecimal v)
	{
		this.endBalance = v;
		
	}
	
}
