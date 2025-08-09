
package nonprofitbookkeeping.reports.generator;

import nonprofitbookkeeping.reports.datasource.scareports.TransferOut10Bean;
import nonprofitbookkeeping.reports.ReportContext;
import nonprofitbookkeeping.service.ReportService;
import java.util.*;

public class TransferOut10JasperGenerator extends AbstractReportGenerator
{
	
	public TransferOut10JasperGenerator(ReportContext ctx, ReportService svc)
	{
	
	}
	
	@Override
	protected List<TransferOut10Bean> getReportData()
	{
		return Collections.singletonList(new TransferOut10Bean());
		
	}
	
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
	protected String getBaseName()
	{
		return "TransferOut10";
		
	}
	
}
