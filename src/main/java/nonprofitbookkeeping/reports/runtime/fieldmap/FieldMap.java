package nonprofitbookkeeping.reports.runtime.fieldmap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * In-memory representation of a fieldmap CSV.
 */
public final class FieldMap
{
    private final List<FieldMapEntry> entries;

    public FieldMap(List<FieldMapEntry> entries)
    {
        this.entries = Collections.unmodifiableList(new ArrayList<>(entries));
    }

    public List<FieldMapEntry> getEntries()
    {
        return entries;
    }

    /**
     * Group entries by fieldName.
     */
    public Map<String, List<FieldMapEntry>> getEntriesByFieldName()
    {
        Map<String, List<FieldMapEntry>> map = new LinkedHashMap<>();
        for (FieldMapEntry e : entries)
        {
            String fieldName = e.getFieldName();
            if (fieldName == null)
            {
                continue;
            }
            map.computeIfAbsent(fieldName, k -> new ArrayList<>()).add(e);
        }
        return map;
    }

    /**
     * Convenience lookup by fieldName; returns an empty list if none are found.
     */
    public List<FieldMapEntry> getByFieldName(String fieldName)
    {
        if (fieldName == null)
        {
            return Collections.emptyList();
        }
        Map<String, List<FieldMapEntry>> map = getEntriesByFieldName();
        List<FieldMapEntry> list = map.get(fieldName);
        return (list != null) ? list : Collections.emptyList();
    }

    /**
     * Builds a SELECT list from dbExpr columns of the fieldmap:
     *
     *   expr1 as fieldName1,
     *   expr2 as fieldName2,
     *   ...
     *
     * Only entries with a non-blank dbExpr and fieldName are included.
     */
    public String buildSelectListFromDbExprs()
    {
        StringBuilder sb = new StringBuilder();
        boolean first = true;

        for (FieldMapEntry e : entries)
        {
            String expr = e.getDbExpr();
            String fieldName = e.getFieldName();
            if (expr == null || expr.trim().isEmpty()
                    || fieldName == null || fieldName.trim().isEmpty())
            {
                continue;
            }

            if (!first)
            {
                sb.append(",\n");
            }
            sb.append(expr).append(" as ").append(fieldName);
            first = false;
        }

        return sb.toString();
    }
}