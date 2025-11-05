package nonprofitbookkeeping.reports;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Objects;

/**
 * Utility helpers for exporting the static assets that make up a Jasper report
 * bundle. Each bundle consists of the metadata properties, JRXML template, and
 * optionally the compiled bean class that supplies data to the report.
 */
public final class ReportBundleResources
{
        private ReportBundleResources()
        {
        }

        /**
         * Copies the bundle resources to the provided output directory. The
         * destination structure mirrors the bundle directory name and includes a
         * {@code beans/} sub-directory when a bean is defined.
         *
         * @param bundle bundle metadata describing the resources to copy
         * @param outputDirectory directory that will contain the extracted bundle
         * @return the directory containing the extracted bundle resources
         * @throws IOException when any classpath resource cannot be read or the
         *                     destination cannot be written
         */
        public static Path copyBundleAssets(ReportBundles.Bundle bundle, Path outputDirectory)
                throws IOException
        {
                Objects.requireNonNull(bundle, "bundle");
                Objects.requireNonNull(outputDirectory, "outputDirectory");

                Path bundleDirectory = outputDirectory.resolve(bundle.directory());
                Files.createDirectories(bundleDirectory);

                ClassLoader loader = ReportBundles.class.getClassLoader();

                copyResource(loader,
                        bundle.metadataResource(),
                        bundleDirectory.resolve(fileName(bundle.metadataResource())));

                copyResource(loader,
                        bundle.jrxmlResource(),
                        bundleDirectory.resolve(fileName(bundle.jrxmlResource())));

                if (bundle.hasBeanClass())
                {
                        String beanResource = bundle.beanClassName().replace('.', '/') + ".class";
                        Path beansDirectory = bundleDirectory.resolve("beans");
                        Files.createDirectories(beansDirectory);

                        String beanFileName = bundle.beanName()
                                .orElseGet(() -> simpleName(bundle.beanClassName()));
                        Path beanTarget = beansDirectory.resolve(beanFileName + ".class");
                        copyResource(loader, beanResource, beanTarget);
                }

                return bundleDirectory;
        }

        private static void copyResource(ClassLoader loader, String resourceName, Path target)
                throws IOException
        {
                try (InputStream in = loader.getResourceAsStream(resourceName))
                {
                        if (in == null)
                        {
                                throw new IOException("Missing classpath resource: " + resourceName);
                        }

                        Path parent = target.getParent();

                        if (parent != null)
                        {
                                Files.createDirectories(parent);
                        }

                        Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
                }
        }

        private static String fileName(String resource)
        {
                int idx = resource.lastIndexOf('/');

                if (idx >= 0 && idx + 1 < resource.length())
                {
                        return resource.substring(idx + 1);
                }

                return resource;
        }

        private static String simpleName(String className)
        {
                int idx = className.lastIndexOf('.');

                if (idx >= 0 && idx + 1 < className.length())
                {
                        return className.substring(idx + 1);
                }

                return className;
        }
}
