package nonprofitbookkeeping.reports.jasper.runtime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.beans.IntrospectionException;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

class ReportDataBundleTest
{
    private static final class SampleBean
    {
        private final String name;

        private SampleBean(String name)
        {
            this.name = name;
        }

        public String getName()
        {
            return name;
        }
    }

    @Test
    void fromRowsPromotesFirstRowFields() throws IntrospectionException
    {
        SampleBean first = new SampleBean("alpha");
        SampleBean second = new SampleBean("beta");

        ReportDataBundle bundle =
            ReportDataBundle.fromRows(List.of(first, second));

        assertEquals("alpha", bundle.get("name"));
        assertEquals(2, bundle.get("rowCount"));
        Object rows = bundle.get("rows");
        assertInstanceOf(List.class, rows);
        List<?> rowList = (List<?>) rows;
        assertEquals(2, rowList.size());
        assertSame(first, rowList.get(0));
        assertSame(second, rowList.get(1));
    }

    @Test
    void fromRowsSupportsMaps() throws IntrospectionException
    {
        Map<String, Object> row = Map.of("name", "map-row");

        ReportDataBundle bundle = ReportDataBundle.fromRows(List.of(row));

        assertEquals("map-row", bundle.get("name"));
        assertEquals(1, bundle.get("rowCount"));
    }
}
