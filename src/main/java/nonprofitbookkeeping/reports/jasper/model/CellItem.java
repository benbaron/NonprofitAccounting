package nonprofitbookkeeping.reports.jasper.model;

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
        return this.row;
    }

    public int col()
    {
        return this.col;
    }

    public boolean isDynamic()
    {
        return this.dynamic;
    }

    public String fieldName()
    {
        return this.fieldName;
    }
}