package nonprofitbookkeeping.reports.jasper.runtime;

import nonprofitbookkeeping.reports.jasper.beans.ASSET_DTL_5aBean;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DataFillerTest
{
    @Test
    void fill_normalizesUppercaseFieldNames()
    {
        Map<String, Object> values = new HashMap<>();
        values.put("AMOUNT_4", "100.00");

        ASSET_DTL_5aBean bean = DataFiller.fill(ASSET_DTL_5aBean.class, values);

        assertEquals("100.00", bean.getAmount_4(),
            "Expected uppercase column labels to map to bean setters.");
    }

    @Test
    void fill_normalizesMixedCaseFieldNames()
    {
        Map<String, Object> values = new HashMap<>();
        values.put("AmOuNt_4", "250.00");

        ASSET_DTL_5aBean bean = DataFiller.fill(ASSET_DTL_5aBean.class, values);

        assertEquals("250.00", bean.getAmount_4(),
            "Expected mixed-case column labels to map to bean setters.");
    }
}
