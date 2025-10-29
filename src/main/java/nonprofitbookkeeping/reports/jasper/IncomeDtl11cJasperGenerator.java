
package nonprofitbookkeeping.reports.jasper;

import java.util.*;
import nonprofitbookkeeping.reports.datasource.scareports.IncomeDtl11cBean;

public class IncomeDtl11cJasperGenerator extends AbstractReportGenerator
{
	
	@Override
	protected Map<String, Object> getReportParameters()
	{
		return Collections.emptyMap();
		
	}
	
        @Override
        protected String getReportPath()
        {
                return "jrxml/sca-reports/INCOME_DTL_11c.jrxml";

        }
	
	@Override
	public String getBaseName()
	{
		return "IncomeDtl11c";
		
	}
	
	/**
	 * Override @see nonprofitbookkeeping.reports.jasper.AbstractReportGenerator#getReportData() 
	 */
	@Override
	protected List<IncomeDtl11cBean> getReportData()
	{
		IncomeDtl11cBean bean = new IncomeDtl11cBean();
		bean.setINCOME_DTL_11c_G24(Double.valueOf(1.0));
		return java.util.Collections.singletonList(bean);
		
	}
	
	
}
