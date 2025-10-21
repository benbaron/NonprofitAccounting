
package nonprofitbookkeeping.reports.jasper;

import java.math.BigDecimal;
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
                return "jrxml/sca-reports/INCOME_DTL_11b_AUTO_STYLED_fixed_-_Copy_rowbased.jrxml";
		
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
		bean.setAmount(BigDecimal.ONE);
		return java.util.Collections.singletonList(bean);
		
	}
	
	
}
