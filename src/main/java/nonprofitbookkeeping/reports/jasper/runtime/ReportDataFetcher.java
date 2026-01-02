
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
	
	private ReportDataFetcher()
	{
	
	}
	
	public static <B> List<B> queryBeans(Class<B> beanClass, String sql)
	{
		
		try (Connection cx = Database.get().getConnection())
		{
			return JdbcBeanLoader.queryBeans(cx, beanClass, sql);
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
