
package nonprofitbookkeeping.reports.datasource.scareports;

import java.math.BigDecimal;

/**
 * Row representing a transfer to an SCA account outside the kingdom.
 */
public class TransferOutOutsideKingdomRow
{
	private String kingdomAndBranch;
	private String reason;
	private String checkNumber;
	private String checkDate;
	private BigDecimal amount;
	
	public TransferOutOutsideKingdomRow()
	{
	
	}
	
	public TransferOutOutsideKingdomRow(String kingdomAndBranch, String reason,
		String checkNumber, String checkDate, BigDecimal amount)
	{
		this.kingdomAndBranch = kingdomAndBranch;
		this.reason = reason;
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
	
	public String getReason()
	{
		return reason;
		
	}
	
	public void setReason(String reason)
	{
		this.reason = reason;
		
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
