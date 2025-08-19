/**
 * NonprofitAccounting
 * IncomeRowBase.java
 * IncomeRowBase
 */
package nonprofitbookkeeping.reports.datasource.scareports;


import java.math.BigDecimal;
import java.util.Objects;

/** Common base for any row that has a description + single monetary amount. */

public abstract class IncomeRowBase implements SupplementalRecord
{
	
	private String description;
	private BigDecimal amount;
	
	protected IncomeRowBase()
	{			
	}
		
	protected IncomeRowBase(String description, BigDecimal amount)
	{
		this.description = description;
		this.amount = amount;
		
	}
	
	// ---------- getters / setters ----------
	public String getDescription()
	{
		return description;
		
	}
	
	public void setDescription(String d)
	{
		this.description = d;
		
	}
	
	public BigDecimal getAmount()
	{
		return amount;
		
	}		
	
	public BigDecimal getValue()
	{
		return BigDecimal.ZERO;		
	}
	
	public void setAmount(BigDecimal a)
	{
		this.amount = a;
		
	}
	
	// ---------- utility ----------
	@Override
	public String toString()
	{
		return getClass().getSimpleName() + '{' + description + ':' + amount +
			'}';
		
	}
	
	@Override
	public int hashCode()
	{
		return Objects.hash(description, amount);
		
	}
	
	@Override
	public boolean equals(Object o)
	{
		if (this == o)
			return true;
		if (!(o instanceof IncomeRowBase other))
			return false;
		return Objects.equals(description, other.description) &&
			Objects.equals(amount, other.amount);
		
	}
	
}
