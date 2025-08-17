package nonprofitbookkeeping.persistence;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

/**
 * Centralizes creation of {@link EntityManager} instances backed by the
 * embedded H2 database.  The persistence unit configuration is located in
 * {@code META-INF/persistence.xml}.  This class allows the rest of the
 * application to access the database without repeatedly creating new
 * {@link EntityManagerFactory} instances.
 */
public final class DatabaseManager {
    private static final EntityManagerFactory emf =
            Persistence.createEntityManagerFactory("nonprofitPU");

    private DatabaseManager() {
        // Utility class
    }

    /**
     * Obtain a new {@link EntityManager} connected to the embedded database.
     *
     * @return fresh {@link EntityManager}
     */
    public static EntityManager getEntityManager() {
        return emf.createEntityManager();
    }
}

