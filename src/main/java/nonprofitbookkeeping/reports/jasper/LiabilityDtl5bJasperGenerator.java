
package nonprofitbookkeeping.reports.jasper;

import java.util.*;
import nonprofitbookkeeping.reports.datasource.scareports.LiabilityDtl5bBean;

public class LiabilityDtl5bJasperGenerator extends AbstractReportGenerator
{
	
	@Override
	protected Map<String, Object> getReportParameters()
	{
		return Collections.emptyMap();
		
	}
	
        @Override
        protected String getReportPath()
        {
                return "jrxml/sca-reports/LIABILITY_DTL_5b.jrxml";

        }
	
	@Override
	public String getBaseName()
	{
		return "LiabilityDtl5b";
		
	}
	
	/**
	 * Override @see nonprofitbookkeeping.reports.jasper.AbstractReportGenerator#getReportData() 
	 */
	@Override
	protected List<LiabilityDtl5bBean> getReportData()
	{
		LiabilityDtl5bBean bean = new LiabilityDtl5bBean();
		bean.setLIABILITY_DTL_5b_E31(Double.valueOf(1.0));
		return java.util.Collections.singletonList(bean);
		
	}
	
	
}
