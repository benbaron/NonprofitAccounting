
package nonprofitbookkeeping.reports.jasper;

import java.util.*;
import nonprofitbookkeeping.reports.datasource.scareports.ExpenseDtl12aBean;

public class ExpenseDtl12aJasperGenerator extends AbstractReportGenerator
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
		return "ExpenseDtl12a";
		
	}
	
	/**
	 * Override @see nonprofitbookkeeping.reports.jasper.AbstractReportGenerator#getReportData() 
	 */
	@Override
	protected List<ExpenseDtl12aBean> getReportData()
	{
		ExpenseDtl12aBean bean = new ExpenseDtl12aBean();
		bean.setEXPENSE_DTL_12a_F23(Double.valueOf(1.0));
		return java.util.Collections.singletonList(bean);
		
	}
	
	
}
