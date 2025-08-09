package nonprofitbookkeeping.model.scaledger;

// context bean for JXLS template
import java.util.List;

/**
 * Represents the context for generating a ledger report, typically for use with a templating engine like JXLS.
 * It encapsulates the account name and a list of {@link LedgerEntry} objects relevant to that account for the report.
 */
public class LedgerReportContext
{
    /** The name of the account for which the ledger report is being generated. */
    public String accountName;
    /** A list of {@link LedgerEntry} objects that pertain to the specified account and report period. */
    public List<LedgerEntry> entries;

    /**
     * Constructs a new LedgerReportContext.
     *
     * @param accountName The name of the account.
     * @param entries A list of {@link LedgerEntry} objects for the report.
     */
    public LedgerReportContext(String accountName, List<LedgerEntry> entries)
    {
        this.accountName = accountName;
        this.entries = entries;
    }
}
