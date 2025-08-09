
package nonprofitbookkeeping.reports.generator;

import java.util.*;

public class TransferIn9JasperGenerator extends AbstractReportGenerator
{
	
        @Override
        protected Map<String, Object> getReportParameters()
        {
		return Collections.emptyMap();
		
	}
	
	@Override
	protected String getReportPath()
	{
		return "jrxml/sca-reports/TRANSFER_IN_9_AUTO_STYLED.jrxml";
		
	}
	
	@Override
	public String getBaseName()
	{
		return "TransferIn9";
		
	}
	
}
