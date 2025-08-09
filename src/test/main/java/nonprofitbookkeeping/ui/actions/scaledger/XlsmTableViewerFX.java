
package nonprofitbookkeeping.ui.actions.scaledger;

import java.io.File;
import java.io.FileInputStream;
import java.util.Vector;

import javax.swing.table.DefaultTableModel;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 * Utility class for reading data from an Excel XLSM file (macro-enabled workbook)
 * and converting a specified sheet into a Swing {@link javax.swing.table.DefaultTableModel}.
 * <p>
 * This class is designed to be used in a JavaFX environment where the {@code DefaultTableModel}
 * might serve as an intermediate data structure before being potentially converted or adapted
 * for use with JavaFX UI components like {@link javafx.scene.control.TableView}.
 * It uses Apache POI for handling Excel file operations.
 * </p>
 */
public class XlsmTableViewerFX
{
	
	/**
	 * Reads a specified sheet from an Excel (XLSM) file and converts its content into a
	 * Swing {@link DefaultTableModel}.
	 * <p>
	 * The method assumes the following structure for the Excel sheet:
	 * <ul>
	 *   <li>The first row of the sheet (obtained by {@code sheet.getFirstRowNum()}) is treated as the header row,
	 *       and its cell values are used as column identifiers for the {@code DefaultTableModel}.</li>
	 *   <li>All subsequent rows are treated as data rows.</li>
	 * </ul>
	 * If a row is physically missing within the data range (between first and last row numbers),
	 * an empty row (a {@code Vector} with no elements, though typically it should match column count)
	 * is added to the model. Cells within a row are read up to the last cell number of that row;
	 * missing cells (where {@code row.getCell(c, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK)} returns a blank cell)
	 * are converted to their string representation (often an empty string).
	 * All cell values are converted to strings using {@link Cell#toString()} before being added to the model.
	 * </p>
	 *
	 * @param file The Excel (XLSM) {@link File} to read. Must not be null and must exist.
	 * @param sheetIndex The zero-based index of the sheet to read from the workbook.
	 * @return A {@link DefaultTableModel} populated with data from the specified sheet.
	 *         Column headers are derived from the first row of the sheet.
	 * @throws java.io.FileNotFoundException if the specified {@code file} does not exist.
	 * @throws org.apache.poi.openxml4j.exceptions.InvalidFormatException if the file format is invalid for XSSFWorkbook (e.g., not OOXML based).
	 * @throws IOException if an I/O error occurs while reading the file or workbook.
	 * @throws IllegalArgumentException if {@code sheetIndex} is out of bounds.
	 * @throws Exception for other potential low-level errors during Apache POI processing.
	 */
	public static DefaultTableModel readXlsmToTableModel(File file, int sheetIndex) throws Exception
	{
		
		try (FileInputStream fis = new FileInputStream(file); Workbook wb = new XSSFWorkbook(fis)) // XSSFWorkbook
																									// for
																									// .xlsx/.xlsm
		{
			
			if (sheetIndex < 0 || sheetIndex >= wb.getNumberOfSheets())
			{
				wb.close(); // Ensure workbook is closed on error path
				fis.close(); // Ensure fis is closed
				throw new IllegalArgumentException("Sheet index " + sheetIndex +
					" is out of bounds for workbook with " + wb.getNumberOfSheets() + " sheets.");
			}
			
			Sheet sheet = wb.getSheetAt(sheetIndex);
			DefaultTableModel model = new DefaultTableModel();
			
			// Header row → column identifiers
			Row header = sheet.getRow(sheet.getFirstRowNum());
			
			Vector<String> columnNames = new Vector<>(); // Initialize outside if block to ensure
															// it's always available for model
			
			if (header != null)
			{
				// Vector<String> cols = new Vector<>(); // Renamed to columnNames
				short firstCol = header.getFirstCellNum();
				// Ensure lastCol is not negative, which can happen for empty rows
				short lastCol = (firstCol >= 0) ? header.getLastCellNum() : 0;
				
				for (int c = firstCol; c < lastCol; c++)
				{
					Cell cell = header.getCell(c, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
					columnNames.add(cell.toString());
				}
				
				model.setColumnIdentifiers(columnNames);
			}
			
			// Data rows
			int firstData = sheet.getFirstRowNum() + 1;
			int lastRow = sheet.getLastRowNum();
			
			for (int r = firstData; r <= lastRow; r++)
			{
				Row row = sheet.getRow(r);
				Vector<Object> data = new Vector<>();
				
				if (row != null)
				{
					short firstCol = row.getFirstCellNum();
					short lastCol = row.getLastCellNum();
					
					for (int c = firstCol; c < lastCol; c++)
					{
						Cell cell = row.getCell(c, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
						data.add(cell.toString());
					}
					
				}
				
				model.addRow(data);
			}
			
			return model;
		}
		
	}
	
}
