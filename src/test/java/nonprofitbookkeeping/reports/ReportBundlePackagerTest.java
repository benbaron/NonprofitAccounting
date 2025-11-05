package nonprofitbookkeeping.reports;

import nonprofitbookkeeping.reports.jasper.IncomeStatementJasperGenerator;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ReportBundlePackagerTest
{
        @Test
        void packageBundleCopiesMetadataTemplateAndBean() throws IOException
        {
                ReportBundles.Bundle bundle = ReportBundles
                        .bundleForGenerator(IncomeStatementJasperGenerator.class);

                Path output = Files.createTempDirectory("bundle-packager-test");

                ReportBundlePackager.packageBundle(bundle, output);

                Path bundleDir = output.resolve(Path.of(bundle.directory()));
                Path metadata = bundleDir.resolve(Path.of(bundle.metadataResource()).getFileName());
                Path jrxml = bundleDir.resolve(Path.of(bundle.jrxmlResource()).getFileName());
                Optional<String> beanName = Optional.ofNullable(bundle.beanName());

                assertAll(
                        () -> assertTrue(Files.exists(metadata), "metadata file should be copied"),
                        () -> assertTrue(Files.exists(jrxml), "jrxml template should be copied"),
                        () -> assertTrue(beanName.isPresent(), "bean name should be present"),
                        () -> assertTrue(
                                Files.exists(bundleDir.resolve(beanName.orElseThrow() + ".class")),
                                "compiled bean should be copied"));
        }

        @Test
        void beanResourceUsesCompiledClassPath()
        {
                ReportBundles.Bundle bundle = ReportBundles
                        .bundleForGenerator(IncomeStatementJasperGenerator.class);

                assertTrue(bundle.beanResource().isPresent());
                assertEquals("nonprofitbookkeeping/reports/datasource/IncomeStatementRowBean.class",
                        bundle.beanResource().orElseThrow());
        }
}
