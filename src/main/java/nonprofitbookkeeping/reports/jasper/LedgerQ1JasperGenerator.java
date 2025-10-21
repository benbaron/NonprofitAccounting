
package nonprofitbookkeeping.reports.jasper;

import java.util.*;
import nonprofitbookkeeping.reports.datasource.scareports.LedgerQ1Bean;

public class LedgerQ1JasperGenerator extends AbstractReportGenerator
{
	
	@Override
	protected Map<String, Object> getReportParameters()
	{
		return Collections.emptyMap();
		
	}
	
	@Override
	protected String getReportPath()
	{
		return "jrxml/sca-reports/Ledger_Q1.jrxml";
		
	}
	
	@Override
	public String getBaseName()
	{
		return "LedgerQ1";
		
	}
	
	/**
	 * Override @see nonprofitbookkeeping.reports.jasper.AbstractReportGenerator#getReportData() 
	 */
	@Override
	protected List<LedgerQ1Bean> getReportData()
	{
		LedgerQ1Bean bean = new LedgerQ1Bean();
		bean.setCol_0("Sample");
		return java.util.Collections.singletonList(bean);
		
	}
	
	
}
