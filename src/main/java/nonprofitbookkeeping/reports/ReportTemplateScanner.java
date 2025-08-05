
package nonprofitbookkeeping.reports;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Stream;

/**
 * Utility class for discovering Jasper report templates bundled with the application.
 */
public final class ReportTemplateScanner
{
	/**
	 * 
	 * Constructor ReportTemplateScanner
	 */
	private ReportTemplateScanner()
	{
	}
	
	/**
	 * Scans the {@code src/main/resources/jrxml} directory on the classpath and
	 * returns a map of display names to report type keys.
	 *
	 * @return map where each key is a user friendly display name and the value
	 *         is the corresponding report type key used by {@link ReportContext}.
	 */
	public static Map<String, String> discoverTemplates()
	{
	    Map<String, String> templates = new LinkedHashMap<>();

	    // Define your base directory - could also be injected, read from a config, etc.
	    Path baseDir = Paths.get(System.getProperty("user.dir")); // runtime working dir

//	    Path baseDir = Paths.get(System.getProperty("app.base.dir", "."));  // Fallback to current dir
	    Path reportsDir = baseDir.resolve("jrxml");

	    if (!Files.isDirectory(reportsDir))
	    {
	        System.err.println("Reports directory not found: " + reportsDir.toAbsolutePath());
	        return templates;
	    }

	    // convert a stream of names to 
	    try (Stream<Path> stream = Files.list(reportsDir))
	    {
	        stream
	            .filter(p -> p.getFileName().toString().endsWith(".jrxml"))
	            .forEach(p -> {
	                String fileName = p.getFileName().toString();
	                String base = fileName.substring(0, fileName.length() - ".jrxml".length());

	                if (base.endsWith("Alt"))
	                {
	                    base = base.substring(0, base.length() - 3);
	                }

	                String display = toDisplayName(base);
	                String key = toKey(base);
	                templates.putIfAbsent(display, key);
	            });
	    }
	    catch (IOException e)
	    {
	        e.printStackTrace();
	    }

	    return templates;
	}

	/**
	 * Convert base string to display name
	 * 
	 * @param base : name stem
	 * @return displayable string
	 */
	private static String toDisplayName(String base)
	{
		String withSpaces = base.replace('_', ' ');
		withSpaces = withSpaces.replaceAll("([a-z])([A-Z])", "$1 $2");
		String[] parts = withSpaces.split("\\s+");
		StringBuilder sb = new StringBuilder();
		
		for (String part : parts)
		{
			if (part.isEmpty())
				continue;
			sb.append(Character.toUpperCase(part.charAt(0))).append(part.substring(1).toLowerCase())
				.append(' ');
		}
		
		return sb.toString().trim();
	}
	
	/**
	 * Converts a string to a jasper report name
	 * 
	 * @param base
	 * @return
	 */
	private static String toKey(String base)
	{
		String snake = base.replaceAll("([a-z])([A-Z])", "$1_$2").replace('-', '_')
			.replace(' ', '_').toLowerCase();
		return snake + "_jasper";
	}
	
}
