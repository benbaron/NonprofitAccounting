
package nonprofitbookkeeping.reports.jasper;

import java.util.*;
import nonprofitbookkeeping.reports.datasource.scareports.SecondaryAccounts2bBean;

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
        protected List<SecondaryAccounts2bBean> getReportData()
        {
                SecondaryAccounts2bBean bean = new SecondaryAccounts2bBean();
                return java.util.Collections.singletonList(bean);

        }

	
	
}
