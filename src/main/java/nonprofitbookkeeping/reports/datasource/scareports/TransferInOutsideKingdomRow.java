
package nonprofitbookkeeping.reports.datasource.scareports;

import java.math.BigDecimal;

/**
 * Row representing a transfer received from an SCA account outside the
 * kingdom. Includes the originating kingdom/branch, check details and amount.
 */

public class TransferInOutsideKingdomRow implements SupplementalRecord
{
	private String kingdomAndBranch;
	private String checkNumber;
	private String checkDate;
	private BigDecimal amount;
	
	public TransferInOutsideKingdomRow()
	{
	
	}
	
	public TransferInOutsideKingdomRow(String kingdomAndBranch,
		String checkNumber, String checkDate, BigDecimal amount)
	{
		this.kingdomAndBranch = kingdomAndBranch;
		this.checkNumber = checkNumber;
		this.checkDate = checkDate;
		this.amount = amount;
		
	}
	
	public String getKingdomAndBranch()
	{
		return kingdomAndBranch;
		
	}
	
	public void setKingdomAndBranch(String kingdomAndBranch)
	{
		this.kingdomAndBranch = kingdomAndBranch;
		
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

