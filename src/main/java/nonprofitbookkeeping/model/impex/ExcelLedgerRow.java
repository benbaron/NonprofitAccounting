package nonprofitbookkeeping.model.impex;

import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a single row from an imported Excel ledger.
 * The row contains general transaction info and up to four
 * allocation groups consisting of amount and account details.
 */
@Data
@NoArgsConstructor
public class ExcelLedgerRow {
    /** Transaction date from the spreadsheet. */
    private LocalDate date;
    /** Optional check number. */
    private String checkNumber;
    /** Indicator of which bank cleared the transaction. */
    private String clearBank;
    /** Payee or source/destination. */
    private String toFrom;
    /** Memo or notes column. */
    private String memoNotes;
    /** Optional budget tracking value. */
    private String budgetTracking;

    /** List of up to four allocation groups. */
    private List<Allocation> allocations = new ArrayList<>();

    /**
     * Represents one amount allocation within a row.
     */
    @Data
    @NoArgsConstructor
    public static class Allocation {
        private BigDecimal amount;
        private String assetLiabilityAccount;
        private String incomeCategory;
        private String expenseCategory;
        private String fund;
    }
}
