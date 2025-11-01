
package nonprofitbookkeeping.reports.jasper;

import java.util.*;
import nonprofitbookkeeping.reports.datasource.scareports.IncomeDtl11bBean;

public class IncomeDtl11bJasperGenerator extends AbstractReportGenerator
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
		return "IncomeDtl11b";
		
	}
	
	/**
	 * Override @see nonprofitbookkeeping.reports.jasper.AbstractReportGenerator#getReportData() 
	 */
	@Override
	protected List<IncomeDtl11bBean> getReportData()
	{
		IncomeDtl11bBean bean = new IncomeDtl11bBean();
		bean.setINCOME_DTL_11b_G12(Double.valueOf(1.0));
		return java.util.Collections.singletonList(bean);
		
	}
	
	
}
