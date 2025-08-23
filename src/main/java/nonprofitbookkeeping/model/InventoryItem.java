/**
 * nonprofit-scaledger-ribbon.zip_expanded
 * InventoryItem.java
 * InventoryItem
 */
package nonprofitbookkeeping.model;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Represents an inventory item, typically a fixed asset, subject to depreciation.
 * This class stores details about the item's cost, acquisition date, useful life,
 * and depreciation information.
 * Lombok's {@code @Data} and {@code @AllArgsConstructor}
 * are used for boilerplate code generation.
 */
@Entity
@Table(name = "inventory_items")
@Data
@AllArgsConstructor
public class InventoryItem
{
	/** The unique identifier for the inventory item. */
        @Id
        @JsonProperty private String id;
	/** The name or description of the inventory item. */
	@JsonProperty private String name;
	/** The date when the inventory item was acquired, typically in "YYYY-MM-DD" format. */
	@JsonProperty private String acquired;
	/** The original cost of the inventory item. */
	@JsonProperty private BigDecimal cost;
	/** The accumulated depreciation amount for the item. */
	@JsonProperty private BigDecimal accDep;
	/** The net book value of the item (Cost - Accumulated Depreciation). */
        @JsonProperty private BigDecimal netValue;
	/** The estimated useful life of the item in years. */
	@JsonProperty private int lifeYears;
	/** The annual depreciation rate (e.g., 0.10 for 10%). Can be null if not applicable or calculated differently. */
	@JsonProperty private BigDecimal depreciationRate;
        /** The depreciation method used (e.g., "Straight-Line"). */
        @JsonProperty private String depreciationMethod;

        /**
         * Public no-arg constructor required by JPA.
         */
        public InventoryItem() {
        }

	/**  
	 * Constructs an InventoryItem with essential details.
	 * Initializes depreciationRate to null and depreciationMethod to "Straight-Line" by default.
	 * Accumulated depreciation and net value are not set by this constructor initially.
	 * @param id The unique identifier for the item.
	 * @param name The name of the item.
	 * @param cost The original cost of the item.
	 * @param acquired The acquisition date of the item (e.g., "YYYY-MM-DD").
	 * @param lifeYears The estimated useful life of the item in years.
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
	 * Gets the unique identifier of the inventory item.
	 * @return The item ID.
	 */
	public String getId()
	{
		return this.id;
	}

	/**
	 * Gets the name of the inventory item.
	 * @return The item name.
	 */
	public String getName()
	{
		return this.name;
	}

	/**
	 * Gets the acquisition date of the inventory item.
	 * @return The acquisition date as a string (e.g., "YYYY-MM-DD").
	 */
	public String getAcquiredDate()
	{
		return this.acquired;
	}

	/**
	 * Gets the original cost of the inventory item.
	 * @return The item cost.
	 */
	public BigDecimal getCost()
	{
		return this.cost;
	}

	/**
	 * Gets the accumulated depreciation of the inventory item.
	 * This value is typically set via {@link #withAccumDep(BigDecimal)} or a direct setter.
	 * @return The accumulated depreciation amount.
	 */
	public BigDecimal getAccumulatedDepreciation()
	{
		return this.accDep;
	}

	/**
	 * Gets the net book value of the inventory item.
	 * This value is calculated as Cost - Accumulated Depreciation.
	 * @return The net book value.
	 */
	public BigDecimal getNetBookValue()
	{
		return this.netValue;
	}

	/**
	 * Gets the estimated useful life of the inventory item in years.
	 * @return The useful life in years.
	 */
	public int getLifeYears()
	{
		return this.lifeYears;
	}

	/**
	 * Sets the accumulated depreciation for this item and recalculates its net book value.
	 * This method provides a fluent interface.
	 * @param accDep1 The accumulated depreciation amount to set.
	 * @return This InventoryItem instance for chaining.
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
	 * Sets the accumulated depreciation for the item.
	 * Note: This is a stub method. The {@code @Data} annotation from Lombok should provide this setter.
	 * Consider using {@link #withAccumDep(BigDecimal)} for recalculating net value.
	 * @param valueOf The accumulated depreciation amount to set.
	 */
        public void setAccumulatedDepreciation(BigDecimal valueOf)
        {
                withAccumDep(valueOf);
        }

	/**
	 * Sets the depreciation rate for the item.
	 * Note: This is a stub method. The {@code @Data} annotation from Lombok should provide this setter.
	 * @param valueOf The depreciation rate to set (e.g., 0.10 for 10%).
	 */
        public void setDepreciationRate(BigDecimal valueOf)
        {
                this.depreciationRate = valueOf;
        }

	/**
	 * Gets the depreciation rate of the item.
	 * Note: This is a stub method and currently returns null.
	 * The {@code @Data} annotation from Lombok should provide this getter.
	 * @return The depreciation rate, or null if not set or if stub is not replaced.
	 */
        public BigDecimal getDepreciationRate()
        {
                return this.depreciationRate;
        }

	/**
	 * Sets the depreciation method for the item.
	 * Note: This is a stub method. The {@code @Data} annotation from Lombok should provide this setter.
	 * @param method The depreciation method to set (e.g., "Straight-Line").
	 */
        public void setDepreciationMethod(Object method) // Parameter type should likely be String
        {
                this.depreciationMethod = method == null ? null : method.toString();
        }

	/**
	 * Gets the net book value of the item.
	 * Note: This is a stub method and currently returns null.
	 * The {@code @Data} annotation from Lombok should provide a getter for 'netValue'.
	 * The actual calculation is typically Cost - Accumulated Depreciation.
	 * @return The net book value, or null if not calculated or if stub is not replaced.
	 */
        public BigDecimal getNetValue()
        {
                return this.netValue;
        }
	
}
