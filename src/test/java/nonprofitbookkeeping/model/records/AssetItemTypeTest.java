package nonprofitbookkeeping.model.records;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class AssetItemTypeTest
{
    @Test
    void mapsEnumNameAndDisplayName()
    {
        assertEquals(AssetItemType.INVENTORY, AssetItemType.fromStorageValue("INVENTORY"));
        assertEquals(AssetItemType.DEPRECIATION_5_YEAR, AssetItemType.fromStorageValue("5-year depreciation"));
    }

    @Test
    void unknownValueFallsBackToNull()
    {
        assertNull(AssetItemType.fromStorageValue("EQUIPMENT"));
        assertNull(AssetItemType.fromStorageValue(""));
        assertNull(AssetItemType.fromStorageValue(null));
    }
}
