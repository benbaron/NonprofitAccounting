
package nonprofitbookkeeping.reports.generator;

import java.math.BigDecimal;
import java.util.*;
import nonprofitbookkeeping.reports.datasource.scareports.Newsletter15Bean;

public class Newsletter15JasperGenerator extends AbstractReportGenerator
{
	
	@Override
	protected Map<String, Object> getReportParameters()
	{
		return Collections.emptyMap();
		
	}
	
	@Override
	protected String getReportPath()
	{
                return "jrxml/sca-reports/NEWSLETTER_15_AUTO_STYLED_fixed_labeled_-_Copy_rowbased.jrxml";
		
	}
	
	@Override
	public String getBaseName()
	{
		return "Newsletter15";
		
	}
	
	/**
	 * Override @see nonprofitbookkeeping.reports.generator.AbstractReportGenerator#getReportData() 
	 */
	@Override
	protected List<Newsletter15Bean> getReportData()
	{
		Newsletter15Bean bean = new Newsletter15Bean();
		bean.setAdj_gross_income_a_b_c(BigDecimal.ONE);
		return java.util.Collections.singletonList(bean);
		
	}
	
	
}
