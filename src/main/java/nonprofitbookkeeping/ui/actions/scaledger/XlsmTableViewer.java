
package nonprofitbookkeeping.ui.actions.scaledger;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.io.File;
import java.io.FileInputStream;
import java.util.Vector;

public class XlsmTableViewer
{
	
	/**
	 * Reads an XLSM file and converts the specified sheet into a DefaultTableModel.
	 * Assumes the first row contains column headers.
	 *
	 * @param file       the XLSM file to read
	 * @param sheetIndex the zero-based index of the sheet to read
	 * @return a DefaultTableModel containing the sheet data
	 * @throws Exception if an error occurs while reading the file
	 */
	public static DefaultTableModel readXlsmToTableModel(File file, int sheetIndex) throws Exception
	{
		// Open the file using a FileInputStream.
		FileInputStream fis = new FileInputStream(file);
		// XSSFWorkbook can read both .xlsx and .xlsm files.
		Workbook workbook = new XSSFWorkbook(fis);
		Sheet sheet = workbook.getSheetAt(sheetIndex);
		DefaultTableModel model = new DefaultTableModel();
		
		// Assume the first row contains headers.
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
		
		// Process all rows after the header.
		int firstDataRow = sheet.getFirstRowNum() + 1;
		int lastRow = sheet.getLastRowNum();
		
		for (int r = firstDataRow; r <= lastRow; r++)
		{
			Row row = sheet.getRow(r);
			// Create a new row vector; if row is null, add an empty row.
			Vector<Object> rowData = new Vector<>();
			
			if (row != null)
			{
				short firstCol = row.getFirstCellNum();
				short lastCol = row.getLastCellNum();
				
				for (int c = firstCol; c < lastCol; c++)
				{
					Cell cell = row.getCell(c, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
					rowData.add(cell.toString());
				}
				
			}
			
			model.addRow(rowData);
		}
		
		workbook.close();
		fis.close();
		return model;
	}
	
	/**
	 * Creates a JTable with the given table model and displays it in a JFrame.
	 *
	 * @param model the table model to display
	 */
	public static void createAndShowGUI(DefaultTableModel model)
	{
		JTable table = new JTable(model);
		JScrollPane scrollPane = new JScrollPane(table);
		JFrame frame = new JFrame("XLSM Table Viewer");
		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		frame.add(scrollPane);
		frame.setSize(800, 600);
		frame.setLocationRelativeTo(null); // center the frame
		frame.setVisible(true);
	}
	
	/**
	 * Main method: reads an XLSM file and shows its first sheet in a JTable.
	 *
	 * @param args command line arguments
	 */
	public static void main(String[] args)
	{
		// Change "example.xlsm" to the path of your XLSM file.
		File file = new File("example.xlsm");
		
		try
		{
			// Read the first sheet (index 0) into a DefaultTableModel.
			DefaultTableModel model = readXlsmToTableModel(file, 0);
			// Schedule the GUI creation on the Event Dispatch Thread.
			SwingUtilities.invokeLater(() -> createAndShowGUI(model));
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		
	}
	
}
