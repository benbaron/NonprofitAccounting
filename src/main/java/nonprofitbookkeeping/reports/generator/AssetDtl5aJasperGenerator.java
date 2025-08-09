
package nonprofitbookkeeping.reports.generator;

import java.util.*;

public class AssetDtl5aJasperGenerator extends AbstractReportGenerator
{
	
        @Override
        protected Map<String, Object> getReportParameters()
        {
		return Collections.emptyMap();
		
	}
	
	@Override
	protected String getReportPath()
	{
		return "jrxml/sca-reports/ASSET_DTL_5a_AUTO_STYLED.jrxml";
		
	}
	
	@Override
	protected String getBaseName()
	{
		return "AssetDtl5a";
		
	}
	
}
