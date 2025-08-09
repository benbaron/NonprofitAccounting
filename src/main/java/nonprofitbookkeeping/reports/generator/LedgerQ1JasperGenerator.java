
package nonprofitbookkeeping.reports.generator;

import java.util.*;

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
	
}
