package nonprofitbookkeeping.reports;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import nonprofitbookkeeping.reports.jasper.TrialBalanceJasperGenerator;

class ReportBundleResourcesTest
{
        @Test
        void copiesBeanAlongsideBundle(@TempDir Path outputDirectory) throws IOException
        {
                ReportBundles.Bundle bundle = ReportBundles
                        .bundleForGenerator(TrialBalanceJasperGenerator.class);

                Path bundleDir = ReportBundleResources.copyBundleAssets(bundle, outputDirectory);

                Path metadataPath = bundleDir.resolve("trial-balance.properties");
                Path templatePath = bundleDir.resolve("TrialBalance.jrxml");
                Path beanPath = bundleDir.resolve("beans/TrialBalanceRowBean.class");

                assertTrue(Files.exists(metadataPath), "metadata file should be copied");
                assertTrue(Files.exists(templatePath), "JRXML template should be copied");
                assertTrue(Files.exists(beanPath), "bean class should be packaged with the bundle");

                Properties props = new Properties();

                try (InputStream in = Files.newInputStream(metadataPath))
                {
                        props.load(in);
                }

                assertEquals("TrialBalanceRowBean", props.getProperty("beanName"));
        }
}
