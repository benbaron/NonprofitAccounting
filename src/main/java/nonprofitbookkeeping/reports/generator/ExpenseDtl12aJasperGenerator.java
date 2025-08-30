
package nonprofitbookkeeping.reports.generator;

import java.math.BigDecimal;
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
                return "jrxml/sca-reports/EXPENSE_DTL_12a_ROW_BASED.jrxml";
		
	}
	
	@Override
	public String getBaseName()
	{
		return "ExpenseDtl12a";
		
	}
	
	/**
	 * Override @see nonprofitbookkeeping.reports.generator.AbstractReportGenerator#getReportData() 
	 */
	@Override
	protected List<ExpenseDtl12aBean> getReportData()
	{
		ExpenseDtl12aBean bean = new ExpenseDtl12aBean();
		bean.setAmount(BigDecimal.ONE);
		return java.util.Collections.singletonList(bean);
		
	}
	
	
}
