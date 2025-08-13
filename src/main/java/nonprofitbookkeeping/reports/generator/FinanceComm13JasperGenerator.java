
package nonprofitbookkeeping.reports.generator;

import java.math.BigDecimal;
import java.util.*;
import nonprofitbookkeeping.reports.datasource.scareports.FinanceComm13Bean;

public class FinanceComm13JasperGenerator extends AbstractReportGenerator
{
	
	@Override
	protected Map<String, Object> getReportParameters()
	{
		return Collections.emptyMap();
		
	}
	
	@Override
	protected String getReportPath()
	{
		return "jrxml/sca-reports/FINANCE_COMM_13_AUTO_STYLED.jrxml";
		
	}
	
	@Override
	public String getBaseName()
	{
		return "FinanceComm13";
		
	}
	
	/**
	 * Override @see nonprofitbookkeeping.reports.generator.AbstractReportGenerator#getReportData() 
	 */
	@Override
	protected List<FinanceComm13Bean> getReportData()
	{
		FinanceComm13Bean bean = new FinanceComm13Bean();
		bean.setExchequer(BigDecimal.ONE);
		return java.util.Collections.singletonList(bean);
		
	}
	
	
}
