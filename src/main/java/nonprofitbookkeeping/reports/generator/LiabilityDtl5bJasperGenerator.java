
package nonprofitbookkeeping.reports.generator;

import java.util.*;

public class LiabilityDtl5bJasperGenerator extends AbstractReportGenerator
{
	
        @Override
        protected Map<String, Object> getReportParameters()
        {
		return Collections.emptyMap();
		
	}
	
	@Override
	protected String getReportPath()
	{
		return "jrxml/sca-reports/LIABILITY_DTL_5b_AUTO_STYLED.jrxml";
		
	}
	
	@Override
	protected String getBaseName()
	{
		return "LiabilityDtl5b";
		
	}
	
}
