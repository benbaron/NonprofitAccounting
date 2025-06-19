
package nonprofitbookkeeping.service;

import jakarta.persistence.EntityManager;
import nonprofitbookkeeping.model.Donor;
import nonprofitbookkeeping.persistence.DonorRepository;
import nonprofitbookkeeping.persistence.DatabaseManager;

<<<<<<< Upstream, based on origin/codex/read-provided-xlsx-file
=======
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Date;
import java.util.ArrayList;
>>>>>>> b1f07f2 Extend SQL support
import java.util.List;

/**
<<<<<<< Upstream, based on origin/codex/read-provided-xlsx-file
 * Service layer for {@link Donor} entities using JPA for persistence.
=======
 * Service class for managing {@link Donor} information.
 * This class provides functionalities to add, edit, remove, and retrieve donor data.
 * Donor information is persisted using SQL via {@link DatabaseManager}.
>>>>>>> b1f07f2 Extend SQL support
 */
public class DonorService
{
	
        /** Add a donor to the database. */
        public void addDonor(Donor d)
        {
                try (EntityManager em = DatabaseManager.getEntityManager())
                {
                        DonorRepository repository = new DonorRepository(em);
                        repository.save(d);
                }

<<<<<<< Upstream, based on origin/codex/read-provided-xlsx-file
=======
    /** Constructs a new {@code DonorService}. */
    public DonorService() {
    }

    /**
     * Adds a new donor to the service.
     *
     * @param donor The {@link Donor} object to add. Must not be null.
     * @throws NullPointerException if {@code donor} is null (due to ArrayList behavior).
     */
    public void addDonor(Donor donor) {
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "MERGE INTO donor(donor_id,name,total_donations,last_donation_date,donation_amount,donation_type,donation_date) KEY(donor_id) VALUES(?,?,?,?,?,?,?)"))
        {
            ps.setString(1, donor.getDonorId());
            ps.setString(2, donor.getName());
            ps.setBigDecimal(3, donor.getTotalDonations());
            ps.setDate(4, donor.getLastDonationDate() == null ? null : new Date(donor.getLastDonationDate().getTime()));
            ps.setBigDecimal(5, donor.getDonationAmount());
            ps.setString(6, donor.getDonationType());
            ps.setDate(7, donor.getDonationDate() == null ? null : new Date(donor.getDonationDate().getTime()));
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error adding donor", e);
        }
    }

    /**
     * Edits an existing donor's information.
     * The donor to be edited is identified by their name. If found, the donation amount,
     * donation type, and donation date of the existing donor are updated with values
     * from {@code updatedDonor}.
     *
     * @param donorName The name of the donor to edit.
     * @param updatedDonor A {@link Donor} object containing the updated information.
     *                     Only donation amount, type, and date are currently updated from this object.
     * @return {@code true} if a donor with the given name was found and updated, {@code false} otherwise.
     */
    public boolean editDonor(String donorName, Donor updatedDonor) {
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "UPDATE donor SET donation_amount=?, donation_type=?, donation_date=? WHERE name=?"))
        {
            ps.setBigDecimal(1, updatedDonor.getDonationAmount());
            ps.setString(2, updatedDonor.getDonationType());
            ps.setDate(3, updatedDonor.getDonationDate() == null ? null : new Date(updatedDonor.getDonationDate().getTime()));
            ps.setString(4, donorName);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Error editing donor", e);
>>>>>>> b1f07f2 Extend SQL support
        }
<<<<<<< Upstream, based on origin/codex/read-provided-xlsx-file

        /**
         * Edit an existing donor.
         *
         * @param donorId id of donor
         * @param updated updated donor data
         * @return true if donor existed and was updated
         */
        public boolean editDonor(String donorId, Donor updated)
        {
                try (EntityManager em = DatabaseManager.getEntityManager())
                {
                        DonorRepository repository = new DonorRepository(em);
                        return repository.findById(donorId).map(existing -> {
                                existing.setName(updated.getName());
                                existing.setDonationAmount(updated.getDonationAmount());
                                existing.setDonationDate(updated.getDonationDate());
                                existing.setDonationType(updated.getDonationType());
                                existing.setTotalDonations(updated.getTotalDonations());
                                existing.setLastDonationDate(updated.getLastDonationDate());
                                repository.save(existing);
                                return true;
                        }).orElse(false);
                }
=======
    }
>>>>>>> b1f07f2 Extend SQL support

<<<<<<< Upstream, based on origin/codex/read-provided-xlsx-file
        }
=======
    /**
     * Removes a donor from the service by their name.
     *
     * @param donorName The name of the donor to remove.
     * @return {@code true} if a donor with the given name was found and removed, {@code false} otherwise.
     */
    public boolean removeDonor(String donorName) {
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement("DELETE FROM donor WHERE name = ?"))
        {
            ps.setString(1, donorName);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Error removing donor", e);
        }
    }
>>>>>>> b1f07f2 Extend SQL support

<<<<<<< Upstream, based on origin/codex/read-provided-xlsx-file
        /** Remove a donor by id. */
        public boolean removeDonor(String donorId)
        {
                try (EntityManager em = DatabaseManager.getEntityManager())
                {
                        DonorRepository repository = new DonorRepository(em);
                        return repository.delete(donorId);
                }

        }

        /** Retrieve all donors. */
        public List<Donor> getAllDonors()
        {
                try (EntityManager em = DatabaseManager.getEntityManager())
                {
                        DonorRepository repository = new DonorRepository(em);
                        return repository.findAll();
                }

        }
	
	/** Compatibility stub: data is stored in DB so explicit save is unnecessary. */
	public void saveDonors(java.io.File companyDirectory)
	{
		
		// no-op
	}
	
	/** Compatibility stub: data is loaded on demand from the DB. */
	public void loadDonors(java.io.File companyDirectory)
	{
		
		// no-op
	}
	
=======
    /**
     * Retrieves a list of all donors currently managed by this service.
     *
     * @return A new {@link ArrayList} containing all {@link Donor} objects.
     *         This is a copy, so modifications to the returned list will not affect the internal storage.
     *         Returns an empty list if no donors are present.
     */
    public List<Donor> getAllDonors() {
        List<Donor> list = new ArrayList<>();
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT donor_id,name,total_donations,last_donation_date,donation_amount,donation_type,donation_date FROM donor"))
        {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Donor d = new Donor();
                d.setDonorId(rs.getString(1));
                d.setName(rs.getString(2));
                d.setTotalDonations(rs.getBigDecimal(3));
                Date ld = rs.getDate(4);
                if (ld != null) d.setLastDonationDate(new java.util.Date(ld.getTime()));
                d.setDonationAmount(rs.getBigDecimal(5));
                d.setDonationType(rs.getString(6));
                Date dd = rs.getDate(7);
                if (dd != null) d.setDonationDate(new java.util.Date(dd.getTime()));
                list.add(d);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error loading donors", e);
        }
        return list;
    }
>>>>>>> b1f07f2 Extend SQL support
}

