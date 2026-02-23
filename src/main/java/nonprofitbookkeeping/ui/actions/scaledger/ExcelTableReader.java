
package nonprofitbookkeeping.ui.actions.scaledger;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import javax.swing.table.DefaultTableModel;
import java.io.File;
import java.io.FileInputStream;
import java.util.Vector;

// TODO: Auto-generated Javadoc
/**
 * Utility class for reading data from an Excel sheet into a Swing {@link javax.swing.table.DefaultTableModel}.
 * This class uses Apache POI to interact with Excel files (specifically XSSFWorkbook for .xlsx format).
 */
public class ExcelTableReader
{
	
	/**
	 * Reads a specified sheet from an Excel file and converts its content into a {@link DefaultTableModel}.
	 * <p>
	 * The method assumes the first row of the Excel sheet contains the column headers.
	 * Subsequent rows are read as data. If a row is physically missing in the sheet
	 * (within the range of {@code sheet.getFirstRowNum() + 1} to {@code sheet.getLastRowNum()}),
	 * an empty row is added to the model. Cells within a row are read up to the last
	 * cell number of that row; missing cells within this range are treated as blank.
	 * All cell values are converted to their string representation using {@link Cell#toString()}.
	 * </p>
	 *
	 * @param file The Excel {@link File} to read. Must exist and be a valid .xlsx file.
	 * @param sheetIndex The zero-based index of the sheet to read from the workbook.
	 * @return A {@link DefaultTableModel} populated with the data from the specified Excel sheet.
	 *         The model will have column headers based on the first row of the sheet.
	 * @throws Exception for other potential errors during workbook processing by Apache POI (though more specific exceptions are preferred).
	 */
	public static DefaultTableModel readSheetAsTable(File file, int sheetIndex) throws Exception
	{
		FileInputStream fis = new FileInputStream(file); // Can throw FileNotFoundException
		Workbook workbook = new XSSFWorkbook(fis); // Can throw IOException, InvalidFormatException
		Sheet sheet = workbook.getSheetAt(sheetIndex);
		DefaultTableModel model = new DefaultTableModel();
		
		// Build column headers from the first row.
		Row headerRow = sheet.getRow(sheet.getFirstRowNum());
		
		if (headerRow != null)
		{
			Vector<String> columnNames = new Vector<>();
			short firstCol = headerRow.getFirstCellNum();
			short lastCol = headerRow.getLastCellNum();
			
			for (int c = firstCol; c < lastCol; c++)
			{
				Cell cell = headerRow.getCell(c, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
				columnNames.add(cell.toString());
			}
			
			model.setColumnIdentifiers(columnNames);
		}
		
		// Process all rows after the header row.
		int firstRow = sheet.getFirstRowNum() + 1;
		int lastRow = sheet.getLastRowNum();
		
		for (int r = firstRow; r <= lastRow; r++)
		{
			Row row = sheet.getRow(r);
			
			if (row == null)
			{
				// If the row is missing, add an empty row.
				model.addRow(new Vector<>());
				continue;
			}
			
			Vector<String> rowData = new Vector<>();
			short firstCol = row.getFirstCellNum();
			short lastCol = row.getLastCellNum();
			
			for (int c = firstCol; c < lastCol; c++)
			{
				Cell cell = row.getCell(c, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
				rowData.add(cell.toString());
			}
			
			model.addRow(rowData);
		}
		
		workbook.close();
		fis.close();
		return model;
	}
	
}
