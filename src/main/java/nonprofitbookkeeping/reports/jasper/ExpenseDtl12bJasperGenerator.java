
package nonprofitbookkeeping.reports.jasper;

import java.util.*;
import nonprofitbookkeeping.reports.datasource.scareports.ExpenseDtl12bBean;

public class ExpenseDtl12bJasperGenerator extends AbstractReportGenerator
{
	
	@Override
	protected Map<String, Object> getReportParameters()
	{
		return Collections.emptyMap();
		
	}
	
        @Override
        protected String getReportPath()
        {
                return "jrxml/sca-reports/EXPENSE_DTL_12b.jrxml";

        }
	
	@Override
	public String getBaseName()
	{
		return "ExpenseDtl12b";
		
	}
	
	/**
	 * Override @see nonprofitbookkeeping.reports.jasper.AbstractReportGenerator#getReportData() 
	 */
	@Override
	protected List<ExpenseDtl12bBean> getReportData()
	{
		ExpenseDtl12bBean bean = new ExpenseDtl12bBean();
		bean.setEXPENSE_DTL_12b_I16(Double.valueOf(1.0));
		return java.util.Collections.singletonList(bean);
		
	}
	
	
}
