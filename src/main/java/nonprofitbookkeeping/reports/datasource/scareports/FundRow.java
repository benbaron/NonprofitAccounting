/**
 * NonprofitAccounting FundRow.java FundRow
 */

package nonprofitbookkeeping.reports.datasource.scareports;

import java.math.BigDecimal;

/** Row for FUND 14 – balance of a single restricted / designated fund. */
public final class FundRow implements SupplementalRecord
{
	
	private String fundName;
	private BigDecimal startingBalance;
	private BigDecimal income;
	private BigDecimal expenses;
	private BigDecimal endingBalance;
	
	public FundRow()
	{
		
	}
	
	public FundRow(String fundName,
		BigDecimal starting,
		BigDecimal income,
		BigDecimal expenses,
		BigDecimal ending)
	{
		this.fundName = fundName;
		this.startingBalance = starting;
		this.income = income;
		this.expenses = expenses;
		this.endingBalance = ending;
		
	}
	
	// getters / setters
	public String getFundName()
	{
		return fundName;
		
	}
	
	public void setFundName(String n)
	{
		this.fundName = n;
		
	}
	
	public BigDecimal getStartingBalance()
	{
		return startingBalance;
		
	}
	
	public void setStartingBalance(BigDecimal v)
	{
		this.startingBalance = v;
		
	}
	
	public BigDecimal getIncome()
	{
		return income;
		
	}
	
	public void setIncome(BigDecimal v)
	{
		this.income = v;
		
	}
	
	public BigDecimal getExpenses()
	{
		return expenses;
		
	}
	
	public void setExpenses(BigDecimal v)
	{
		this.expenses = v;
		
	}
	
	public BigDecimal getEndingBalance()
	{
		return endingBalance;
		
	}
	
	public void setEndingBalance(BigDecimal v)
	{
		this.endingBalance = v;
		
	}
	
	public BigDecimal getBalance()
	{
		return BigDecimal.ZERO;
		
	}
	
}
