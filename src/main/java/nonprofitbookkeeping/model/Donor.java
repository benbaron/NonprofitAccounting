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
	 * serialVersionUID : long
	 */
	private static final long serialVersionUID = 4930419801310521918L;
	private String donorId;
	private String name;
	private BigDecimal totalDonations;
	private Date lastDonationDate;
	private BigDecimal donationAmount;
	private String donationType;
	private Date donationDate;
	/**
	 * @return the donorId
	 */
	public String getDonorId()
	{
		return this.donorId;
	}
	/**
	 * @param donorId the donorId to set
	 */
	public void setDonorId(String donorId)
	{
		this.donorId = donorId;
	}
	/**
	 * @return the name
	 */
	public String getName()
	{
		return this.name;
	}
	/**
	 * @param name the name to set
	 */
	public void setName(String name)
	{
		this.name = name;
	}
	/**
	 * @return the totalDonations
	 */
	public BigDecimal getTotalDonations()
	{
		return this.totalDonations;
	}
	/**
	 * @param totalDonations the totalDonations to set
	 */
	public void setTotalDonations(BigDecimal totalDonations)
	{
		this.totalDonations = totalDonations;
	}
	/**
	 * @return the lastDonationDate
	 */
	public Date getLastDonationDate()
	{
		return this.lastDonationDate;
	}
	/**
	 * @param lastDonationDate the lastDonationDate to set
	 */
	public void setLastDonationDate(Date lastDonationDate)
	{
		this.lastDonationDate = lastDonationDate;
	}
	/**
	 * @return the donationAmount
	 */
	public BigDecimal getDonationAmount()
	{
		return this.donationAmount;
	}
	/**
	 * @param donationAmount the donationAmount to set
	 */
	public void setDonationAmount(BigDecimal donationAmount)
	{
		this.donationAmount = donationAmount;
	}
	/**
	 * @return the donationType
	 */
	public String getDonationType()
	{
		return this.donationType;
	}
	/**
	 * @param donationType the donationType to set
	 */
	public void setDonationType(String donationType)
	{
		this.donationType = donationType;
	}
	/**
	 * @return the donationDate
	 */
	public Date getDonationDate()
	{
		return this.donationDate;
	}
	/**
	 * @param donationDate the donationDate to set
	 */
	public void setDonationDate(Date donationDate)
	{
		this.donationDate = donationDate;
	}


}