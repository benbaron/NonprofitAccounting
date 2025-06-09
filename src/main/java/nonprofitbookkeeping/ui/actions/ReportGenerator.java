
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

/**
 * Utility class for generating Excel-based reports using JXLS templates and Apache POI.
 * This class focuses on creating ledger reports, potentially with multiple sheets
 * representing different accounts and a summary cover sheet.
 * It assumes data is provided via a static {@code beans} map, which needs to be populated beforehand.
 */
public class ReportGenerator
{
	/**
	 * Static map assumed to be pre-populated with ledger data.
	 * Keys are typically ledger identifiers (e.g., "ledgerQ1", "ledgerQ2"),
	 * and values are lists of {@link LedgerEntry} objects.
	 * This field's static nature implies global state, which might be a concern for reusability and testing.
	 */
	private static Map <String, List<LedgerEntry>> beans;
	
	/**
	 * Generates a single Excel sheet (as a {@link Workbook} object in memory)
	 * based on a JXLS template file and a given {@link LedgerReportContext}.
	 * The context provides the account name and entries to populate the template.
	 *
	 * @param templateFile The JXLS template {@link File} (e.g., an .xlsx file with JXLS markup).
	 * @param context The {@link LedgerReportContext} containing data for the report,
	 *                including the account name and a list of {@link LedgerEntry} objects.
	 * @return A new Apache POI {@link Workbook} (specifically XSSFWorkbook for .xlsx)
	 *         containing the processed sheet.
	 * @throws IOException If an error occurs during file input/output operations or
	 *                     while processing the JXLS template.
	 */
	public static Workbook generateSingleSheet(	File templateFile,
												LedgerReportContext context) throws IOException
	{
		
		try (InputStream is = new FileInputStream(templateFile))
		{
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			Context jxlsContext = new Context();
			jxlsContext.putVar("account", context); // Makes the entire context available as 'account'
			jxlsContext.putVar("entries", context.entries); // Specifically makes entries available as 'entries'
			
			JxlsHelper.getInstance().processTemplate(is, out, jxlsContext);
			
			// Read the generated content from ByteArrayOutputStream into a new Workbook
			try (InputStream generatedStream = new ByteArrayInputStream(out.toByteArray()))
			{
				return new XSSFWorkbook(generatedStream);
			}
			
		}
		
	}
	
	/**
	 * Generates a multi-sheet Excel ledger report and saves it to the specified output file.
	 * This method iterates through ledger entries (assumed to be pre-populated in the static {@code beans} map),
	 * groups them by account (specifically {@code entry.assetAccount}), and generates a separate sheet
	 * for each account using {@link #generateSingleSheet(File, LedgerReportContext)}.
	 * It also creates a "Cover" sheet summarizing the accounts and entry counts.
	 * <p>
	 * Note: This method relies on the static {@code beans} map being populated correctly before invocation.
	 * The grouping logic currently uses {@code entry.assetAccount} as the key; this might need adjustment
	 * depending on the desired report structure.
	 * </p>
	 *
	 * @param outputFile The {@link File} where the generated Excel report will be saved.
	 *                   If the file exists, it will be overwritten.
	 * @param templateFile The JXLS template {@link File} used for generating individual account sheets.
	 * @throws IOException If an error occurs during file input/output operations,
	 *                     JXLS template processing, or workbook writing.
	 * @throws FormulaParseException If there's an error parsing a formula during sheet copying (less likely with current cell-by-cell copy).
	 * @throws IllegalStateException If an issue occurs with cell types during copying (less likely with current cell-by-cell copy).
	 * @throws FileNotFoundException If the {@code templateFile} is not found.
	 * @throws NullPointerException If the static {@code beans} map is null when accessed.
	 */
	public static void generateReport(	File outputFile,
										File templateFile)
															throws IOException,
															FormulaParseException,
															IllegalStateException,
															FileNotFoundException
	{
		if (beans == null) {
            throw new NullPointerException("The static 'beans' map in ReportGenerator has not been initialized with data.");
        }
		try (Workbook masterWb = new XSSFWorkbook()) // Create the master workbook for output
		{
			Map<String, List<LedgerEntry>> accountToEntries = new HashMap<String, List<LedgerEntry>>();
			
			// Combine entries from ledgerQ1 - ledgerQ4 from the static beans map

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
