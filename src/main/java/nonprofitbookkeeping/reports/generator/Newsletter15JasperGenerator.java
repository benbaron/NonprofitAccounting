
package nonprofitbookkeeping.reports.generator;

import java.util.*;

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
		return "jrxml/sca-reports/NEWSLETTER_15_AUTO_STYLED.jrxml";
		
	}
	
	@Override
	public String getBaseName()
	{
		return "Newsletter15";
		
	}
	
}
