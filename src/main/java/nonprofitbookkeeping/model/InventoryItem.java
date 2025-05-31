/**
 * nonprofit-scaledger-ribbon.zip_expanded
 * InventoryItem.java
 * InventoryItem
 */
package nonprofitbookkeeping.model;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonProperty;

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
	@JsonProperty private String id, name, acquired;
	@JsonProperty private BigDecimal cost, accDep, netValue;
	@JsonProperty private int lifeYears;
	@JsonProperty private BigDecimal depreciationRate; // e.g., 0.10 for 10%
	@JsonProperty private String depreciationMethod; // e.g., "Straight-Line"

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
		this.depreciationRate = null; // Default to null
		this.depreciationMethod = "Straight-Line"; // Default method
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
		this.accDep = accDep1;
		if (this.cost == null) {
			this.netValue = null;
		} else if (this.accDep == null) {
			this.netValue = this.cost;
		} else {
			this.netValue = this.cost.subtract(this.accDep);
		}
		return this;
	}

	/**
	 * @param valueOf
	 */
	public void setAccumulatedDepreciation(BigDecimal valueOf)
	{
		// TODO Auto-generated method stub
		
	}

	/**
	 * @param valueOf
	 */
	public void setDepreciationRate(BigDecimal valueOf)
	{
		// TODO Auto-generated method stub
		
	}

	/**
	 * @return
	 */
	public BigDecimal getDepreciationRate()
	{
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @param object
	 */
	public void setDepreciationMethod(Object object)
	{
		// TODO Auto-generated method stub
		
	}

	/**
	 * @return
	 */
	public BigDecimal getNetValue()
	{
		// TODO Auto-generated method stub
		return null;
	}
	
}
