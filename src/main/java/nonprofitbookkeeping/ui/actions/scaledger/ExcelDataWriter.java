package nonprofitbookkeeping.ui.actions.scaledger;

// uses Apache POI to write data back to workbook
import org.apache.poi.ss.usermodel.*;
import javax.swing.table.DefaultTableModel;
import java.io.*;

public class ExcelDataWriter
{
	/**
	 * Writes from table model to XLSM output
	 * 
	 * @param inputFile
	 * @param outputFile
	 * @param sheetName
	 * @param model
	 * @throws Exception
	 */
    public static void writeModifiedCopy(File inputFile, 
                                         File outputFile, 
                                         String sheetName, 
                                         DefaultTableModel model) throws Exception
    {
        Workbook workbook = WorkbookFactory.create(inputFile);
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
