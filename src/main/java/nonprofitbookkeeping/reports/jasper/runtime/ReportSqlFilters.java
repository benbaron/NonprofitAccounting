
package nonprofitbookkeeping.reports.jasper.runtime;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Builds SQL WHERE clauses and parameter binders from a {@link ReportContext}.
 *
 * Expected table aliases:
 *   jt = journal_transaction
 *   je = journal_entry
 *   af = account_fund
 */
public final class ReportSqlFilters
{
	
	@FunctionalInterface
	private interface SqlBinder
	{
		void bind(PreparedStatement ps, int index) throws SQLException;
		
	}
	
	private final String whereClause;
	private final List<SqlBinder> binders;
	
	private ReportSqlFilters(String whereClause, List<SqlBinder> binders)
	{
		this.whereClause = whereClause;
		this.binders = binders;
		
	}
	
	public String whereClause()
	{
		return this.whereClause;
		
	}
	
	public JdbcBeanLoader.SqlParameterSetter parameterSetter()
	{
		return ps -> {
			int index = 1;
			
			for (SqlBinder binder : this.binders)
			{
				binder.bind(ps, index++);
			}
			
		};
		
	}
	
	public static ReportSqlFilters fromContext(ReportContext context)
	{
		List<String> clauses = new ArrayList<>();
		List<SqlBinder> binders = new ArrayList<>();
		
		if (context != null)
		{
			
			if (context.getStartDate() != null)
			{
				clauses.add("cast(jt.date_text as date) >= ?");
				Date start = Date.valueOf(context.getStartDate());
				binders.add((ps, index) -> ps.setDate(index, start));
			}
			
			if (context.getEndDate() != null)
			{
				clauses.add("cast(jt.date_text as date) <= ?");
				Date end = Date.valueOf(context.getEndDate());
				binders.add((ps, index) -> ps.setDate(index, end));
			}
			
			List<String> fundIds = normalizeList(context.getFundIds());
			
			if (!fundIds.isEmpty())
			{
				String placeholders = placeholders(fundIds.size());
				clauses.add(
					"(je.fund_number in (" + placeholders + ") " +
						"or jt.associated_fund_name in (" + placeholders +
						") " +
						"or af.fund_id in (" + placeholders + "))");
				binders.addAll(bindStringList(fundIds));
				binders.addAll(bindStringList(fundIds));
				binders.addAll(bindStringList(fundIds));
			}
			
			List<String> accountIds =
				normalizeList(context.getAccountIdsForDetailReport());
			
			if (!accountIds.isEmpty())
			{
				String placeholders = placeholders(accountIds.size());
				
				if (context.isRequireAllAccounts())
				{
					clauses.add(
						"jt.id in (" +
							"select txn_id from journal_entry " +
							"where account_number in (" + placeholders + ") " +
							"group by txn_id " +
							"having count(distinct account_number) = ?" +
							")");
					binders.addAll(bindStringList(accountIds));
					binders.add(
						(ps, index) -> ps.setInt(index, accountIds.size()));
				}
				else
				{
					clauses.add("je.account_number in (" + placeholders + ")");
					binders.addAll(bindStringList(accountIds));
				}
				
			}
			
			if (context.getMemoFilter() != null &&
				!context.getMemoFilter().isBlank())
			{
				clauses.add("lower(jt.memo) like ?");
				String memo = "%" + context.getMemoFilter().toLowerCase() + "%";
				binders.add((ps, index) -> ps.setString(index, memo));
			}
			
		}
		
		String whereClause = clauses.isEmpty() ?
			"" : "where " + String.join("\n  and ", clauses);
		
		return new ReportSqlFilters(whereClause, binders);
		
	}
	
	private static String placeholders(int count)
	{
		return String.join(",", java.util.Collections.nCopies(count, "?"));
		
	}
	
	private static List<String> normalizeList(List<String> values)
	{
		
		if (values == null)
		{
			return List.of();
		}
		
		List<String> normalized = new ArrayList<>();
		
		for (String value : values)
		{
			
			if (value != null && !value.isBlank())
			{
				normalized.add(value);
			}
			
		}
		
		return normalized;
		
	}
	
	private static List<SqlBinder> bindStringList(List<String> values)
	{
		List<SqlBinder> binders = new ArrayList<>();
		
		for (String value : values)
		{
			binders.add((ps, index) -> ps.setString(index, value));
		}
		
		return binders;
		
	}
	
}
