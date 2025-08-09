
package nonprofitbookkeeping.reports.generator;

import nonprofitbookkeeping.reports.ReportContext;
import nonprofitbookkeeping.service.ReportService;
import java.util.*;

public class PrimaryAccountJasperGenerator extends AbstractReportGenerator
{
	
	public PrimaryAccountJasperGenerator(ReportContext ctx, ReportService svc)
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
		return "jrxml/sca-reports/PRIMARY_ACCOUNT_2a_fixed_labeled.jrxml";
		
	}
	
	@Override
	protected String getBaseName()
	{
		return "PrimaryAccount";
		
	}
	
}
