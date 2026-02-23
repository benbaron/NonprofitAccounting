package nonprofitbookkeeping.reports.jasper.model;

import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;

// TODO: Auto-generated Javadoc
/**
 * Logical representation of a single Excel sheet, including layout items and
 * the discovered dynamic fields.
 */
public final class SheetModel
{
    
    /** The sheet name. */
    private final String sheetName;
    
    /** The items. */
    private final List<CellItem> items;
    
    /** The fields. */
    private final Map<String, FieldInfo> fields;

    /**
     * Instantiates a new sheet model.
     *
     * @param sheetName the sheet name
     * @param items the items
     * @param fields the fields
     */
    public SheetModel(String sheetName, List<CellItem> items, Map<String, FieldInfo> fields)
    {
        this.sheetName = sheetName;
        this.items = items;
        this.fields = (fields != null ? fields : new LinkedHashMap<>());
    }

    /**
     * Backwards-compatible constructor without a fields map.
     *
     * @param sheetName the sheet name
     * @param items the items
     */
    public SheetModel(String sheetName, List<CellItem> items)
    {
        this(sheetName, items, new LinkedHashMap<>());
    }

    /**
     * Sheet name.
     *
     * @return the string
     */
    public String sheetName()
    {
        return this.sheetName;
    }

    /**
     * Items.
     *
     * @return the list
     */
    public List<CellItem> items()
    {
        return this.items;
    }

    /**
     * Fields.
     *
     * @return the map
     */
    public Map<String, FieldInfo> fields()
    {
        return this.fields;
    }
}