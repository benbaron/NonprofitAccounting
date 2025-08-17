package nonprofitbookkeeping.persistence.dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import nonprofitbookkeeping.model.InventoryItem;

import java.util.List;
import java.util.Optional;

/**
 * DAO for {@link InventoryItem} entities.
 */
public class InventoryDao {
    private final EntityManager entityManager;

    public InventoryDao(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public void save(InventoryItem item) {
        EntityTransaction tx = entityManager.getTransaction();
        tx.begin();
        if (entityManager.find(InventoryItem.class, item.getId()) == null) {
            entityManager.persist(item);
        } else {
            entityManager.merge(item);
        }
        tx.commit();
    }

    public List<InventoryItem> findAll() {
        return entityManager.createQuery("SELECT i FROM InventoryItem i", InventoryItem.class)
                .getResultList();
    }

    public Optional<InventoryItem> findById(String id) {
        return Optional.ofNullable(entityManager.find(InventoryItem.class, id));
    }

    public boolean delete(String id) {
        EntityTransaction tx = entityManager.getTransaction();
        tx.begin();
        InventoryItem item = entityManager.find(InventoryItem.class, id);
        if (item != null) {
            entityManager.remove(item);
            tx.commit();
            return true;
        }
        tx.commit();
        return false;
    }
}
