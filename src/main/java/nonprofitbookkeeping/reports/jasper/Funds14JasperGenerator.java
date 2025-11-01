
package nonprofitbookkeeping.reports.jasper;

import java.util.*;
import nonprofitbookkeeping.reports.datasource.scareports.Funds14Bean;

public class Funds14JasperGenerator extends AbstractReportGenerator
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
		return "Funds14";
		
	}
	
	/**
	 * Override @see nonprofitbookkeeping.reports.jasper.AbstractReportGenerator#getReportData() 
	 */
	@Override
	protected List<Funds14Bean> getReportData()
	{
		Funds14Bean bean = new Funds14Bean();
		bean.setFUNDS_14_F11(Double.valueOf(1.0));
		return java.util.Collections.singletonList(bean);
		
	}
	
	
}
