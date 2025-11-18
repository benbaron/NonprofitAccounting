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

                ReportBundles.Bundle bundle = ReportBundles.bundleForGenerator(generator);

                assertNotNull(bundle, "Expected fixture bundle to be discovered");
                assertEquals("Nested Metadata Fixture", bundle.displayName());
                assertEquals("fixture/metadata", bundle.directory());
                assertEquals("fixture/metadata/nested-metadata.properties", bundle.id());
                assertEquals(
                        "nonprofitbookkeeping/reports/bundles/fixture/metadata/nested-metadata.properties",
                        bundle.metadataResource());
                assertEquals(
                        "nonprofitbookkeeping/reports/bundles/fixture/metadata/fixture-template.jrxml",
                        bundle.jrxmlResource());
        }
}
