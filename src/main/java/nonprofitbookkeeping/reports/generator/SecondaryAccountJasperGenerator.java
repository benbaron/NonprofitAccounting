
package nonprofitbookkeeping.reports.generator;

import java.math.BigDecimal;
import java.util.*;
import nonprofitbookkeeping.reports.datasource.scareports.SecondaryAccountBean;

public class SecondaryAccountJasperGenerator extends AbstractReportGenerator
{
	
	@Override
	protected Map<String, Object> getReportParameters()
	{
		return Collections.emptyMap();
		
	}
	
	@Override
	protected String getReportPath()
	{
		return "jrxml/sca-reports/SECONDARY_ACCOUNT_2B.jrxml";
		
	}
	
	@Override
	public String getBaseName()
	{
		return "SecondaryAccount";
		
	}

	/**
	 * Override @see nonprofitbookkeeping.reports.generator.AbstractReportGenerator#getReportData() 
	 */
        @Override
        protected List<SecondaryAccountBean> getReportData()
        {
                SecondaryAccountBean bean = new SecondaryAccountBean();
                bean.setBankName("Sample Bank");
                bean.setAccountNumber("000111");
                bean.setStatementBalance(BigDecimal.ZERO);
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
