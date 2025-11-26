package nonprofitbookkeeping.reports;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

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
                assertEquals("fixture/metadata", bundle.metadataDirectory());
                assertEquals("fixture", bundle.bundleRoot());
                assertEquals("fixture/metadata/nested-metadata.properties", bundle.id());
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

                ReportBundles.Bundle bundle = ReportBundles.bundleForGenerator(generator);

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

                IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                        () -> ReportBundles.bundleForGenerator(generator),
                        "Top-level bundles should be ignored to avoid loading legacy metadata");

                assertEquals(
                        "No report bundle registered for generator: " + generator,
                        ex.getMessage());
        }
}
