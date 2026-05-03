package nonprofitbookkeeping.ui.actions.scaledger;

import java.util.ArrayList;
import java.util.List;


/**
 * Represents all ledger rows from one quarter sheet
 * (for example "Ledger_Q4").
 *
 * Also stores the sheet name as a label like "Ledger_Q4" or "Q4 2024".
 */
public class LedgerQuarter
{
    
    /** The sheet name. */
    private final String sheetName;
    
    /** The rows. */
    private final List<LedgerRow> rows = new ArrayList<>();

    /**
     * Instantiates a new ledger quarter.
     *
     * @param sheetName the sheet name
     */
    public LedgerQuarter(String sheetName)
    {
        this.sheetName = sheetName;
    }

    /**
     * Gets the sheet name.
     *
     * @return the sheet name
     */
    public String getSheetName()
    {
        return this.sheetName;
    }

    /**
     * Gets the rows.
     *
     * @return the rows
     */
    public List<LedgerRow> getRows()
    {
        return this.rows;
    }

    /**
     * Adds the row.
     *
     * @param row the row
     */
    public void addRow(LedgerRow row)
    {
        if (row != null && !row.isEffectivelyBlank())
        {
            this.rows.add(row);
        }
    }
}
