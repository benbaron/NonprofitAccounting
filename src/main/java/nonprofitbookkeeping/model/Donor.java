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


}
