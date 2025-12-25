package nonprofitbookkeeping.reports.jasper;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import nonprofitbookkeeping.exception.ActionCancelledException;
import nonprofitbookkeeping.exception.NoFileCreatedException;
import nonprofitbookkeeping.reports.jasper.runtime.ReportBundles;
import nonprofitbookkeeping.reports.jasper.runtime.ReportContext;

/**
 * Jasper generator for the TransactionReport template bundle.
 */
public class TransactionReportJasperGenerator extends AbstractReportGenerator
{
	private final ReportBundles.Bundle bundle;
	
	public TransactionReportJasperGenerator()
	{
		this(null);
		
	}
	
	public TransactionReportJasperGenerator(ReportContext context)
	{
		this.bundle =
			ReportBundles.bundleForGenerator(TransactionReportJasperGenerator.class);
	}
	
	@Override
	protected List<?> getReportData()
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
		throws ActionCancelledException, NoFileCreatedException
	{
		return this.bundle.jrxmlResource();
		
	}
	
	@Override
	public String getBaseName()
	{
		return "TransactionReport";
		
	}
	
}
