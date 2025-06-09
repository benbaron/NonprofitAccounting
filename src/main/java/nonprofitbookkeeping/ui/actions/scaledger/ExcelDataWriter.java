package nonprofitbookkeeping.ui.actions.scaledger;

// uses Apache POI to write data back to workbook
import org.apache.poi.ss.usermodel.*;
import javax.swing.table.DefaultTableModel;
import java.io.*;

/**
 * Utility class for writing data from a {@link javax.swing.table.DefaultTableModel}
 * back to an existing Excel (XLSM) file, effectively creating a modified copy.
 * This class uses Apache POI for Excel manipulation.
 */
public class ExcelDataWriter
{
	/**
	 * Writes data from a {@link DefaultTableModel} to a specified sheet in an Excel workbook,
	 * creating a modified copy of an input Excel file.
	 * <p>
	 * The method reads an existing workbook from {@code inputFile}, finds the specified {@code sheetName},
	 * and then updates cells in that sheet based on the data in the {@code model}.
	 * Data writing starts at a fixed offset: row index + 9 and column index + 3 from the
	 * sheet's perspective (0-indexed). This means data from {@code model.getValueAt(0,0)}
	 * goes to cell D10 (column 3, row 9 in 0-indexed terms).
	 * </p>
	 * <p>
	 * It attempts to preserve cell types for numbers and booleans. Other values are converted
	 * to strings; if a string can be parsed as a double, it's written as a number, otherwise as a string.
	 * If rows or cells do not exist at the target location, they are created.
	 * The modified workbook is then saved to {@code outputFile}.
	 * </p>
	 * 
	 * @param inputFile The source Excel file (e.g., .xlsm) to read from. Must exist.
	 * @param outputFile The destination file where the modified workbook will be saved.
	 *                   If it exists, it will be overwritten.
	 * @param sheetName The name of the sheet within the workbook to write data to.
	 * @param model The {@link DefaultTableModel} containing the data to write.
	 * @throws IllegalArgumentException if the specified {@code sheetName} is not found in the {@code inputFile}.
	 * @throws org.apache.poi.openxml4j.exceptions.InvalidFormatException if the input file format is invalid.
	 * @throws IOException if an I/O error occurs during reading the input file or writing the output file.
	 * @throws Exception for other potential errors during workbook processing (though more specific exceptions are preferred).
	 */
    public static void writeModifiedCopy(File inputFile, 
                                         File outputFile, 
                                         String sheetName, 
                                         DefaultTableModel model) throws Exception
    {
        Workbook workbook = WorkbookFactory.create(inputFile); // Can throw InvalidFormatException, IOException
        Sheet sheet = workbook.getSheet(sheetName);
        if (sheet == null)
        {
            throw new IllegalArgumentException("Sheet not found: " + sheetName);
        }

        for (int rowIndex = 0; rowIndex < model.getRowCount(); rowIndex++)
        {
            Row row = sheet.getRow(rowIndex + 9);
            if (row == null) row = sheet.createRow(rowIndex + 9);

            for (int colIndex = 0; colIndex < model.getColumnCount(); colIndex++)
            {
                Cell cell = row.getCell(colIndex + 3, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
                Object value = model.getValueAt(rowIndex, colIndex);

                if (value instanceof Number)
                {
                    cell.setCellValue(((Number) value).doubleValue());
                }
                else if (value instanceof Boolean)
                {
                    cell.setCellValue((Boolean) value);
                }
                else
                {
                    try
                    {
                        double d = Double.parseDouble(value.toString());
                        cell.setCellValue(d);
                    }
                    catch (NumberFormatException e)
                    {
                        cell.setCellValue(value.toString());
                    }
                }
            }
        }

        try (FileOutputStream fos = new FileOutputStream(outputFile))
        {
            workbook.write(fos);
        }
        workbook.close();
    }
}
