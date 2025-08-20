
package nonprofitbookkeeping.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import nonprofitbookkeeping.model.SupplementalRecord;

import java.util.List;
import java.util.Optional;

/** Repository for {@link SupplementalRecord} entities. */
public class SupplementalRecordRepository
{
	private final EntityManager entityManager;
	
	public SupplementalRecordRepository(EntityManager entityManager)
	{
		this.entityManager = entityManager;
		
	}
	
	public SupplementalRecord save(SupplementalRecord record)
	{
		EntityTransaction tx = entityManager.getTransaction();
		tx.begin();
		
		if (record.getId() == 0)
		{
			entityManager.persist(record);
		}
		else
		{
			record = entityManager.merge(record);
		}
		
		tx.commit();
		return record;
		
	}
	
	public List<SupplementalRecord> findAll()
	{
		return entityManager
			.createQuery("SELECT s FROM SupplementalRecord s",
				SupplementalRecord.class)
			.getResultList();
		
	}
	
	public Optional<SupplementalRecord> findById(int id)
	{
		return Optional
			.ofNullable(entityManager.find(SupplementalRecord.class, id));
		
	}
	
	public boolean delete(int id)
	{
		EntityTransaction tx = entityManager.getTransaction();
		tx.begin();
		SupplementalRecord record =
			entityManager.find(SupplementalRecord.class, id);
		
		if (record != null)
		{
			entityManager.remove(record);
			tx.commit();
			return true;
		}
		
		tx.commit();
		return false;
		
	}
	
}
