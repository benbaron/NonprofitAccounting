
package nonprofitbookkeeping.reports.generator;

import nonprofitbookkeeping.reports.datasource.scareports.TransferIn9Bean;
import nonprofitbookkeeping.reports.ReportContext;
import nonprofitbookkeeping.service.ReportService;
import java.util.*;

public class TransferIn9JasperGenerator extends AbstractReportGenerator
{
	
	public TransferIn9JasperGenerator(ReportContext ctx, ReportService svc)
	{
	
	}
	
	@Override
	protected List<TransferIn9Bean> getReportData()
	{
		return Collections.singletonList(new TransferIn9Bean());
		
	}
	
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
	protected String getBaseName()
	{
		return "TransferIn9";
		
	}
	
}
