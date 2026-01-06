package nonprofitbookkeeping.reports;

import nonprofitbookkeeping.reports.jasper.runtime.ReportBundles;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ReportBundleDiscoveryTest
{
	@Test
	void discoversBundleUnderMetadataDirectory()
	{
		String generator =
			"nonprofitbookkeeping.reports.fixture.DiscoveryBundleGenerator";
		
		ReportBundles.Bundle bundle =
			ReportBundles.bundleForGenerator(generator);
		
		assertNotNull(bundle, "Expected discovery bundle to be available");
		assertEquals("Discovery Bundle Fixture", bundle.displayName());
		assertEquals("discovery/metadata", bundle.metadataDirectory());
		assertEquals("discovery", bundle.bundleRoot());
		assertEquals("discovery/metadata/discovery-bundle.properties",
			bundle.id());
		assertEquals(
			"nonprofitbookkeeping/reports/bundles/discovery/metadata/discovery-bundle.properties",
			bundle.metadataResource());
		assertEquals(
			"nonprofitbookkeeping/reports/bundles/discovery/discovery-template.jrxml",
			bundle.jrxmlResource());
		
	}
	
	@Test
	void discoversTopLevelMetadata()
	{
		String generator =
			"nonprofitbookkeeping.reports.fixture.DiscoveryTopLevelGenerator";
		
		ReportBundles.Bundle bundle =
			ReportBundles.bundleForGenerator(generator);
		
		assertNotNull(bundle, "Expected top-level discovery bundle");
		assertEquals("Discovery Top-level Fixture", bundle.displayName());
		assertEquals("", bundle.metadataDirectory());
		assertEquals("", bundle.bundleRoot());
		assertEquals("discovery-top-level.properties", bundle.id());
		assertEquals(
			"nonprofitbookkeeping/reports/discovery-top-level.properties",
			bundle.metadataResource());
		assertEquals("nonprofitbookkeeping/reports/top-level-template.jrxml",
			bundle.jrxmlResource());
		
	}
}
