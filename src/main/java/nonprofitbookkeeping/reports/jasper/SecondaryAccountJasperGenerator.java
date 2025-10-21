
package nonprofitbookkeeping.reports.jasper;

import java.util.*;
import nonprofitbookkeeping.reports.datasource.scareports.SecondaryAccount2bBean;

public class SecondaryAccountJasperGenerator extends AbstractReportGenerator
{
	
	@Override
	protected Map<String, Object> getReportParameters()
	{
		return Collections.emptyMap();
		
	}
	
	@Override
	protected String getReportPath()
	{
                return "jrxml/sca-reports/SECONDARY_ACCOUNT_2B_fixed_labeled.jrxml";
		
	}
	
	@Override
	public String getBaseName()
	{
		return "SecondaryAccount";
		
	}

	/**
	 * Override @see nonprofitbookkeeping.reports.jasper.AbstractReportGenerator#getReportData() 
	 */
        @Override
        protected List<SecondaryAccount2bBean> getReportData()
        {
                SecondaryAccount2bBean bean = new SecondaryAccount2bBean();
                return java.util.Collections.singletonList(bean);

        }

	
	
}
