
package nonprofitbookkeeping.reports.datasource.scareports;

import java.math.BigDecimal;

/**
 * Row bean representing a check that has not yet cleared the bank
 * statement. Each row contains the check number, the date the check was
 * written, and the amount of the check.
 */
<<<<<<< HEAD
public class PrimaryAccountCheckRow extends ScaRowBase {
=======
public class PrimaryAccountCheckRow implements SupplementalRecord
{
>>>>>>> refs/remotes/origin/codex/add-interface-and-extend-ledgerentry
	private String checkNumber;
	private String checkDate;
	private BigDecimal amount;
	
	public PrimaryAccountCheckRow()
	{
	
	}
	
	public PrimaryAccountCheckRow(String checkNumber, String checkDate,
		BigDecimal amount)
	{
		this.checkNumber = checkNumber;
		this.checkDate = checkDate;
		this.amount = amount;
		
	}
	
	public String getCheckNumber()
	{
		return checkNumber;
		
	}
	
	public void setCheckNumber(String checkNumber)
	{
		this.checkNumber = checkNumber;
		
	}
	
	public String getCheckDate()
	{
		return checkDate;
		
	}
	
	public void setCheckDate(String checkDate)
	{
		this.checkDate = checkDate;
		
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
