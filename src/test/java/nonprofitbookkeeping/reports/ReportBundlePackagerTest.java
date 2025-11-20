package nonprofitbookkeeping.reports;

import nonprofitbookkeeping.service.ReportService.ReportType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ReportBundlePackagerTest
{
        @TempDir
        Path tempDir;

        @Test
        void beanEntryUsesBundleRoot() throws IOException
        {
                Path bundlesDir = tempDir.resolve("bundles");
                Path bundleRoot = bundlesDir.resolve("fixture");
                Path metadataDir = bundleRoot.resolve("metadata");
                Path beansDir = bundleRoot.resolve("beans");
                Files.createDirectories(metadataDir);
                Files.createDirectories(beansDir);

                Path beanFile = beansDir.resolve("FixtureBean.class");
                byte[] beanBytes = "bean-bytes".getBytes();
                Files.write(beanFile, beanBytes);

                ReportBundles.Bundle bundle = new ReportBundles.Bundle(
                        "fixture/metadata/bundle.properties",
                        "fixture/metadata",
                        "fixture",
                        "nonprofitbookkeeping/reports/bundles/fixture/metadata/bundle.properties",
                        "Fixture",
                        "nonprofitbookkeeping/reports/bundles/fixture/templates/template.jrxml",
                        "nonprofitbookkeeping.reports.fixture.Generator",
                        "nonprofitbookkeeping.reports.fixture.FixtureBean",
                        null,
                        ReportType.GENERAL_LEDGER_JASPER);

                ReportBundlePackager packager = new ReportBundlePackager(bundlesDir);

                assertEquals("fixture/beans/FixtureBean.class",
                        packager.beanEntryName(bundle, beanFile));

                ByteArrayOutputStream buffer = new ByteArrayOutputStream();

                try (ZipOutputStream zipOut = new ZipOutputStream(buffer))
                {
                                packager.addBeanToArchive(bundle, beanFile, zipOut);
                }

                try (ZipInputStream zipIn = new ZipInputStream(
                        new ByteArrayInputStream(buffer.toByteArray())))
                {
                        ZipEntry entry = zipIn.getNextEntry();
                        assertNotNull(entry, "Expected bean entry to be written");
                        assertEquals("fixture/beans/FixtureBean.class", entry.getName());
                        byte[] actual = zipIn.readAllBytes();
                        assertArrayEquals(beanBytes, actual);
                }
        }
}
