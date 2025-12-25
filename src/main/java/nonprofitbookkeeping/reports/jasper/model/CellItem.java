package nonprofitbookkeeping.reports.generator.model;

/**
 * Represents a single cell from an Excel sheet in the logical model.
 */
public final class CellItem
{
    private final int row;
    private final int col;
    private final boolean dynamic;
    private final String fieldName;

    public CellItem(int row, int col, boolean dynamic, String fieldName)
    {
        this.row = row;
        this.col = col;
        this.dynamic = dynamic;
        this.fieldName = fieldName;
    }

    public int row()
    {
        return row;
    }

    public int col()
    {
        return col;
    }

    public boolean isDynamic()
    {
        return dynamic;
    }

    public String fieldName()
    {
        return fieldName;
    }
}