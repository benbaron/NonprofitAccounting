/**
 * nonprofit-scaledger-ribbon.zip_expanded
 * Donor.java
 * Donor
 */
package nonprofitbookkeeping.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


/**
 * Donor represents a donor with an ID, name, total donation amount, and last donation date.
 * For JSON mapping, this class must have appropriate getters and setters.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Donor implements Serializable
{
	/**
	 * The unique identifier for this serializable class.
	 */
	private static final long serialVersionUID = 4930419801310521918L;
	/** The unique identifier for the donor. */
	private String donorId;
	/** The name of the donor. */
	private String name;
	/** The total cumulative amount donated by this donor. */
	private BigDecimal totalDonations;
	/** The date of the last donation made by this donor. */
	private Date lastDonationDate;
	/** The amount of a specific donation. This might represent the most recent donation or a donation being processed. */
	private BigDecimal donationAmount;
	/** The type or category of the donation (e.g., "Monetary", "In-Kind"). */
	private String donationType;
	/** The date of a specific donation, corresponding to the {@code donationAmount}. */
	private Date donationDate;

	// Note: Lombok @Data generates getters and setters.
	// The explicit getters and setters below are redundant but Javadoc is provided as they exist.

	/**
	 * Gets the donor's unique identifier.
	 * @return The donor ID.
	 */
	public String getDonorId()
	{
		return this.donorId;
	}
	/**
	 * Sets the donor's unique identifier.
	 * @param donorId The donor ID to set.
	 */
	public void setDonorId(String donorId)
	{
		this.donorId = donorId;
	}
	/**
	 * Gets the name of the donor.
	 * @return The donor's name.
	 */
	public String getName()
	{
		return this.name;
	}
	/**
	 * Sets the name of the donor.
	 * @param name The donor's name to set.
	 */
	public void setName(String name)
	{
		this.name = name;
	}
	/**
	 * Gets the total cumulative donations from this donor.
	 * @return The total donations amount.
	 */
	public BigDecimal getTotalDonations()
	{
		return this.totalDonations;
	}
	/**
	 * Sets the total cumulative donations for this donor.
	 * @param totalDonations The total donations amount to set.
	 */
	public void setTotalDonations(BigDecimal totalDonations)
	{
		this.totalDonations = totalDonations;
	}
	/**
	 * Gets the date of the last donation made by this donor.
	 * @return The last donation date.
	 */
	public Date getLastDonationDate()
	{
		return this.lastDonationDate;
	}
	/**
	 * Sets the date of the last donation made by this donor.
	 * @param lastDonationDate The last donation date to set.
	 */
	public void setLastDonationDate(Date lastDonationDate)
	{
		this.lastDonationDate = lastDonationDate;
	}
	/**
	 * Gets the amount of a specific donation.
	 * This might represent the most recent donation or a donation being processed.
	 * @return The specific donation amount.
	 */
	public BigDecimal getDonationAmount()
	{
		return this.donationAmount;
	}
	/**
	 * Sets the amount of a specific donation.
	 * @param donationAmount The specific donation amount to set.
	 */
	public void setDonationAmount(BigDecimal donationAmount)
	{
		this.donationAmount = donationAmount;
	}
	/**
	 * Gets the type or category of the donation.
	 * @return The donation type (e.g., "Monetary", "In-Kind").
	 */
	public String getDonationType()
	{
		return this.donationType;
	}
	/**
	 * Sets the type or category of the donation.
	 * @param donationType The donation type to set.
	 */
	public void setDonationType(String donationType)
	{
		this.donationType = donationType;
	}
	/**
	 * Gets the date of a specific donation.
	 * This corresponds to the {@code donationAmount}.
	 * @return The date of the specific donation.
	 */
	public Date getDonationDate()
	{
		return this.donationDate;
	}
	/**
	 * Sets the date of a specific donation.
	 * @param donationDate The date of the specific donation to set.
	 */
	public void setDonationDate(Date donationDate)
	{
		this.donationDate = donationDate;
	}


}