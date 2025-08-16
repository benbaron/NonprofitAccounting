
package nonprofitbookkeeping.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Represents a single sale transaction for the Sales & COG panel.
 */
@Entity
@Table(name = "sale_records")
public class SaleRecord
{
	/** Unique identifier for this sale. */
        @Id
        @JsonProperty private String id;
	/** Date of the sale in ISO format (YYYY-MM-DD). */
	@JsonProperty private String date;
	/** Item sold. */
	@JsonProperty private String item;
	/** Quantity sold. */
	@JsonProperty private int qty;
	/** Unit price. */
	@JsonProperty private BigDecimal price;
	/** Unit cost. */
	@JsonProperty private BigDecimal cost;
	
	public SaleRecord()
	{
	}
	
	public SaleRecord(String id, String date, String item, int qty, BigDecimal price,
		BigDecimal cost)
	{
		this.id = id;
		this.date = date;
		this.item = item;
		this.qty = qty;
		this.price = price;
		this.cost = cost;
	}
	
	public String getId()
	{
		return this.id;
	}
	
	public String getDate()
	{
		return this.date;
	}
	
	public String getItem()
	{
		return this.item;
	}
	
	public int getQty()
	{
		return this.qty;
	}
	
	public BigDecimal getPrice()
	{
		return this.price;
	}
	
	public BigDecimal getCost()
	{
		return this.cost;
	}
	
	public void setId(String id)
	{
		this.id = id;
	}
	
	public void setDate(String date)
	{
		this.date = date;
	}
	
	public void setItem(String item)
	{
		this.item = item;
	}
	
	public void setQty(int qty)
	{
		this.qty = qty;
	}
	
	public void setPrice(BigDecimal price)
	{
		this.price = price;
	}
	
	public void setCost(BigDecimal cost)
	{
		this.cost = cost;
	}
	
}

