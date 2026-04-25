package nonprofitbookkeeping.panels;

import nonprofitbookkeeping.model.records.AssetRecord;
import nonprofitbookkeeping.model.records.AssetItemType;
import nonprofitbookkeeping.ui.AssetsRegisterPanel;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AssetRowMappingRegressionTest
{
    @Test
    void editedValuesRemainInRowAndMapToRecord()
    {
        AssetsRegisterPanel.AssetRow row = new AssetsRegisterPanel.AssetRow("asset-1", "", "", "", "", null, "");

        row.setDateAcquired("2026-04-01");
        row.setDescription("Desk");
        row.setItemCount("2");
        row.setApproxValueTotal("199.99");
        row.setAccumulatedDepreciation("25.00");
        row.setItemType(AssetItemType.DEPRECIATION_5_YEAR);

        AssetRecord record = row.toRecord(1);

        assertEquals(LocalDate.parse("2026-04-01"), record.dateAcquired());
        assertEquals("Desk", record.description());
        assertEquals(2, record.itemCount());
        assertEquals(new BigDecimal("199.99"), record.approxValueTotal());
        assertEquals(new BigDecimal("25.00"), record.accumulatedDepreciation());
        assertEquals(AssetItemType.DEPRECIATION_5_YEAR, record.itemType());
    }

    @Test
    void mappingPreservesNonVisibleFieldsFromSourceRecord()
    {
        AssetRecord source = new AssetRecord(
            "asset-2",
            LocalDate.parse("2025-01-01"),
            "Laptop",
            1,
            new BigDecimal("500.00"),
            new BigDecimal("125.00"),
            new BigDecimal("500.00"),
            AssetItemType.DEPRECIATION_7_YEAR,
            "Operations",
            new BigDecimal("500.00"),
            1,
            null,
            null,
            null,
            Map.of("key", "value")
        );

        AssetsRegisterPanel.AssetRow row = AssetsRegisterPanel.AssetRow.fromRecord(source);
        row.setDescription("Updated Laptop");

        AssetRecord mapped = row.toRecord(1);

        assertEquals(new BigDecimal("500.00"), mapped.valuePerItem());
        assertEquals(AssetItemType.DEPRECIATION_7_YEAR, mapped.itemType());
        assertEquals(new BigDecimal("125.00"), mapped.accumulatedDepreciation());
        assertEquals(Map.of("key", "value"), mapped.extensions());
        assertEquals("Updated Laptop", mapped.description());
    }
}
