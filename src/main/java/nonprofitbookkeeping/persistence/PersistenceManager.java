package nonprofitbookkeeping.persistence;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

/**
 * Provides application wide access to the JPA {@link EntityManager}.
 * This centralizes configuration so services no longer create their own
 * {@link EntityManagerFactory} instances.
 */
public final class PersistenceManager {
    private static final EntityManagerFactory emf =
            Persistence.createEntityManagerFactory("nonprofitPU");

    private PersistenceManager() {
    }

    /**
     * Obtain a new {@link EntityManager} for interacting with the database.
     * @return an EntityManager from the shared factory.
     */
    public static EntityManager getEntityManager() {
        return emf.createEntityManager();
    }
}
