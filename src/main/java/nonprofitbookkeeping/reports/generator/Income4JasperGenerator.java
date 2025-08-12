
package nonprofitbookkeeping.reports.generator;

import java.math.BigDecimal;
import java.util.*;
import nonprofitbookkeeping.reports.datasource.scareports.Income4Bean;

public class Income4JasperGenerator extends AbstractReportGenerator
{
	
	@Override
	protected Map<String, Object> getReportParameters()
	{
		return null;
		
	}
	
	@Override
	protected String getReportPath()
	{
		return "jrxml/sca-reports/INCOME_4_AUTO_STYLED.jrxml";
		
	}
	
	@Override
	public String getBaseName()
	{
		return "Income4";
		
	}
	
	/**
	 * Override @see nonprofitbookkeeping.reports.generator.AbstractReportGenerator#getReportData() 
	 */
        @Override
        protected List<Income4Bean> getReportData()
        {
                Income4Bean bean = new Income4Bean();
                bean.set_1_contact_info(BigDecimal.ZERO);
                bean.set_2_a_primary_account(BigDecimal.ZERO);
                bean.set_2_b_secondary_accounts(BigDecimal.ZERO);
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
