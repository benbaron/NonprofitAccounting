
package nonprofitbookkeeping.reports.jasper.runtime;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;

import nonprofitbookkeeping.service.ReportService.ReportType;


/**
 * Registry of available Jasper report templates.
 * Each entry maps a user facing display name to the JRXML template
 * and the generator class responsible for binding data.
 */
public final class ReportTemplates
{
	
	/**
	 * Immutable holder for template metadata.
	 *
	 * @param displayName the display name
	 * @param jrxmlPath the jrxml path
	 * @param generatorClassName the generator class name
	 * @param beanClassName the bean class name
	 * @param beanName the bean name
	 * @param reportType the report type
	 * @param bundleId the bundle id
	 * @param metadataResource the metadata resource
	 * @param description the description
	 */
	public record TemplateInfo(String displayName,
		String jrxmlPath,
		String generatorClassName,
		String beanClassName,
		String beanName,
		ReportType reportType,
		String bundleId,
		String metadataResource,
		String description)
	{
		/**
		 * Returns the report type identifier used by {@link nonprofitbookkeeping.service.ReportService}.
		 *
		 * @return report type identifier
		 */
		public String reportTypeKey()
		{
			return this.reportType.id();
			
		}
		
	}
	
	/** The Constant TEMPLATES. */
	private static final Map<String, TemplateInfo> TEMPLATES =
		createTemplates();
	
	/**
	 * Instantiates a new report templates.
	 */
	private ReportTemplates()
	{
	
	}
	
	/**
	 * Creates the templates.
	 *
	 * @return the map
	 */
	private static Map<String, TemplateInfo> createTemplates()
	{
		Map<String, TemplateInfo> map = new LinkedHashMap<>();
		var bundles = new ArrayList<>(ReportBundles.bundles());
		
		bundles.sort(Comparator.comparing(ReportBundles.Bundle::displayName,
			String.CASE_INSENSITIVE_ORDER));
		
		for (ReportBundles.Bundle bundle : bundles)
		{
			TemplateInfo info = new TemplateInfo(bundle.displayName(),
				bundle.jrxmlResource(),
				bundle.generatorClassName(),
				bundle.beanClassName(),
				bundle.beanName(),
				bundle.reportType(),
				bundle.id(),
				bundle.metadataResource(),
				bundle.description());
			map.put(bundle.displayName(), info);
		}
		
		return Map.copyOf(map);
		
	}
	
	/**
	 * Provides the map of available templates keyed by their display names.
	 *
	 * @return immutable mapping of display names to template info
	 */
	public static Map<String, TemplateInfo> templates()
	{
		return TEMPLATES;
		
	}
	
}
