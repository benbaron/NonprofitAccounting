package nonprofitbookkeeping.persistence;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import nonprofitbookkeeping.model.AccountingTransaction;

import java.util.List;
import java.util.Optional;

/** Repository for {@link AccountingTransaction} entities. */
public class AccountingTransactionRepository {
    private final EntityManager entityManager;

    public AccountingTransactionRepository(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public AccountingTransaction save(AccountingTransaction txObj) {
        EntityTransaction tx = entityManager.getTransaction();
        tx.begin();
        if (txObj.getId() == 0) {
            entityManager.persist(txObj);
        } else {
            txObj = entityManager.merge(txObj);
        }
        tx.commit();
        return txObj;
    }

    public List<AccountingTransaction> findAll() {
        return entityManager.createQuery("SELECT t FROM AccountingTransaction t", AccountingTransaction.class)
                .getResultList();
    }

    public Optional<AccountingTransaction> findById(int id) {
        return Optional.ofNullable(entityManager.find(AccountingTransaction.class, id));
    }

    public boolean delete(int id) {
        EntityTransaction tx = entityManager.getTransaction();
        tx.begin();
        AccountingTransaction transaction = entityManager.find(AccountingTransaction.class, id);
        if (transaction != null) {
            entityManager.remove(transaction);
            tx.commit();
            return true;
        }
        tx.commit();
        return false;
    }
}
