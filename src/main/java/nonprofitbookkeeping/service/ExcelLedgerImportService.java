package nonprofitbookkeeping.service;

import nonprofitbookkeeping.model.impex.ExcelLedgerRow;
import nonprofitbookkeeping.model.impex.ExcelLedgerRow.Allocation;
import org.apache.poi.ss.usermodel.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
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
public class ExcelLedgerImportService {

    /**
     * Reads the first worksheet of the given Excel file and converts the rows
     * into {@link ExcelLedgerRow} objects.
     *
     * @param file Excel workbook to read (.xlsx or .xlsm).
     * @return list of parsed rows in order of appearance.
     * @throws IOException if the file cannot be read or parsed.
     */
    public static List<ExcelLedgerRow> importSpreadsheet(File file) throws IOException {
        if (file == null || !file.exists()) {
            throw new IOException("Input Excel file does not exist: " + file);
        }

        try (FileInputStream fis = new FileInputStream(file);
             Workbook workbook = WorkbookFactory.create(fis)) {
            Sheet sheet = workbook.getSheetAt(0);
            if (sheet == null) {
                return Collections.emptyList();
            }

            HeaderMapping mapping = buildHeaderMapping(sheet.getRow(sheet.getFirstRowNum()));
            List<ExcelLedgerRow> results = new ArrayList<>();
            int firstRow = sheet.getFirstRowNum() + 1;
            int lastRow = sheet.getLastRowNum();
            for (int r = firstRow; r <= lastRow; r++) {
                Row row = sheet.getRow(r);
                if (row == null) {
                    continue;
                }
                ExcelLedgerRow record = parseRow(row, mapping);
                // Skip completely blank rows
                if (!record.getAllocations().isEmpty() || record.getDate() != null) {
                    results.add(record);
                }
            }
            return results;
        }
    }

    /* ------------------------------------------------------------- */
    private static class HeaderMapping {
        int date = -1;
        int check = -1;
        int clearBank = -1;
        int toFrom = -1;
        int memo = -1;
        int budget = -1;
        GroupColumns[] groups = new GroupColumns[4];
        HeaderMapping() {
            for (int i = 0; i < groups.length; i++) {
                groups[i] = new GroupColumns();
            }
        }
    }

    private static class GroupColumns {
        int amount = -1;
        int asset = -1;
        int income = -1;
        int expense = -1;
        int fund = -1;
    }

    private static HeaderMapping buildHeaderMapping(Row header) {
        HeaderMapping map = new HeaderMapping();
        if (header == null) {
            return map;
        }
        DataFormatter fmt = new DataFormatter();
        for (Cell cell : header) {
            String text = fmt.formatCellValue(cell).toLowerCase();
            int idx = cell.getColumnIndex();
            if (text.contains("date")) {
                map.date = idx;
            } else if (text.contains("check")) {
                map.check = idx;
            } else if (text.contains("clear") && text.contains("bank")) {
                map.clearBank = idx;
            } else if (text.contains("to") && text.contains("from")) {
                map.toFrom = idx;
            } else if (text.contains("memo") || text.contains("note")) {
                map.memo = idx;
            } else if (text.contains("budget")) {
                map.budget = idx;
            } else if (text.contains("amount")) {
                group(map.groups, text).amount = idx;
            } else if (text.contains("asset") || text.contains("liability")) {
                group(map.groups, text).asset = idx;
            } else if (text.contains("income")) {
                group(map.groups, text).income = idx;
            } else if (text.contains("expense")) {
                group(map.groups, text).expense = idx;
            } else if (text.contains("fund")) {
                group(map.groups, text).fund = idx;
            }
        }
        return map;
    }

    private static GroupColumns group(GroupColumns[] groups, String headerText) {
        int index = extractGroupIndex(headerText);
        if (index < 0 || index >= groups.length) {
            index = firstAvailableGroup(groups);
        }
        return groups[index];
    }

    private static int firstAvailableGroup(GroupColumns[] groups) {
        for (int i = 0; i < groups.length; i++) {
            // choose first group that has at least one unset column
            GroupColumns g = groups[i];
            if (g.amount == -1 && g.asset == -1 && g.income == -1 && g.expense == -1 && g.fund == -1) {
                return i;
            }
        }
        return 0;
    }

    private static int extractGroupIndex(String text) {
        for (char c : text.toCharArray()) {
            if (Character.isDigit(c)) {
                int idx = Character.digit(c, 10) - 1; // digits are 1-based
                if (idx >= 0) {
                    return idx;
                }
            }
        }
        return -1;
    }

    private static ExcelLedgerRow parseRow(Row row, HeaderMapping map) {
        DataFormatter fmt = new DataFormatter();
        ExcelLedgerRow out = new ExcelLedgerRow();
        if (map.date >= 0) {
            out.setDate(readDate(row.getCell(map.date)));
        }
        if (map.check >= 0) {
            out.setCheckNumber(fmt.formatCellValue(row.getCell(map.check)).trim());
        }
        if (map.clearBank >= 0) {
            out.setClearBank(fmt.formatCellValue(row.getCell(map.clearBank)).trim());
        }
        if (map.toFrom >= 0) {
            out.setToFrom(fmt.formatCellValue(row.getCell(map.toFrom)).trim());
        }
        if (map.memo >= 0) {
            out.setMemoNotes(fmt.formatCellValue(row.getCell(map.memo)).trim());
        }
        if (map.budget >= 0) {
            out.setBudgetTracking(fmt.formatCellValue(row.getCell(map.budget)).trim());
        }

        for (GroupColumns g : map.groups) {
            Allocation alloc = readAllocation(row, g, fmt);
            if (alloc != null) {
                out.getAllocations().add(alloc);
            }
        }
        return out;
    }

    private static Allocation readAllocation(Row row, GroupColumns g, DataFormatter fmt) {
        if (g.amount < 0 && g.asset < 0 && g.income < 0 && g.expense < 0 && g.fund < 0) {
            return null;
        }
        Allocation a = new Allocation();
        if (g.amount >= 0) {
            String val = fmt.formatCellValue(row.getCell(g.amount)).trim();
            if (val.isBlank()) {
                return null; // no amount means skip this allocation
            }
            try {
                a.setAmount(new BigDecimal(val.replace(",", "")));
            } catch (NumberFormatException e) {
                // treat as zero if not parseable
                a.setAmount(BigDecimal.ZERO);
            }
        }
        if (g.asset >= 0) {
            a.setAssetLiabilityAccount(fmt.formatCellValue(row.getCell(g.asset)).trim());
        }
        if (g.income >= 0) {
            a.setIncomeCategory(fmt.formatCellValue(row.getCell(g.income)).trim());
        }
        if (g.expense >= 0) {
            a.setExpenseCategory(fmt.formatCellValue(row.getCell(g.expense)).trim());
        }
        if (g.fund >= 0) {
            a.setFund(fmt.formatCellValue(row.getCell(g.fund)).trim());
        }
        return a;
    }

    private static LocalDate readDate(Cell cell) {
        if (cell == null) {
            return null;
        }
        if (cell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(cell)) {
            return cell.getDateCellValue().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        }
        String txt = new DataFormatter().formatCellValue(cell).trim();
        if (txt.isBlank()) {
            return null;
        }
        try {
            return LocalDate.parse(txt);
        } catch (Exception e) {
            return null;
        }
    }
}
