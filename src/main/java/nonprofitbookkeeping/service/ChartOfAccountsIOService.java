
package nonprofitbookkeeping.service;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import nonprofitbookkeeping.model.ChartOfAccounts;
import nonprofitbookkeeping.model.Account;
import nonprofitbookkeeping.model.AccountSide;
import nonprofitbookkeeping.model.AccountType;
import nonprofitbookkeeping.model.supplemental.SupplementalLineKind;

// TODO: Auto-generated Javadoc
/**
 * Utility for persisting a {@link ChartOfAccounts} to / from either a human-readable
 * JSON document or an Excel spreadsheet (.xlsx). The JSON file format is:
 *
 * <pre>{@code
 * {
 *   "_schemaVersion" : 1,
 *   "rootAccounts"   : [ { ... }, { ... } ]
 * }
 * }</pre>
 *
 * <p>The mapper is pre-configured to:</p>
 * <ul>
 *   <li>write ISO-8601 strings for any {@code java.time} values,</li>
 *   <li>indent output for readability,</li>
 *   <li>ignore unknown JSON properties on import (forward compatibility).</li>
 * </ul>
 * This service uses Jackson for JSON processing.
 */
public final class ChartOfAccountsIOService
{
	
	/** Jackson ObjectMapper configured for ChartOfAccounts serialization and deserialization. */
	private final ObjectMapper mapper;
	
	/**
	 * Constructs a new {@code ChartOfAccountsIOService}.
	 * Initializes and configures a Jackson {@link ObjectMapper} with settings for
	 * handling Java Time (JSR-310) types, disabling timestamp writing for dates (uses ISO-8601 strings),
	 * and enabling indented (pretty-printed) JSON output.
	 */
	public ChartOfAccountsIOService()
	{
		this.mapper = new ObjectMapper().registerModule(new JavaTimeModule())
			.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS) // Use ISO-8601 strings
			.enable(SerializationFeature.INDENT_OUTPUT); // Pretty print
	}
	
	/**
	 * Writes the given {@link ChartOfAccounts} object to a JSON file at the specified {@link Path}.
	 * If the parent directories for the path do not exist, they will be created.
	 * The output JSON will be pretty-printed.
	 *
	 * @param coa  The {@link ChartOfAccounts} object to persist. Must not be {@code null}.
	 * @param path The {@link Path} to the target JSON file. This can be an absolute or relative path.
	 *             The file will be overwritten if it already exists.
	 * @throws IOException if an error occurs during directory creation or file writing,
	 *                     or if {@code coa} cannot be serialized to JSON.
	 * @throws NullPointerException if {@code coa} or {@code path} is null.
	 */
	public void exportToJson(ChartOfAccounts coa, Path path) throws IOException
	{
		
		if (coa == null)
		{
			throw new NullPointerException("ChartOfAccounts object cannot be null.");
		}
		
		if (path == null)
		{
			throw new NullPointerException("Output path cannot be null.");
		}
		
		Files.createDirectories(path.getParent());
		this.mapper.writeValue(path.toFile(), coa);
	}
	
	/**
	 * Reads a {@link ChartOfAccounts} object from the JSON file at the specified {@link Path}.
	 * The JSON file is expected to be in the format previously produced by
	 * {@link #exportToJson(ChartOfAccounts, Path)}.
	 *
	 * @param path The {@link Path} to the JSON file to read.
	 * @return A fully populated, mutable {@link ChartOfAccounts} object.
	 * @throws IOException if the file does not exist, cannot be read, or contains invalid JSON
	 *                     that cannot be deserialized into a {@code ChartOfAccounts} object.
	 * @throws NullPointerException if {@code path} is null.
	 */
	public ChartOfAccounts importFromJson(Path path) throws IOException
	{
		
		if (path == null)
		{
			throw new NullPointerException("Input path cannot be null.");
		}
		
		return this.mapper.readValue(path.toFile(), ChartOfAccounts.class);
	}
	
	/**
	 * Writes the given {@link ChartOfAccounts} object to an Excel workbook at the
	 * specified {@link Path}. The workbook will contain a single sheet named
	 * "COA" with columns:
	 * <code>Number, Name, Type, Parent, IncreaseSide, OpeningBalance, SupplementalKinds</code>.
	 *
	 * @param coa  The {@link ChartOfAccounts} to export. Must not be {@code null}.
	 * @param path The destination {@link Path} for the workbook. Parent directories
	 *             will be created if necessary.
	 * @throws IOException if an error occurs while writing the file.
	 */
	public static void exportToXlsx(ChartOfAccounts coa, Path path) throws IOException
	{
		
		if (coa == null)
		{
			throw new NullPointerException("ChartOfAccounts object cannot be null.");
		}
		
		if (path == null)
		{
			throw new NullPointerException("Output path cannot be null.");
		}
		
		Files.createDirectories(path.getParent());
		
		try (Workbook wb = new XSSFWorkbook(); OutputStream out = Files.newOutputStream(path))
		{
			Sheet sheet = wb.createSheet("COA");
			
			Row header = sheet.createRow(0);
			header.createCell(0).setCellValue("Number");
			header.createCell(1).setCellValue("Name");
			header.createCell(2).setCellValue("Type");
			header.createCell(3).setCellValue("Parent");
			header.createCell(4).setCellValue("IncreaseSide");
			header.createCell(5).setCellValue("OpeningBalance");
			header.createCell(6).setCellValue("SupplementalKinds");
			
			int rowIdx = 1;
			
			for (Account acc : coa.getAccounts())
			{
				Row row = sheet.createRow(rowIdx++);
				row.createCell(0).setCellValue(acc.getAccountNumber());
				row.createCell(1).setCellValue(acc.getName());
				row.createCell(2)
					.setCellValue(acc.getAccountType() != null ? acc.getAccountType().name() : "");
				row.createCell(3)
					.setCellValue(acc.getParentAccountId() != null ? acc.getParentAccountId() : "");
				row.createCell(4).setCellValue(
					acc.getIncreaseSide() != null ? acc.getIncreaseSide().name() : "");
				
				if (acc.getOpeningBalance() != null)
				{
					row.createCell(5).setCellValue(acc.getOpeningBalance().doubleValue());
				}
				else
				{
					row.createCell(5).setCellValue(0);
				}
				
				row.createCell(6).setCellValue(
					encodeSupplementalKinds(acc.getSupplementalLineKinds()));
				
			}
			
			wb.write(out);
		}
		
	}
	

	
	
	/**
	 * Reads a {@link ChartOfAccounts} from an Excel workbook at the given path.
	 * The expected sheet layout matches that produced by {@link #exportToXlsx}.
	 *
	 * @param path The path to the workbook to read.
	 * @return A populated {@link ChartOfAccounts} instance.
	 * @throws IOException if the file cannot be read or parsed.
	 */
	public static ChartOfAccounts importFromXlsx(Path path) throws IOException
	{
		
		if (path == null)
		{
			throw new NullPointerException("Input path cannot be null.");
		}
		
		ChartOfAccounts coa = new ChartOfAccounts();
		Map<String, Account> accountMap = new HashMap<>();
		Map<String, String> parentNumbers = new HashMap<>();
		
		try (InputStream in = Files.newInputStream(path); Workbook wb = WorkbookFactory.create(in))
		{
			FormulaEvaluator eval = wb.getCreationHelper().createFormulaEvaluator();
			Sheet sheet = wb.getSheetAt(0);
			boolean header = true;
			
			for (Row row : sheet)
			{
				
				if (header)
				{
					header = false;
					continue;
				}
				
				// Col 0 = account number (must be integer)
				Integer numberInt = getInt(row.getCell(0), eval);
				
				if (numberInt == null)
				{
					continue; // skip blank/invalid
				}
				
				String number = numberInt.toString(); // rest of your model uses String keys
				
				// Col 1 = name
				String name = getString(row.getCell(1), eval);
				
				// Col 2 = type (enum)
				String typeStr = getString(row.getCell(2), eval);
				AccountType type = null;
				
				if (typeStr != null && !typeStr.isBlank())
				{
					
					try
					{
						type = AccountType.valueOf(typeStr.trim());
					}
					catch (Exception ignore)
					{
					}
					
				}
				
				// Col 3 = parent account number (int -> string)
				Integer parentInt = getInt(row.getCell(3), eval);
				String parentNum = parentInt != null ? parentInt.toString() : null;
				
				// Col 4 = normal balance side (enum)
				String incSideStr = getString(row.getCell(4), eval);
				AccountSide side = null;
				
				if (incSideStr != null && !incSideStr.isBlank())
				{
					
					try
					{
						side = AccountSide.valueOf(incSideStr.trim());
					}
					catch (Exception ignore)
					{
					}
					
				}
				
				// Col 5 = opening balance (BigDecimal)
				BigDecimal bal = getBigDecimal(row.getCell(5), eval, BigDecimal.ZERO);
				
				// Col 6 = supplemental kinds (comma-separated)
				String supplementalKinds = getString(row.getCell(6), eval);
				
				Account acc = new Account(number, name, side);
				acc.setAccountType(type);
				acc.setOpeningBalance(bal);
				acc.setSupplementalLineKinds(
					decodeSupplementalKinds(supplementalKinds));
				
				accountMap.put(number, acc);
				parentNumbers.put(number, parentNum);
			}
			
		}
		
		// Build hierarchy
		for (Map.Entry<String, Account> e : accountMap.entrySet())
		{
			String num = e.getKey();
			Account acc = e.getValue();
			String parentNum = parentNumbers.get(num);
			
			if (parentNum == null || parentNum.isBlank())
			{
				coa.addAccount(acc);
			}
			else
			{
				Account parent = accountMap.get(parentNum);
				
				if (parent != null)
				{
					coa.addSubAccount(parent, acc);
				}
				else
				{
					coa.addAccount(acc); // fall back to root if parent missing
				}
				
			}
			
		}
		
		return coa;
	}
	
	/**
	 * Get a string from the cell.
	 *
	 * @param cell the cell
	 * @param eval the eval
	 * @return String Value
	 */
	private static String getString(Cell cell, FormulaEvaluator eval)
	{
		if (cell == null)
			return null;
		
		CellType type = cell.getCellType();
		
		if (type == CellType.FORMULA)
		{
			type = eval.evaluateFormulaCell(cell);
		}
		
		switch(type)
		{
			case STRING:
				return cell.getStringCellValue().trim();
				
			case NUMERIC:
				// If you want pure numeric rejected, 
				// return null here instead
				return BigDecimal.valueOf(cell.getNumericCellValue()).toPlainString();
				
			case BLANK:
				return null;
				
			default:
				return cell.toString().trim(); // last resort
		}
		
	}
	
	/**
	 * Get a Big Decimal from the cell.
	 *
	 * @param cell the cell
	 * @param eval the eval
	 * @param defaultVal the default val
	 * @return Big Decimal value
	 */
	private static BigDecimal getBigDecimal(Cell cell, FormulaEvaluator eval, BigDecimal defaultVal)
	{
		
		if (cell == null)
		{
			return defaultVal;
		}
		
		CellType type = cell.getCellType();
		
		if (type == CellType.FORMULA)
		{
			type = eval.evaluateFormulaCell(cell);
		}
		
		switch(type)
		{
			case NUMERIC:
				return BigDecimal.valueOf(cell.getNumericCellValue());
				
			case STRING:
				String s = cell.getStringCellValue().trim();
				if (s.isEmpty())
					return defaultVal;
				try
				{
					return new BigDecimal(s.replace(",", ""));
				}
				catch (NumberFormatException ex)
				{
					return defaultVal;
				}
			case BLANK:
				return defaultVal;
				
			default:
				return defaultVal;
		}
		
	}
	
	/**
	 * Read an integer.
	 *
	 * @param cell the cell
	 * @param eval the eval
	 * @return Integer Value
	 */
	private static Integer getInt(Cell cell, FormulaEvaluator eval)
	{
		
		if (cell == null)
		{
			return null; // or throw
		}
		
		CellType type = cell.getCellType();
		
		if (type == CellType.FORMULA)
		{
			type = eval.evaluateFormulaCell(cell);
		}
		
		switch(type)
		{
			case NUMERIC:
			{
				double d = cell.getNumericCellValue();
				
				if (Double.isNaN(d) || Double.isInfinite(d))
				{
					throw new IllegalArgumentException("Numeric cell is NaN/Inf");
				}
				
				if (Math.rint(d) != d)
				{
					throw new IllegalArgumentException("Value is not an integer: " + d);
				}
				
				if (d < Integer.MIN_VALUE || d > Integer.MAX_VALUE)
				{
					throw new IllegalArgumentException("Out of int range: " + d);
				}
				
				return (int) d;
			}
			
			case STRING:
			{
				String s = cell.getStringCellValue().trim();
				
				if (s.isEmpty())
				{
					return null; // or throw
				}
				
				// strip commas/spaces if your sheet has them
				s = s.replace(",", "");
				return Integer.valueOf(s);
			}
			
			case BLANK:
				return null; // or throw
				
			default:
				throw new IllegalArgumentException("Unsupported cell type: " + type);
		}
		
	}

	/**
	 * Encode supplemental kinds.
	 *
	 * @param kinds the kinds
	 * @return the string
	 */
	private static String encodeSupplementalKinds(
		java.util.List<SupplementalLineKind> kinds)
	{
		
		if (kinds == null || kinds.isEmpty())
		{
			return "";
		}
		
		return kinds.stream()
			.filter(kind -> kind != null)
			.map(SupplementalLineKind::name)
			.sorted()
			.reduce((a, b) -> a + "," + b)
			.orElse("");
	}

	/**
	 * Decode supplemental kinds.
	 *
	 * @param value the value
	 * @return the java.util. list
	 */
	private static java.util.List<SupplementalLineKind> decodeSupplementalKinds(
		String value)
	{
		
		if (value == null || value.isBlank())
		{
			return java.util.List.of();
		}
		
		String[] parts = value.split(",");
		java.util.List<SupplementalLineKind> kinds = new java.util.ArrayList<>();
		
		for (String part : parts)
		{
			String trimmed = part.trim();
			
			if (trimmed.isEmpty())
			{
				continue;
			}
			
			try
			{
				kinds.add(SupplementalLineKind.valueOf(trimmed));
			}
			catch (IllegalArgumentException ex)
			{
				// ignore unknown values
			}
			
		}
		
		return kinds;
	}
	
}
