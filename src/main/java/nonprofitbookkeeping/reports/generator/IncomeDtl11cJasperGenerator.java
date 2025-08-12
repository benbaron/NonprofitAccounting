
package nonprofitbookkeeping.reports.generator;

import java.math.BigDecimal;
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
		return "jrxml/sca-reports/INCOME_DTL_11c_AUTO_STYLED.jrxml";
		
	}
	
	@Override
	public String getBaseName()
	{
		return "IncomeDtl11c";
		
	}

	/**
	 * Override @see nonprofitbookkeeping.reports.generator.AbstractReportGenerator#getReportData() 
	 */
        @Override
        protected List<IncomeDtl11cBean> getReportData()
        {
                IncomeDtl11cBean bean = new IncomeDtl11cBean();
                bean.set_11_b_income_dtl(BigDecimal.ZERO);
                bean.set_11_c_income_dtl(BigDecimal.ZERO);
                bean.set_1_contact_info(BigDecimal.ZERO);
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
