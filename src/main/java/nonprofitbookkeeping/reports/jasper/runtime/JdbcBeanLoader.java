
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Executes a JDBC query and maps each row to a bean using DataFiller.
 *
 * Convention:
 *   - SQL aliases columns with bean field names:
 *       SELECT a.name AS accountname, s.opening_balance AS openingbalance ...
 */
public final class JdbcBeanLoader
{
	private static final Logger LOGGER =
		LoggerFactory.getLogger(JdbcBeanLoader.class);
	
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
		
		LOGGER.debug("Executing report query for beanClass={}, sql={}",
			beanClass.getName(), sql);
		
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
				int rowIndex = 0;
				
				// result set
				while (rs.next())
				{
					rowIndex++;
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
						
						LOGGER.trace(
							"Row {} column {} label={} value={} valueType={}",
							rowIndex,
							i,
							label,
							value,
							value == null ? "null" :
								value.getClass().getName());
						
					}
					
					LOGGER.debug(
						"\nMapping row {} for beanClass={} with columns={}",
						rowIndex, beanClass.getName(), row);
					
					B bean = DataFiller.fill(beanClass, row);
					
					LOGGER.debug("Mapped row {} to bean {}", rowIndex, bean);
					result.add(bean);
				}
				
				return result;
			}
			
		}
		
	}

	/**
	 * Run the SQL query and merge each row into a single bean using
	 * a row-based naming convention.
	 *
	 * Row 1 uses the original column label. Subsequent rows append
	 * "_2", "_3", etc. to the column label so Jasper beans can
	 * expose fields like item_description_2 or amount_3.
	 *
	 * @param cx open JDBC connection
	 * @param beanClass bean type to instantiate
	 * @param sql SQL to execute
	 * @param paramSetter binder for PreparedStatement parameters (may be null)
	 * @return a list with a single populated bean, or empty if no rows
	 * @throws SQLException when SQL errors occur
	 */
	public static <B> List<B> queryRowBasedBeans(
		Connection cx,
		Class<B> beanClass,
		String sql,
		SqlParameterSetter paramSetter
	) throws SQLException
	{
		LOGGER.debug("Executing row-based report query for beanClass={}, sql={}",
			beanClass.getName(), sql);

		try (PreparedStatement ps = cx.prepareStatement(sql))
		{
			if (paramSetter != null)
			{
				paramSetter.setParameters(ps);
			}

			try (ResultSet rs = ps.executeQuery())
			{
				Map<String, Object> merged = new HashMap<>();
				ResultSetMetaData md = rs.getMetaData();
				int cols = md.getColumnCount();
				int rowIndex = 0;

				while (rs.next())
				{
					rowIndex++;
					for (int i = 1; i <= cols; i++)
					{
						String label = md.getColumnLabel(i);
						if (label == null || label.isBlank())
						{
							label = md.getColumnName(i);
						}
						if (label == null || label.isBlank())
						{
							continue;
						}
						String mappedLabel = rowIndex == 1
							? label
							: label + "_" + rowIndex;
						Object value = rs.getObject(i);
						merged.put(mappedLabel, value);
						LOGGER.trace(
							"Row {} column {} label={} mappedLabel={} value={} valueType={}",
							rowIndex,
							i,
							label,
							mappedLabel,
							value,
							value == null ? "null" :
								value.getClass().getName());
					}
				}

				if (rowIndex == 0)
				{
					LOGGER.debug("No rows returned for beanClass={}, sql={}",
						beanClass.getName(), sql);
					return List.of();
				}

				LOGGER.debug(
					"Merged {} rows into beanClass={} with columns={}",
					rowIndex, beanClass.getName(), merged.keySet());
				B bean = DataFiller.fill(beanClass, merged);
				return List.of(bean);
			}
		}
	}
	
	
	/**
	 * Query beans.
	 *
	 * @param <B> the generic type
	 * @param cx the cx
	 * @param beanClass the bean class
	 * @param sql the sql
	 * @return the list
	 * @throws SQLException the SQL exception
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
			LOGGER.debug("No rows returned for beanClass={}, sql={}",
				beanClass.getName(), sql);
		}
		else
		{
			LOGGER.debug("Loaded {} beans for beanClass={}", lb.size(),
				beanClass.getName());
		}
		
		return lb;
		
	}
	
}
