
package nonprofitbookkeeping.reports.jasper.runtime;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Loader for the *_fieldmap.csv files emitted by the Excelâ†’JRXML/bean generator.
 *
 * Expected columns:
 *   0: sheetName
 *   1: cellRef
 *   2: fieldName
 *   3: javaType
 *   4: excelFormat
 *   5: dbExpr (optional)
 */
public final class FieldMapLoader
{
	
	private FieldMapLoader()
	{
	
	}
	
	/**
	 * Load a fieldmap from a filesystem path.
	 */
	public static FieldMap load(Path csvPath) throws IOException
	{
		
		try (BufferedReader reader =
			Files.newBufferedReader(csvPath, StandardCharsets.UTF_8))
		{
			return loadFromReader(reader, csvPath.toString());
		}
		
	}
	
	/**
	 * Load a fieldmap from a classpath resource, using the given anchor class
	 * for resource resolution.
	 *
	 * Example:
	 *   FieldMapLoader.loadFromResource(
	 *       AccountSummaryDataProvider.class,
	 *       "/reports/AccountSummary_fieldmap.csv");
	 */
	public static FieldMap loadFromResource(Class<?> anchor,
		String resourcePath)
		throws IOException
	{
		
		InputStream in = anchor.getResourceAsStream(resourcePath);
		
		if (in == null)
		{
			throw new FileNotFoundException(
				"Fieldmap resource not found on classpath: " + resourcePath);
		}
		
		try (BufferedReader reader =
			new BufferedReader(
				new InputStreamReader(in, StandardCharsets.UTF_8)))
		{
			return loadFromReader(reader, resourcePath);
		}
		
	}
	
	/**
	 * Convenience overload using FieldMapLoader.class as the anchor.
	 *
	 * If resourcePath starts with "/", it is treated as absolute.
	 */
	public static FieldMap loadFromResource(String resourcePath)
		throws IOException
	{
		return loadFromResource(FieldMapLoader.class, resourcePath);
		
	}
	
	// --- Internal helpers ---
	
	private static FieldMap loadFromReader(BufferedReader reader,
		String sourceId)
		throws IOException
	{
		
		String header = reader.readLine();
		
		if (header == null)
		{
			throw new IOException("Empty fieldmap: " + sourceId);
		}
		
		List<FieldMapEntry> entries = new ArrayList<>();
		String sheetName = null;
		
		String line;
		
		while ((line = reader.readLine()) != null)
		{
			String trimmed = line.trim();
			
			if (trimmed.isEmpty())
			{
				continue;
			}
			
			if (trimmed.startsWith("#"))
			{
				continue;
			}
			
			List<String> cols = parseCsvLine(line);
			
			if (cols.size() < 5)
			{
				throw new IOException(
					"Expected at least 5 columns in fieldmap, got " +
						cols.size() +
						" in " + sourceId + " line: " + line);
			}
			
			String sName = cols.get(0);
			String cellRef = cols.get(1);
			String fieldName = cols.get(2);
			String javaType = cols.get(3);
			String excelFmt = emptyToNull(cols.get(4));
			
			String dbExpr = null;
			
			if (cols.size() > 5)
			{
				dbExpr = emptyToNull(cols.get(5));
			}
			
			if (sheetName == null)
			{
				sheetName = sName;
			}
			
			FieldMapEntry entry = new FieldMapEntry(
				sName,
				cellRef,
				fieldName,
				javaType,
				excelFmt,
				dbExpr
			);
			entries.add(entry);
		}
		
		if (sheetName == null)
		{
			throw new IOException("No data rows in fieldmap: " + sourceId);
		}
		
		return new FieldMap(sheetName, entries);
		
	}
	
	private static String emptyToNull(String s)
	{
		
		if (s == null)
		{
			return null;
		}
		
		String t = s.trim();
		return t.isEmpty() ? null : t;
		
	}
	
	/**
	 * Parse a single CSV line.
	 *
	 * This is a small RFC4180-ish parser that understands:
	 *  - fields separated by commas
	 *  - double-quoted fields
	 *  - doubled double-quotes ("") inside quoted fields
	 */
	private static List<String> parseCsvLine(String line)
	{
		List<String> cols = new ArrayList<>();
		StringBuilder sb = new StringBuilder();
		boolean inQuotes = false;
		
		for (int i = 0; i < line.length(); i++)
		{
			char ch = line.charAt(i);
			
			if (inQuotes)
			{
				
				if (ch == '"')
				{
					
					// possible escaped quote
					if (i + 1 < line.length() && line.charAt(i + 1) == '"')
					{
						sb.append('"');
						i++;
					}
					else
					{
						inQuotes = false;
					}
					
				}
				else
				{
					sb.append(ch);
				}
				
			}
			else
			{
				
				if (ch == '"')
				{
					inQuotes = true;
				}
				else if (ch == ',')
				{
					cols.add(sb.toString());
					sb.setLength(0);
				}
				else
				{
					sb.append(ch);
				}
				
			}
			
		}
		
		cols.add(sb.toString());
		return cols;
		
	}
	
}
