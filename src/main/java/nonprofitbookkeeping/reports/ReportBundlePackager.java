package nonprofitbookkeeping.reports;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Utility for exporting Jasper report bundles (metadata, template, and data beans)
 * into distributable ZIP archives. Each bundle is packaged using the metadata
 * discovered by {@link ReportBundles} so consumers have a single artifact that
 * contains everything required to run a report outside of the main application.
 */
public final class ReportBundlePackager
{
        private static final String SOURCE_ROOT = "src/main/java";

        private ReportBundlePackager()
        {
        }

        /**
         * Packages all discovered report bundles into individual ZIP archives located
         * under the supplied output directory.
         *
         * @param outputDirectory Directory that will receive the generated ZIP files.
         * @return immutable list of generated archive paths.
         * @throws IOException if any bundle resource cannot be read or the archive
         *         cannot be written.
         */
        public static List<Path> packageAll(Path outputDirectory) throws IOException
        {
                Objects.requireNonNull(outputDirectory, "outputDirectory");

                List<Path> archives = new ArrayList<>();

                for (ReportBundles.Bundle bundle : ReportBundles.bundles())
                {
                        archives.add(packageBundle(bundle, outputDirectory));
                }

                return List.copyOf(archives);
        }

        /**
         * Packages a single report bundle into a ZIP archive. The archive includes the
         * bundle's metadata file, JRXML template, and the associated bean source (when
         * available) or compiled class file as a fallback.
         *
         * @param bundle          Bundle metadata describing the resources that should
         *                        be packaged.
         * @param outputDirectory Destination directory for the generated archive.
         * @return Path to the created ZIP archive.
         * @throws IOException if any of the bundle resources cannot be read or the
         *         archive cannot be written.
         */
        public static Path packageBundle(ReportBundles.Bundle bundle, Path outputDirectory)
                throws IOException
        {
                Objects.requireNonNull(bundle, "bundle");
                Objects.requireNonNull(outputDirectory, "outputDirectory");

                Files.createDirectories(outputDirectory);

                String archiveName = sanitizeId(bundle.id()) + ".zip";
                Path archivePath = outputDirectory.resolve(archiveName);
                Files.deleteIfExists(archivePath);

                try (ZipOutputStream zip = new ZipOutputStream(Files.newOutputStream(archivePath)))
                {
                        addResource(zip,
                                bundle.metadataResource(),
                                bundle.metadataResource());
                        addResource(zip,
                                bundle.jrxmlResource(),
                                bundle.jrxmlResource());
                        addBeanResource(zip, bundle);
                }

                return archivePath;
        }

        private static void addBeanResource(ZipOutputStream zip, ReportBundles.Bundle bundle)
                throws IOException
        {
                if (!bundle.hasBeanClass())
                {
                        return;
                }

                String beanClassName = bundle.beanClassName();
                String simpleName = beanSimpleName(beanClassName);
                String targetPrefix = "nonprofitbookkeeping/reports/bundles/"
                        + bundle.directory() + "/";

                Path sourceFile = locateBeanSource(beanClassName);

                if (sourceFile != null)
                {
                        addFile(zip, sourceFile, targetPrefix + simpleName + ".java");
                        return;
                }

                String classResource = beanClassName.replace('.', '/') + ".class";
                addResource(zip, classResource, targetPrefix + simpleName + ".class");
        }

        private static Path locateBeanSource(String beanClassName)
        {
                Path base = Paths.get(SOURCE_ROOT);
                String candidate = beanClassName;

                while (candidate != null)
                {
                        Path source = base.resolve(candidate.replace('.', '/') + ".java");

                        if (Files.exists(source))
                        {
                                return source;
                        }

                        int dollar = candidate.lastIndexOf('$');

                        if (dollar < 0)
                        {
                                break;
                        }

                        candidate = candidate.substring(0, dollar);
                }

                return null;
        }

        private static void addResource(ZipOutputStream zip,
                String resource,
                String entryName) throws IOException
        {
                ClassLoader loader = ReportBundles.class.getClassLoader();

                try (InputStream in = loader.getResourceAsStream(resource))
                {
                        if (in == null)
                        {
                                throw new IOException("Unable to locate bundle resource: " + resource);
                        }

                        zip.putNextEntry(new ZipEntry(entryName));
                        in.transferTo(zip);
                        zip.closeEntry();
                }
        }

        private static void addFile(ZipOutputStream zip, Path file, String entryName)
                throws IOException
        {
                zip.putNextEntry(new ZipEntry(entryName));

                try (InputStream in = Files.newInputStream(file))
                {
                        in.transferTo(zip);
                }

                zip.closeEntry();
        }

        private static String beanSimpleName(String beanClassName)
        {
                String simple = beanClassName.substring(beanClassName.lastIndexOf('.') + 1);
                int dollar = simple.lastIndexOf('$');

                if (dollar >= 0)
                {
                        return simple.substring(dollar + 1);
                }

                return simple;
        }

        private static String sanitizeId(String id)
        {
                return id.replace('/', '_').replace('\\', '_');
        }
}
