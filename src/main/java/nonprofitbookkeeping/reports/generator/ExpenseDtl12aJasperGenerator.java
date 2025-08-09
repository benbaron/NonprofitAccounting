
package nonprofitbookkeeping.reports.generator;

import nonprofitbookkeeping.reports.datasource.scareports.ExpenseDtl12aBean;
import nonprofitbookkeeping.reports.ReportContext;
import nonprofitbookkeeping.service.ReportService;
import java.util.*;

public class ExpenseDtl12aJasperGenerator extends AbstractReportGenerator
{
	
	public ExpenseDtl12aJasperGenerator(ReportContext ctx, ReportService svc)
	{
	
	}
	
	@Override
	protected List<ExpenseDtl12aBean> getReportData()
	{
		return Collections.singletonList(new ExpenseDtl12aBean());
		
	}
	
	@Override
	protected Map<String, Object> getReportParameters()
	{
		return Collections.emptyMap();
		
	}
	
	@Override
	protected String getReportPath()
	{
		return "jrxml/sca-reports/EXPENSE_DTL_12a_AUTO_STYLED (1).jrxml";
		
	}
	
	@Override
	protected String getBaseName()
	{
		return "ExpenseDtl12a";
		
	}
	
}
