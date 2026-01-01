package nonprofitbookkeeping.reports.jasper;

import nonprofitbookkeeping.core.Database;
import nonprofitbookkeeping.reports.jasper.beans.TransactionReportBean;
import nonprofitbookkeeping.reports.jasper.runtime.JdbcBeanLoader;
import nonprofitbookkeeping.reports.jasper.runtime.ReportSqlFilters;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Jasper generator for the TransactionReport template.
 */
public class TransactionReportJasperGenerator
	extends JdbcReportGenerator<TransactionReportBean>
{
	@Override
	protected List<TransactionReportBean> getReportData()
	{
		ReportSqlFilters filters =
			ReportSqlFilters.fromContext(getReportContext());
		String sql = """
			select
			  jt.date_text as date,
			  jt.memo as memo,
			  je.account_number as accountNumber,
			  je.amount as amount
			from journal_transaction jt
			join journal_entry je on jt.id = je.txn_id
			left join account a on a.account_number = je.account_number
			left join account_fund af on af.account_number = a.account_number
			""" + "\n" + filters.whereClause() +
			"\norder by jt.booking_ts, jt.id, je.id";

		try (Connection cx = Database.get().getConnection())
		{
			return JdbcBeanLoader.queryBeans(cx, TransactionReportBean.class, sql,
				filters.parameterSetter());
		}
		catch (SQLException ex)
		{
			throw new IllegalStateException(
				"Unable to load TransactionReport data", ex);
		}
	}
	
	@Override
	protected Map<String, Object> getReportParameters()
	{
		return Collections.emptyMap();
	}
	
	@Override
	protected String getReportPath()
	{
		return bundledReportPath();
	}
	
	@Override
	public String getBaseName()
	{
		return "TransactionReport";
	}
}
