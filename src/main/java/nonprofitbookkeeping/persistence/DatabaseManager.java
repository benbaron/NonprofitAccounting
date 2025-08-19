
package nonprofitbookkeeping.persistence;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

import java.sql.Connection;
import java.sql.SQLException;

import org.flywaydb.core.Flyway;


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
	
	static
	{
		initialize();
		
	}
	
	private DatabaseManager()
	{
		
		// Utility class
	}
	
	/**
	 * Initialize the underlying {@link EntityManagerFactory} if needed.
	 */
	public static synchronized void initialize()
	{
		
		if (emf == null || !emf.isOpen())
		{
			emf = Persistence.createEntityManagerFactory("nonprofitPU");
		}
		
	}
	
	/**
	 * Shut down the {@link EntityManagerFactory} and release resources.
	 */
	public static synchronized void shutdown()
	{
		
		if (emf != null && emf.isOpen())
		{
			emf.close();
		}
		
		emf = null;
		
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
	
	public static Connection getConnection() throws SQLException
	{
		return connectionPool.getConnection();
		
	}
	
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
	
	public static synchronized void shutdown()
	{
		
		if (emf.isOpen())
		{
			emf.close();
		}
		
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

