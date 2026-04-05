package nonprofitbookkeeping.persistence.sclx;

import jakarta.enterprise.context.ApplicationScoped;
import nonprofitbookkeeping.model.sclx.LedgerLink;

/**
 * Repository for persisting {@link LedgerLink} payloads.
 */
@ApplicationScoped
public class LedgerLinkRepository extends AbstractSclxBeanRepository<LedgerLink>
{
    public LedgerLinkRepository()
    {
        super("sclx.LedgerLink", LedgerLink.class);
    }
}
