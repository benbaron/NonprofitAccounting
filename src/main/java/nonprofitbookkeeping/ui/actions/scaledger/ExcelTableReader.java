
package nonprofitbookkeeping.ui.actions.scaledger;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import javax.swing.table.DefaultTableModel;
import java.io.File;
import java.io.FileInputStream;
import java.util.Vector;

public class ExcelTableReader
{
	
	/**
	 * Reads an Excel sheet and returns a DefaultTableModel representing the sheet's data.
	 * Assumes that the first row of the sheet contains column headers.
	 *
	 * @param file       the Excel file to read
	 * @param sheetIndex the zero-based index of the sheet to read
	 * @return a DefaultTableModel with the sheet's data
	 * @throws Exception if an error occurs during reading
	 */
	public static DefaultTableModel readSheetAsTable(File file, int sheetIndex) throws Exception
	{
		FileInputStream fis = new FileInputStream(file);
		Workbook workbook = new XSSFWorkbook(fis);
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
