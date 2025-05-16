
package nonprofitbookkeeping.ui.actions;

import org.jxls.common.Context;
import org.jxls.util.JxlsHelper;

import nonprofitbookkeeping.model.scaledger.LedgerEntry;
import nonprofitbookkeeping.model.scaledger.LedgerReportContext;

import org.apache.poi.ss.formula.FormulaParseException;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReportGenerator
{
	private static Map <String, List<LedgerEntry>> beans;
	
	public static Workbook generateSingleSheet(	File templateFile,
												LedgerReportContext context) throws IOException
	{
		
		try (InputStream is = new FileInputStream(templateFile))
		{
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			Context jxlsContext = new Context();
			jxlsContext.putVar("account", context);
			jxlsContext.putVar("entries", context.entries);
			
			JxlsHelper.getInstance().processTemplate(is, out, jxlsContext);
			
			try (InputStream generatedStream = new ByteArrayInputStream(out.toByteArray()))
			{
				return new XSSFWorkbook(generatedStream);
			}
			
		}
		
	}
	
	/**
	 * @param outputFile
	 * @param templateFile
	 * @throws IOException
	 * @throws FormulaParseException
	 * @throws IllegalStateException
	 * @throws FileNotFoundException
	 */
	public static void generateReport(	File outputFile,
										File templateFile)
															throws IOException,
															FormulaParseException,
															IllegalStateException,
															FileNotFoundException
	{
		try (Workbook masterWb = new XSSFWorkbook())
		{
			Map<String, List<LedgerEntry>> accountToEntries = new HashMap<String, List<LedgerEntry>>();
			
			// Combine entries from ledgerQ1 - ledgerQ4
			for (String ledgerKey : new String[]
			{ "ledgerQ1", "ledgerQ2", "ledgerQ3", "ledgerQ4" })
			{
				List<LedgerEntry> entries = beans.get(ledgerKey);
				
				for (LedgerEntry entry : entries)
				{
					// Group by assetAccount (adjust grouping as needed)
					String account = entry.assetAccount;
					accountToEntries.computeIfAbsent(account, k -> new ArrayList<>())
						.add(entry);
				}
				
			}
			
			List<String> summaryRows = new ArrayList<>();
			
			for (Map.Entry<String, List<LedgerEntry>> entry : accountToEntries.entrySet())
			{
				String accountName = entry.getKey().replaceAll("[\\\\/:*?\"<>|]", "_");
				LedgerReportContext ctx =
					new LedgerReportContext(accountName, entry.getValue());
				Workbook accountWb = ReportGenerator.generateSingleSheet(templateFile, ctx);
				Sheet sheetToCopy = accountWb.getSheetAt(0);
				Sheet newSheet = masterWb.createSheet(accountName);
				
				for (int r = 0; r <= sheetToCopy.getLastRowNum(); r++)
				{
					org.apache.poi.ss.usermodel.Row srcRow = sheetToCopy.getRow(r);
					org.apache.poi.ss.usermodel.Row destRow = newSheet.createRow(r);
					
					if (srcRow != null)
					{
						
						for (int c = 0; c < srcRow.getLastCellNum(); c++)
						{
							org.apache.poi.ss.usermodel.Cell srcCell = srcRow.getCell(c);
							
							if (srcCell != null)
							{
								org.apache.poi.ss.usermodel.Cell destCell =
									destRow.createCell(c);
								
								switch(srcCell.getCellType())
								{
									case STRING:
										destCell.setCellValue(srcCell.getStringCellValue());
										break;
									
									case NUMERIC:
										destCell.setCellValue(srcCell.getNumericCellValue());
										break;
									
									case BOOLEAN:
										destCell.setCellValue(srcCell.getBooleanCellValue());
										break;
									
									case FORMULA:
										destCell.setCellFormula(srcCell.getCellFormula());
										break;
									
									default:
										destCell.setBlank();
										break;
								}
								
							}
							
						}
						
					}
					
				}
				
				summaryRows.add(accountName + " (" + ctx.entries.size() + " entries)");
			}
			
			// Create cover sheet
			Sheet cover = masterWb.createSheet("Cover");
			cover.createRow(0).createCell(0).setCellValue("Ledger Report Summary");
			
			for (int i = 0; i < summaryRows.size(); i++)
			{
				cover.createRow(i + 2).createCell(0).setCellValue(summaryRows.get(i));
			}
			
			try (FileOutputStream fos = new FileOutputStream(outputFile))
			{
				masterWb.write(fos);
			}
		}
		
	}
	
}
