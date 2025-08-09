
package nonprofitbookkeeping.reports.generator;

import java.util.*;

public class Balance3v2JasperGenerator extends AbstractReportGenerator
{
	/**
	 * 
	 * Constructor Balance3v2JasperGenerator
	 * @param ctx
	 * @param svc
	 */
        @Override protected Map<String, Object> getReportParameters()
        {
                return Collections.emptyMap();
		
	}
	
	@Override protected String getReportPath()
	{
		return "jrxml/sca-reports/BALANCE_3_AUTO_STYLED_fixed_labeled.jrxml";
		
	}
	
	@Override protected String getBaseName()
	{
		return "Balance3v2";
		
	}
	
}
