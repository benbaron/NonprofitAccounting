
package nonprofitbookkeeping.reports.jasper.runtime;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * In-memory representation of a single sheet's field mapping CSV.
 *
 * Typically created via {@link FieldMapLoader}.
 */
public final class FieldMap
{
	
	private final List<FieldMapEntry> entries;
	/**
	 * Instantiates a new field map.
	 *
	 * @param sheetName the sheet name
	 * @param entries the entries
	 */
	FieldMap(String sheetName, List<FieldMapEntry> entries)
	{
		this.entries = Collections.unmodifiableList(new ArrayList<>(entries));
		
		Map<String, FieldMapEntry> tmp = new LinkedHashMap<>();
		
		for (FieldMapEntry e : entries)
		{
			tmp.put(e.getFieldName(), e);
		}
		
		Collections.unmodifiableMap(tmp);
		
	}
	
	
	/**
	 * All entries, in CSV order.
	 */
	public List<FieldMapEntry> getEntries()
	{
		return this.entries;
		
	}
	
	
}
