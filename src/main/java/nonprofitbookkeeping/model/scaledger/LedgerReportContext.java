package nonprofitbookkeeping.model.scaledger;

// context bean for JXLS template
import java.util.List;

public class LedgerReportContext
{
    public String accountName;
    public List<LedgerEntry> entries;

    public LedgerReportContext(String accountName, List<LedgerEntry> entries)
    {
        this.accountName = accountName;
        this.entries = entries;
    }
}
