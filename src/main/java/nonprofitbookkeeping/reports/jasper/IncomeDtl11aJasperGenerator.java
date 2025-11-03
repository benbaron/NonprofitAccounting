
package nonprofitbookkeeping.reports.jasper;

import java.util.*;
import nonprofitbookkeeping.reports.datasource.scareports.IncomeDtl11aBean;


public class IncomeDtl11aJasperGenerator extends AbstractReportGenerator
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
		return "IncomeDtl11a";
		
	}
	
	/**
	 * Override @see nonprofitbookkeeping.reports.jasper.AbstractReportGenerator#getReportData() 
	 */
	@Override
	protected List<IncomeDtl11aBean> getReportData()
	{
		IncomeDtl11aBean bean = new IncomeDtl11aBean();
		bean.setINCOME_DTL_11a_E20(Double.valueOf(1.0));
		return java.util.Collections.singletonList(bean);
		
	}
	
	
}
