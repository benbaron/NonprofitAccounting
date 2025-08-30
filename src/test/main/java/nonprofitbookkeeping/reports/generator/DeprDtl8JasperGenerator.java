
package nonprofitbookkeeping.reports.generator;

import nonprofitbookkeeping.reports.ReportContext;
import nonprofitbookkeeping.service.ReportService;
import java.util.*;

public class DeprDtl8JasperGenerator extends AbstractReportGenerator
{
	
	public DeprDtl8JasperGenerator(ReportContext ctx, ReportService svc)
	{
	
	}
	
        @Override protected Map<String, Object> getReportParameters()
        {
                return Collections.emptyMap();
		
	}
	
	@Override protected String getReportPath()
	{
                return "jrxml/sca-reports/DEPR_DTL_8_ROWS_2SECTIONS.jrxml";
		
	}
	
	@Override public String getBaseName()
	{
		return "DeprDtl8";
		
	}
	
}
