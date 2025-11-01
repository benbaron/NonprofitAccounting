
package nonprofitbookkeeping.reports.jasper;

import java.util.*;
import nonprofitbookkeeping.reports.datasource.scareports.Income4Bean;

public class Income4JasperGenerator extends AbstractReportGenerator
{
	
	@Override
	protected Map<String, Object> getReportParameters()
	{
		return null;
		
	}
	
        @Override
        protected String getReportPath()
        {
                return bundledReportPath();

        }
	
	@Override
	public String getBaseName()
	{
		return "Income4";
		
	}
	
	/**
	 * Override @see nonprofitbookkeeping.reports.jasper.AbstractReportGenerator#getReportData() 
	 */
	@Override
	protected List<Income4Bean> getReportData()
	{
		Income4Bean bean = new Income4Bean();
		bean.setINCOME_4_J11(Double.valueOf(1.0));
		return java.util.Collections.singletonList(bean);
		
	}
	
	
}
