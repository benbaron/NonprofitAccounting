
package nonprofitbookkeeping.reports.jasper.runtime;

import java.io.IOException;
import java.util.Map;

/**
 * Builds SQL select lists from report fieldmap resources.
 */
public final class FieldMapSqlBuilder
{
	
	/**
	 * Instantiates a new field map sql builder.
	 */
	private FieldMapSqlBuilder()
	{
	
	}
	
	/**
	 * Builds the select list.
	 *
	 * @param fieldmapResource the fieldmap resource
	 * @param overrides the overrides
	 * @return the string
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
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
			String dbExpr = entry.getDbExpr();
			
			if (expr == null || expr.isBlank())
			{
				if (dbExpr != null && !dbExpr.isBlank())
				{
					expr = dbExpr;
				}
				else
				{
					expr = "NULL";
				}
			}
			
			sb.append(expr).append(" as ").append(fieldName);
			first = false;
		}
		
		if (sb.isEmpty())
		{
			throw new IOException("SB is empty.");
		}
		
		return sb.toString();
		
	}
	
}
