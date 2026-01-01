package nonprofitbookkeeping.reports.jasper;

import nonprofitbookkeeping.reports.jasper.runtime.ReportContext;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Base generator that resolves report fields from a {@link ReportContext}.
 */
public abstract class FieldMappedReportGenerator extends AbstractReportGenerator
{
	private final Map<String, Function<ReportContext, Object>> fieldMappings;
	
	protected FieldMappedReportGenerator()
	{
		this.fieldMappings = new HashMap<>();
		registerDefaultFieldMappings(this.fieldMappings);
		registerFieldMappings(this.fieldMappings);
	}
	
	/**
	 * Hook for subclasses to provide additional field mappings.
	 *
	 * @param mappings mutable map for registering field resolvers
	 */
	protected void registerFieldMappings(
		Map<String, Function<ReportContext, Object>> mappings)
	{
		// Default no-op.
	}
	
	/**
	 * Resolves the value of a field based on its explicit mapping.
	 *
	 * @param fieldName field name to resolve
	 * @param context report context
	 * @return resolved value or {@code null} when no mapping is registered
	 */
	protected Object resolveFieldValue(String fieldName, ReportContext context)
	{
		if (fieldName == null || fieldName.isBlank() || context == null)
		{
			return null;
		}
		
		Function<ReportContext, Object> resolver =
			this.fieldMappings.get(fieldName);
		
		if (resolver == null)
		{
			return null;
		}
		
		return resolver.apply(context);
	}
	
	private void registerDefaultFieldMappings(
		Map<String, Function<ReportContext, Object>> mappings)
	{
		mappings.put("report_type", ReportContext::getReportType);
		mappings.put("start_date", ReportContext::getStartDate);
		mappings.put("end_date", ReportContext::getEndDate);
		mappings.put("output_format", ReportContext::getOutputFormat);
		mappings.put("selected_budget", ReportContext::getSelectedBudget);
		mappings.put("fund_ids", ReportContext::getFundIds);
		mappings.put("account_ids_for_detail_report",
			ReportContext::getAccountIdsForDetailReport);
		mappings.put("transaction_type", ReportContext::getTransactionType);
		mappings.put("memo_filter", ReportContext::getMemoFilter);
		mappings.put("require_all_accounts",
			ctx -> ctx.isRequireAllAccounts());
		mappings.put("beans", ReportContext::getBeans);
	}
}
