package nonprofitbookkeeping.reports.jasper.runtime;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


/**
 * Wrapper that collapses multiple data rows into a single Jasper bean.
 * The first row's properties are promoted to top-level fields, while the
 * full row list is exposed under the {@code rows} field for use in bands.
 */
public final class ReportDataBundle extends LinkedHashMap<String, Object>
{
    
    /** The Constant ROWS_KEY. */
    private static final String ROWS_KEY = "rows";
    
    /** The Constant ROW_COUNT_KEY. */
    private static final String ROW_COUNT_KEY = "rowCount";

    /**
     * Instantiates a new report data bundle.
     */
    private ReportDataBundle()
    {
    }

    /**
     * From rows.
     *
     * @param rows the rows
     * @return the report data bundle
     * @throws IntrospectionException the introspection exception
     */
    public static ReportDataBundle fromRows(List<?> rows)
        throws IntrospectionException
    {
        ReportDataBundle bundle = new ReportDataBundle();
        if (rows == null || rows.isEmpty())
        {
            bundle.put(ROWS_KEY, List.of());
            bundle.put(ROW_COUNT_KEY, 0);
            return bundle;
        }

        Object first = rows.get(0);
        if (first instanceof Map<?, ?> firstMap)
        {
            for (Map.Entry<?, ?> entry : firstMap.entrySet())
            {
                Object key = entry.getKey();
                if (key != null)
                {
                    bundle.put(key.toString(), entry.getValue());
                }
            }
        }
        else
        {
            BeanInfo info = Introspector.getBeanInfo(first.getClass());
            for (PropertyDescriptor descriptor : info.getPropertyDescriptors())
            {
                String name = descriptor.getName();
                if ("class".equals(name))
                {
                    continue;
                }
                if (descriptor.getReadMethod() == null)
                {
                    continue;
                }
                try
                {
                    Object value = descriptor.getReadMethod().invoke(first);
                    bundle.put(name, value);
                }
                catch (ReflectiveOperationException ex)
                {
                    throw new IllegalStateException(
                        "Unable to read property '" + name +
                            "' from " + first.getClass().getName(),
                        ex
                    );
                }
            }
        }

        bundle.put(ROWS_KEY, List.copyOf(rows));
        bundle.put(ROW_COUNT_KEY, rows.size());
        return bundle;
    }
}
