
package nonprofitbookkeeping.service;

import nonprofitbookkeeping.model.impex.ExcelLedgerRow;
import org.apache.poi.ss.usermodel.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;

/**
 * Utility service for importing a specialized Excel ledger format.
 */
public class ExcelLedgerImportService
{
	private static final Logger LOGGER =
		LoggerFactory.getLogger(ExcelLedgerImportService.class);
	
	/**
	 * Reads the first worksheet of the given Excel file and converts the rows
	 * into {@link ExcelLedgerRow} objects.
	 *
	 * @param file Excel workbook to read (.xlsx or .xlsm).
	 * @return list of parsed rows in order of appearance.
	 * @throws IOException if the file cannot be read or parsed.
	 */
	public static List<ExcelLedgerRow> importSpreadsheet(File file)
		throws IOException
	{
		
		if (file == null || !file.exists())
		{
			throw new IOException("Input Excel file does not exist: " + file);
		}
		
		try (FileInputStream fis = new FileInputStream(file);
			Workbook workbook = WorkbookFactory.create(fis))
		{
			Sheet sheet = workbook.getSheetAt(0);
			
			if (sheet == null)
			{
				return Collections.emptyList();
			}
			
			// Ingest the body rows
			List<ExcelLedgerRow> results = new ArrayList<>();
			int firstRow = sheet.getFirstRowNum() + 1;
			int lastRow = sheet.getLastRowNum();
			
			DataFormatter formatter = new DataFormatter();
			FormulaEvaluator evaluator = workbook.getCreationHelper()
				.createFormulaEvaluator();
			
			for (int r = firstRow; r <= lastRow; r++)
			{
				LOGGER.debug("Row number {}", r);
				Row row = sheet.getRow(r);
				
				if (row == null)
				{
					continue;
				}
				
				RowReader reader = new RowReader(row, r, formatter, evaluator);
				ExcelLedgerRow excelLedgerRow = reader.readLedgerRow();
				results.add(excelLedgerRow);
				
				// debug
				java.io.StringWriter sw = new java.io.StringWriter();
				printRow(row, formatter, evaluator, sw);
				LOGGER.trace(sw.toString());
				
			}
			
			return results;
		}
		
	}
	
	/**
	 * printRow
	 * 
	 * @param row
	 * @param formatter
	 * @param evaluator
	 * @param out
	 */
	public static void printRow(Row row,
		DataFormatter formatter,
		FormulaEvaluator evaluator,
		Appendable out)
	{
		
		if (row == null)
		{
			return;
		}
		
		try
		{
			
			for (int i = row.getFirstCellNum(); i < row.getLastCellNum(); i++)
			{
				Cell cell =
					row.getCell(i, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
				String value = (cell == null) ? "" :
					" " +
						"[" + i + "]=" +
						formatter.formatCellValue(cell, evaluator);
				out.append(value);
			}
			
			out.append('\n');
		}
		catch (IOException e)
		{
			throw new UncheckedIOException(e);
		}
		
	}
	
	
	/**
	 * Helper wrapper that provides easy access to cell values from a row.
	 * All values returned are trimmed strings and each access is printed
	 * with the row/column information for debugging purposes.
	 */
	private static class RowReader
	{
		final Row row;
		final int index;
		final DataFormatter fmt;
		final FormulaEvaluator eval;
		
		RowReader(Row row, int index, DataFormatter fmt,
			FormulaEvaluator evaluator)
		{
			this.row = row;
			this.index = index;
			this.fmt = fmt;
			this.eval = evaluator;
			
		}
		
		/**
		 * isBlank
		 * @param s
		 * @return
		 */
		private static boolean isBlank(String s)
		{
			return s == null || s.trim().isEmpty();
			
		}
		
		/**
		 * readLedgerRow
		 * @param row
		 * @param fmt
		 * @param eval
		 * @return
		 */
		public ExcelLedgerRow readLedgerRow()
		{
			
			if (this.row == null)
			{
				return null;
			}
			
			ExcelLedgerRow bean = new ExcelLedgerRow();
			
			int c = this.row.getFirstCellNum();
			
			if (c < 0)
			{
				return null; // empty
			}
			
			// ---- Fixed columns (in the declared order) ----
			bean.setBalance(
				readBigDecimal(this.row.getCell(c++), this.fmt, this.eval));
			bean.setDate(
				readLocalDate(this.row.getCell(c++), this.fmt, this.eval));
			bean.setCheckNumber(
				readString(this.row.getCell(c++), this.fmt, this.eval));
			c++; // skip amount
			bean.setClearBank(
				readString(this.row.getCell(c++), this.fmt, this.eval));
			bean.setToFrom(
				readString(this.row.getCell(c++), this.fmt, this.eval));
			bean.setMemoNotes(
				readString(this.row.getCell(c++), this.fmt, this.eval));
			bean.setBudgetTracking(
				readString(this.row.getCell(c++), this.fmt, this.eval));
			
			c++; // skip number column
			bean.setNetTotal(
				readBigDecimal(this.row.getCell(c++), this.fmt, this.eval));
			
			// ---- Allocation groups (5 columns each) ----
			final int GROUP_SIZE = 5;
			int last = this.row.getLastCellNum();
			
			while (c < last)
			{
				
				for (int i = 0; i < 2; i++)
				{
					// Pull raw strings first
					String amt =
						readString(this.row.getCell(c), this.fmt, this.eval);
					String acct = readString(this.row.getCell(c + 1), this.fmt,
						this.eval);
					String income = readString(this.row.getCell(c + 2),
						this.fmt, this.eval);
					String exp = readString(this.row.getCell(c + 3), this.fmt,
						this.eval);
					String fund = readString(this.row.getCell(c + 4), this.fmt,
						this.eval);
					
					boolean allBlank =
						(isBlank(amt) && isBlank(acct) && isBlank(income) &&
							isBlank(exp) && isBlank(fund));
					
					if (!allBlank)
					{
						ExcelLedgerRow.Allocation a =
							new ExcelLedgerRow.Allocation();
						a.setAmount(readBigDecimal(this.row.getCell(c),
							this.fmt,
							this.eval)); // use numeric parse foramt
						a.setAssetLiabilityAccount(acct);
						a.setIncomeCategory(income);
						a.setExpenseCategory(exp);
						a.setFund(fund);
						bean.getAllocations().add(a);
					}
					
					c += GROUP_SIZE; // advance regardless; tolerate gaps
				}
				
				c++; // skip the number column
			}
			
			LOGGER.trace("Bean: {}", bean);
			return bean;
			
		}
		
		
		/**
		 * readString
		 * @param cell
		 * @param fmt
		 * @param eval
		 * @return
		 */
		private static String readString(Cell cell,
			DataFormatter fmt,
			FormulaEvaluator eval)
		{
			
			if (cell == null)
			{
				return null;
			}
			
			return fmt.formatCellValue(cell, eval).trim();
			
		}
		
		/**
		 * readBigDecimal
		 * @param cell
		 * @param fmt
		 * @param eval
		 * @return
		 */
		private static BigDecimal readBigDecimal(Cell cell,
			DataFormatter fmt,
			FormulaEvaluator eval)
		{
			
			if (cell == null)
			{
				return null;
			}
			
			// If it’s a pure numeric cell, avoid the formatter’s
			// rounding/formatting
			CellType type = cell.getCellType();
			
			if (type == CellType.FORMULA)
			{
				type = eval.evaluateFormulaCell(cell);
			}
			
			if (type == CellType.NUMERIC)
			{
				return BigDecimal.valueOf(cell.getNumericCellValue());
			}
			
			// Otherwise parse the formatted text
			String s = fmt.formatCellValue(cell, eval).trim();
			
			if (s.isEmpty())
			{
				return null;
			}
			
			// Handle common accounting formats: commas and parentheses for
			// negatives
			s = s.replace(",", "");
			
			if (s.startsWith("(") && s.endsWith(")"))
			{
				s = "-" + s.substring(1, s.length() - 1);
			}
			
			try
			{
				return new BigDecimal(s);
			}
			catch (NumberFormatException ex)
			{
				return null; // or BigDecimal.ZERO, depending on your rule
			}
			
		}
		
		/**
		 * readLocalDate
		 * @param cell
		 * @param fmt
		 * @param eval
		 * @return
		 */
		private static LocalDate readLocalDate(Cell cell,
			DataFormatter fmt,
			FormulaEvaluator eval)
		{
			
			if (cell == null)
			{
				return null;
			}
			
			CellType type = cell.getCellType();
			
			if (type == CellType.FORMULA)
			{
				type = eval.evaluateFormulaCell(cell);
			}
			
			// Numeric Excel date?
			if (type == CellType.NUMERIC && DateUtil.isCellDateFormatted(cell))
			{
				return cell.getLocalDateTimeCellValue().toLocalDate();
			}
			
			// Fallback: parse text
			String s = fmt.formatCellValue(cell, eval).trim();
			
			if (s.isEmpty())
			{
				return null;
			}
			
			// Try a few common patterns
			DateTimeFormatter[] patterns = new DateTimeFormatter[]
			{
				DateTimeFormatter.ISO_LOCAL_DATE, // 2025-07-26
				DateTimeFormatter.ofPattern("M/d/uuuu"), // 7/26/2025
				DateTimeFormatter.ofPattern("M/d/uu"), // 7/26/25
				DateTimeFormatter.ofPattern("MM/dd/uuuu"), // 07/26/2025
				DateTimeFormatter.ofPattern("MM/dd/uu"), // 07/26/25
				DateTimeFormatter.ofPattern("d-M-uuuu"), // 26-7-2025
				DateTimeFormatter.ofPattern("d-MMM-uuuu") // 26-Jul-2025
			};
			
			for (DateTimeFormatter f : patterns)
			{
				
				try
				{
					return LocalDate.parse(s, f);
				}
				catch (DateTimeParseException ignore)
				{
				}
				
			}
			
			// Give up
			return null;
			
		}
		
	}
	
}
