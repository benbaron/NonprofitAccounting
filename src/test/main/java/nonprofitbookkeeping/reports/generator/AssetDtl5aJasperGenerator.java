
package nonprofitbookkeeping.reports.generator;

import nonprofitbookkeeping.reports.ReportContext;
import nonprofitbookkeeping.service.ReportService;
import java.util.*;

public class AssetDtl5aJasperGenerator extends AbstractReportGenerator
{
	
	public AssetDtl5aJasperGenerator(ReportContext ctx, ReportService svc)
	{
		
	}
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
	public String getBaseName()
	{
		return "AssetDtl5a";
		
	}
	
}
