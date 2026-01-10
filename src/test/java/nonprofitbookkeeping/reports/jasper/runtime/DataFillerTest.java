package nonprofitbookkeeping.reports.jasper.runtime;

import nonprofitbookkeeping.reports.jasper.beans.AssetDtl5aUndepositedFundsLineItem;

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
        values.put("AMOUNT_LEFT", "100.00");

        AssetDtl5aUndepositedFundsLineItem bean =
            DataFiller.fill(
                AssetDtl5aUndepositedFundsLineItem.class,
                values
            );

        assertEquals("100.00", bean.getAmount_left(),
            "Expected uppercase column labels to map to bean setters.");
    }

    @Test
    void fill_normalizesMixedCaseFieldNames()
    {
        Map<String, Object> values = new HashMap<>();
        values.put("AmOuNt_LeFt", "250.00");

        AssetDtl5aUndepositedFundsLineItem bean =
            DataFiller.fill(
                AssetDtl5aUndepositedFundsLineItem.class,
                values
            );

        assertEquals("250.00", bean.getAmount_left(),
            "Expected mixed-case column labels to map to bean setters.");
    }
}
