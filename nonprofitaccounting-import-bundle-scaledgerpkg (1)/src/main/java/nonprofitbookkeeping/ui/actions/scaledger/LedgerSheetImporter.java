package nonprofitbookkeeping.ui.actions.scaledger;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.EncryptedDocumentException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Reads one ledger sheet (e.g. "Ledger_Q4") from the workbook (.xlsx/.xlsm),
 * and converts it into a LedgerQuarter of LedgerRow objects.
 *
 * Assumptions:
 *  Row 10  : headers
 *  Rows 11+: data rows
 *
 *  Core descriptive columns:
 *    DATE, CHECK #, Clear Bank, TO/FROM,
 *    MEMO/NOTES, BUDGET TRACKING
 *
 *  Then four split groups:
 *    Group 1: Amount..Fund        (M-Q)
 *    Group 2: Amount..Fund        (R-V)
 *    Group 3: Amount..Fund        (X-AB)
 *    Group 4: Amount..Fund        (AC-AG)
 *
 * Each group is 5 columns:
 *    Amount
 *    Asset/Liability Account
 *    Income Category
 *    Expense Category
 *    General or Dedicated Fund
 *
 * We preserve literal strings. We also attach canonicalCategory
 * via a ChartTranslationMap if provided.
 */
public class LedgerSheetImporter
{
    /**
     * Import the given sheet of the workbook (.xlsx or .xlsm).
     *
     * @param workbookPath path to Excel file
     * @param sheetName    e.g. "Ledger_Q4"
     * @param translation  chart-of-accounts translator (may be null)
     */
    public LedgerQuarter importQuarter(Path workbookPath,
                                       String sheetName,
                                       ChartTranslationMap translation)
        throws IOException
    {
        try (InputStream in = Files.newInputStream(workbookPath);
             Workbook wb = WorkbookFactory.create(in))
        {
            Sheet sheet = wb.getSheet(sheetName);
            if (sheet == null)
            {
                throw new IOException("Sheet not found: " + sheetName);
            }

            int headerRowIdx = findHeaderRow(sheet);
            if (headerRowIdx < 0)
            {
                throw new IOException("Could not locate header row in " + sheetName);
            }

            ColumnIndex cols = mapColumns(sheet.getRow(headerRowIdx));

            LedgerQuarter quarter = new LedgerQuarter(sheetName);

            int startDataRow = headerRowIdx + 1;
            int lastRow = sheet.getLastRowNum();

            for (int r = startDataRow; r <= lastRow; r++)
            {
                Row poiRow = sheet.getRow(r);
                if (poiRow == null)
                {
                    continue;
                }

                LedgerRow row = new LedgerRow();
                row.setSheetRowNumber(r);

                // Core descriptive columns
                row.setDate(CellUtil.readDate(poiRow, cols.colDate));
                row.setCheckNumber(CellUtil.readString(poiRow, cols.colCheckNum));
                row.setClearedBankTag(CellUtil.readString(poiRow, cols.colClearBank));
                row.setToFrom(CellUtil.readString(poiRow, cols.colToFrom));
                row.setMemo(CellUtil.readString(poiRow, cols.colMemo));
                row.setBudgetNotes(CellUtil.readString(poiRow, cols.colBudgetNotes));

                // Up to 4 split groups
                addSplitFromGroup(poiRow, cols.group1, row, translation);
                addSplitFromGroup(poiRow, cols.group2, row, translation);
                addSplitFromGroup(poiRow, cols.group3, row, translation);
                addSplitFromGroup(poiRow, cols.group4, row, translation);

                if (!row.isEffectivelyBlank())
                {
                    quarter.addRow(row);
                }
            }

            return quarter;
        }
        catch (EncryptedDocumentException | InvalidFormatException e)
        {
            throw new IOException("Invalid Excel format: " + e.getMessage(), e);
        }
    }

    /**
     * Scan first ~30 rows for a row that looks like the header row,
     * i.e. includes "DATE" and "TO/FROM".
     */
    private int findHeaderRow(Sheet sheet)
    {
        int scanMax = Math.min(sheet.getLastRowNum(), 30);

        for (int r = sheet.getFirstRowNum(); r <= scanMax; r++)
        {
            Row row = sheet.getRow(r);
            if (row == null)
            {
                continue;
            }

            boolean sawDate = false;
            boolean sawToFrom = false;

            for (Cell c : row)
            {
                String v = CellUtil.readString(row, c.getColumnIndex());
                if (v == null)
                {
                    continue;
                }
                String up = v.trim().toUpperCase();

                if (up.equals("DATE"))
                {
                    sawDate = true;
                }
                if (up.startsWith("TO/FROM"))
                {
                    sawToFrom = true;
                }
            }

            if (sawDate && sawToFrom)
            {
                return r;
            }
        }

        return -1;
    }

    /**
     * Build a ColumnIndex by reading the header row.
     * We match headers by their visible text.
     */
    private ColumnIndex mapColumns(Row headerRow)
    {
        ColumnIndex idx = new ColumnIndex();

        for (Cell c : headerRow)
        {
            String headerText = CellUtil.readString(headerRow, c.getColumnIndex());
            if (headerText == null)
            {
                continue;
            }

            String norm = headerText.trim().toUpperCase();

            if (norm.equals("DATE"))
            {
                idx.colDate = c.getColumnIndex();
            }
            else if (norm.equals("CHECK #") || norm.equals("CHECK#"))
            {
                idx.colCheckNum = c.getColumnIndex();
            }
            else if (norm.startsWith("CLEAR BANK"))
            {
                idx.colClearBank = c.getColumnIndex();
            }
            else if (norm.startsWith("TO/FROM"))
            {
                idx.colToFrom = c.getColumnIndex();
            }
            else if (norm.startsWith("MEMO/NOTES"))
            {
                idx.colMemo = c.getColumnIndex();
            }
            else if (norm.startsWith("BUDGET TRACKING"))
            {
                idx.colBudgetNotes = c.getColumnIndex();
            }
            else if (norm.equals("NET TOTAL"))
            {
                idx.colNetTotal = c.getColumnIndex();
            }
        }

        // Detect the four split groups by scanning for repeating headers:
        // "Amount", "Asset/Liability Account", "Income Category",
        // "Expense Category", "General or Dedicated Fund".
        idx.group1 = detectSplitGroup(headerRow, "AMOUNT", 0);
        idx.group2 = detectSplitGroup(headerRow, "AMOUNT",
            idx.group1 != null ? idx.group1.amountCol + 1 : 0);
        idx.group3 = detectSplitGroup(headerRow, "AMOUNT",
            idx.group2 != null ? idx.group2.amountCol + 1 : 0);
        idx.group4 = detectSplitGroup(headerRow, "AMOUNT",
            idx.group3 != null ? idx.group3.amountCol + 1 : 0);

        return idx;
    }

    /**
     * Try to locate a split group starting at/after startSearchCol.
     * A group looks like 5 consecutive headers:
     *
     *   AMOUNT
     *   Asset/Liability Account
     *   Income Category
     *   Expense Category
     *   General or Dedicated Fund
     */
    private SplitGroup detectSplitGroup(Row headerRow,
                                        String firstHeaderMustMatch,
                                        int startSearchCol)
    {
        int maxCol = headerRow.getLastCellNum(); // lastCellNum is 1+lastIndex

        for (int c = startSearchCol; c < maxCol; c++)
        {
            String h0 = CellUtil.readString(headerRow, c);
            if (h0 == null)
            {
                continue;
            }

            if (!h0.trim().equalsIgnoreCase(firstHeaderMustMatch))
            {
                continue;
            }

            String h1 = CellUtil.readString(headerRow, c + 1);
            String h2 = CellUtil.readString(headerRow, c + 2);
            String h3 = CellUtil.readString(headerRow, c + 3);
            String h4 = CellUtil.readString(headerRow, c + 4);

            if (h1 == null || h2 == null || h3 == null || h4 == null)
            {
                continue;
            }

            String u1 = h1.trim().toUpperCase();
            String u2 = h2.trim().toUpperCase();
            String u3 = h3.trim().toUpperCase();
            String u4 = h4.trim().toUpperCase();

            boolean looksRight =
                u1.startsWith("ASSET/LIABILITY ACCOUNT") &&
                u2.startsWith("INCOME CATEGORY") &&
                u3.startsWith("EXPENSE CATEGORY") &&
                u4.startsWith("GENERAL OR DEDICATED FUND");

            if (looksRight)
            {
                SplitGroup g = new SplitGroup();
                g.amountCol = c;
                g.assetLiabCol = c + 1;
                g.incomeCatCol = c + 2;
                g.expenseCatCol = c + 3;
                g.fundCol = c + 4;
                return g;
            }
        }

        return null;
    }

    /**
     * Read one group's split from a row and attach it to the LedgerRow.
     * Also apply translation map to get canonicalCategory.
     */
    private void addSplitFromGroup(Row poiRow,
                                   SplitGroup group,
                                   LedgerRow outRow,
                                   ChartTranslationMap translation)
    {
        if (group == null)
        {
            return;
        }

        LedgerSplit split = new LedgerSplit();

        // amount
        split.setAmount(CellUtil.readAmount(poiRow, group.amountCol));

        // category columns
        split.setAssetLiabilityAccount(
            CellUtil.readString(poiRow, group.assetLiabCol));
        split.setIncomeCategory(
            CellUtil.readString(poiRow, group.incomeCatCol));
        split.setExpenseCategory(
            CellUtil.readString(poiRow, group.expenseCatCol));

        // fund
        split.setFund(
            CellUtil.readString(poiRow, group.fundCol));

        // canonical
        String rawPrimary = split.getPrimaryRawCategory();
        if (translation != null)
        {
            String canon = translation.translate(rawPrimary);
            split.setCanonicalCategory(canon);
        }
        else
        {
            split.setCanonicalCategory(null);
        }

        // Only add if not empty
        if (!split.isEmpty())
        {
            outRow.addSplit(split);
        }
    }

    /**
     * Helper struct for top-level columns.
     */
    private static class ColumnIndex
    {
        int colDate = -1;
        int colCheckNum = -1;
        int colClearBank = -1;
        int colToFrom = -1;
        int colMemo = -1;
        int colBudgetNotes = -1;
        int colNetTotal = -1;

        SplitGroup group1;
        SplitGroup group2;
        SplitGroup group3;
        SplitGroup group4;
    }

    /**
     * Helper struct for a 5-column repeating split group.
     */
    private static class SplitGroup
    {
        int amountCol;
        int assetLiabCol;
        int incomeCatCol;
        int expenseCatCol;
        int fundCol;
    }
}
