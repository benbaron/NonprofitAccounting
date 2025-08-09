
package nonprofitbookkeeping.reports.generator;

import java.util.*;

public class InventoryDtl6JasperGenerator extends AbstractReportGenerator
{
	
        @Override
        protected Map<String, Object> getReportParameters()
        {
		return Collections.emptyMap();
		
	}
	
	@Override
	protected String getReportPath()
	{
		return "jrxml/sca-reports/INVENTORY_DTL_6_AUTO_STYLED.jrxml";
		
	}
	
	@Override
	protected String getBaseName()
	{
		return "InventoryDtl6";
		
	}
	
}
