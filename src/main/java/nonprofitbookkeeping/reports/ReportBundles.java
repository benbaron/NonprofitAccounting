package nonprofitbookkeeping.reports;

import nonprofitbookkeeping.service.ReportService.ReportType;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.FileSystem;
import java.nio.file.FileSystemNotFoundException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;

/**
 * Loads Jasper report bundle metadata and exposes lookup helpers for
 * generators and UI registries.
 */
public final class ReportBundles
{
        private static final String BUNDLES_ROOT =
                "nonprofitbookkeeping/reports/bundles";
        private static final String TEMPLATE_ROOT =
                "nonprofitbookkeeping/reports";

        /** Immutable metadata describing a single generator/template bundle. */
        public record Bundle(String id,
                String metadataDirectory,
                String bundleRoot,
                String metadataResource,
                String displayName,
                String jrxmlResource,
                String generatorClassName,
                String beanClassName,
                String beanName,
                String description,
                ReportType reportType)
        {
                /** @return true when a bean class was documented for this bundle. */
                public boolean hasBeanClass()
                {
                        return this.beanClassName != null && !this.beanClassName.isBlank();
                }

                /**
                 * @deprecated Prefer {@link #metadataDirectory()} for clarity.
                 */
                @Deprecated
                public String directory()
                {
                        return this.metadataDirectory;
                }
        }

        private static final Map<String, Bundle> BUNDLES_BY_ID;
        private static final Map<String, Bundle> BUNDLES_BY_GENERATOR;

        static
        {
                BundlesData data = loadBundles();
                BUNDLES_BY_ID = data.byId();
                BUNDLES_BY_GENERATOR = data.byGenerator();
        }

        private ReportBundles()
        {
        }

        /**
         * Returns all discovered bundles in deterministic order.
         *
         * @return immutable collection of bundles
         */
        public static Collection<Bundle> bundles()
        {
                return Collections.unmodifiableCollection(BUNDLES_BY_ID.values());
        }

        /**
         * Lookup helper for generator classes that need their JRXML resource.
         *
         * @param generatorClass Generator implementation class
         * @return bundle metadata describing the generator
         */
        public static Bundle bundleForGenerator(Class<?> generatorClass)
        {
                return bundleForGenerator(generatorClass.getName());
        }

        /**
         * Lookup helper for generator class names.
         *
         * @param generatorClassName fully qualified class name
         * @return bundle metadata describing the generator
         */
        public static Bundle bundleForGenerator(String generatorClassName)
        {
                Bundle bundle = BUNDLES_BY_GENERATOR.get(generatorClassName);

                if (bundle == null)
                {
                        throw new IllegalArgumentException(
                                "No report bundle registered for generator: "
                                        + generatorClassName);
                }

                return bundle;
        }

        private static BundlesData loadBundles()
        {
                Map<String, Bundle> byId = new LinkedHashMap<>();
                Map<String, Bundle> byGenerator = new LinkedHashMap<>();

                List<BundleResource> resources = discoverBundleResources();
                ClassLoader loader = ReportBundles.class.getClassLoader();

                for (BundleResource resource : resources)
                {
                        String metadataPath = BUNDLES_ROOT + "/"
                                + resource.metadataDirectory() + "/" + resource.fileName();

                        Properties props = new Properties();

                        try (InputStream in = loader.getResourceAsStream(metadataPath))
                        {
                                if (in == null)
                                {
                                        throw new IllegalStateException(
                                                "Missing bundle metadata resource: "
                                                        + metadataPath);
                                }

                                props.load(in);
                        }
                        catch (IOException e)
                        {
                                throw new IllegalStateException(
                                        "Unable to load bundle metadata: " + metadataPath, e);
                        }

                        String displayName = require(props, "displayName", metadataPath);
                        String generator = require(props, "generatorClass", metadataPath);
                        String reportTypeName = require(props, "reportType", metadataPath);
                        String templateName = require(props, "template", metadataPath);

                        ReportType reportType;

                        try
                        {
                                reportType = ReportType.valueOf(reportTypeName.trim());
                        }
                        catch (IllegalArgumentException ex)
                        {
                                throw new IllegalStateException(
                                        "Unknown report type '" + reportTypeName + "' in "
                                                + metadataPath,
                                        ex);
                        }

                        String beanClass = props.getProperty("beanClass");

                        if (beanClass != null && beanClass.isBlank())
                        {
                                beanClass = null;
                        }

                        String beanName = null;

                        if (beanClass != null)
                        {
                                String rawBeanName = props.getProperty("beanName");

                                if (rawBeanName == null || rawBeanName.isBlank())
                                {
                                        throw new IllegalStateException(
                                                "Missing required property 'beanName' in " + metadataPath);
                                }

                                beanName = rawBeanName.trim();
                        }

                        String description = Optional
                                .ofNullable(props.getProperty("description"))
                                .map(String::trim)
                                .filter(s -> !s.isEmpty())
                                .orElse(null);

                        String jrxmlResource = buildTemplatePath(resource.bundleRoot(),
                                templateName);

                        String id = resource.metadataDirectory() + "/"
                                + resource.fileName();
                        Bundle bundle = new Bundle(id,
                                resource.metadataDirectory(),
                                resource.bundleRoot(),
                                metadataPath,
                                displayName,
                                jrxmlResource,
                                generator,
                                beanClass,
                                beanName,
                                description,
                                reportType);

                        Bundle existing = byId.putIfAbsent(id, bundle);

                        if (existing != null)
                        {
                                throw new IllegalStateException(
                                        "Duplicate bundle id detected: " + id);
                        }

                        Bundle previous = byGenerator.put(generator, bundle);

                        if (previous != null)
                        {
                                throw new IllegalStateException(
                                        "Generator " + generator
                                                + " is already associated with bundle "
                                                + previous.id());
                        }
                }

                return new BundlesData(Map.copyOf(byId), Map.copyOf(byGenerator));
        }

        private static String require(Properties props, String key, String source)
        {
                String value = props.getProperty(key);

                if (value == null || value.isBlank())
                {
                        throw new IllegalStateException(
                                "Missing required property '" + key + "' in " + source);
                }

                return value.trim();
        }

        private static List<BundleResource> discoverBundleResources()
        {
                List<BundleResource> resources = new ArrayList<>();
                ClassLoader loader = ReportBundles.class.getClassLoader();

                try
                {
                        Enumeration<URL> roots = loader.getResources(BUNDLES_ROOT);

                        while (roots.hasMoreElements())
                        {
                                URL url = roots.nextElement();
                                resources.addAll(scanRoot(url));
                        }
                }
                catch (IOException e)
                {
                        throw new IllegalStateException("Unable to enumerate bundle roots", e);
                }

                return resources;
        }

        private static List<BundleResource> scanRoot(URL url)
        {
                String protocol = url.getProtocol();

                try
                {
                        if ("file".equals(protocol))
                        {
                                Path base = Paths.get(url.toURI());
                                return collectBundleResources(base);
                        }

                        if ("jar".equals(protocol))
                        {
                                return collectFromJar(url);
                        }
                }
                catch (IOException | URISyntaxException e)
                {
                        throw new IllegalStateException("Failed scanning bundles at " + url, e);
                }

                throw new IllegalStateException("Unsupported bundle protocol: " + protocol);
        }

        private static List<BundleResource> collectFromJar(URL url)
                throws IOException, URISyntaxException
        {
                String spec = url.toExternalForm();
                int separator = spec.indexOf("!/");

                if (separator < 0)
                {
                        throw new IllegalStateException("Malformed jar URL: " + spec);
                }

                URI jarUri = URI.create(spec.substring(0, separator));
                String entryPath = spec.substring(separator + 2);

                boolean created = false;
                FileSystem fs;

                try
                {
                        fs = FileSystems.getFileSystem(jarUri);
                }
                catch (FileSystemNotFoundException e)
                {
                        fs = FileSystems.newFileSystem(jarUri, Collections.emptyMap());
                        created = true;
                }

                try
                {
                        Path base = fs.getPath(entryPath);
                        return collectBundleResources(base);
                }
                finally
                {
                        if (created)
                        {
                                fs.close();
                        }
                }
        }

        private static List<BundleResource> collectBundleResources(Path base)
                throws IOException
        {
                if (!Files.exists(base))
                {
                        return List.of();
                }

                List<BundleResource> resources = new ArrayList<>();

                try (var paths = Files.walk(base))
                {
                        paths.filter(Files::isRegularFile)
                                .filter(p -> p.getFileName().toString().endsWith(".properties"))
                                .forEach(p -> {
                                        Path relative = base.relativize(p);

                                        if (relative.getNameCount() < 2)
                                        {
                                                return;
                                        }

                                        Path directory = relative.subpath(0,
                                                relative.getNameCount() - 1);
                                        String dir = directory.toString().replace('\\', '/');
                                        String bundleRoot = computeBundleRoot(directory);
                                        String file = relative.getFileName().toString();
                                        resources.add(
                                                new BundleResource(dir, bundleRoot, file));
                                });
                }

                return resources;
        }

        private static String buildTemplatePath(String bundleRoot, String templateName)
        {
                String normalizedTemplate = templateName.trim();

                if (normalizedTemplate.startsWith("/"))
                {
                        normalizedTemplate = normalizedTemplate.substring(1);
                }

                String prefix = Optional.ofNullable(bundleRoot).orElse("").trim();
                ClassLoader loader = ReportBundles.class.getClassLoader();

                List<String> candidates = new ArrayList<>();

                if (!prefix.isEmpty())
                {
                        candidates.add(BUNDLES_ROOT + "/" + prefix + "/"
                                + normalizedTemplate);
                }

                candidates.add(BUNDLES_ROOT + "/" + normalizedTemplate);

                if (!prefix.isEmpty())
                {
                        candidates.add(TEMPLATE_ROOT + "/" + prefix + "/"
                                + normalizedTemplate);
                }

                candidates.add(TEMPLATE_ROOT + "/" + normalizedTemplate);

                for (String candidate : candidates)
                {
                        if (loader.getResource(candidate) != null)
                        {
                                return candidate;
                        }
                }

                // Fall back to the most specific expected location to preserve error context
                return candidates.get(0);
        }

        private static String computeBundleRoot(Path metadataDirectory)
        {
                Path root = metadataDirectory;

                Path fileName = metadataDirectory.getFileName();

                if (fileName != null && fileName.toString().equals("metadata"))
                {
                        root = metadataDirectory.getParent();
                }

                if (root == null)
                {
                        return "";
                }

                if (root.getNameCount() == 0)
                {
                        return "";
                }

                return root.toString().replace('\\', '/');
        }

        private record BundleResource(String metadataDirectory,
                String bundleRoot,
                String fileName)
        {
                BundleResource
                {
                        Objects.requireNonNull(metadataDirectory, "metadataDirectory");
                        Objects.requireNonNull(bundleRoot, "bundleRoot");
                        Objects.requireNonNull(fileName, "fileName");
                }
        }

        private record BundlesData(Map<String, Bundle> byId,
                Map<String, Bundle> byGenerator)
        {
        }
}
