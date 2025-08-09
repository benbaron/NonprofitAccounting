
package nonprofitbookkeeping.reports.generator;

import nonprofitbookkeeping.reports.datasource.scareports.LedgerQ1Bean;
import nonprofitbookkeeping.reports.ReportContext;
import nonprofitbookkeeping.service.ReportService;
import java.util.*;

public class LedgerQ1JasperGenerator extends AbstractReportGenerator
{
	
	public LedgerQ1JasperGenerator(ReportContext ctx, ReportService svc)
	{
	
	}
	
	@Override
	protected List<LedgerQ1Bean> getReportData()
	{
		return Collections.singletonList(new LedgerQ1Bean());
		
	}
	
	@Override
	protected Map<String, Object> getReportParameters()
	{
		return Collections.emptyMap();
		
	}
	
	@Override
	protected String getReportPath()
	{
		return "jrxml/sca-reports/Ledger_Q1.jrxml";
		
	}
	
	@Override
	public String getBaseName()
	{
		return "LedgerQ1";
		
	}
	
}
