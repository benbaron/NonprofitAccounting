
package nonprofitbookkeeping.reports.datasource.scareports;

import java.math.BigDecimal;


public class InventoryDtl6Row implements SupplementalRecord
{
	private String category;
	private String itemDescription;
	private String skuOrId;
	private String unit;
	private BigDecimal quantityStart;
	private BigDecimal quantityAdditions;
	private BigDecimal quantityReductions;
	private BigDecimal quantityEnd;
	private BigDecimal unitCost;
	private BigDecimal endingValue;
	private String notes;
	
	public InventoryDtl6Row()
	{
	
	}
	
	public InventoryDtl6Row(String category, String itemDescription, String skuOrId, String unit,
			BigDecimal quantityStart, BigDecimal quantityAdditions, BigDecimal quantityReductions,
			BigDecimal quantityEnd, BigDecimal unitCost, BigDecimal endingValue, String notes)
	{
		this.category = category;
		this.itemDescription = itemDescription;
		this.skuOrId = skuOrId;
		this.unit = unit;
		this.quantityStart = quantityStart;
		this.quantityAdditions = quantityAdditions;
		this.quantityReductions = quantityReductions;
		this.quantityEnd = quantityEnd;
		this.unitCost = unitCost;
		this.endingValue = endingValue;
		this.notes = notes;
		
	}
	
	public String getCategory()
	{
		return category;
		
	}
	
	public void setCategory(String category)
	{
		this.category = category;
		
	}
	
	public String getItemDescription()
	{
		return itemDescription;
		
	}
	
	public void setItemDescription(String itemDescription)
	{
		this.itemDescription = itemDescription;
		
	}
	
	public String getSkuOrId()
	{
		return skuOrId;
		
	}
	
	public void setSkuOrId(String skuOrId)
	{
		this.skuOrId = skuOrId;
		
	}
	
	public String getUnit()
	{
		return unit;
		
	}
	
	public void setUnit(String unit)
	{
		this.unit = unit;
		
	}
	
	public BigDecimal getQuantityStart()
	{
		return quantityStart;
		
	}
	
	public void setQuantityStart(BigDecimal quantityStart)
	{
		this.quantityStart = quantityStart;
		
	}
	
	public BigDecimal getQuantityAdditions()
	{
		return quantityAdditions;
		
	}
	
	public void setQuantityAdditions(BigDecimal quantityAdditions)
	{
		this.quantityAdditions = quantityAdditions;
		
	}
	
	public BigDecimal getQuantityReductions()
	{
		return quantityReductions;
		
	}
	
	public void setQuantityReductions(BigDecimal quantityReductions)
	{
		this.quantityReductions = quantityReductions;
		
	}
	
	public BigDecimal getQuantityEnd()
	{
		return quantityEnd;
		
	}
	
	public void setQuantityEnd(BigDecimal quantityEnd)
	{
		this.quantityEnd = quantityEnd;
		
	}
	
	public BigDecimal getUnitCost()
	{
		return unitCost;
		
	}
	
	public void setUnitCost(BigDecimal unitCost)
	{
		this.unitCost = unitCost;
		
	}
	
	public BigDecimal getEndingValue()
	{
		return endingValue;
		
	}
	
	public void setEndingValue(BigDecimal endingValue)
	{
		this.endingValue = endingValue;
		
	}
	
	public String getNotes()
	{
		return notes;
		
	}
	
	public void setNotes(String notes)
	{
		this.notes = notes;
		
	}
	
}
