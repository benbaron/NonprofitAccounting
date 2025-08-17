package nonprofitbookkeeping.persistence;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import nonprofitbookkeeping.model.SaleRecord;

import java.util.List;
import java.util.Optional;

/** Repository for {@link SaleRecord} entities. */
public class SaleRecordRepository {
    private final EntityManager entityManager;

    public SaleRecordRepository(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public void save(SaleRecord record) {
        EntityTransaction tx = entityManager.getTransaction();
        tx.begin();
        if (entityManager.find(SaleRecord.class, record.getId()) == null) {
            entityManager.persist(record);
        } else {
            entityManager.merge(record);
        }
        tx.commit();
    }

    public List<SaleRecord> findAll() {
        return entityManager.createQuery("SELECT s FROM SaleRecord s", SaleRecord.class).getResultList();
    }

    public Optional<SaleRecord> findById(String id) {
        return Optional.ofNullable(entityManager.find(SaleRecord.class, id));
    }

    public boolean delete(String id) {
        EntityTransaction tx = entityManager.getTransaction();
        tx.begin();
        SaleRecord record = entityManager.find(SaleRecord.class, id);
        if (record != null) {
            entityManager.remove(record);
            tx.commit();
            return true;
        }
        tx.commit();
        return false;
    }
}
