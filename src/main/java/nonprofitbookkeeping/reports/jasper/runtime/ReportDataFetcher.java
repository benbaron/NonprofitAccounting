
package nonprofitbookkeeping.reports.jasper.runtime;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import nonprofitbookkeeping.core.Database;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility for loading report beans via JDBC.
 */
public final class ReportDataFetcher
{
	private static final Logger LOGGER =
		LoggerFactory.getLogger(ReportDataFetcher.class);
	
	/**
	 * Instantiates a new report data fetcher.
	 */
	private ReportDataFetcher()
	{
	
	}
	
	/**
	 * Query beans.
	 *
	 * @param <B> the generic type
	 * @param beanClass the bean class
	 * @param sql the sql
	 * @return the list
	 */
	public static <B> List<B> queryBeans(Class<B> beanClass, String sql)
	{
		
		LOGGER.debug("Fetching report data for beanClass={}, sql={}",
			beanClass.getName(), sql);
		try (Connection cx = Database.get().getConnection())
		{				
			List<B> lb = JdbcBeanLoader.queryBeans(cx, beanClass, sql);
			if (lb.isEmpty())
			{
				LOGGER.info(
					"No rows returned for beanClass={}, report will rely on "
						+ "JRXML no-data handling",
					beanClass.getName());
			}
			else
			{
				LOGGER.debug("Fetched {} rows for beanClass={}", lb.size(),
					beanClass.getName());
			}
			return lb;
		}
		catch (SQLException ex)
		{
			throw new IllegalStateException(
				"Failed to query report data for " + beanClass.getSimpleName(),
				ex
			);
		}
		
	}
	
}
