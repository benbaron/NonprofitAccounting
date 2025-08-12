
package nonprofitbookkeeping.reports.generator;

import java.math.BigDecimal;
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
		return "jrxml/sca-reports/INCOME_DTL_11a_AUTO_STYLED.jrxml";
		
	}
	
	@Override
	public String getBaseName()
	{
		return "IncomeDtl11a";
		
	}

	/**
	 * Override @see nonprofitbookkeeping.reports.generator.AbstractReportGenerator#getReportData() 
	 */
        @Override
        protected List<IncomeDtl11aBean> getReportData()
        {
                IncomeDtl11aBean bean = new IncomeDtl11aBean();
                bean.set_1_contact_info(BigDecimal.ZERO);
                bean.set_1a_fundraising_income_internal_event(BigDecimal.ZERO);
                bean.set_1a_fundraising_income_internal_event_2(BigDecimal.ZERO);
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
