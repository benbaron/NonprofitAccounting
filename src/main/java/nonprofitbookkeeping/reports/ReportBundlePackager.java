package nonprofitbookkeeping.reports;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Optional;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Helper for packaging report bundle assets (metadata, templates, and optional
 * beans) into a distributable archive. The packager understands the
 * "metadata" subdirectory convention used by {@link ReportBundles} and can
 * emit bean entries relative to the bundle root rather than the metadata
 * directory.
 */
public final class ReportBundlePackager
{
        private final Path bundlesDirectory;

        /**
         * @param bundlesDirectory base directory that contains bundle root
         *            folders.
         */
        public ReportBundlePackager(Path bundlesDirectory)
        {
                Objects.requireNonNull(bundlesDirectory, "bundlesDirectory");
                this.bundlesDirectory = bundlesDirectory.toAbsolutePath().normalize();
        }

        /**
         * Adds the provided bean file to the destination archive, returning the
         * path used for the entry.
         *
         * @param bundle bundle metadata describing the target directory
         * @param beanFile compiled bean class that should be archived
         * @param zipOut open ZIP stream receiving the bean entry
         * @return the entry name written to the archive
         * @throws IOException if the file cannot be copied into the archive
         */
        public String addBeanToArchive(ReportBundles.Bundle bundle,
                Path beanFile,
                ZipOutputStream zipOut) throws IOException
        {
                Objects.requireNonNull(zipOut, "zipOut");

                String entryName = beanEntryName(bundle, beanFile);
                ZipEntry entry = new ZipEntry(entryName);
                zipOut.putNextEntry(entry);
                Files.copy(beanFile, zipOut);
                zipOut.closeEntry();
                return entryName;
        }

        /**
         * Computes the relative entry name that should be used for the bean
         * when adding it to an archive.
         *
         * @param bundle bundle metadata describing the target directory
         * @param beanFile compiled bean class that should be archived
         * @return entry name suitable for {@link ZipEntry}
         */
        public String beanEntryName(ReportBundles.Bundle bundle, Path beanFile)
        {
                Objects.requireNonNull(bundle, "bundle");
                Objects.requireNonNull(beanFile, "beanFile");

                String bundleRoot = Optional.ofNullable(bundle.bundleRoot()).orElse("").trim();
                Path bundleRootPath = bundleRootPath(bundleRoot);
                Path beanPath = beanFile.toAbsolutePath().normalize();

                if (!beanPath.startsWith(bundleRootPath))
                {
                        throw new IllegalArgumentException("Bean file " + beanFile
                                + " must reside under bundle root " + bundleRootPath);
                }

                Path relative = bundleRootPath.relativize(beanPath);
                String entry = relative.toString().replace('\\', '/');

                if (entry.isEmpty())
                {
                        throw new IllegalArgumentException(
                                "Bean file cannot be located at the bundle root itself");
                }

                if (bundleRoot.isEmpty())
                {
                        return entry;
                }

                return bundleRoot + "/" + entry;
        }

        private Path bundleRootPath(String bundleRoot)
        {
                if (bundleRoot.isEmpty())
                {
                        return bundlesDirectory;
                }

                Path relative = Path.of(bundleRoot);
                return bundlesDirectory.resolve(relative).normalize();
        }
}
