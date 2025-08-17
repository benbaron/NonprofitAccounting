package nonprofitbookkeeping.persistence.dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import nonprofitbookkeeping.persistence.entity.LedgerEntryEntity;

import java.util.List;
import java.util.Optional;

/**
 * Data access object for {@link LedgerEntryEntity}.
 */
public class LedgerEntryDao {
    private final EntityManager entityManager;

    public LedgerEntryDao(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    /** Persist or update a ledger entry. */
    public void save(LedgerEntryEntity entry) {
        EntityTransaction tx = entityManager.getTransaction();
        tx.begin();
        if (entry.getId() == null) {
            entityManager.persist(entry);
        } else {
            entityManager.merge(entry);
        }
        tx.commit();
    }

    /** Retrieve all ledger entries. */
    public List<LedgerEntryEntity> findAll() {
        return entityManager.createQuery("SELECT e FROM LedgerEntryEntity e", LedgerEntryEntity.class)
                .getResultList();
    }

    /** Find a ledger entry by id. */
    public Optional<LedgerEntryEntity> findById(Long id) {
        return Optional.ofNullable(entityManager.find(LedgerEntryEntity.class, id));
    }
}
