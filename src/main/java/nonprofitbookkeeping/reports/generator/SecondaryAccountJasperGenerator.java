
package nonprofitbookkeeping.reports.generator;

import nonprofitbookkeeping.reports.datasource.scareports.SecondaryAccountBean;
import nonprofitbookkeeping.reports.ReportContext;
import nonprofitbookkeeping.service.ReportService;
import java.util.*;

public class SecondaryAccountJasperGenerator extends AbstractReportGenerator
{
	
	public SecondaryAccountJasperGenerator(ReportContext ctx, ReportService svc)
	{
	
	}
	
	@Override
	protected List<SecondaryAccountBean> getReportData()
	{
		return Collections.singletonList(new SecondaryAccountBean());
		
	}
	
	@Override
	protected Map<String, Object> getReportParameters()
	{
		return Collections.emptyMap();
		
	}
	
	@Override
	protected String getReportPath()
	{
		return "jrxml/sca-reports/SECONDARY_ACCOUNT_2B.jrxml";
		
	}
	
	@Override
	public String getBaseName()
	{
		return "SecondaryAccount";
		
	}
	
}
