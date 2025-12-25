package nonprofitbookkeeping.reports.jasper;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import nonprofitbookkeeping.exception.ActionCancelledException;
import nonprofitbookkeeping.exception.NoFileCreatedException;
import nonprofitbookkeeping.reports.jasper.runtime.ReportBundles;
import nonprofitbookkeeping.reports.jasper.runtime.ReportContext;

/**
 * Generator that renders a Jasper report using metadata discovered in
 * {@link ReportBundles}.
 */
public class BundledTemplateJasperGenerator extends AbstractReportGenerator
{
	private final ReportBundles.Bundle bundle;
	private final ReportContext context;
	
	public BundledTemplateJasperGenerator(ReportBundles.Bundle bundle,
		ReportContext context)
	{
		this.bundle = bundle;
		this.context = context;
		
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
		
		if (this.bundle == null)
		{
			return "report";
		}
		
		String baseName = this.bundle.id();
		
		if (baseName == null || baseName.isBlank())
		{
			baseName = this.bundle.displayName();
		}
		
		if (baseName == null || baseName.isBlank())
		{
			baseName = "report";
		}
		
		return baseName.replace('/', '_');
		
	}
	
	public ReportContext getContext()
	{
		return this.context;
		
	}
	
}
