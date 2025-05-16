
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
 * Utility class that reads an <strong>.xlsm</strong> workbook and converts a sheet into a Swing
 * {@link DefaultTableModel}.  The logic mirrors the original Swing implementation but can be used
 * from Java FX code since {@code DefaultTableModel} is only an intermediate format (later converted
 * to a Java FX {@code TableView}).
 */
public class XlsmTableViewerFX
{
	
	/**
	 * Reads an XLSM file and converts the specified sheet into a {@link DefaultTableModel}. Assumes
	 * the first row contains column headers.
	 *
	 * @param file       the XLSM file to read
	 * @param sheetIndex zero‑based index of the sheet
	 * @return populated {@code DefaultTableModel}
	 */
	public static DefaultTableModel readXlsmToTableModel(File file, int sheetIndex) throws Exception
	{
		
		try (FileInputStream fis = new FileInputStream(file); Workbook wb = new XSSFWorkbook(fis))
		{
			Sheet sheet = wb.getSheetAt(sheetIndex);
			DefaultTableModel model = new DefaultTableModel();
			
			// Header row → column identifiers
			Row header = sheet.getRow(sheet.getFirstRowNum());
			
			if (header != null)
			{
				Vector<String> cols = new Vector<>();
				short firstCol = header.getFirstCellNum();
				short lastCol = header.getLastCellNum();
				
				for (int c = firstCol; c < lastCol; c++)
				{
					Cell cell = header.getCell(c, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
					cols.add(cell.toString());
				}
				
				model.setColumnIdentifiers(cols);
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
