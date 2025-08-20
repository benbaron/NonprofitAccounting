package nonprofitbookkeeping.reports.datasource.scareports;

// RegaliaSalesRow.java
import java.math.BigDecimal;


public class RegaliaSalesRow implements SupplementalRecord
{
	// Section values: "PURCHASE", "SALE", "ADJUSTMENT"
	private String section;
	private String entryDate; // keep as String to allow free-form dates
	private String itemDescription;
	private String quantity; // string for things like "1 set"
	private BigDecimal unitCost;
	private BigDecimal unitPrice;
	private BigDecimal amount;
	private String counterparty;
	private String notes;
	
	public RegaliaSalesRow()
	{
	
	}
	
	public RegaliaSalesRow(String section, String entryDate, String itemDescription,
			String quantity,
			BigDecimal unitCost, BigDecimal unitPrice, BigDecimal amount,
			String counterparty, String notes)
	{
		this.section = section;
		this.entryDate = entryDate;
		this.itemDescription = itemDescription;
		this.quantity = quantity;
		this.unitCost = unitCost;
		this.unitPrice = unitPrice;
		this.amount = amount;
		this.counterparty = counterparty;
		this.notes = notes;
		
	}
	
	public String getSection()
	{
		return section;
		
	}
	
	public void setSection(String section)
	{
		this.section = section;
		
	}
	
	public String getEntryDate()
	{
		return entryDate;
		
	}
	
	public void setEntryDate(String entryDate)
	{
		this.entryDate = entryDate;
		
	}
	
	public String getItemDescription()
	{
		return itemDescription;
		
	}
	
	public void setItemDescription(String itemDescription)
	{
		this.itemDescription = itemDescription;
		
	}
	
	public String getQuantity()
	{
		return quantity;
		
	}
	
	public void setQuantity(String quantity)
	{
		this.quantity = quantity;
		
	}
	
	public BigDecimal getUnitCost()
	{
		return unitCost;
		
	}
	
	public void setUnitCost(BigDecimal unitCost)
	{
		this.unitCost = unitCost;
		
	}
	
	public BigDecimal getUnitPrice()
	{
		return unitPrice;
		
	}
	
	public void setUnitPrice(BigDecimal unitPrice)
	{
		this.unitPrice = unitPrice;
		
	}
	
	public BigDecimal getAmount()
	{
		return amount;
		
	}
	
	public void setAmount(BigDecimal amount)
	{
		this.amount = amount;
		
	}
	
	public String getCounterparty()
	{
		return counterparty;
		
	}
	
	public void setCounterparty(String counterparty)
	{
		this.counterparty = counterparty;
		
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
