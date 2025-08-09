
package nonprofitbookkeeping.reports.generator;

import java.util.*;

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
	protected String getBaseName()
	{
		return "SecondaryAccount";
		
	}
	
}
