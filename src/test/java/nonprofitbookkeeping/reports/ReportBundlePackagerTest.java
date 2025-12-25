
package nonprofitbookkeeping.reports;

import nonprofitbookkeeping.reports.jasper.runtime.ReportBundlePackager;
import nonprofitbookkeeping.reports.jasper.runtime.ReportBundles;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import static org.junit.jupiter.api.Assertions.*;

class ReportBundlePackagerTest
{
	@Test
	void packageBundleIncludesMetadataTemplateAndBean() throws IOException
	{
		ReportBundles.Bundle bundle =
			ReportBundles.bundleForGenerator(
				nonprofitbookkeeping.reports.jasper.TransactionReportJasperGenerator.class);
		
		Path tempDir = Files.createTempDirectory("bundlePackageTest");
		
		Path archive = ReportBundlePackager.packageBundle(bundle, tempDir);
		
		assertTrue(Files.exists(archive), "Archive should be created");
		
		try (ZipFile zip = new ZipFile(archive.toFile()))
		{
			assertNotNull(zip.getEntry(bundle.metadataResource()),
				"Metadata file should be present in archive");
			assertNotNull(zip.getEntry(bundle.jrxmlResource()),
				"JRXML template should be present in archive");
			
			String beanBaseName = bundle.beanName() != null ?
				bundle.beanName() : simpleNameFromClass(bundle.beanClassName());
			String beanDirectory = "nonprofitbookkeeping/reports/bundles/" +
				bundle.directory() + "/";
			ZipEntry beanEntry =
				zip.getEntry(beanDirectory + beanBaseName + ".java");
			
			if (beanEntry == null)
			{
				beanEntry =
					zip.getEntry(beanDirectory + beanBaseName + ".class");
			}
			
			assertNotNull(beanEntry, "Bean source or class should be packaged");
		}
		
	}
	
	private static String simpleNameFromClass(String className)
	{
		
		if (className == null || className.isBlank())
		{
			return null;
		}
		
		String simple = className.substring(className.lastIndexOf('.') + 1);
		int dollar = simple.lastIndexOf('$');
		return dollar >= 0 ? simple.substring(dollar + 1) : simple;
		
	}
	
}
