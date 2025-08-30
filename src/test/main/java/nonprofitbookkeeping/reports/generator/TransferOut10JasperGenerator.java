
package nonprofitbookkeeping.reports.generator;

import nonprofitbookkeeping.reports.ReportContext;
import nonprofitbookkeeping.service.ReportService;
import java.util.*;

public class TransferOut10JasperGenerator extends AbstractReportGenerator
{
	
	public TransferOut10JasperGenerator(ReportContext ctx, ReportService svc)
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
                return "jrxml/sca-reports/TRANSFER_OUT_10_AUTO_STYLED_fixed_labeled_-_Copy_rowbased.jrxml";
		
	}
	
	@Override
	public String getBaseName()
	{
		return "TransferOut10";
		
	}
	
}
