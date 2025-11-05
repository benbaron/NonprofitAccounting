package nonprofitbookkeeping.reports;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Objects;

/**
 * Utility for exporting the resources associated with a {@link ReportBundles.Bundle}.
 * The packager copies the bundle metadata, JRXML template, and optional compiled bean
 * class into the requested output directory so the bundle can be distributed as a
 * self-contained unit.
 */
public final class ReportBundlePackager
{
        private ReportBundlePackager()
        {
        }

        /**
         * Packages all discovered bundles into the supplied output directory.
         * Each bundle will be copied into a sub-directory matching its bundle directory.
         *
         * @param outputRoot destination directory
         * @throws IOException when copying bundle resources fails
         */
        public static void packageAll(Path outputRoot) throws IOException
        {
                Objects.requireNonNull(outputRoot, "outputRoot");

                for (ReportBundles.Bundle bundle : ReportBundles.bundles())
                {
                        packageBundle(bundle, outputRoot);
                }
        }

        /**
         * Packages a single bundle into the supplied output directory.
         *
         * @param bundle metadata describing the bundle resources
         * @param outputRoot destination directory
         * @throws IOException when copying bundle resources fails
         */
        public static void packageBundle(ReportBundles.Bundle bundle, Path outputRoot)
                throws IOException
        {
                Objects.requireNonNull(bundle, "bundle");
                Objects.requireNonNull(outputRoot, "outputRoot");

                Path bundleDir = outputRoot.resolve(Path.of(bundle.directory()));
                Files.createDirectories(bundleDir);

                copyResource(bundle.metadataResource(),
                        bundleDir.resolve(Path.of(bundle.metadataResource()).getFileName()));
                copyResource(bundle.jrxmlResource(),
                        bundleDir.resolve(Path.of(bundle.jrxmlResource()).getFileName()));

                bundle.beanResource().ifPresent(resource -> {
                        String beanFileName = bundle.beanName() != null
                                ? bundle.beanName() + ".class"
                                : Path.of(resource).getFileName().toString();
                        Path target = bundleDir.resolve(beanFileName);

                        try
                        {
                                copyResource(resource, target);
                        }
                        catch (IOException e)
                        {
                                throw new IllegalStateException(
                                        "Failed copying bean class for bundle " + bundle.id(), e);
                        }
                });
        }

        private static void copyResource(String resourcePath, Path target) throws IOException
        {
                ClassLoader loader = ReportBundles.class.getClassLoader();

                try (InputStream in = loader.getResourceAsStream(resourcePath))
                {
                        if (in == null)
                        {
                                throw new IOException("Resource not found on classpath: " + resourcePath);
                        }

                        Files.createDirectories(target.getParent());
                        Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
                }
        }
}
