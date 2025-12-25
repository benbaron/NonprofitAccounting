
package nonprofitbookkeeping.reports;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ReportBundlesTest
{
	@Test
	void discoversMetadataNestedUnderSubdirectories()
	{
		String generator =
			"nonprofitbookkeeping.reports.fixture.MetadataFixtureGenerator";
		
		ReportBundles.Bundle bundle =
			ReportBundles.bundleForGenerator(generator);
		
		assertNotNull(bundle, "Expected fixture bundle to be discovered");
		assertEquals("Nested Metadata Fixture", bundle.displayName());
		assertEquals("fixture/metadata", bundle.metadataDirectory());
		assertEquals("fixture", bundle.bundleRoot());
		assertEquals("fixture/metadata/nested-metadata.properties",
			bundle.id());
		assertEquals(
			"nonprofitbookkeeping/reports/bundles/fixture/metadata/nested-metadata.properties",
			bundle.metadataResource());
		assertEquals(
			"nonprofitbookkeeping/reports/bundles/fixture/fixture-template.jrxml",
			bundle.jrxmlResource());
		
	}
	
	@Test
	void resolvesTemplateRelativeToBundleRoot()
	{
		String generator =
			"nonprofitbookkeeping.reports.fixture.MultiDirectoryFixtureGenerator";
		
		ReportBundles.Bundle bundle =
			ReportBundles.bundleForGenerator(generator);
		
		assertEquals("Split Template Fixture", bundle.displayName());
		assertEquals("split/metadata", bundle.metadataDirectory());
		assertEquals("split", bundle.bundleRoot());
		assertEquals(
			"nonprofitbookkeeping/reports/bundles/split/templates/separate-template.jrxml",
			bundle.jrxmlResource());
		
	}
	
	@Test
	void discoversTopLevelMetadataAndTemplates()
	{
		String generator =
			"nonprofitbookkeeping.reports.fixture.TopLevelGenerator";
		
		ReportBundles.Bundle bundle =
			ReportBundles.bundleForGenerator(generator);
		
		assertNotNull(bundle, "Expected top-level bundle to be discovered");
		assertEquals("Top-level Template", bundle.displayName());
		assertEquals("", bundle.metadataDirectory());
		assertEquals("", bundle.bundleRoot());
		assertEquals("top-level-template.properties", bundle.id());
		assertEquals(
			"nonprofitbookkeeping/reports/top-level-template.properties",
			bundle.metadataResource());
		assertEquals("nonprofitbookkeeping/reports/top-level-template.jrxml",
			bundle.jrxmlResource());
		
	}
	
}
