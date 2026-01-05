
package nonprofitbookkeeping.reports.jasper.runtime;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import nonprofitbookkeeping.core.Database;

/**
 * Utility for loading report beans via JDBC.
 */
public final class ReportDataFetcher
{
	
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
		
		try (Connection cx = Database.get().getConnection())
		{				
			List<B> lb = JdbcBeanLoader.queryBeans(cx, beanClass, sql);
			System.out.println(lb.toString());
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
