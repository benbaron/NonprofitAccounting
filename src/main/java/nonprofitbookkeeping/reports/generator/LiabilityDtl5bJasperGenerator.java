
package nonprofitbookkeeping.reports.generator;

import nonprofitbookkeeping.reports.datasource.scareports.LiabilityDtl5bBean;
import nonprofitbookkeeping.reports.ReportContext;
import nonprofitbookkeeping.service.ReportService;
import java.util.*;

public class LiabilityDtl5bJasperGenerator extends AbstractReportGenerator
{
	
	public LiabilityDtl5bJasperGenerator(ReportContext ctx, ReportService svc)
	{
	
	}
	
	@Override
	protected List<LiabilityDtl5bBean> getReportData()
	{
		return Collections.singletonList(new LiabilityDtl5bBean());
		
	}
	
	@Override
	protected Map<String, Object> getReportParameters()
	{
		return Collections.emptyMap();
		
	}
	
	@Override
	protected String getReportPath()
	{
		return "jrxml/sca-reports/LIABILITY_DTL_5b_AUTO_STYLED.jrxml";
		
	}
	
	@Override
	public String getBaseName()
	{
		return "LiabilityDtl5b";
		
	}
	
}
