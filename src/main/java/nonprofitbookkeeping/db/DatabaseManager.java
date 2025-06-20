package nonprofitbookkeeping.db;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

/**
 * Simple helper for managing the JPA {@link EntityManagerFactory}.
 */
public final class DatabaseManager {
    private static final EntityManagerFactory emf =
            Persistence.createEntityManagerFactory("nonprofitPU");

    private DatabaseManager() { }

    /**
     * Obtain a new {@link EntityManager}.
     *
     * @return EntityManager for the persistence unit
     */
    public static EntityManager getEntityManager() {
        return emf.createEntityManager();
    }
}
