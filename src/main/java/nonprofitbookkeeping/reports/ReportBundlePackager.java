package nonprofitbookkeeping.reports;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

/**
 * Compatibility facade for packaging report bundles.
 */
public final class ReportBundlePackager
{
	private ReportBundlePackager()
	{
	}
	
	public static List<Path> packageAll(Path outputDirectory) throws IOException
	{
		return nonprofitbookkeeping.reports.jasper.runtime.ReportBundlePackager
			.packageAll(outputDirectory);
		
	}
	
	public static Path packageBundle(ReportBundles.Bundle bundle,
		Path outputDirectory) throws IOException
	{
		Objects.requireNonNull(bundle, "bundle");
		return nonprofitbookkeeping.reports.jasper.runtime.ReportBundlePackager
			.packageBundle(bundle.delegate(), outputDirectory);
		
	}
}
