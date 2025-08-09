
package nonprofitbookkeeping.reports.generator;

import java.util.*;

public class TransferOut10JasperGenerator extends AbstractReportGenerator
{
	
        @Override
        protected Map<String, Object> getReportParameters()
        {
		return Collections.emptyMap();
		
	}
	
	@Override
	protected String getReportPath()
	{
		return "jrxml/sca-reports/TRANSFER_OUT_10_AUTO_STYLED.jrxml";
		
	}
	
	@Override
	public String getBaseName()
	{
		return "TransferOut10";
		
	}
	
}
