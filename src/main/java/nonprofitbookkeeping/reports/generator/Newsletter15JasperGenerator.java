
package nonprofitbookkeeping.reports.generator;

import nonprofitbookkeeping.reports.datasource.scareports.Newsletter15Bean;
import nonprofitbookkeeping.reports.ReportContext;
import nonprofitbookkeeping.service.ReportService;
import java.util.*;

public class Newsletter15JasperGenerator extends AbstractReportGenerator
{
	
	public Newsletter15JasperGenerator(ReportContext ctx, ReportService svc)
	{
	
	}
	
	@Override
	protected List<Newsletter15Bean> getReportData()
	{
		return Collections.singletonList(new Newsletter15Bean());
		
	}
	
	@Override
	protected Map<String, Object> getReportParameters()
	{
		return Collections.emptyMap();
		
	}
	
	@Override
	protected String getReportPath()
	{
		return "jrxml/sca-reports/NEWSLETTER_15_AUTO_STYLED.jrxml";
		
	}
	
	@Override
	public String getBaseName()
	{
		return "Newsletter15";
		
	}
	
}
