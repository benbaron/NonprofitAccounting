
package nonprofitbookkeeping.reports.datasource.scareports;

import java.math.BigDecimal;

/**
 * Row representing a prepaid expense. Prepaid expenses list a description
 * along with the prior and current amounts that have been paid in advance.
 */
<<<<<<< HEAD
public class PrepaidExpenseRow extends ScaRowBase {
=======
public class PrepaidExpenseRow implements SupplementalRecord
{
>>>>>>> refs/remotes/origin/codex/add-interface-and-extend-ledgerentry
	private String description;
	private BigDecimal priorAmount;
	private BigDecimal currentAmount;
	
	public PrepaidExpenseRow()
	{
	
	}
	
	public PrepaidExpenseRow(String description, BigDecimal priorAmount,
		BigDecimal currentAmount)
	{
		this.description = description;
		this.priorAmount = priorAmount;
		this.currentAmount = currentAmount;
		
	}
	
	public String getDescription()
	{
		return description;
		
	}
	
	public void setDescription(String description)
	{
		this.description = description;
		
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
