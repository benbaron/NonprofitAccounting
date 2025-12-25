package nonprofitbookkeeping.reports.jasper;

import nonprofitbookkeeping.reports.jasper.beans.TransactionReportBean;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Jasper generator for the TransactionReport template.
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
		return Collections.emptyMap();
	}
	
	@Override
	protected String getReportPath()
	{
		return bundledReportPath();
	}
	
	@Override
	public String getBaseName()
	{
		return "TransactionReport";
	}
}
