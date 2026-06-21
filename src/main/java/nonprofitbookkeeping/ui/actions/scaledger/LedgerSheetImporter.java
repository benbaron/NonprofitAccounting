/*
 * 
 */
package nonprofitbookkeeping.ui.actions.scaledger;

import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.eventusermodel.XSSFReader;
import org.apache.poi.xssf.eventusermodel.XSSFSheetXMLHandler;
import org.apache.poi.xssf.model.SharedStrings;
import org.apache.poi.xssf.model.StylesTable;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;

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
    private static final int MAX_STREAMING_DATA_ROWS = 1000;
    private static final int HEADER_SCAN_MAX_ROWS = 30;

    /**
     * Import the given sheet of the workbook (.xlsx or .xlsm).
     *
     * @param workbookPath path to Excel file
     * @param sheetName    e.g. "Ledger_Q4"
     * @param translation  chart-of-accounts translator (may be null)
     * @return the ledger quarter
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public LedgerQuarter importQuarter(Path workbookPath,
                                       String sheetName,
                                       ChartTranslationMap translation)
        throws IOException
    {
        if (isStreamingWorkbook(workbookPath))
        {
            return importQuarterStreamingXlsx(workbookPath, sheetName, translation);
        }

        return importQuarterInMemory(workbookPath, sheetName, translation);
    }

    private LedgerQuarter importQuarterInMemory(Path workbookPath,
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
        catch (EncryptedDocumentException e)
        {
            throw new IOException("Invalid Excel format: " + e.getMessage(), e);
        }
    }

    /**
     * Import quarter streaming xlsx.
     *
     * @param workbookPath the workbook path
     * @param sheetName the sheet name
     * @param translation the translation
     * @return the ledger quarter
     * @throws IOException Signals that an I/O exception has occurred.
     */
    private LedgerQuarter importQuarterStreamingXlsx(Path workbookPath,
                                                     String sheetName,
                                                     ChartTranslationMap translation)
        throws IOException
    {
        try (OPCPackage pkg = OPCPackage.open(workbookPath.toFile()))
        {
            XSSFReader reader = new XSSFReader(pkg);
            StylesTable styles = reader.getStylesTable();
            SharedStrings strings = reader.getSharedStringsTable();

            InputStream sheetStream = null;
            XSSFReader.SheetIterator sheets =
                (XSSFReader.SheetIterator) reader.getSheetsData();
            while (sheets.hasNext())
            {
                InputStream stream = sheets.next();
                String name = sheets.getSheetName();
                if (sheetName.equals(name))
                {
                    sheetStream = stream;
                    break;
                }
                stream.close();
            }

            if (sheetStream == null)
            {
                throw new IOException("Sheet not found: " + sheetName);
            }

            LedgerStreamingHandler handler =
                new LedgerStreamingHandler(sheetName, translation);
            DataFormatter formatter = new DataFormatter();
            ContentHandler xssfHandler = new XSSFSheetXMLHandler(
                styles,
                null,
                strings,
                handler,
                formatter,
                false);

            XMLReader parser = createXmlReader(xssfHandler);
            try
            {
                parser.parse(new InputSource(sheetStream));
            }
            catch (StopParsingException ignored)
            {
                // Intentional early stop.
            }
            catch (SAXException e)
            {
                throw new IOException("Failed to parse sheet: " + sheetName, e);
            }
            finally
            {
                sheetStream.close();
            }

            handler.ensureHeaderFound(sheetName);
            return handler.getQuarter();
        }
        catch (EncryptedDocumentException e)
        {
            throw new IOException("Invalid Excel format: " + e.getMessage(), e);
        }
        catch (Exception e)
        {
            throw new IOException("Unable to read workbook: " + e.getMessage(), e);
        }
    }

    /**
     * Scan first ~30 rows for a row that looks like the header row,
     * i.e. includes "DATE" and "TO/FROM".
     */
    private int findHeaderRow(Sheet sheet)
    {
        int scanMax = Math.min(sheet.getLastRowNum(), HEADER_SCAN_MAX_ROWS);

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
     * Map columns.
     *
     * @param headerValues the header values
     * @return the column index
     */
    private ColumnIndex mapColumns(Map<Integer, String> headerValues)
    {
        ColumnIndex idx = new ColumnIndex();

        for (Map.Entry<Integer, String> entry : headerValues.entrySet())
        {
            String headerText = entry.getValue();
            if (headerText == null)
            {
                continue;
            }

            String norm = headerText.trim().toUpperCase(Locale.ROOT);
            int colIndex = entry.getKey();

            if (norm.equals("DATE"))
            {
                idx.colDate = colIndex;
            }
            else if (norm.equals("CHECK #") || norm.equals("CHECK#"))
            {
                idx.colCheckNum = colIndex;
            }
            else if (norm.startsWith("CLEAR BANK"))
            {
                idx.colClearBank = colIndex;
            }
            else if (norm.startsWith("TO/FROM"))
            {
                idx.colToFrom = colIndex;
            }
            else if (norm.startsWith("MEMO/NOTES"))
            {
                idx.colMemo = colIndex;
            }
            else if (norm.startsWith("BUDGET TRACKING"))
            {
                idx.colBudgetNotes = colIndex;
            }
            else if (norm.equals("NET TOTAL"))
            {
                idx.colNetTotal = colIndex;
            }
        }

        idx.group1 = detectSplitGroup(headerValues, "AMOUNT", 0);
        idx.group2 = detectSplitGroup(headerValues, "AMOUNT",
            idx.group1 != null ? idx.group1.amountCol + 1 : 0);
        idx.group3 = detectSplitGroup(headerValues, "AMOUNT",
            idx.group2 != null ? idx.group2.amountCol + 1 : 0);
        idx.group4 = detectSplitGroup(headerValues, "AMOUNT",
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

    private SplitGroup detectSplitGroup(Map<Integer, String> headerValues,
                                        String firstHeaderMustMatch,
                                        int startSearchCol)
    {
        int maxCol = headerValues.keySet().stream()
            .mapToInt(Integer::intValue)
            .max()
            .orElse(-1);

        for (int c = startSearchCol; c <= maxCol - 4; c++)
        {
            String h0 = getCellString(headerValues, c);
            if (h0 == null)
            {
                continue;
            }

            if (!h0.trim().equalsIgnoreCase(firstHeaderMustMatch))
            {
                continue;
            }

            String h1 = getCellString(headerValues, c + 1);
            String h2 = getCellString(headerValues, c + 2);
            String h3 = getCellString(headerValues, c + 3);
            String h4 = getCellString(headerValues, c + 4);

            if (h1 == null || h2 == null || h3 == null || h4 == null)
            {
                continue;
            }

            String u1 = h1.trim().toUpperCase(Locale.ROOT);
            String u2 = h2.trim().toUpperCase(Locale.ROOT);
            String u3 = h3.trim().toUpperCase(Locale.ROOT);
            String u4 = h4.trim().toUpperCase(Locale.ROOT);

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
     * Adds the split from group.
     *
     * @param rowValues the row values
     * @param group the group
     * @param outRow the out row
     * @param translation the translation
     */
    private void addSplitFromGroup(Map<Integer, String> rowValues,
                                   SplitGroup group,
                                   LedgerRow outRow,
                                   ChartTranslationMap translation)
    {
        if (group == null)
        {
            return;
        }

        LedgerSplit split = new LedgerSplit();

        split.setAmount(readAmount(rowValues, group.amountCol));

        split.setAssetLiabilityAccount(
            getCellString(rowValues, group.assetLiabCol));
        split.setIncomeCategory(
            getCellString(rowValues, group.incomeCatCol));
        split.setExpenseCategory(
            getCellString(rowValues, group.expenseCatCol));

        split.setFund(
            getCellString(rowValues, group.fundCol));

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

        if (!split.isEmpty())
        {
            outRow.addSplit(split);
        }
    }

    /**
     * Read date.
     *
     * @param rowValues the row values
     * @param colIdx the col idx
     * @return the local date
     */
    private LocalDate readDate(Map<Integer, String> rowValues, int colIdx)
    {
        String value = getCellString(rowValues, colIdx);
        if (value == null)
        {
            return null;
        }

        String trimmed = value.trim();
        if (trimmed.isEmpty())
        {
            return null;
        }

        if (trimmed.matches("\\d+(\\.\\d+)?"))
        {
            try
            {
                double numeric = Double.parseDouble(trimmed);
                return DateUtil.getJavaDate(numeric)
                    .toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate();
            }
            catch (NumberFormatException ignored)
            {
                return null;
            }
        }

        DateTimeFormatter formatter = new DateTimeFormatterBuilder()
            .parseCaseInsensitive()
            .appendOptional(DateTimeFormatter.ofPattern("M/d/uuuu"))
            .appendOptional(DateTimeFormatter.ofPattern("M/d/uu"))
            .appendOptional(DateTimeFormatter.ofPattern("MM/dd/uuuu"))
            .appendOptional(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
            .toFormatter(Locale.US);

        try
        {
            return LocalDate.parse(trimmed, formatter);
        }
        catch (DateTimeParseException e)
        {
            return null;
        }
    }

    /**
     * Read amount.
     *
     * @param rowValues the row values
     * @param colIdx the col idx
     * @return the java.math. big decimal
     */
    private java.math.BigDecimal readAmount(Map<Integer, String> rowValues, int colIdx)
    {
        String value = getCellString(rowValues, colIdx);
        if (value == null)
        {
            return java.math.BigDecimal.ZERO;
        }

        String trimmed = value.trim();
        if (trimmed.isEmpty())
        {
            return java.math.BigDecimal.ZERO;
        }

        String cleaned = trimmed.replace(",", "");
        try
        {
            return new java.math.BigDecimal(cleaned);
        }
        catch (NumberFormatException ex)
        {
            return java.math.BigDecimal.ZERO;
        }
    }

    /**
     * Gets the cell string.
     *
     * @param rowValues the row values
     * @param colIdx the col idx
     * @return the cell string
     */
    private String getCellString(Map<Integer, String> rowValues, int colIdx)
    {
        if (rowValues == null || colIdx < 0)
        {
            return null;
        }

        String value = rowValues.get(colIdx);
        if (value == null)
        {
            return null;
        }

        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    /**
     * Checks if is streaming workbook.
     *
     * @param workbookPath the workbook path
     * @return true, if is streaming workbook
     */
    private boolean isStreamingWorkbook(Path workbookPath)
    {
        String name = workbookPath.getFileName().toString().toLowerCase(Locale.ROOT);
        return name.endsWith(".xlsx") || name.endsWith(".xlsm");
    }

    /**
     * Creates the xml reader.
     *
     * @param handler the handler
     * @return the XML reader
     * @throws IOException Signals that an I/O exception has occurred.
     */
    private XMLReader createXmlReader(ContentHandler handler) throws IOException
    {
        try
        {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            factory.setNamespaceAware(true);
            XMLReader parser = factory.newSAXParser().getXMLReader();
            parser.setContentHandler(handler);
            return parser;
        }
        catch (ParserConfigurationException | SAXException e)
        {
            throw new IOException("Unable to initialize XML reader", e);
        }
    }

    /**
     * The Class StopParsingException.
     */
    private static class StopParsingException extends RuntimeException
    {
        private static final long serialVersionUID = 1L;
    }

    /**
     * The Class LedgerStreamingHandler.
     */
    private class LedgerStreamingHandler
        implements XSSFSheetXMLHandler.SheetContentsHandler
    {
        private final LedgerQuarter quarter;
        private final ChartTranslationMap translation;

        private int headerRowIdx = -1;
        private ColumnIndex cols;
        private int dataRowsProcessed = 0;
        private Map<Integer, String> rowValues;

        LedgerStreamingHandler(String sheetName, ChartTranslationMap translation)
        {
            this.quarter = new LedgerQuarter(sheetName);
            this.translation = translation;
        }

        LedgerQuarter getQuarter()
        {
            return this.quarter;
        }

        void ensureHeaderFound(String sheetName) throws IOException
        {
            if (this.headerRowIdx < 0)
            {
                throw new IOException("Could not locate header row in " + sheetName);
            }
        }

        @Override
        public void startRow(int rowNum)
        {
            this.rowValues = new HashMap<>();
        }

        @Override
        public void endRow(int rowNum)
        {
            if (this.headerRowIdx < 0)
            {
                if (rowNum <= HEADER_SCAN_MAX_ROWS && looksLikeHeader(this.rowValues))
                {
                    this.headerRowIdx = rowNum;
                    this.cols = mapColumns(this.rowValues);
                }
                return;
            }

            if (rowNum <= this.headerRowIdx)
            {
                return;
            }

            if (MAX_STREAMING_DATA_ROWS > 0 &&
                this.dataRowsProcessed >= MAX_STREAMING_DATA_ROWS)
            {
                throw new StopParsingException();
            }

            LedgerRow row = new LedgerRow();
            row.setSheetRowNumber(rowNum);

            row.setDate(readDate(this.rowValues, this.cols.colDate));
            row.setCheckNumber(getCellString(this.rowValues, this.cols.colCheckNum));
            row.setClearedBankTag(getCellString(this.rowValues, this.cols.colClearBank));
            row.setToFrom(getCellString(this.rowValues, this.cols.colToFrom));
            row.setMemo(getCellString(this.rowValues, this.cols.colMemo));
            row.setBudgetNotes(getCellString(this.rowValues, this.cols.colBudgetNotes));

            addSplitFromGroup(this.rowValues, this.cols.group1, row, this.translation);
            addSplitFromGroup(this.rowValues, this.cols.group2, row, this.translation);
            addSplitFromGroup(this.rowValues, this.cols.group3, row, this.translation);
            addSplitFromGroup(this.rowValues, this.cols.group4, row, this.translation);

            if (!row.isEffectivelyBlank())
            {
                this.quarter.addRow(row);
            }

            this.dataRowsProcessed++;
        }

        @Override
        public void cell(String cellReference, String formattedValue,
                         org.apache.poi.xssf.usermodel.XSSFComment comment)
        {
            if (cellReference == null)
            {
                return;
            }

            int col = new CellReference(cellReference).getCol();
            if (formattedValue == null)
            {
                return;
            }

            this.rowValues.put(col, formattedValue);
        }

        @Override
        public void headerFooter(String text, boolean isHeader, String tagName)
        {
        }

        private boolean looksLikeHeader(Map<Integer, String> values)
        {
            boolean sawDate = false;
            boolean sawToFrom = false;

            for (String value : values.values())
            {
                if (value == null)
                {
                    continue;
                }
                String up = value.trim().toUpperCase(Locale.ROOT);
                if (up.equals("DATE"))
                {
                    sawDate = true;
                }
                if (up.startsWith("TO/FROM"))
                {
                    sawToFrom = true;
                }
            }

            return sawDate && sawToFrom;
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
