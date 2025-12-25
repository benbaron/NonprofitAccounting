package nonprofitbookkeeping.reports;

import nonprofitbookkeeping.service.ReportService;

import java.util.Collection;
import java.util.stream.Collectors;

/**
 * Compatibility wrapper around {@link nonprofitbookkeeping.reports.jasper.runtime.ReportBundles}.
 */
public final class ReportBundles
{
	private ReportBundles()
	{
	}
	
	public static Collection<Bundle> bundles()
	{
		return nonprofitbookkeeping.reports.jasper.runtime.ReportBundles.bundles()
			.stream()
			.map(Bundle::new)
			.collect(Collectors.toUnmodifiableList());
	}
	
	public static Bundle bundleForGenerator(Class<?> generatorClass)
	{
		return new Bundle(
			nonprofitbookkeeping.reports.jasper.runtime.ReportBundles
				.bundleForGenerator(generatorClass));
	}
	
	public static Bundle bundleForGenerator(String generatorClassName)
	{
		return new Bundle(
			nonprofitbookkeeping.reports.jasper.runtime.ReportBundles
				.bundleForGenerator(generatorClassName));
	}
	
	public static final class Bundle
	{
		private final nonprofitbookkeeping.reports.jasper.runtime.ReportBundles.Bundle
			delegate;
		
		Bundle(nonprofitbookkeeping.reports.jasper.runtime.ReportBundles.Bundle
			delegate)
		{
			this.delegate = delegate;
		}
		
		nonprofitbookkeeping.reports.jasper.runtime.ReportBundles.Bundle
			runtimeBundle()
		{
			return this.delegate;
		}
		
		public String id()
		{
			return this.delegate.id();
		}
		
		public String metadataDirectory()
		{
			return this.delegate.metadataDirectory();
		}
		
		public String bundleRoot()
		{
			return this.delegate.bundleRoot();
		}
		
		public String metadataResource()
		{
			return this.delegate.metadataResource();
		}
		
		public String displayName()
		{
			return this.delegate.displayName();
		}
		
		public String jrxmlResource()
		{
			return this.delegate.jrxmlResource();
		}
		
		public String generatorClassName()
		{
			return this.delegate.generatorClassName();
		}
		
		public String beanClassName()
		{
			return this.delegate.beanClassName();
		}
		
		public String beanName()
		{
			return this.delegate.beanName();
		}
		
		public String description()
		{
			return this.delegate.description();
		}
		
		public ReportService.ReportType reportType()
		{
			return this.delegate.reportType();
		}
		
		public boolean hasBeanClass()
		{
			return this.delegate.hasBeanClass();
		}
		
		public String directory()
		{
			return this.delegate.directory();
		}
	}
}
