package nonprofitbookkeeping.reports.jasper;

import nonprofitbookkeeping.exception.ActionCancelledException;
import nonprofitbookkeeping.exception.NoFileCreatedException;
import nonprofitbookkeeping.reports.jasper.runtime.ReportBundles;
import nonprofitbookkeeping.reports.jasper.runtime.ReportContext;

import java.util.Collections;
import java.util.Map;

/**
 * Generator that relies entirely on bundled JRXML metadata.
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
	protected java.util.List<?> getReportData()
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
		
		if (this.bundle == null)
		{
			throw new NoFileCreatedException(
				"Bundle metadata unavailable for template generator");
		}
		
		return this.bundle.jrxmlResource();
		
	}
	
	@Override
	public String getBaseName()
	{
		String resource = this.bundle == null ? null : this.bundle.jrxmlResource();
		
		if (resource == null || resource.isBlank())
		{
			return "report";
		}
		
		String fileName = resource.substring(resource.lastIndexOf('/') + 1);
		int dot = fileName.lastIndexOf('.');
		return (dot >= 0) ? fileName.substring(0, dot) : fileName;
		
	}
	
	public ReportContext getContext()
	{
		return this.context;
		
	}
}
