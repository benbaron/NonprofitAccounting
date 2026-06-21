package nonprofitbookkeeping.report.template;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Values supplied to compact semantic report templates. */
public class SemanticReportValueSet
{
	private final Map<String, Object> scalars = new LinkedHashMap<>();
	private final Map<String, List<Map<String, Object>>> tables = new LinkedHashMap<>();

	public void put(String key, Object value)
	{
		this.scalars.put(key, value);
	}

	public Object get(String key)
	{
		return this.scalars.get(key);
	}

	public Map<String, Object> scalars()
	{
		return Collections.unmodifiableMap(this.scalars);
	}

	public void putTable(String key, List<Map<String, Object>> rows)
	{
		this.tables.put(key, new ArrayList<>(rows));
	}

	public List<Map<String, Object>> table(String key)
	{
		return this.tables.getOrDefault(key, List.of());
	}

	public Map<String, List<Map<String, Object>>> tables()
	{
		return Collections.unmodifiableMap(this.tables);
	}
}
