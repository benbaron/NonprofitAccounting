
package nonprofitbookkeeping.reports.generator;

import java.math.BigDecimal;
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
		return "jrxml/sca-reports/EXPENSE_DTL_12b_AUTO_STYLED.jrxml";
		
	}
	
	@Override
	public String getBaseName()
	{
		return "ExpenseDtl12b";
		
	}
	
	/**
	 * Override @see nonprofitbookkeeping.reports.generator.AbstractReportGenerator#getReportData() 
	 */
        @Override
        protected List<ExpenseDtl12bBean> getReportData()
        {
                ExpenseDtl12bBean bean = new ExpenseDtl12bBean();
                bean.set_10_transfer_out(BigDecimal.ZERO);
                bean.set_10_transfer_out_2(BigDecimal.ZERO);
                bean.set_11_a_income_dtl(BigDecimal.ZERO);
                return java.util.Collections.singletonList(bean);

        }
	
	/**
	 * Override @see nonprofitbookkeeping.reports.generator.AbstractReportGenerator#setReportData(java.util.List) 
	 */
	@Override
	public void setReportData(List<?> data)
	{
		// TODO Auto-generated method stub
		
		
	}
	
}
