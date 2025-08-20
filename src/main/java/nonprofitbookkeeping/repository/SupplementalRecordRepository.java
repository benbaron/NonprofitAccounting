package nonprofitbookkeeping.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import nonprofitbookkeeping.persistence.entity.SupplementalRecordEntity;

import java.util.List;
import java.util.Optional;

/** Repository for {@link SupplementalRecordEntity} entities. */
public class SupplementalRecordRepository {
    private final EntityManager entityManager;

    public SupplementalRecordRepository(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public SupplementalRecordEntity save(SupplementalRecordEntity record) {
        EntityTransaction tx = entityManager.getTransaction();
        tx.begin();
        if (record.getId() == null) {
            entityManager.persist(record);
        } else {
            record = entityManager.merge(record);
        }
        tx.commit();
        return record;
    }

    public List<SupplementalRecordEntity> findAll() {
        return entityManager.createQuery("SELECT s FROM SupplementalRecordEntity s", SupplementalRecordEntity.class)
                .getResultList();
    }

    public Optional<SupplementalRecordEntity> findById(Long id) {
        return Optional.ofNullable(entityManager.find(SupplementalRecordEntity.class, id));
    }

    public boolean delete(Long id) {
        EntityTransaction tx = entityManager.getTransaction();
        tx.begin();
        SupplementalRecordEntity record = entityManager.find(SupplementalRecordEntity.class, id);
        if (record != null) {
            entityManager.remove(record);
            tx.commit();
            return true;
        }
        tx.commit();
        return false;
    }
}
