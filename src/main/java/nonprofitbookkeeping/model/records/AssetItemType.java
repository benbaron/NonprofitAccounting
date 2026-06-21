package nonprofitbookkeeping.model.records;

import java.util.Arrays;

/**
 * Supported asset item types for the asset register.
 */
public enum AssetItemType
{
    INVENTORY("Inventory"),
    DEPRECIATION_5_YEAR("5-year depreciation"),
    DEPRECIATION_7_YEAR("7-year depreciation");

    private final String displayName;

    AssetItemType(String displayName)
    {
        this.displayName = displayName;
    }

    public String displayName()
    {
        return this.displayName;
    }

    public static AssetItemType fromStorageValue(String value)
    {
        if (value == null || value.isBlank())
        {
            return null;
        }
        String normalized = value.trim();
        return Arrays.stream(values())
            .filter(type -> type.name().equalsIgnoreCase(normalized)
                || type.displayName.equalsIgnoreCase(normalized))
            .findFirst()
            .orElse(null);
    }
}
