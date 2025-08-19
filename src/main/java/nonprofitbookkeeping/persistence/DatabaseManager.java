package nonprofitbookkeeping.persistence;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import org.flywaydb.core.Flyway;


/**
 * Centralizes creation of {@link EntityManager} instances backed by the
 * embedded H2 database.  The persistence unit configuration is located in
 * {@code META-INF/persistence.xml}.  This class allows the rest of the
 * application to access the database without repeatedly creating new
 * {@link EntityManagerFactory} instances.
 */
public final class DatabaseManager {
    private static final EntityManagerFactory emf;

    static {
        Flyway.configure()
                .dataSource("jdbc:h2:mem:nonprofit;DB_CLOSE_DELAY=-1", "sa", "")
                .load()
                .migrate();
        emf = Persistence.createEntityManagerFactory("nonprofitPU");

    }

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

    public static Connection getConnection() throws SQLException {
        return connectionPool.getConnection();
    }

    public static synchronized void startServer() {
        if (server == null) {
            try {
                server = Server.createTcpServer("-tcpAllowOthers").start();
            } catch (SQLException e) {
                throw new IllegalStateException("Failed to start H2 server", e);
            }
        }
    }

    public static synchronized void shutdown() {
        if (emf.isOpen()) {
            emf.close();
        }
        if (connectionPool != null) {
            connectionPool.dispose();
            connectionPool = null;
        }
        if (server != null) {
            server.stop();
            server = null;
        }
    }
}

