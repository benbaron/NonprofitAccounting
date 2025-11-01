
package nonprofitbookkeeping.reports.jasper;

import java.util.*;
import nonprofitbookkeeping.reports.datasource.scareports.InventoryDtl6Bean;

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
                return bundledReportPath();

        }
	
	@Override
	public String getBaseName()
	{
		return "InventoryDtl6";
		
	}
	
	/**
	 * Override @see nonprofitbookkeeping.reports.jasper.AbstractReportGenerator#getReportData() 
	 */
	@Override
	protected List<InventoryDtl6Bean> getReportData()
	{
		InventoryDtl6Bean bean = new InventoryDtl6Bean();
		bean.setINVENTORY_DTL_6_M17(Double.valueOf(1.0));
		return java.util.Collections.singletonList(bean);
		
	}
	
	
}
