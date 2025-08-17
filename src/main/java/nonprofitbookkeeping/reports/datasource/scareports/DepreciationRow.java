
package nonprofitbookkeeping.reports.datasource.scareports;


import java.math.BigDecimal;

public class DepreciationRow extends ScaRowBase {
	// Section identifier: "FIVE_YEAR" or "SEVEN_YEAR"
	private String schedule;
	
	// Purpose-based fields
	private String assetName;
	private String acquiredDate; // keep as String to match spreadsheet text
	private BigDecimal acquisitionCost;
	private BigDecimal priorDepreciation;
	private BigDecimal depreciationThisPeriod;
	private BigDecimal accumulatedDepreciation;
	private BigDecimal netBookValue;
	private String notes;
	
	public DepreciationRow()
	{
	
	}
	
	public DepreciationRow(
			String schedule,
			String assetName,
			String acquiredDate,
			BigDecimal acquisitionCost,
			BigDecimal priorDepreciation,
			BigDecimal depreciationThisPeriod,
			BigDecimal accumulatedDepreciation,
			BigDecimal netBookValue,
			String notes)
	{
		this.schedule = schedule;
		this.assetName = assetName;
		this.acquiredDate = acquiredDate;
		this.acquisitionCost = acquisitionCost;
		this.priorDepreciation = priorDepreciation;
		this.depreciationThisPeriod = depreciationThisPeriod;
		this.accumulatedDepreciation = accumulatedDepreciation;
		this.netBookValue = netBookValue;
		this.notes = notes;
		
	}
	
	public String getSchedule()
	{
		return schedule;
		
	}
	
	public void setSchedule(String schedule)
	{
		this.schedule = schedule;
		
	}
	
	public String getAssetName()
	{
		return assetName;
		
	}
	
	public void setAssetName(String assetName)
	{
		this.assetName = assetName;
		
	}
	
	public String getAcquiredDate()
	{
		return acquiredDate;
		
	}
	
	public void setAcquiredDate(String acquiredDate)
	{
		this.acquiredDate = acquiredDate;
		
	}
	
	public BigDecimal getAcquisitionCost()
	{
		return acquisitionCost;
		
	}
	
	public void setAcquisitionCost(BigDecimal acquisitionCost)
	{
		this.acquisitionCost = acquisitionCost;
		
	}
	
	public BigDecimal getPriorDepreciation()
	{
		return priorDepreciation;
		
	}
	
	public void setPriorDepreciation(BigDecimal priorDepreciation)
	{
		this.priorDepreciation = priorDepreciation;
		
	}
	
	public BigDecimal getDepreciationThisPeriod()
	{
		return depreciationThisPeriod;
		
	}
	
	public void setDepreciationThisPeriod(BigDecimal depreciationThisPeriod)
	{
		this.depreciationThisPeriod = depreciationThisPeriod;
		
	}
	
	public BigDecimal getAccumulatedDepreciation()
	{
		return accumulatedDepreciation;
		
	}
	
	public void setAccumulatedDepreciation(BigDecimal accumulatedDepreciation)
	{
		this.accumulatedDepreciation = accumulatedDepreciation;
		
	}
	
	public BigDecimal getNetBookValue()
	{
		return netBookValue;
		
	}
	
	public void setNetBookValue(BigDecimal netBookValue)
	{
		this.netBookValue = netBookValue;
		
	}
	
	public String getNotes()
	{
		return notes;
		
	}
	
	public void setNotes(String notes)
	{
		this.notes = notes;
		
	}
	
	public BigDecimal getRemainingValue()
	{
		return BigDecimal.ZERO;
		
	}
	
}
