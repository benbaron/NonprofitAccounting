
package nonprofitbookkeeping.reports.jasper;

import java.util.*;
import nonprofitbookkeeping.reports.datasource.scareports.Newsletter15Bean;

public class Newsletter15JasperGenerator extends AbstractReportGenerator
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
		return "Newsletter15";
		
	}
	
	/**
	 * Override @see nonprofitbookkeeping.reports.jasper.AbstractReportGenerator#getReportData() 
	 */
	@Override
	protected List<Newsletter15Bean> getReportData()
	{
		Newsletter15Bean bean = new Newsletter15Bean();
		bean.setNEWSLETTER_15_E14(Double.valueOf(1.0));
		return java.util.Collections.singletonList(bean);
		
	}
	
	
}
