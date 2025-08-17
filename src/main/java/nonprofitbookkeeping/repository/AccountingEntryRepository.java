package nonprofitbookkeeping.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import nonprofitbookkeeping.model.AccountingEntry;

import java.util.List;
import java.util.Optional;

/** Repository for {@link AccountingEntry} entities. */
public class AccountingEntryRepository {
    private final EntityManager entityManager;

    public AccountingEntryRepository(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public AccountingEntry save(AccountingEntry entry) {
        EntityTransaction tx = entityManager.getTransaction();
        tx.begin();
        if (entry.getId() == 0) {
            entityManager.persist(entry);
        } else {
            entry = entityManager.merge(entry);
        }
        tx.commit();
        return entry;
    }

    public List<AccountingEntry> findAll() {
        return entityManager.createQuery("SELECT e FROM AccountingEntry e", AccountingEntry.class)
                .getResultList();
    }

    public Optional<AccountingEntry> findById(int id) {
        return Optional.ofNullable(entityManager.find(AccountingEntry.class, id));
    }

    public boolean delete(int id) {
        EntityTransaction tx = entityManager.getTransaction();
        tx.begin();
        AccountingEntry entry = entityManager.find(AccountingEntry.class, id);
        if (entry != null) {
            entityManager.remove(entry);
            tx.commit();
            return true;
        }
        tx.commit();
        return false;
    }
}
