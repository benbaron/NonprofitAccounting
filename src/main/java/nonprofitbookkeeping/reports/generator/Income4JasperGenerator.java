
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
                return "jrxml/sca-reports/INCOME_4_AUTO_STYLED_labeled.jrxml";
		
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
		bean.setExchequer(BigDecimal.ONE);
		return java.util.Collections.singletonList(bean);
		
	}
	
	
}
