package nonprofitbookkeeping.model;

import java.util.Vector;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents a page (sheet) of an Excel workbook.
 * Can hold either formulas or evaluated values.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ExcelWorkbookPage
{
    /**
     * The tabular data from the spreadsheet.
     * Each inner Vector represents a row; each String is a cell.
     */
    private Vector<Vector<String>> data;

    /**
     * Constructs an empty ExcelWorkbookPage.
     */
    public ExcelWorkbookPage()
    {
        this.data = new Vector<>();
    }

    /**
     * Gets the stored data.
     * 
     * @return the page data as a Vector of Vectors of Strings
     */
    public Vector<Vector<String>> getData()
    {
        return this.data;
    }

    /**
     * Sets the page data.
     * 
     * @param data the new data to store
     */
    public void setData(Vector<Vector<String>> data)
    {
        this.data = data;
    }

    /**
     * Adds a single row to the page data.
     * 
     * @param row a Vector of Strings representing one row
     */
    public void addRow(Vector<String> row)
    {
        this.data.add(row);
    }
}
