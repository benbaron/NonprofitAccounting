
package nonprofitbookkeeping.reports.generator;

import nonprofitbookkeeping.reports.ReportContext;
import nonprofitbookkeeping.service.ReportService;
import java.util.*;

public class InventoryDtl6JasperGenerator extends AbstractReportGenerator
{
	
	public InventoryDtl6JasperGenerator(ReportContext ctx, ReportService svc)
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
                return "jrxml/sca-reports/INVENTORY_DTL_6_ROWS.jrxml";
		
	}
	
	@Override
	public String getBaseName()
	{
		return "InventoryDtl6";
		
	}
	
}
