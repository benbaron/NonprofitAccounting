
package nonprofitbookkeeping.reports.generator;

import java.math.BigDecimal;
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
                return "jrxml/sca-reports/LIABILITY_DETAIL_5b_ROW.jrxml";
		
	}
	
	@Override
	public String getBaseName()
	{
		return "LiabilityDtl5b";
		
	}
	
	/**
	 * Override @see nonprofitbookkeeping.reports.generator.AbstractReportGenerator#getReportData() 
	 */
	@Override
	protected List<LiabilityDtl5bBean> getReportData()
	{
		LiabilityDtl5bBean bean = new LiabilityDtl5bBean();
		bean.setCurrent_amount(BigDecimal.ONE);
		return java.util.Collections.singletonList(bean);
		
	}
	
	
}
