package nonprofitbookkeeping.persistence;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

/**
 * Provides a singleton {@link EntityManagerFactory} for the application.
 * <p>
 * The persistence unit is configured via {@code META-INF/persistence.xml} and
 * uses an in-memory H2 database by default.  This utility exposes a convenient
 * method to obtain an {@link EntityManager} without forcing callers to manage
 * the factory lifecycle.
 * </p>
 */
public final class EntityManagerProvider {

    private static final EntityManagerFactory EMF =
            Persistence.createEntityManagerFactory("nonprofitPU");

    private EntityManagerProvider() {
        // utility class
    }

    /**
     * Returns a new {@link EntityManager} instance.
     *
     * @return a fresh {@code EntityManager}
     */
    public static EntityManager getEntityManager() {
        return EMF.createEntityManager();
    }
}
