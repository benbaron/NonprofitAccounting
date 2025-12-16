package nonprofitbookkeeping.reports.generator.model;

import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;

/**
 * Logical representation of a single Excel sheet, including layout items and
 * the discovered dynamic fields.
 */
public final class SheetModel
{
    private final String sheetName;
    private final List<CellItem> items;
    private final Map<String, FieldInfo> fields;

    public SheetModel(String sheetName, List<CellItem> items, Map<String, FieldInfo> fields)
    {
        this.sheetName = sheetName;
        this.items = items;
        this.fields = (fields != null ? fields : new LinkedHashMap<>());
    }

    /**
     * Backwards-compatible constructor without a fields map.
     */
    public SheetModel(String sheetName, List<CellItem> items)
    {
        this(sheetName, items, new LinkedHashMap<>());
    }

    public String sheetName()
    {
        return sheetName;
    }

    public List<CellItem> items()
    {
        return items;
    }

    public Map<String, FieldInfo> fields()
    {
        return fields;
    }
}