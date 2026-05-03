package nonprofitbookkeeping.reports.jasper;

import nonprofitbookkeeping.reports.jasper.runtime.ReportBundles;
import nonprofitbookkeeping.reports.jasper.runtime.ReportContext;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Generator used when only bundle metadata is available on the classpath.
 */
public class BundledTemplateJasperGenerator extends AbstractReportGenerator
{
	
	/** The bundle. */
	private final ReportBundles.Bundle bundle;
	
	/** The context. */
	private final ReportContext context;
	
	/**
	 * Instantiates a new bundled template jasper generator.
	 *
	 * @param bundle the bundle
	 * @param context the context
	 */
	public BundledTemplateJasperGenerator(ReportBundles.Bundle bundle,
		ReportContext context)
	{
		this.bundle = bundle;
		this.context = context;
	}
	
	/**
	 * Override @see nonprofitbookkeeping.reports.jasper.AbstractReportGenerator#getReportData() 
	 */
	@Override
	protected List<?> getReportData()
	{
		return Collections.emptyList();
	}
	
	/**
	 * Override @see nonprofitbookkeeping.reports.jasper.AbstractReportGenerator#getReportParameters() 
	 */
	@Override
	protected Map<String, Object> getReportParameters()
	{
		Map<String, Object> params = new HashMap<>();
		
		if (this.bundle != null && this.bundle.displayName() != null)
		{
			params.put("P_REPORT_TITLE", this.bundle.displayName());
		}
		
		if (this.context != null && this.context.getStartDate() != null)
		{
			params.put("P_REPORT_START_DATE", this.context.getStartDate());
		}
		
		if (this.context != null && this.context.getEndDate() != null)
		{
			params.put("P_REPORT_END_DATE", this.context.getEndDate());
		}
		
		return params;
	}
	
	/**
	 * Override @see nonprofitbookkeeping.reports.jasper.AbstractReportGenerator#getReportPath() 
	 */
	@Override
	protected String getReportPath()
	{
		return this.bundle == null ? bundledReportPath() :
			this.bundle.jrxmlResource();
	}
	
	/**
	 * Override @see nonprofitbookkeeping.reports.jasper.AbstractReportGenerator#getBaseName() 
	 */
	@Override
	public String getBaseName()
	{
		return baseNameFromBundle();
	}
	
	/**
	 * Base name from bundle.
	 *
	 * @return the string
	 */
	private String baseNameFromBundle()
	{
		
		if (this.bundle == null || this.bundle.jrxmlResource() == null)
		{
			return "report";
		}
		
		String resource = this.bundle.jrxmlResource();
		String filename = resource.substring(resource.lastIndexOf('/') + 1);
		int dot = filename.lastIndexOf('.');
		return dot > 0 ? filename.substring(0, dot) : filename;
	}
}
