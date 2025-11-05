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

        /** Immutable metadata describing a single generator/template bundle. */
        public record Bundle(String id,
                String directory,
                String metadataResource,
                String displayName,
                String jrxmlResource,
                String generatorClassName,
                String beanClassName,
                String beanSimpleName,
                String description,
                ReportType reportType)
        {
                /** @return true when a bean class was documented for this bundle. */
                public boolean hasBeanClass()
                {
                        return this.beanClassName != null && !this.beanClassName.isBlank();
                }

                /**
                 * Resolves the simple bean name for display or packaging purposes.
                 *
                 * @return optional simple bean name, empty when no bean class is defined
                 */
                public Optional<String> beanName()
                {
                        if (!hasBeanClass())
                        {
                                return Optional.empty();
                        }

                        if (this.beanSimpleName != null && !this.beanSimpleName.isBlank())
                        {
                                return Optional.of(this.beanSimpleName);
                        }

                        String className = this.beanClassName.substring(
                                this.beanClassName.lastIndexOf('.') + 1);
                        return Optional.of(className);
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
                        String metadataPath = BUNDLES_ROOT + "/" + resource.directory() + "/"
                                + resource.fileName();

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

                        String beanClass = Optional.ofNullable(props.getProperty("beanClass"))
                                .map(String::trim)
                                .filter(s -> !s.isEmpty())
                                .orElse(null);

                        String beanSimpleName = null;

                        if (beanClass != null)
                        {
                                beanSimpleName = Optional
                                        .ofNullable(props.getProperty("beanName"))
                                        .map(String::trim)
                                        .filter(s -> !s.isEmpty())
                                        .orElseGet(() -> beanClass
                                                .substring(beanClass.lastIndexOf('.') + 1));
                        }
                        else if (props.getProperty("beanName") != null)
                        {
                                throw new IllegalStateException(
                                        "Bean name defined without bean class in " + metadataPath);
                        }

                        String description = Optional
                                .ofNullable(props.getProperty("description"))
                                .map(String::trim)
                                .filter(s -> !s.isEmpty())
                                .orElse(null);

                        String jrxmlResource = BUNDLES_ROOT + "/" + resource.directory()
                                + "/" + templateName;

                        String id = resource.directory() + "/" + resource.fileName();
                        Bundle bundle = new Bundle(id,
                                resource.directory(),
                                metadataPath,
                                displayName,
                                jrxmlResource,
                                generator,
                                beanClass,
                                beanSimpleName,
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

                try (var paths = Files.walk(base, 2))
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
                                        String file = relative.getFileName().toString();
                                        resources.add(new BundleResource(dir, file));
                                });
                }

                return resources;
        }

        private record BundleResource(String directory, String fileName)
        {
                BundleResource
                {
                        Objects.requireNonNull(directory, "directory");
                        Objects.requireNonNull(fileName, "fileName");
                }
        }

        private record BundlesData(Map<String, Bundle> byId,
                Map<String, Bundle> byGenerator)
        {
        }
}
