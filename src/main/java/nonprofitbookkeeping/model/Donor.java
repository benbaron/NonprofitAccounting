/**
 * nonprofit-scaledger-ribbon.zip_expanded Donor.java Donor
 */

package nonprofitbookkeeping.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Column;
import jakarta.persistence.OneToMany;
import jakarta.persistence.CascadeType;
import nonprofitbookkeeping.model.Donation;
import java.util.List;
import java.util.ArrayList;


/**
 * Donor represents a donor with an ID, name, total donation amount, and last donation date.
 * For JSON mapping, this class must have appropriate getters and setters.
 */
@Entity
@Table(name = "donors")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "donor")
public class Donor implements Serializable
{
	/**
	 * The unique identifier for this serializable class.
	 */
	private static final long serialVersionUID = 4930419801310521918L;
<<<<<<< Upstream, based on origin/codex/read-provided-xlsx-file
	/** The unique identifier for the donor. */
	@Id private String donorId;
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
	
=======
        /** The unique identifier for the donor. */
        @Id
        @Column(name = "donor_id")
        private String donorId;

        /** The name of the donor. */
        @Column(name = "name")
        private String name;

        /** The total cumulative amount donated by this donor. */
        @Column(name = "total_donations")
        private BigDecimal totalDonations;

        /** The date of the last donation made by this donor. */
        @Column(name = "last_donation_date")
        private Date lastDonationDate;

        /** List of individual donations associated with this donor. */
        @OneToMany(mappedBy = "donor", cascade = CascadeType.ALL, orphanRemoval = true)
        private List<Donation> donations = new ArrayList<>();

>>>>>>> a0d4b45 Remove binary document and zip files
	// Note: Lombok @Data generates getters and setters.
	// The explicit getters and setters below are redundant but Javadoc is
	// provided as they exist.
	
	/**  
	 * Constructor Donor
	 * @param object
	 * @param text
	 * @param text2
	 * @param text3
	 */
	public Donor(Object object, String text, String text2, String text3)
	{
		// TODO Auto-generated constructor stub
		
	}

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
<<<<<<< Upstream, based on origin/codex/read-provided-xlsx-file
	
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
	
	/**
	 * @return
	 */
	public String getEmail()
	{
		// TODO Auto-generated method stub
		return null;
		
	}
	
	/**
	 * @return
	 */
	public String getPhone()
	{
		// TODO Auto-generated method stub
		return null;
		
	}
	
	/**
	 * @return
	 */
	public Object getId()
	{
		// TODO Auto-generated method stub
		return null;
		
	}
	
	/**
	 * @param email
	 */
	public void setEmail(String email)
	{
		// TODO Auto-generated method stub
		
		
	}
	
	/**
	 * @param phone
	 */
	public void setPhone(String phone)
	{
		// TODO Auto-generated method stub
		
		
	}
	
	
=======

        /**
         * Gets the list of donations linked to this donor.
         *
         * @return list of {@link Donation} entities
         */
        public List<Donation> getDonations()
        {
                return this.donations;
        }

        /**
         * Replaces the donation list for this donor.
         *
         * @param donations list of donations to associate
         */
        public void setDonations(List<Donation> donations)
        {
                this.donations = donations;
        }


>>>>>>> a0d4b45 Remove binary document and zip files
}
