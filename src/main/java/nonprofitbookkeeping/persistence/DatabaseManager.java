
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
public final class DatabaseManager
{
	private static EntityManagerFactory emf;
	private static JdbcConnectionPool connectionPool;
	private static Server server;
	
	static
	{
		initialize();
		
	}
	
	private DatabaseManager()
	{
		
		// Utility class
	}
	
	/**
	 * Initialize the underlying {@link EntityManagerFactory} and connection
	 * pool if needed.
	 */
	public static synchronized void initialize()
	{
		
		if (emf == null || !emf.isOpen())
		{
			emf = Persistence.createEntityManagerFactory("nonprofitPU");
		}
		
		if (connectionPool == null)
		{
			connectionPool = JdbcConnectionPool.create(
				"jdbc:h2:file:./data/nonprofit;AUTO_SERVER=TRUE", "sa",
				"");
		}
		
	}
	
	/**
	 * Obtain a new {@link EntityManager} connected to the embedded database.
	 *
	 * @return fresh {@link EntityManager}
	 */
	public static synchronized EntityManager getEntityManager()
	{
		
		if (emf == null || !emf.isOpen())
		{
			initialize();
		}
		
		return emf.createEntityManager();
		
	}
	
	/**
	 * Retrieve a raw JDBC {@link Connection} from the underlying connection
	 * pool.
	 */
	public static Connection getConnection() throws SQLException
	{
		
		if (connectionPool == null)
		{
			initialize();
		}
		
		return connectionPool.getConnection();
		
	}
	
	/**
	 * Start an H2 TCP server to allow external database connections during
	 * development or debugging.
	 */
	public static synchronized void startServer()
	{
		
		if (server == null)
		{
			
			try
			{
				server = Server.createTcpServer("-tcpAllowOthers").start();
			}
			catch (SQLException e)
			{
				throw new IllegalStateException("Failed to start H2 server", e);
			}
			
		}
		
	}
	
	/**
	 * Shut down the {@link EntityManagerFactory}, connection pool and H2
	 * server, releasing all resources.
	 */
	public static synchronized void shutdown()
	{
		
		if (emf != null && emf.isOpen())
		{
			emf.close();
		}
		
		emf = null;
		
		if (connectionPool != null)
		{
			connectionPool.dispose();
			connectionPool = null;
		}
		
		if (server != null)
		{
			server.stop();
			server = null;
		}
		
	}
	
}

