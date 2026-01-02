
package nonprofitbookkeeping.reports.jasper.runtime;

import java.io.IOException;
import java.util.Map;

/**
 * Builds SQL select lists from report fieldmap resources.
 */
public final class FieldMapSqlBuilder
{
	
	private FieldMapSqlBuilder()
	{
	
	}
	
	public static String buildSelectList(
		String fieldmapResource,
		Map<String, String> overrides
	) throws IOException
	{
		FieldMap fieldMap = FieldMapLoader.loadFromResource(fieldmapResource);
		StringBuilder sb = new StringBuilder();
		boolean first = true;
		
		for (FieldMapEntry entry : fieldMap.getEntries())
		{
			
			if (!first)
			{
				sb.append(",\n");
			}
			
			String fieldName = entry.getFieldName();
			String expr = overrides != null ? overrides.get(fieldName) : null;
			
			if (expr == null || expr.isBlank())
			{
				expr = "NULL";
			}
			
			sb.append(expr).append(" as ").append(fieldName);
			first = false;
		}
		
		return sb.toString();
		
	}
	
}
