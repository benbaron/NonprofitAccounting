
package nonprofitbookkeeping.reports.jasper.runtime;

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
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * Loads Jasper report bundle metadata and exposes lookup helpers for
 * generators and UI registries.
 */
public final class ReportBundles
{
	private static final List<String> BUNDLE_ROOTS = List.of(
		"nonprofitbookkeeping/reports/bundles",
		"nonprofitbookkeeping/reports");
	
	private static final String TEMPLATE_ROOT = "nonprofitbookkeeping/reports";
	
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
	private static final Logger LOGGER =
		Logger.getLogger(ReportBundles.class.getName());
	
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
				"No report bundle registered for generator: " +
					generatorClassName);
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
			String metadataPath = buildMetadataPath(resource.root(),
				resource.metadataDirectory(), resource.fileName());
			
			Properties props = new Properties();
			
			try (InputStream in = loader.getResourceAsStream(metadataPath))
			{
				
				if (in == null)
				{
					throw new IllegalStateException(
						"Missing bundle metadata resource: " + metadataPath);
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
				System.err
					.println("Skipping bundle due to unknown report type '" +
						reportTypeName + "' in " + metadataPath);
				continue;
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
					LOGGER.log(Level.WARNING,
						"Bundle {0} declares beanClass {1} without beanName; " +
							"defaulting beanName to null",
						new Object[]
						{ metadataPath, beanClass });
				}
				else
				{
					beanName = rawBeanName.trim();
				}
				
			}
			
			String description = Optional
				.ofNullable(props.getProperty("description"))
				.map(String::trim)
				.filter(s -> !s.isEmpty())
				.orElse(null);
			
			String jrxmlResource = buildTemplatePath(loader,
				resource.root(), resource.bundleRoot(), templateName);
			
			String id =
				resource.metadataDirectory().isBlank() ? resource.fileName() :
					resource.metadataDirectory() + "/" + resource.fileName();
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
				LOGGER.log(Level.WARNING,
					"Duplicate bundle id detected: {0}; keeping {1}",
					new Object[] { id, existing.metadataResource() });
				continue;
			}
			
			Bundle previous = byGenerator.putIfAbsent(generator, bundle);
			
			if (previous != null)
			{
				LOGGER.log(Level.WARNING,
					"Generator {0} already associated with bundle {1}; " +
						"skipping {2}",
					new Object[]
					{ generator, previous.id(), bundle.id() });
				byId.remove(id);
				continue;
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
		
		for (String root : BUNDLE_ROOTS)
		{
			
			try
			{
				Enumeration<URL> roots = loader.getResources(root);
				
				while (roots.hasMoreElements())
				{
					URL url = roots.nextElement();
					resources.addAll(scanRoot(url, root));
				}
				
			}
			catch (IOException e)
			{
				throw new IllegalStateException(
					"Unable to enumerate bundle roots", e);
			}
			
		}
		
		return resources;
		
	}
	
	private static List<BundleResource> scanRoot(URL url, String resourceRoot)
	{
		String protocol = url.getProtocol();
		
		try
		{
			
			if ("file".equals(protocol))
			{
				Path base = Paths.get(url.toURI());
				return collectBundleResources(base, resourceRoot);
			}
			
			if ("jar".equals(protocol))
			{
				return collectFromJar(url, resourceRoot);
			}
			
		}
		catch (IOException | URISyntaxException e)
		{
			throw new IllegalStateException("Failed scanning bundles at " + url,
				e);
		}
		
		throw new IllegalStateException(
			"Unsupported bundle protocol: " + protocol);
		
	}
	
	private static List<BundleResource> collectFromJar(URL url,
		String resourceRoot)
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
			return collectBundleResources(base, resourceRoot);
		}
		finally
		{
			
			if (created)
			{
				fs.close();
			}
			
		}
		
	}
	
	private static List<BundleResource> collectBundleResources(Path base,
		String resourceRoot) throws IOException
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
				.forEach(p ->
				{
					Path relative = base.relativize(p);
					
					if (resourceRoot.equals(TEMPLATE_ROOT) &&
						relative.getNameCount() > 0 &&
						"bundles".equals(relative.getName(0).toString()))
					{
						return;
					}
					
					Path directory = relative.getNameCount() > 1 ? relative
						.subpath(0, relative.getNameCount() - 1) : Path.of("");
					String dir = directory.toString().replace('\\', '/');
					String bundleRoot = computeBundleRoot(directory);
					String file = relative.getFileName().toString();
					resources
						.add(new BundleResource(resourceRoot, dir, bundleRoot,
							file));
				});
		}
		
		return resources;
		
	}
	
	private static String buildMetadataPath(String root,
		String metadataDirectory,
		String fileName)
	{
		
		if (metadataDirectory == null || metadataDirectory.isBlank())
		{
			return root + "/" + fileName;
		}
		
		return root + "/" + metadataDirectory + "/" + fileName;
		
	}
	
	private static String buildTemplatePath(ClassLoader loader,
		String resourceRoot,
		String bundleRoot,
		String templateName)
	{
		String normalizedTemplate = templateName.trim();
		
		if (normalizedTemplate.startsWith("/"))
		{
			normalizedTemplate = normalizedTemplate.substring(1);
		}
		
		String prefix = Optional.ofNullable(bundleRoot).orElse("").trim();
		
		List<String> candidates = new ArrayList<>();
		
		if (!prefix.isEmpty())
		{
			candidates
				.add(resourceRoot + "/" + prefix + "/" + normalizedTemplate);
			candidates
				.add(TEMPLATE_ROOT + "/" + prefix + "/" + normalizedTemplate);
		}
		
		candidates.add(resourceRoot + "/" + normalizedTemplate);
		candidates.add(TEMPLATE_ROOT + "/" + normalizedTemplate);
		
		for (String candidate : candidates)
		{
			
			if (resourceExists(loader, candidate))
			{
				return candidate;
			}
			
		}
		
		return candidates.get(0);
		
	}
	
	private static boolean resourceExists(ClassLoader loader, String resource)
	{
		return loader.getResource(resource) != null;
		
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
	
	private record BundleResource(String root,
		String metadataDirectory,
		String bundleRoot,
		String fileName)
	{
		BundleResource
		{
			Objects.requireNonNull(root, "root");
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
