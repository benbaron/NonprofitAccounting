
package nonprofitbookkeeping.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import nonprofitbookkeeping.model.Donor;
import nonprofitbookkeeping.repository.DonorRepository;

import java.util.List;

/**
 * Service layer for {@link Donor} entities using JPA for persistence.
 */
public class DonorService
{
	
	private final DonorRepository repository;
	
	/**
	 * Constructs the service and prepares the underlying repository.
	 */
	public DonorService()
	{
		EntityManagerFactory emf =
			Persistence.createEntityManagerFactory("nonprofitPU");
		EntityManager em = emf.createEntityManager();
		this.repository = new DonorRepository(em);
		
	}
	
	/** Add a donor to the database. */
	public void addDonor(Donor d)
	{
		this.repository.save(d);
		
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
		return this.repository.findById(donorId).map(existing -> {
			existing.setName(updated.getName());
			existing.setDonationAmount(updated.getDonationAmount());
			existing.setDonationDate(updated.getDonationDate());
			existing.setDonationType(updated.getDonationType());
			existing.setTotalDonations(updated.getTotalDonations());
			existing.setLastDonationDate(updated.getLastDonationDate());
			this.repository.save(existing);
			return true;
		}).orElse(false);
		
	}
	
	/** Remove a donor by id. */
	public boolean removeDonor(String donorId)
	{
		return this.repository.delete(donorId);
		
	}
	
	/** Retrieve all donors. */
	public List<Donor> getAllDonors()
	{
		return this.repository.findAll();
		
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

