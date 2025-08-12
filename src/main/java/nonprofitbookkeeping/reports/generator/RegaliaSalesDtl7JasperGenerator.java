
package nonprofitbookkeeping.reports.generator;

import java.math.BigDecimal;
import java.util.*;
import nonprofitbookkeeping.reports.datasource.scareports.RegaliaSalesDtl7Bean;

public class RegaliaSalesDtl7JasperGenerator extends AbstractReportGenerator
{
	
	@Override
	protected Map<String, Object> getReportParameters()
	{
		return Collections.emptyMap();
		
	}
	
	@Override
	protected String getReportPath()
	{
		return "jrxml/sca-reports/REGALIA_SALES_DTL_7_AUTO_STYLED.jrxml";
		
	}
	
	@Override
	public String getBaseName()
	{
		return "RegaliaSalesDtl7";
		
	}

	/**
	 * Override @see nonprofitbookkeeping.reports.generator.AbstractReportGenerator#getReportData() 
	 */
        @Override
        protected List<RegaliaSalesDtl7Bean> getReportData()
        {
                RegaliaSalesDtl7Bean bean = new RegaliaSalesDtl7Bean();
                bean.set_10_b_transfer_out(BigDecimal.ZERO);
                bean.set_10_c_transfer_out(BigDecimal.ZERO);
                bean.set_10_d_transfer_out(BigDecimal.ZERO);
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
