
package nonprofitbookkeeping.reports.generator;

import nonprofitbookkeeping.reports.datasource.scareports.IncomeDtl11aBean;
import nonprofitbookkeeping.reports.ReportContext;
import nonprofitbookkeeping.service.ReportService;
import java.util.*;

public class IncomeDtl11aJasperGenerator extends AbstractReportGenerator
{
	
	public IncomeDtl11aJasperGenerator(ReportContext ctx, ReportService svc)
	{
	
	}
	
	@Override
	protected List<IncomeDtl11aBean> getReportData()
	{
		return Collections.singletonList(new IncomeDtl11aBean());
		
	}
	
	@Override
	protected Map<String, Object> getReportParameters()
	{
		return Collections.emptyMap();
		
	}
	
	@Override
	protected String getReportPath()
	{
		return "jrxml/sca-reports/INCOME_DTL_11a_AUTO_STYLED.jrxml";
		
	}
	
	@Override
	public String getBaseName()
	{
		return "IncomeDtl11a";
		
	}
	
}
