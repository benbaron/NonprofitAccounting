
package nonprofitbookkeeping.reports.datasource.scareports;

import java.math.BigDecimal;

/**
 * Row representing an asset that does not fall into another category. It
 * stores a simple description alongside prior and current amounts.
 */

public class OtherAssetRow implements SupplementalRecord
{
	private String description;
	private BigDecimal priorAmount;
	private BigDecimal currentAmount;
	
	public OtherAssetRow()
	{
	
	}
	
	public OtherAssetRow(String description, BigDecimal priorAmount,
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
