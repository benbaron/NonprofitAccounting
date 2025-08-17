package nonprofitbookkeeping.persistence;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import nonprofitbookkeeping.model.Donor;

import java.util.List;
import java.util.Optional;

/** Repository for {@link Donor} entities. */
public class DonorRepository {
    private final EntityManager entityManager;

    public DonorRepository(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public void save(Donor donor) {
        EntityTransaction tx = entityManager.getTransaction();
        tx.begin();
        if (entityManager.find(Donor.class, donor.getDonorId()) == null) {
            entityManager.persist(donor);
        } else {
            entityManager.merge(donor);
        }
        tx.commit();
    }

    public List<Donor> findAll() {
        return entityManager.createQuery("SELECT d FROM Donor d", Donor.class).getResultList();
    }

    public Optional<Donor> findById(String id) {
        return Optional.ofNullable(entityManager.find(Donor.class, id));
    }

    public boolean delete(String id) {
        EntityTransaction tx = entityManager.getTransaction();
        tx.begin();
        Donor donor = entityManager.find(Donor.class, id);
        if (donor != null) {
            entityManager.remove(donor);
            tx.commit();
            return true;
        }
        tx.commit();
        return false;
    }
}
