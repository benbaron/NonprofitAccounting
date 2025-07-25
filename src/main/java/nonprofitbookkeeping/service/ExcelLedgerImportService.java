
package nonprofitbookkeeping.service;

import nonprofitbookkeeping.model.impex.ExcelLedgerRow;
import nonprofitbookkeeping.model.impex.ExcelLedgerRow.Allocation;
import org.apache.poi.ss.usermodel.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

/**
 * Utility service for importing a specialized Excel ledger format.
 * <p>
 * The first row of the sheet is used as a header. Columns are matched
 * case-insensitively and by partial text to the expected fields. Any
 * unmatched columns are ignored. Up to four allocation groups can be
 * read (Amount, Asset/Liability Account, Income Category, Expense
 * Category, General or Dedicated Fund).
 */
public class ExcelLedgerImportService
{
	
	/**
	 * Reads the first worksheet of the given Excel file and converts the rows
	 * into {@link ExcelLedgerRow} objects.
	 *
	 * @param file Excel workbook to read (.xlsx or .xlsm).
	 * @return list of parsed rows in order of appearance.
	 * @throws IOException if the file cannot be read or parsed.
	 */
	public static List<ExcelLedgerRow> importSpreadsheet(File file) throws IOException
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
			
			// Build the header mapping (column assignments) (assuming it lies in column
			// one)
			HeaderMapping mapping = buildHeaderMapping(sheet.getRow(sheet.getFirstRowNum()));
			
			// Ingest the body rows
			List<ExcelLedgerRow> results = new ArrayList<>();
			int firstRow = sheet.getFirstRowNum() + 1;
			int lastRow = sheet.getLastRowNum();
			
			DataFormatter formatter = new DataFormatter();
			FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();
			
                        for (int r = firstRow; r <= lastRow; r++)
                        {
                                System.out.println("\n\n---------------- Row number " + r + " ------------ ");
                                Row row = sheet.getRow(r);

                                if (row == null)
                                {
                                        continue;
                                }
                                RowReader reader = new RowReader(row, r, formatter, evaluator);
                                java.io.StringWriter sw = new java.io.StringWriter();
                                printRow(row, formatter, evaluator, sw);
                                System.out.println(sw.toString());

                                // Parse a body row
                                ExcelLedgerRow record = parseRow(reader, mapping);
				
				// Skip completely blank rows
				if (!record.getAllocations().isEmpty() || record.getDate() != null)
				{
					results.add(record);
				}
				
			}
			
			return results;
		}
		
	}
	
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
	            Cell cell = row.getCell(i, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
	            String value = (cell == null) ? "" : formatter.formatCellValue(cell, evaluator);

	            out.append(value).append('\t');
	        }
	        out.append('\n');
	    }
	    catch (IOException e)
	    {
	        throw new UncheckedIOException(e);
	    }
	}

	/* ------------------------------------------------------------- */
	private static class HeaderMapping
	{
		int date = -1;
		int check = -1;
		int clearBank = -1;
		int toFrom = -1;
		int memo = -1;
		int budget = -1;
		GroupColumns[] groups = new GroupColumns[4];
		
		HeaderMapping()
		{
			
			for (int i = 0; i < this.groups.length; i++)
			{
				this.groups[i] = new GroupColumns();
			}
			
		}

		/**
		 * Override @see java.lang.Object#toString() 
		 */
		@Override public String toString()
		{
			StringBuilder builder = new StringBuilder();
			builder.append("HeaderMapping [date=");
			builder.append(date);
			builder.append(", check=");
			builder.append(check);
			builder.append(", clearBank=");
			builder.append(clearBank);
			builder.append(", toFrom=");
			builder.append(toFrom);
			builder.append(", memo=");
			builder.append(memo);
			builder.append(", budget=");
			builder.append(budget);
			builder.append(", groups=");
			builder.append(groups);
			builder.append("]");
			return builder.toString();
		}
		
	}
	
	/**
	 * 
	 */
    private static class GroupColumns
    {
        int amount = -1;
        int asset = -1;
        int income = -1;
        int expense = -1;
        int fund = -1;
		/**
		 * Override @see java.lang.Object#toString() 
		 */
		@Override public String toString()
		{
			StringBuilder builder = new StringBuilder();
			builder.append("GroupColumns [amount=");
			builder.append(amount);
			builder.append(", asset=");
			builder.append(asset);
			builder.append(", income=");
			builder.append(income);
			builder.append(", expense=");
			builder.append(expense);
			builder.append(", fund=");
			builder.append(fund);
			builder.append("]");
			return builder.toString();
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
        final FormulaEvaluator evaluator;

        RowReader(Row row, int index, DataFormatter fmt, FormulaEvaluator evaluator)
        {
            this.row = row;
            this.index = index;
            this.fmt = fmt;
            this.evaluator = evaluator;
        }

        String text(int column)
        {
            if (column < 0)
            {
                return "";
            }

            Cell cell = row.getCell(column, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
            String val = (cell == null) ? "" : fmt.formatCellValue(cell, evaluator);
            final String trimmed = val.trim();
            System.out.printf("R%dC%d='%s'%n", index, column, trimmed);
            return trimmed;
        }

        LocalDate date(int column)
        {
            if (column < 0)
            {
                return null;
            }
            LocalDate d = readDate(row.getCell(column));
            System.out.printf("R%dC%d[date]=%s%n", index, column, d);
            return d;
        }
    }
	
	/**
	 * Builds the header mapping by parsing keywords.
	 * @param header row structure
	 * 
	 * @return Header map structure
	 */
	private static HeaderMapping buildHeaderMapping(Row header)
	{
		HeaderMapping map = new HeaderMapping();
		
		if (header == null)
		{
			return map;
		}
		
		DataFormatter fmt = new DataFormatter();
		
		// Search for keywords
		for (Cell cell : header)
		{
			String text = fmt.formatCellValue(cell).toLowerCase();
			int idx = cell.getColumnIndex();
			
			if (text.contains("date"))
			{
				map.date = idx;
			}
			else if (text.contains("check"))
			{
				map.check = idx;
			}
			else if (text.contains("clear") && text.contains("bank"))
			{
				map.clearBank = idx;
			}
			else if (text.contains("to") && text.contains("from"))
			{
				map.toFrom = idx;
			}
			else if (text.contains("memo") || text.contains("note"))
			{
				map.memo = idx;
			}
			else if (text.contains("budget"))
			{
				map.budget = idx;
			}
			
			
			else if (text.contains("amount"))
			{
				group(map.groups, text).amount = idx;
			}
			else if (text.contains("asset") || text.contains("liability"))
			{
				group(map.groups, text).asset = idx;
			}
			else if (text.contains("income"))
			{
				group(map.groups, text).income = idx;
			}
			else if (text.contains("expense"))
			{
				group(map.groups, text).expense = idx;
			}
			else if (text.contains("fund"))
			{
				group(map.groups, text).fund = idx;
			}
			
		}
                System.out.println("map: " + map);
                return map;
        }
	
	/**
	 * Determines the group to use
	 * 
	 * @param groups array
	 * @param headerText
	 * @return Group Columns structure
	 */
	private static GroupColumns group(GroupColumns[] groups, String headerText)
	{
		// look for the group index in the column def i.e.
		// Amount 1
		// otherwise, take the first available group
		int index = extractGroupIndex(headerText);
		
		if (index < 0 || index >= groups.length)
		{
			index = firstAvailableGroup(groups);
		}
		
		return groups[index];
	}
	
	/**
	 * Finds the first available group to fill
	 * 
	 * @param groups 
	 * 
	 * @return group index found
	 */
	private static int firstAvailableGroup(GroupColumns[] groups)
	{
		
		for (int i = 0; i < groups.length; i++)
		{
			// choose first group that has at least one unset column
			GroupColumns g = groups[i];
			
			if (g.amount == -1 && g.asset == -1 && 
				g.income == -1 && g.expense == -1 &&
				g.fund == -1)
			{
				return i;
			}
			
		}
		
		return 0;
	}
	
	/**
	 * Extracts the group index - not sure why
	 * @param text
	 * @return
	 */
	private static int extractGroupIndex(String text)
	{
		
		for (char c : text.toCharArray())
		{
			
			if (Character.isDigit(c))
			{
				int idx = Character.digit(c, 10) - 1; // digits are 1-based
				
				if (idx >= 0)
				{
					return idx;
				}
				
			}
			
		}
		
		return -1;
	}
	
	/**
	 * Parses a ledger row using the mapping into the internal 
	 * structure.
	 * 
	 * @param row
	 * @param map
	 * @return
	 */
        private static ExcelLedgerRow parseRow(RowReader reader, HeaderMapping map)
        {
            ExcelLedgerRow out = new ExcelLedgerRow();

            if (map.date >= 0)
            {
                LocalDate date = reader.date(map.date);
                System.out.println("date = " + date);
                out.setDate(date);
            }

            if (map.check >= 0)
            {
                String checkNumber = reader.text(map.check);
                System.out.println("checkNumber = " + checkNumber);
                out.setCheckNumber(checkNumber);
            }

            if (map.clearBank >= 0)
            {
                String clearBank = reader.text(map.clearBank);
                System.out.println("clearBank = " + clearBank);
                out.setClearBank(clearBank);
            }

            if (map.toFrom >= 0)
            {
                String toFrom = reader.text(map.toFrom);
                System.out.println("toFrom = " + toFrom);
                out.setToFrom(toFrom);
            }

            if (map.memo >= 0)
            {
                String memoNotes = reader.text(map.memo);
                System.out.println("memoNotes = " + memoNotes);
                out.setMemoNotes(memoNotes);
            }

            if (map.budget >= 0)
            {
                String budgetTracking = reader.text(map.budget);
                System.out.println("budgetTracking = " + budgetTracking);
                out.setBudgetTracking(budgetTracking);
            }

            // Parse the group columns
            for (GroupColumns g : map.groups)
            {
                Allocation alloc = readAllocation(reader, g);

                if (alloc != null)
                {
                    // Uncomment if you also want to print allocations:
                    System.out.println("allocation = " + alloc);
                    out.getAllocations().add(alloc);
	        }
	    }

	    return out;
	}

	
	/**
	 * Reads the group columns into the allocation internal data structure
	 *  
	 * @param row
	 * @param g
	 * @param fmt
	 * 
	 * @return
	 */
        private static Allocation readAllocation(RowReader reader,
                                                 GroupColumns g)
        {
            if (g.amount < 0 && g.asset < 0 && g.income < 0 && g.expense < 0 && g.fund < 0)
            {
                return null;
            }

            Allocation a = new Allocation();

            if (g.amount >= 0)
            {
                String amountRaw = reader.text(g.amount);
                System.out.println("amount(raw) = " + amountRaw);

	        if (amountRaw.isBlank())
	        {
                    System.out.println("amount(raw) is blank → skipping allocation");
	            return null; // no amount means skip this allocation
	        }

	        try
	        {
                    BigDecimal amount = new BigDecimal(amountRaw.replace(",", ""));
                    System.out.println("amount(parsed) = " + amount);
	            a.setAmount(amount);
	        }
	        catch (NumberFormatException e)
	        {
                    System.out.println("amount(parse error) → treating as 0");
	            a.setAmount(BigDecimal.ZERO);
	        }
	    }

            if (g.asset >= 0)
            {
                String asset = reader.text(g.asset);
                System.out.println("assetLiabilityAccount = " + asset);
                a.setAssetLiabilityAccount(asset);
            }

            if (g.income >= 0)
            {
                String income = reader.text(g.income);
                System.out.println("incomeCategory = " + income);
                a.setIncomeCategory(income);
            }

            if (g.expense >= 0)
            {
                String expense = reader.text(g.expense);
                System.out.println("expenseCategory = " + expense);
                a.setExpenseCategory(expense);
            }

            if (g.fund >= 0)
            {
                String fund = reader.text(g.fund);
                System.out.println("fund = " + fund);
                a.setFund(fund);
            }

	    return a;
	}

	
	/**
	 * Parses the date field into a LocalDate
	 * 
	 * @param cell
	 * @return the date
	 */
	private static LocalDate readDate(Cell cell)
	{
		
		if (cell == null)
		{
			return null;
		}
		
		if (cell.getCellType() == CellType.NUMERIC && 
			DateUtil.isCellDateFormatted(cell))
		{
			return cell.getDateCellValue()
				.toInstant()
				.atZone(ZoneId.systemDefault())
				.toLocalDate();
		}
		
		String txt = new DataFormatter().formatCellValue(cell).trim();
		
		if (txt.isBlank())
		{
			return null;
		}
		
		try
		{
			return LocalDate.parse(txt);
		}
		catch (Exception e)
		{
			return null;
		}
		
	}
	
	/**
	 * Determines the account name associated with an allocation. The
	 * allocation may specify an asset/liability account, an income
	 * category, or an expense category. The first non-blank value is
	 * returned.
	 *
	 * @param alloc The {@link Allocation} to inspect.
	 * @return The account or category name, or {@code null} if none is set.
	 */
	public static String determineAccountName(Allocation alloc)
	{
		
		if (alloc == null)
		{
			return null;
		}
		
		if (alloc.getAssetLiabilityAccount() != null && 
			!alloc.getAssetLiabilityAccount().isBlank())
		{
			return alloc.getAssetLiabilityAccount();
		}
		
		if (alloc.getIncomeCategory() != null && 
			!alloc.getIncomeCategory().isBlank())
		{
			return alloc.getIncomeCategory();
		}
		
		if (alloc.getExpenseCategory() != null && 
			!alloc.getExpenseCategory().isBlank())
		{
			return alloc.getExpenseCategory();
		}
		
		return null;
	}
	
}
