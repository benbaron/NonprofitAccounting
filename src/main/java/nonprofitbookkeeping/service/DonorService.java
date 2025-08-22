
package nonprofitbookkeeping.service;

import jakarta.persistence.EntityManager;
import nonprofitbookkeeping.model.Donor;
import nonprofitbookkeeping.persistence.DonorRepository;
import nonprofitbookkeeping.persistence.DatabaseManager;

import java.util.List;

/**
 * Service layer for {@link Donor} entities using JPA for persistence.
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

        }

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

        }

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
	
}

