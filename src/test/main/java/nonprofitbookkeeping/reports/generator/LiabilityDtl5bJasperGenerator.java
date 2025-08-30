
package nonprofitbookkeeping.reports.generator;

import nonprofitbookkeeping.reports.ReportContext;
import nonprofitbookkeeping.service.ReportService;
import java.util.*;

public class LiabilityDtl5bJasperGenerator extends AbstractReportGenerator
{
	
	public LiabilityDtl5bJasperGenerator(ReportContext ctx, ReportService svc)
	{
	
	}
        @Override
        protected Map<String, Object> getReportParameters()
        {
		return Collections.emptyMap();
		
	}
	
	@Override
	protected String getReportPath()
	{
                return "jrxml/sca-reports/LIABILITY_DETAIL_5b_ROW.jrxml";
		
	}
	
	@Override
	public String getBaseName()
	{
		return "LiabilityDtl5b";
		
	}
	
}
