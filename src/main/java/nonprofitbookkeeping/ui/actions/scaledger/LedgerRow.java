package nonprofitbookkeeping.ui.actions.scaledger;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

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
    private LocalDate date;
    private String checkNumber;
    private String clearedBankTag; // e.g. "Oct", "Nov" (reconciled month)
    private String toFrom;
    private String memo;
    private String budgetNotes;
    private Integer sheetRowNumber; // physical Excel row index or Row ID

    private final List<LedgerSplit> splits = new ArrayList<>();

    public LedgerRow()
    {
    }

    public LocalDate getDate()
    {
        return this.date;
    }

    public void setDate(LocalDate date)
    {
        this.date = date;
    }

    public String getCheckNumber()
    {
        return this.checkNumber;
    }

    public void setCheckNumber(String checkNumber)
    {
        this.checkNumber = checkNumber;
    }

    public String getClearedBankTag()
    {
        return this.clearedBankTag;
    }

    public void setClearedBankTag(String clearedBankTag)
    {
        this.clearedBankTag = clearedBankTag;
    }

    public String getToFrom()
    {
        return this.toFrom;
    }

    public void setToFrom(String toFrom)
    {
        this.toFrom = toFrom;
    }

    public String getMemo()
    {
        return this.memo;
    }

    public void setMemo(String memo)
    {
        this.memo = memo;
    }

    public String getBudgetNotes()
    {
        return this.budgetNotes;
    }

    public void setBudgetNotes(String budgetNotes)
    {
        this.budgetNotes = budgetNotes;
    }

    public Integer getSheetRowNumber()
    {
        return this.sheetRowNumber;
    }

    public void setSheetRowNumber(Integer sheetRowNumber)
    {
        this.sheetRowNumber = sheetRowNumber;
    }

    public List<LedgerSplit> getSplits()
    {
        return this.splits;
    }

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
     */
    public boolean isEffectivelyBlank()
    {
        boolean noDate = (this.date == null);
        boolean allSplitsEmpty = this.splits.stream().allMatch(LedgerSplit::isEmpty);
        boolean noMemo = (this.memo == null || this.memo.isBlank());

        return noDate && noMemo && allSplitsEmpty;
    }
}
