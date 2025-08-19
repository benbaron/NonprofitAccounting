
package nonprofitbookkeeping.reports.datasource.scareports;

import java.math.BigDecimal;

/**
 * Row bean representing a deposit that has not yet cleared the bank
 * statement. Each row contains the date of the deposit and the amount
 * of the deposit.
 */
<<<<<<< HEAD
public class PrimaryAccountDepositRow extends ScaRowBase {
=======
public class PrimaryAccountDepositRow implements SupplementalRecord
{
>>>>>>> refs/remotes/origin/codex/add-interface-and-extend-ledgerentry
	private String depositDate;
	private BigDecimal amount;
	
	public PrimaryAccountDepositRow()
	{
	
	}
	
	public PrimaryAccountDepositRow(String depositDate, BigDecimal amount)
	{
		this.depositDate = depositDate;
		this.amount = amount;
		
	}
	
	public String getDepositDate()
	{
		return depositDate;
		
	}
	
	public void setDepositDate(String depositDate)
	{
		this.depositDate = depositDate;
		
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
