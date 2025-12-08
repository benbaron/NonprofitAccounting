
package nonprofitbookkeeping.reports.runtime;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.time.LocalDate;

import nonprofitbookkeeping.reports.ReportContext;
import nonprofitbookkeeping.reports.datasource.AccountSummaryRowBean;

/**
 * Example per-report data provider for an Account Summary report.
 *
 * Assumes a view or table "v_account_summary" with:
 *   - accountname
 *   - openingbalance
 *   - closingbalance
 *   - tx_date (posting date)
 *
 * Adjust the FROM / JOIN / WHERE clauses to match your schema.
 */
public class AccountSummaryDataProvider
	implements ReportDataProvider<AccountSummaryRowBean>
{
	
	@Override
	public Class<AccountSummaryRowBean> beanClass()
	{
		return AccountSummaryRowBean.class;
		
	}
	
	@Override
	public String sql(ReportContext ctx)
	{
		// Simple example that filters by date range if present.
		// You can extend this to use fundIds, accountIdsForDetailReport,
		// transactionType, memoFilter, etc., from the ReportContext.
		return """
			select
			    a.name           as accountname,
			    s.opening_balance as openingbalance,
			    s.closing_balance as closingbalance
			from v_account_summary s
			join accounts a on a.id = s.account_id
			where (:startDate is null or s.tx_date >= :startDate)
			  and (:endDate   is null or s.tx_date <= :endDate)
			order by a.sort_order
			""";
		
		/* NOTE: - If you are not using a SQL dialect that supports named
		 * parameters, replace :startDate / :endDate with ? and align with
		 * parameterSetter. - For pure JDBC, you might prefer: return """ select
		 * a.name as accountname, s.opening_balance as openingbalance,
		 * s.closing_balance as closingbalance from v_account_summary s join
		 * accounts a on a.id = s.account_id where s.tx_date >= ? and s.tx_date
		 * <= ? order by a.sort_order """; */
	}
	
	@Override
	public JdbcBeanLoader.SqlParameterSetter parameterSetter(ReportContext ctx)
	{
		// This implementation assumes you switch the SQL above to use ? ? for
		// startDate and endDate. If you actually use named parameters with a
		// framework (JPA, Spring, etc.), you'll bind differently.
		return new JdbcBeanLoader.SqlParameterSetter()
		{
			@Override
			public void setParameters(PreparedStatement ps) throws SQLException
			{
				LocalDate start = ctx.getStartDate();
				LocalDate end = ctx.getEndDate();
				
				// Example: both dates required (simpler); adjust to your needs.
				if (start != null)
				{
					ps.setObject(1, start);
				}
				else
				{
					ps.setNull(1, Types.DATE);
				}
				
				if (end != null)
				{
					ps.setObject(2, end);
				}
				else
				{
					ps.setNull(2, Types.DATE);
				}
				
				// If you want to also bind funds, accounts, etc.:
				// List<String> funds = ctx.getFundIds();
				// List<String> accounts = ctx.getAccountIdsForDetailReport();
				// and adjust the SQL to include IN (...) clauses.
			}
			
		};
		
	}
	
}
