
package nonprofitbookkeeping.reports.generator;

import nonprofitbookkeeping.reports.ReportContext;
import nonprofitbookkeeping.reports.datasource.scareports.DeprDtl8Bean;
import nonprofitbookkeeping.service.ReportService;
import java.util.*;

public class DeprDtl8JasperGenerator extends AbstractReportGenerator
{
	
	public DeprDtl8JasperGenerator(ReportContext ctx, ReportService svc)
	{
	
	}
	
	@Override protected List<DeprDtl8Bean> getReportData()
	{
		return Collections.singletonList(new DeprDtl8Bean());
		
	}
	
	@Override protected Map<String, Object> getReportParameters()
	{
		return Collections.emptyMap();
		
	}
	
	@Override protected String getReportPath()
	{
		return "jrxml/sca-reports/DEPR_DTL_8_AUTO_STYLED_labeled.jrxml";
		
	}
	
	@Override public String getBaseName()
	{
		return "DeprDtl8";
		
	}
	
}
