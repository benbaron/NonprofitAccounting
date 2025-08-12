
package nonprofitbookkeeping.reports.generator;

import java.math.BigDecimal;
import java.util.*;
import nonprofitbookkeeping.reports.datasource.scareports.Funds14Bean;

public class Funds14JasperGenerator extends AbstractReportGenerator
{
	
	@Override
	protected Map<String, Object> getReportParameters()
	{
		return Collections.emptyMap();
		
	}
	
	@Override
	protected String getReportPath()
	{
		return "jrxml/sca-reports/FUNDS_14_AUTO_STYLED.jrxml";
		
	}
	
	@Override
	public String getBaseName()
	{
		return "Funds14";
		
	}
	
	/**
	 * Override @see nonprofitbookkeeping.reports.generator.AbstractReportGenerator#getReportData() 
	 */
        @Override
        protected List<Funds14Bean> getReportData()
        {
                Funds14Bean bean = new Funds14Bean();
                bean.set_1_contact_info(BigDecimal.ZERO);
                bean.set_4_income(BigDecimal.ZERO);
                bean.setAll_non_dedicated_funds(BigDecimal.ONE);
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
