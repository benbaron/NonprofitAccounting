
package nonprofitbookkeeping.reports.datasource.scareports;

import java.math.BigDecimal;

/** Row for Liability Detail (5b). */
>>>>>>> branch 'codex/read-provided-xlsx-file' of git@github.com:benbaron/NonprofitAccounting.git
public class LiabilityDtl5bRow implements SupplementalRecord
{
	
	/** Section: "DEFERRED_REVENUE", "PAYABLES", or "OTHER_LIABILITIES". */
	private String section;
	
	/** Event name (for Deferred Revenue) OR "Owed To" (for Payables/Other). */
	private String itemName;
	
	/** Reason (used for Payables/Other; may be blank for Deferred Revenue). */
	private String reason;
	
	/** Prior amount (BigDecimal). */
	private BigDecimal priorAmount;
	
	/** Current amount (BigDecimal). */
	private BigDecimal currentAmount;
	
	// Getters / setters
	public String getSection()
	{
		return section;
		
	}
	
	public void setSection(String section)
	{
		this.section = section;
		
	}
	
	public String getItemName()
	{
		return itemName;
		
	}
	
	public void setItemName(String itemName)
	{
		this.itemName = itemName;
		
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
