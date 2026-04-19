package nonprofitbookkeeping.panels;

import nonprofitbookkeeping.model.records.AssetRecord;
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
        AssetsRegisterPanel.AssetRow row = new AssetsRegisterPanel.AssetRow("asset-1", "", "", "", "");

        row.setDateAcquired("2026-04-01");
        row.setDescription("Desk");
        row.setItemCount("2");
        row.setApproxValueTotal("199.99");

        AssetRecord record = row.toRecord(1);

        assertEquals(LocalDate.parse("2026-04-01"), record.dateAcquired());
        assertEquals("Desk", record.description());
        assertEquals(2, record.itemCount());
        assertEquals(new BigDecimal("199.99"), record.approxValueTotal());
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
            new BigDecimal("500.00"),
            "EQUIPMENT",
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
        assertEquals("EQUIPMENT", mapped.itemType());
        assertEquals(Map.of("key", "value"), mapped.extensions());
        assertEquals("Updated Laptop", mapped.description());
    }
}
