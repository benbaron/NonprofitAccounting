package nonprofitbookkeeping.reports.jasper.runtime;

import nonprofitbookkeeping.core.Database;
import nonprofitbookkeeping.reports.jasper.AbstractReportGenerator;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.List;

/**
 * Base Jasper generator that fetches report data from JDBC using field map metadata.
 *
 * @param <B> bean type used as the Jasper data source row
 */
public abstract class FieldMappedReportGenerator<B>
	extends AbstractReportGenerator
{
	private static final Map<String, ReportDataProvider<?>>
		DATA_PROVIDER_REGISTRY = new ConcurrentHashMap<>();
	
	private final FieldMap fieldMap;
	private final Class<B> beanClass;
	private final ReportContext context;
	
	protected FieldMappedReportGenerator(Class<B> beanClass,
		FieldMap fieldMap,
		ReportContext context)
	{
		this.beanClass = Objects.requireNonNull(beanClass,
			"beanClass");
		this.fieldMap = fieldMap;
		this.context = context;
	}
	
	protected FieldMappedReportGenerator(Class<B> beanClass,
		FieldMap fieldMap)
	{
		this(beanClass, fieldMap, null);
	}
	
	protected FieldMappedReportGenerator(Class<B> beanClass,
		String fieldMapResource,
		ReportContext context)
	{
		this(beanClass, loadFieldMap(fieldMapResource), context);
	}
	
	protected FieldMappedReportGenerator(Class<B> beanClass,
		String fieldMapResource)
	{
		this(beanClass, fieldMapResource, null);
	}
	
	public static void registerReportDataProvider(String reportTypeKey,
		ReportDataProvider<?> provider)
	{
		Objects.requireNonNull(reportTypeKey, "reportTypeKey");
		Objects.requireNonNull(provider, "provider");
		DATA_PROVIDER_REGISTRY.put(reportTypeKey, provider);
	}
	
	public static void unregisterReportDataProvider(String reportTypeKey)
	{
		if (reportTypeKey != null)
		{
			DATA_PROVIDER_REGISTRY.remove(reportTypeKey);
		}
	}
	
	protected FieldMap getFieldMap()
	{
		return this.fieldMap;
	}
	
	protected Class<B> getBeanClass()
	{
		return this.beanClass;
	}
	
	protected ReportContext getReportContext()
	{
		return this.context;
	}
	
	@Override
	protected List<B> getReportData()
	{
		return queryReportData(this.fieldMap, this.context);
	}
	
	/**
	 * Optional per-report SQL override. Override in subclasses to supply SQL
	 * and skip the registry/default field map logic.
	 */
	protected String reportSql(FieldMap fieldMap, ReportContext context)
	{
		return null;
	}
	
	/**
	 * Optional per-report parameter binder override. Override in subclasses
	 * if no provider is used.
	 */
	protected JdbcBeanLoader.SqlParameterSetter
		reportSqlParameterSetter(FieldMap fieldMap, ReportContext context)
	{
		return null;
	}
	
	/**
	 * Optional per-report provider override. Override in subclasses to supply
	 * SQL and parameter binding.
	 */
	protected ReportDataProvider<B> reportDataProvider(FieldMap fieldMap,
		ReportContext context)
	{
		return null;
	}
	
	protected List<B> queryReportData(FieldMap fieldMap,
		ReportContext context)
	{
		ReportDataProvider<B> provider = resolveProvider(fieldMap, context);
		String sql = resolveSql(fieldMap, context, provider);
		
		if (sql == null || sql.isBlank())
		{
			return Collections.emptyList();
		}
		
		JdbcBeanLoader.SqlParameterSetter paramSetter =
			resolveParameterSetter(fieldMap, context, provider);
		
		try (Connection cx = Database.get().getConnection())
		{
			return JdbcBeanLoader.queryBeans(cx, this.beanClass, sql,
				paramSetter);
		}
		catch (SQLException e)
		{
			throw new IllegalStateException(
				"Failed to query report data for " +
					this.beanClass.getName(), e);
		}
	}
	
	private ReportDataProvider<B> resolveProvider(FieldMap fieldMap,
		ReportContext context)
	{
		ReportDataProvider<B> provider = reportDataProvider(fieldMap, context);
		
		if (provider != null)
		{
			return provider;
		}
		
		String reportTypeKey = context == null ? null :
			context.getReportType();
		
		if (reportTypeKey != null)
		{
			ReportDataProvider<?> registered =
				DATA_PROVIDER_REGISTRY.get(reportTypeKey);
			
			if (registered != null)
			{
				@SuppressWarnings("unchecked")
				ReportDataProvider<B> typed =
					(ReportDataProvider<B>) registered;
				return typed;
			}
		}
		
		return null;
	}
	
	private String resolveSql(FieldMap fieldMap, ReportContext context,
		ReportDataProvider<B> provider)
	{
		String sql = reportSql(fieldMap, context);
		
		if (sql != null && !sql.isBlank())
		{
			return sql;
		}
		
		if (provider != null)
		{
			sql = provider.sql(context);
			
			if (sql != null && !sql.isBlank())
			{
				return sql;
			}
		}
		
		if (fieldMap == null)
		{
			return null;
		}
		
		String selectList = fieldMap.buildSelectListFromDbExprs();
		
		if (selectList == null || selectList.isBlank())
		{
			return null;
		}
		
		return "select\n" + selectList;
	}
	
	private JdbcBeanLoader.SqlParameterSetter resolveParameterSetter(
		FieldMap fieldMap,
		ReportContext context,
		ReportDataProvider<B> provider)
	{
		JdbcBeanLoader.SqlParameterSetter paramSetter =
			reportSqlParameterSetter(fieldMap, context);
		
		if (paramSetter != null)
		{
			return paramSetter;
		}
		
		return provider == null ? null : provider.parameterSetter(context);
	}
	
	private static FieldMap loadFieldMap(String fieldMapResource)
	{
		Objects.requireNonNull(fieldMapResource, "fieldMapResource");
		
		try
		{
			return FieldMapLoader.loadFromResource(fieldMapResource);
		}
		catch (Exception ex)
		{
			throw new IllegalStateException(
				"Unable to load field map resource: " +
					fieldMapResource, ex);
		}
	}
}
