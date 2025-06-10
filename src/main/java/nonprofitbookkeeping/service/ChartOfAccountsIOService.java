
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
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import nonprofitbookkeeping.model.ChartOfAccounts;
import nonprofitbookkeeping.model.Account;
import nonprofitbookkeeping.model.AccountSide;
import nonprofitbookkeeping.model.AccountType;

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
		this.mapper = new ObjectMapper()
			.registerModule(new JavaTimeModule()) // For Java 8 Date/Time types
			.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS) // Use ISO-8601 strings
			.enable(SerializationFeature.INDENT_OUTPUT); // Pretty print
			// DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES is disabled by default, which is good for forward compatibility.
	}
	
	/* ------------------------------------------------------------------ */
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
		if (coa == null) {
            throw new NullPointerException("ChartOfAccounts object cannot be null.");
        }
        if (path == null) {
            throw new NullPointerException("Output path cannot be null.");
        }
		Files.createDirectories(path.getParent());
		this.mapper.writeValue(path.toFile(), coa);
	}
	
	/* ------------------------------------------------------------------ */
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
                if (path == null) {
            throw new NullPointerException("Input path cannot be null.");
        }
                return this.mapper.readValue(path.toFile(), ChartOfAccounts.class);
    }

    /* ------------------------------------------------------------------ */
    /**
     * Writes the given {@link ChartOfAccounts} object to an Excel workbook at the
     * specified {@link Path}. The workbook will contain a single sheet named
     * "COA" with columns:
     * <code>Number, Name, Type, Parent, IncreaseSide, OpeningBalance</code>.
     *
     * @param coa  The {@link ChartOfAccounts} to export. Must not be {@code null}.
     * @param path The destination {@link Path} for the workbook. Parent directories
     *             will be created if necessary.
     * @throws IOException if an error occurs while writing the file.
     */
    public void exportToXlsx(ChartOfAccounts coa, Path path) throws IOException
    {
        if (coa == null) {
            throw new NullPointerException("ChartOfAccounts object cannot be null.");
        }
        if (path == null) {
            throw new NullPointerException("Output path cannot be null.");
        }

        Files.createDirectories(path.getParent());
        try (Workbook wb = new XSSFWorkbook(); OutputStream out = Files.newOutputStream(path)) {
            Sheet sheet = wb.createSheet("COA");

            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("Number");
            header.createCell(1).setCellValue("Name");
            header.createCell(2).setCellValue("Type");
            header.createCell(3).setCellValue("Parent");
            header.createCell(4).setCellValue("IncreaseSide");
            header.createCell(5).setCellValue("OpeningBalance");

            int rowIdx = 1;
            for (Account acc : coa.getAccounts()) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(acc.getAccountNumber());
                row.createCell(1).setCellValue(acc.getName());
                row.createCell(2).setCellValue(acc.getAccountType() != null ? acc.getAccountType().name() : "");
                row.createCell(3).setCellValue(acc.getParentAccount() != null ? acc.getParentAccount().getAccountNumber() : "");
                row.createCell(4).setCellValue(acc.getIncreaseSide() != null ? acc.getIncreaseSide().name() : "");
                if (acc.getOpeningBalance() != null) {
                    row.createCell(5).setCellValue(acc.getOpeningBalance().doubleValue());
                } else {
                    row.createCell(5).setCellValue(0);
                }
            }

            wb.write(out);
        }
    }

    /* ------------------------------------------------------------------ */
    /**
     * Reads a {@link ChartOfAccounts} from an Excel workbook at the given path.
     * The expected sheet layout matches that produced by {@link #exportToXlsx}.
     *
     * @param path The path to the workbook to read.
     * @return A populated {@link ChartOfAccounts} instance.
     * @throws IOException if the file cannot be read or parsed.
     */
    public ChartOfAccounts importFromXlsx(Path path) throws IOException
    {
        if (path == null) {
            throw new NullPointerException("Input path cannot be null.");
        }

        ChartOfAccounts coa = new ChartOfAccounts();
        Map<String, Account> accountMap = new HashMap<>();
        Map<String, String> parentNumbers = new HashMap<>();

        try (InputStream in = Files.newInputStream(path); Workbook wb = WorkbookFactory.create(in)) {
            Sheet sheet = wb.getSheetAt(0);
            boolean header = true;
            for (Row row : sheet) {
                if (header) { header = false; continue; }
                Cell numCell = row.getCell(0);
                if (numCell == null) { continue; }
                String number = numCell.toString().trim();
                if (number.isEmpty()) { continue; }

                String name = row.getCell(1) != null ? row.getCell(1).toString() : null;
                String typeStr = row.getCell(2) != null ? row.getCell(2).toString() : null;
                String parentNum = row.getCell(3) != null ? row.getCell(3).toString() : null;
                String incSideStr = row.getCell(4) != null ? row.getCell(4).toString() : null;
                String balStr = row.getCell(5) != null ? row.getCell(5).toString() : null;

                AccountType type = null;
                if (typeStr != null && !typeStr.isBlank()) {
                    try { type = AccountType.valueOf(typeStr.trim()); } catch (Exception ignore) {}
                }

                AccountSide side = null;
                if (incSideStr != null && !incSideStr.isBlank()) {
                    try { side = AccountSide.valueOf(incSideStr.trim()); } catch (Exception ignore) {}
                }

                BigDecimal bal = BigDecimal.ZERO;
                if (balStr != null && !balStr.isBlank()) {
                    try { bal = new BigDecimal(balStr.trim()); } catch (NumberFormatException ignore) {}
                }

                Account acc = new Account(number, name, side);
                acc.setAccountType(type);
                acc.setOpeningBalance(bal);
                accountMap.put(number, acc);
                parentNumbers.put(number, parentNum);
            }
        }

        // Build hierarchy
        for (Map.Entry<String, Account> e : accountMap.entrySet()) {
            String num = e.getKey();
            Account acc = e.getValue();
            String parentNum = parentNumbers.get(num);
            if (parentNum == null || parentNum.isBlank()) {
                coa.addAccount(acc);
            } else {
                Account parent = accountMap.get(parentNum);
                if (parent != null) {
                    coa.addSubAccount(parent, acc);
                } else {
                    coa.addAccount(acc); // fall back to root if parent missing
                }
            }
        }

        return coa;
    }
	
}
