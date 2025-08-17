package nonprofitbookkeeping.persistence;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import nonprofitbookkeeping.model.ScaRecord;

import java.util.List;
import java.util.Optional;

/** Repository for {@link ScaRecord} entities. */
public class ScaRecordRepository {
    private final EntityManager entityManager;

    public ScaRecordRepository(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public void save(ScaRecord record) {
        EntityTransaction tx = entityManager.getTransaction();
        tx.begin();
        if (entityManager.find(ScaRecord.class, record.getId()) == null) {
            entityManager.persist(record);
        } else {
            entityManager.merge(record);
        }
        tx.commit();
    }

    public List<ScaRecord> findAll() {
        return entityManager.createQuery("SELECT s FROM ScaRecord s", ScaRecord.class).getResultList();
    }

    public Optional<ScaRecord> findById(String id) {
        return Optional.ofNullable(entityManager.find(ScaRecord.class, id));
    }

    public boolean delete(String id) {
        EntityTransaction tx = entityManager.getTransaction();
        tx.begin();
        ScaRecord record = entityManager.find(ScaRecord.class, id);
        if (record != null) {
            entityManager.remove(record);
            tx.commit();
            return true;
        }
        tx.commit();
        return false;
    }
}
