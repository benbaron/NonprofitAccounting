
package nonprofitbookkeeping.reports.jasper.runtime;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Executes a JDBC query and maps each row to a bean using DataFiller.
 *
 * Convention:
 *   - SQL aliases columns with bean field names:
 *       SELECT a.name AS accountname, s.opening_balance AS openingbalance ...
 */
public final class JdbcBeanLoader
{
	
	private JdbcBeanLoader()
	{
		
	}
	
	@FunctionalInterface
	public interface SqlParameterSetter
	{
		void setParameters(PreparedStatement ps) throws SQLException;
		
	}
	
	/**
	 * Run the given SQL using the provided connection, bind parameters using
	 * the SqlParameterSetter, and map each row to a bean instance.
	 *
	 * @param cx          open JDBC connection
	 * @param beanClass   bean type to instantiate
	 * @param sql         SQL to execute
	 * @param paramSetter binder for PreparedStatement parameters (may be null)
	 */
	public static <B> List<B> queryBeans(
		Connection cx,
		Class<B> beanClass,
		String sql,
		SqlParameterSetter paramSetter
	) throws SQLException
	{
		
		try (PreparedStatement ps = cx.prepareStatement(sql))
		{
			
			if (paramSetter != null)
			{
				paramSetter.setParameters(ps);
			}
			
			try (ResultSet rs = ps.executeQuery())
			{
				List<B> result = new ArrayList<>();
				ResultSetMetaData md = rs.getMetaData();
				int cols = md.getColumnCount();
				
				while (rs.next())
				{
					Map<String, Object> row = new HashMap<>();
					
					for (int i = 1; i <= cols; i++)
					{
						String label = md.getColumnLabel(i);
						
						if (label == null || label.isBlank())
						{
							label = md.getColumnName(i);
						}
						
						Object value = rs.getObject(i);
						
						if (label != null)
						{
							row.put(label, value);
						}
						
					}
					
					B bean = DataFiller.fill(beanClass, row);
					result.add(bean);
				}
				
				return result;
			}
			
		}
		
	}
	
	/**
	 * Overload: no parameters.
	 */
	public static <B> List<B> queryBeans(
		Connection cx,
		Class<B> beanClass,
		String sql
	) throws SQLException
	{

		List<B> lb = queryBeans(cx, beanClass, sql, null);
		if (lb.isEmpty())
		{
			throw new SQLException("query is empty");
		}
		System.out.println(lb.toString());
		return lb;

		
	}
	
}
