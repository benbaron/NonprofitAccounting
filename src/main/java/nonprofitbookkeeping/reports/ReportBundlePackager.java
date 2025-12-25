package nonprofitbookkeeping.reports;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

/**
 * Compatibility wrapper around {@link nonprofitbookkeeping.reports.jasper.runtime.ReportBundlePackager}.
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
		return nonprofitbookkeeping.reports.jasper.runtime.ReportBundlePackager
			.packageBundle(bundle.runtimeBundle(), outputDirectory);
	}
}
