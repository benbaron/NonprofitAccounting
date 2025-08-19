
package nonprofitbookkeeping.persistence;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import nonprofitbookkeeping.model.Ledger;

import java.util.List;
import java.util.Optional;

/** Repository for {@link Ledger} entities. */
public class LedgerRepository
{
	private final EntityManager entityManager;
	
	public LedgerRepository(EntityManager entityManager)
	{
		this.entityManager = entityManager;
		
	}
	
	public Ledger save(Ledger ledger)
	{
		EntityTransaction tx = entityManager.getTransaction();
		tx.begin();
		
		if (ledger.getId() == null)
		{
			entityManager.persist(ledger);
		}
		else
		{
			ledger = entityManager.merge(ledger);
		}
		
		tx.commit();
		return ledger;
		
	}
	
	public List<Ledger> findAll()
	{
		return entityManager.createQuery("SELECT l FROM Ledger l", Ledger.class)
			.getResultList();
		
	}
	
	public Optional<Ledger> findById(long id)
	{
		return Optional.ofNullable(entityManager.find(Ledger.class, id));
		
	}
	
	public boolean delete(long id)
	{
		EntityTransaction tx = entityManager.getTransaction();
		tx.begin();
		Ledger ledger = entityManager.find(Ledger.class, id);
		
		if (ledger != null)
		{
			entityManager.remove(ledger);
			tx.commit();
			return true;
		}
		
		tx.commit();
		return false;
		
	}
	
	/** Returns the first ledger found or {@code null} if none exist. */
	public Ledger findFirst()
	{
		return entityManager.createQuery("SELECT l FROM Ledger l", Ledger.class)
			.setMaxResults(1)
			.getResultStream()
			.findFirst()
			.orElse(null);
		
	}
	
}
