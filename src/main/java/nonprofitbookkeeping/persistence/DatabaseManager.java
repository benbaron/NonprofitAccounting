package nonprofitbookkeeping.persistence;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import java.sql.Connection;
import java.sql.SQLException;
import org.h2.jdbcx.JdbcConnectionPool;
import org.h2.tools.Server;

/**
 * Centralizes creation of {@link EntityManager} instances backed by the
 * embedded H2 database.  The persistence unit configuration is located in
 * {@code META-INF/persistence.xml}.  This class allows the rest of the
 * application to access the database without repeatedly creating new
 * {@link EntityManagerFactory} instances.
 */
public final class DatabaseManager {
    private static final String JDBC_URL = "jdbc:h2:file:./data/nonprofit;AUTO_SERVER=TRUE";
    private static Server server;
    private static final EntityManagerFactory emf;
    private static JdbcConnectionPool connectionPool;

    static {
        startServer();
        emf = Persistence.createEntityManagerFactory("nonprofitPU");
        connectionPool = JdbcConnectionPool.create(JDBC_URL, "sa", "");
        Runtime.getRuntime().addShutdownHook(new Thread(DatabaseManager::shutdown));
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

