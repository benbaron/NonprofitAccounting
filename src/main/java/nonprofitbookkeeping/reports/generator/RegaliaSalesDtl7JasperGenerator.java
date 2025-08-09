
package nonprofitbookkeeping.reports.generator;

import nonprofitbookkeeping.reports.datasource.scareports.RegaliaSalesDtl7Bean;
import nonprofitbookkeeping.reports.ReportContext;
import nonprofitbookkeeping.service.ReportService;
import java.util.*;

public class RegaliaSalesDtl7JasperGenerator extends AbstractReportGenerator
{
	
	public RegaliaSalesDtl7JasperGenerator(ReportContext ctx, ReportService svc)
	{
	
	}
	
	@Override
	protected List<RegaliaSalesDtl7Bean> getReportData()
	{
		return Collections.singletonList(new RegaliaSalesDtl7Bean());
		
	}
	
	@Override
	protected Map<String, Object> getReportParameters()
	{
		return Collections.emptyMap();
		
	}
	
	@Override
	protected String getReportPath()
	{
		return "jrxml/sca-reports/REGALIA_SALES_DTL_7_AUTO_STYLED.jrxml";
		
	}
	
	@Override
	protected String getBaseName()
	{
		return "RegaliaSalesDtl7";
		
	}
	
}
