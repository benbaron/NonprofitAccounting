package nonprofitbookkeeping.reports.jasper;

import nonprofitbookkeeping.exception.ActionCancelledException;
import nonprofitbookkeeping.exception.NoFileCreatedException;
import nonprofitbookkeeping.reports.jasper.beans.TransactionReportBean;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Basic generator for the TransactionReport template.
 */
public class TransactionReportJasperGenerator extends AbstractReportGenerator
{
	@Override
	protected List<TransactionReportBean> getReportData()
	{
		return Collections.emptyList();
		
	}
	
	@Override
	protected Map<String, Object> getReportParameters()
	{
		return new HashMap<>();
		
	}
	
	@Override
	protected String getReportPath()
		throws ActionCancelledException, NoFileCreatedException
	{
		return bundledReportPath();
		
	}
	
	@Override
	public String getBaseName()
	{
		return "TransactionReport";
		
	}
}
