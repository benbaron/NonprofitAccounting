/**
 * nonprofit-scaledger-ribbon.zip_expanded
 * Grant.java
 * Grant
 */
package nonprofitbookkeeping.model;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;

/**
 * Represents a grant received by the nonprofit organization.
 * This class stores details about the grant such as its ID, the grantor,
 * amount, date awarded, purpose, and current status.
 */
public class Grant
{
	/** The unique identifier for the grant. */
	@JsonProperty String grantId = "";
	/** The name of the organization or individual who awarded the grant. */
	@JsonProperty String grantor = "";
	/** The monetary amount of the grant. */
	@JsonProperty BigDecimal amount = new BigDecimal(0);
	/** The date when the grant was awarded, typically in a string format (e.g., "YYYY-MM-DD"). */
	@JsonProperty String dateAwarded= "";
	/** The specific purpose or project for which the grant was awarded. */
	@JsonProperty String purpose= "";
	/** The current status of the grant (e.g., "Awarded", "Pending", "Completed", "Closed"). */
	@JsonProperty String status= "";
	
	
	/**  
	 * Constructs a new Grant with specified details.
	 * @param grantId The unique identifier for the grant.
	 * @param grantor The name of the grantor.
	 * @param amount The monetary amount of the grant.
	 * @param dateAwarded The date the grant was awarded.
	 * @param purpose The purpose of the grant.
	 * @param status The current status of the grant.
	 */
	public Grant(String grantId, String grantor, 
	             BigDecimal amount, String dateAwarded,
	             String purpose, String status)
	{
		this.grantId = grantId;
		this.grantor = grantor;
		this.amount = amount;
		this.dateAwarded = dateAwarded;
		this.purpose = purpose;
		this.status = status;
	}
	/**
	 * Gets the unique identifier of the grant.
	 * @return The grant ID.
	 */
	public String getGrantId()
	{
		return this.grantId;
	}
	/**
	 * Sets the unique identifier of the grant.
	 * @param grantId The grant ID to set.
	 */
	public void setGrantId(String grantId)
	{
		this.grantId = grantId;
	}
	/**
	 * Gets the name of the grantor.
	 * @return The grantor's name.
	 */
	public String getGrantor()
	{
		return this.grantor;
	}
	/**
	 * Sets the name of the grantor.
	 * @param grantor The grantor's name to set.
	 */
	public void setGrantor(String grantor)
	{
		this.grantor = grantor;
	}
	/**
	 * Gets the monetary amount of the grant.
	 * @return The grant amount.
	 */
	public BigDecimal getAmount()
	{
		return this.amount;
	}
	/**
	 * Sets the monetary amount of the grant.
	 * @param amount The grant amount to set.
	 */
	public void setAmount(BigDecimal amount)
	{
		this.amount = amount;
	}
	/**
	 * Gets the date when the grant was awarded.
	 * @return The date awarded as a string.
	 */
	public String getDateAwarded()
	{
		return this.dateAwarded;
	}
	/**
	 * Sets the date when the grant was awarded.
	 * @param dateAwarded The date awarded to set (e.g., "YYYY-MM-DD").
	 */
	public void setDateAwarded(String dateAwarded)
	{
		this.dateAwarded = dateAwarded;
	}
	/**
	 * Gets the purpose of the grant.
	 * @return The grant's purpose.
	 */
	public String getPurpose()
	{
		return this.purpose;
	}
	/**
	 * Sets the purpose of the grant.
	 * @param purpose The grant's purpose to set.
	 */
	public void setPurpose(String purpose)
	{
		this.purpose = purpose;
	}
	/**
	 * Gets the current status of the grant.
	 * @return The grant's status (e.g., "Awarded", "Pending").
	 */
	public String getStatus()
	{
		return this.status;
	}
	/**
	 * Sets the current status of the grant.
	 * @param status The grant's status to set.
	 */
	public void setStatus(String status)
	{
		this.status = status;
	}

	
}
