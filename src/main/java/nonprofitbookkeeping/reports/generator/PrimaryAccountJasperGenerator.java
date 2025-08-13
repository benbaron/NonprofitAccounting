
package nonprofitbookkeeping.reports.generator;

import java.util.*;
import nonprofitbookkeeping.reports.datasource.scareports.PrimaryAccountBean;

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
		return "jrxml/sca-reports/PRIMARY_ACCOUNT_2a_fixed_labeled.jrxml";
		
	}
	
	@Override
	public String getBaseName()
	{
		return "PrimaryAccount";
		
	}
	
	/**
	 * Override @see nonprofitbookkeeping.reports.generator.AbstractReportGenerator#getReportData() 
	 */
	@Override
	protected List<PrimaryAccountBean> getReportData()
	{
		PrimaryAccountBean bean = new PrimaryAccountBean();
		return java.util.Collections.singletonList(bean);
		
	}
	
	
}
