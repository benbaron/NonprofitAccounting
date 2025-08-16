
package nonprofitbookkeeping.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import nonprofitbookkeeping.model.Donor;

import java.util.List;
import java.util.Optional;

/**
 * Repository class for {@link Donor} entities providing basic CRUD operations
 * using a supplied {@link EntityManager}.
 */
public class DonorRepository
{
	private final EntityManager entityManager;
	
	public DonorRepository(EntityManager entityManager)
	{
		this.entityManager = entityManager;
		
	}
	
	/** Persist or merge a donor. */
	public void save(Donor existing)
	{
		EntityTransaction tx = entityManager.getTransaction();
		tx.begin();
		
		if (entityManager.find(Donor.class, existing.getDonorId()) == null)
		{
			entityManager.persist(existing);
		}
		else
		{
			entityManager.merge(existing);
		}
		
		tx.commit();
		
	}
	
	/** Retrieve all donors. */
	public List<Donor> findAll()
	{
		return entityManager.createQuery("SELECT d FROM Donor d", Donor.class)
			.getResultList();
		
	}
	
	/** Find donor by id. */
	public Optional<Donor> findById(String id)
	{
		return Optional.ofNullable(entityManager.find(Donor.class, id));
		
	}
	
	/** Delete donor by id. */
	public boolean delete(String id)
	{
		EntityTransaction tx = entityManager.getTransaction();
		tx.begin();
		Donor donor = entityManager.find(Donor.class, id);
		
		if (donor != null)
		{
			entityManager.remove(donor);
			tx.commit();
			return true;
		}
		
		tx.commit();
		return false;
		
	}
	
}

