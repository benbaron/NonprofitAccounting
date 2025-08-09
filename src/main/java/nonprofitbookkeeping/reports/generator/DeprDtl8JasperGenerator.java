
package nonprofitbookkeeping.reports.generator;

import java.util.*;

public class DeprDtl8JasperGenerator extends AbstractReportGenerator
{
	
	
        @Override protected Map<String, Object> getReportParameters()
        {
                return Collections.emptyMap();
		
	}
	
	@Override protected String getReportPath()
	{
		return "jrxml/sca-reports/DEPR_DTL_8_AUTO_STYLED_labeled.jrxml";
		
	}
	
	@Override protected String getBaseName()
	{
		return "DeprDtl8";
		
	}
	
}
