/**
 * nonprofit-scaledger-ribbon.zip_expanded
 * InventoryItem.java
 * InventoryItem
 */
package nonprofitbookkeeping.model;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class InventoryItem
{
	private String id, name, acquired;
	private BigDecimal cost, accDep, netValue;
	private int lifeYears;

	/**  
	 * Constructor InventoryItem
	 * @param object
	 * @param text
	 * @param cost
	 * @param string
	 * @param life
	 */
	public InventoryItem(String id, 
	                     String name, 
	                     BigDecimal cost, 
	                     String acquired, 
	                     int lifeYears)
	{	
		this.id = id;
		this.name = name;
		this.cost = cost;
		this.acquired = acquired;
		this.lifeYears = lifeYears;
	}

	/**
	 * @return
	 */
	public String getId()
	{
		return this.id;
	}

	/**
	 * @return
	 */
	public String getName()
	{
		return this.name;
	}

	/**
	 * @return
	 */
	public String getAcquiredDate()
	{
		// TODO Auto-generated method stub
		return this.acquired;
	}

	/**
	 * @return
	 */
	public BigDecimal getCost()
	{
		return this.cost;
	}

	/**
	 * @return
	 */
	public BigDecimal getAccumulatedDepreciation()
	{
		return this.accDep;
	}

	/**
	 * @return
	 */
	public BigDecimal getNetBookValue()
	{
		return this.netValue;
	}

	/**
	 * @return
	 */
	public int getLifeYears()
	{
		return this.lifeYears;
	}

	/**
	 * @param accDep1
	 * @return
	 */
	public InventoryItem withAccumDep(BigDecimal accDep1)
	{
		// TODO Auto-generated method stub
		return null;
	}
	
}
