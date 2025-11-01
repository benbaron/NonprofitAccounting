
package nonprofitbookkeeping.reports.jasper;

import java.util.*;
import nonprofitbookkeeping.reports.datasource.scareports.PrimaryAccount2aBean;

public class PrimaryAccountJasperGenerator extends AbstractReportGenerator
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
		return "PrimaryAccount";
		
	}
	
	/**
	 * Override @see nonprofitbookkeeping.reports.jasper.AbstractReportGenerator#getReportData() 
	 */
	@Override
	protected List<PrimaryAccount2aBean> getReportData()
	{
		PrimaryAccount2aBean bean = new PrimaryAccount2aBean();
		return java.util.Collections.singletonList(bean);
		
	}
	
	
}
