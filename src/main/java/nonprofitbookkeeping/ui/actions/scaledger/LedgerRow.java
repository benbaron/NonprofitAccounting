package nonprofitbookkeeping.ui.actions.scaledger;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

// TODO: Auto-generated Javadoc
/**
 * One transaction row from Ledger_Q1 / Ledger_Q2 / Ledger_Q3 / Ledger_Q4.
 *
 * Columns include:
 *  - BALANCE (running balance)         [C]      (derived in Excel)
 *  - DATE                              [D]
 *  - CHECK #                           [E]
 *  - AMOUNT (row total)                [F]      (derived in Excel)
 *  - Clear Bank (reconciliation tag)   [G]
 *  - TO/FROM                           [H]
 *  - MEMO/NOTES                        [I]
 *  - BUDGET TRACKING / Notes           [J]
 *  - NET TOTAL (signed net)            [L]      (derived in Excel)
 *
 * Then up to four split legs:
 *  Leg 1 columns M-Q (Amount, A/L Acct, Income Cat, Expense Cat, Fund)
 *  Leg 2 columns R-V
 *  Leg 3 columns X-AB
 *  Leg 4 columns AC-AG
 */
public class LedgerRow
{
    
    /** The date. */
    private LocalDate date;
    
    /** The check number. */
    private String checkNumber;
    
    /** The cleared bank tag. */
    private String clearedBankTag; // e.g. "Oct", "Nov" (reconciled month)
    
    /** The to from. */
    private String toFrom;
    
    /** The memo. */
    private String memo;
    
    /** The budget notes. */
    private String budgetNotes;
    
    /** The sheet row number. */
    private Integer sheetRowNumber; // physical Excel row index or Row ID

    /** The splits. */
    private final List<LedgerSplit> splits = new ArrayList<>();

    /**
     * Instantiates a new ledger row.
     */
    public LedgerRow()
    {
    }

    /**
     * Gets the date.
     *
     * @return the date
     */
    public LocalDate getDate()
    {
        return this.date;
    }

    /**
     * Sets the date.
     *
     * @param date the new date
     */
    public void setDate(LocalDate date)
    {
        this.date = date;
    }

    /**
     * Gets the check number.
     *
     * @return the check number
     */
    public String getCheckNumber()
    {
        return this.checkNumber;
    }

    /**
     * Sets the check number.
     *
     * @param checkNumber the new check number
     */
    public void setCheckNumber(String checkNumber)
    {
        this.checkNumber = checkNumber;
    }

    /**
     * Gets the cleared bank tag.
     *
     * @return the cleared bank tag
     */
    public String getClearedBankTag()
    {
        return this.clearedBankTag;
    }

    /**
     * Sets the cleared bank tag.
     *
     * @param clearedBankTag the new cleared bank tag
     */
    public void setClearedBankTag(String clearedBankTag)
    {
        this.clearedBankTag = clearedBankTag;
    }

    /**
     * Gets the to from.
     *
     * @return the to from
     */
    public String getToFrom()
    {
        return this.toFrom;
    }

    /**
     * Sets the to from.
     *
     * @param toFrom the new to from
     */
    public void setToFrom(String toFrom)
    {
        this.toFrom = toFrom;
    }

    /**
     * Gets the memo.
     *
     * @return the memo
     */
    public String getMemo()
    {
        return this.memo;
    }

    /**
     * Sets the memo.
     *
     * @param memo the new memo
     */
    public void setMemo(String memo)
    {
        this.memo = memo;
    }

    /**
     * Gets the budget notes.
     *
     * @return the budget notes
     */
    public String getBudgetNotes()
    {
        return this.budgetNotes;
    }

    /**
     * Sets the budget notes.
     *
     * @param budgetNotes the new budget notes
     */
    public void setBudgetNotes(String budgetNotes)
    {
        this.budgetNotes = budgetNotes;
    }

    /**
     * Gets the sheet row number.
     *
     * @return the sheet row number
     */
    public Integer getSheetRowNumber()
    {
        return this.sheetRowNumber;
    }

    /**
     * Sets the sheet row number.
     *
     * @param sheetRowNumber the new sheet row number
     */
    public void setSheetRowNumber(Integer sheetRowNumber)
    {
        this.sheetRowNumber = sheetRowNumber;
    }

    /**
     * Gets the splits.
     *
     * @return the splits
     */
    public List<LedgerSplit> getSplits()
    {
        return this.splits;
    }

    /**
     * Adds the split.
     *
     * @param split the split
     */
    public void addSplit(LedgerSplit split)
    {
        if (split != null && !split.isEmpty())
        {
            this.splits.add(split);
        }
    }

    /**
     * Convenience check: true if this row is basically spacing /
     * informational only (no date AND no non-empty splits).
     *
     * @return true, if is effectively blank
     */
    public boolean isEffectivelyBlank()
    {
        boolean noDate = (this.date == null);
        boolean allSplitsEmpty = this.splits.stream().allMatch(LedgerSplit::isEmpty);
        boolean noMemo = (this.memo == null || this.memo.isBlank());

        return noDate && noMemo && allSplitsEmpty;
    }
}
