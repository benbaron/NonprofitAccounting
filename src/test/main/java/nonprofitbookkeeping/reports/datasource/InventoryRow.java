/**
 * NonprofitAccounting InventoryRow.java InventoryRow
 */

package nonprofitbookkeeping.reports.datasource;

import java.math.BigDecimal;

/** INVENTORY 6 worksheet – one inventory item. */
public final class InventoryRow
{
	
	private String item;
	private int quantity;
	private BigDecimal unitCost;
	private BigDecimal totalCost;
	
	public InventoryRow()
	{
	
	}
	
	public InventoryRow(String item, int qty, BigDecimal unitCost)
	{
		this.item = item;
		this.quantity = qty;
		this.unitCost = unitCost;
		this.totalCost = unitCost.multiply(BigDecimal.valueOf(qty));
		
	}
	
	public String getItem()
	{
		return item;
		
	}
	
	public void setItem(String i)
	{
		this.item = i;
		
	}
	
	public int getQuantity()
	{
		return quantity;
		
	}
	
	public void setQuantity(int q)
	{
		this.quantity = q;
		
	}
	
	public BigDecimal getUnitCost()
	{
		return unitCost;
		
	}
	
	public void setUnitCost(BigDecimal c)
	{
		this.unitCost = c;
		
	}
	
	public BigDecimal getTotalCost()
	{
		return totalCost;
		
	}
	
	public void setTotalCost(BigDecimal t)
	{
		this.totalCost = t;
		
	}
	
	public BigDecimal getValue()
	{
		return BigDecimal.ZERO;
		
	}
}
