package nonprofitbookkeeping.reports;

import java.util.List;

import org.junit.jupiter.api.Test;

import nonprofitbookkeeping.reports.jasper.IncomeDtl11aJasperGenerator;

import static org.junit.jupiter.api.Assertions.*;

class ReportBundlesTest
{
        @Test
        void beanMetadataIsIncludedInPackagedResources()
        {
                ReportBundles.Bundle bundle =
                        ReportBundles.bundleForGenerator(IncomeDtl11aJasperGenerator.class);

                assertEquals("IncomeDtl11aBean", bundle.beanName());
                assertTrue(bundle.beanResourcePath().isPresent(),
                        "Expected bean resource path to be present");

                String beanResource = bundle.beanResourcePath().orElseThrow();
                assertEquals(
                        "nonprofitbookkeeping/reports/datasource/scareports/IncomeDtl11aBean.class",
                        beanResource);

                List<String> resources = bundle.packagedResources();
                assertTrue(resources.contains(bundle.metadataResource()));
                assertTrue(resources.contains(bundle.jrxmlResource()));
                assertTrue(resources.contains(beanResource));
                assertEquals(3, resources.size());
        }
}
