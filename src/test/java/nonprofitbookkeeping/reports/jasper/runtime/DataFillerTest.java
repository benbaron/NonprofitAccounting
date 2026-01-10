package nonprofitbookkeeping.reports.jasper.runtime;

import nonprofitbookkeeping.reports.jasper.beans.AssetDtl5aUndepositedFundsEntry;

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
        values.put("AMOUNT", "100.00");

        AssetDtl5aUndepositedFundsEntry bean =
            DataFiller.fill(AssetDtl5aUndepositedFundsEntry.class, values);

        assertEquals("100.00", bean.getAmount(),
            "Expected uppercase column labels to map to bean setters.");
    }

    @Test
    void fill_normalizesMixedCaseFieldNames()
    {
        Map<String, Object> values = new HashMap<>();
        values.put("AmOuNt", "250.00");

        AssetDtl5aUndepositedFundsEntry bean =
            DataFiller.fill(AssetDtl5aUndepositedFundsEntry.class, values);

        assertEquals("250.00", bean.getAmount(),
            "Expected mixed-case column labels to map to bean setters.");
    }
}
