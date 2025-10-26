
package nonprofitbookkeeping.reports.generator;

import nonprofitbookkeeping.reports.ReportContext;
import nonprofitbookkeeping.service.ReportService;
import java.util.*;

public class Balance3v2JasperGenerator extends AbstractReportGenerator
{
	/**
	 * 
	 * Constructor Balance3v2JasperGenerator
	 * @param ctx
	 * @param svc
	 */
	public Balance3v2JasperGenerator(ReportContext ctx, ReportService svc)
	{
	
	}
        @Override protected Map<String, Object> getReportParameters()
        {
                return Collections.emptyMap();
		
	}
	
        @Override protected String getReportPath()
        {
                // Match the production generator's updated template location
                return "jrxml/sca-reports/BALANCE_3_FIXED_SEMANTIC_STRINGS_v2.jrxml";

        }
	
	@Override public String getBaseName()
	{
		return "Balance3v2";
		
	}
	
}
