package nonprofitbookkeeping.reports;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import nonprofitbookkeeping.service.ReportService.ReportType;

/**
 * Compatibility facade for report bundle metadata.
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
		return new Bundle(nonprofitbookkeeping.reports.jasper.runtime.ReportBundles
			.bundleForGenerator(generatorClass));
		
	}
	
	public static Bundle bundleForGenerator(String generatorClassName)
	{
		return new Bundle(nonprofitbookkeeping.reports.jasper.runtime.ReportBundles
			.bundleForGenerator(generatorClassName));
		
	}
	
	public static final class Bundle
	{
		private final nonprofitbookkeeping.reports.jasper.runtime.ReportBundles.Bundle
			delegate;
		
		private Bundle(
			nonprofitbookkeeping.reports.jasper.runtime.ReportBundles.Bundle
				delegate)
		{
			this.delegate = delegate;
			
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
		
		public ReportType reportType()
		{
			return this.delegate.reportType();
			
		}
		
		public boolean hasBeanClass()
		{
			return this.delegate.hasBeanClass();
			
		}
		
		public String directory()
		{
			return this.delegate.metadataDirectory();
			
		}
		
		nonprofitbookkeeping.reports.jasper.runtime.ReportBundles.Bundle
			delegate()
		{
			return this.delegate;
			
		}
		
	}
	
	static List<nonprofitbookkeeping.reports.jasper.runtime.ReportBundles.Bundle>
		toRuntimeBundles(Collection<Bundle> bundles)
	{
		return bundles.stream()
			.map(Bundle::delegate)
			.collect(Collectors.toUnmodifiableList());
		
	}
}
