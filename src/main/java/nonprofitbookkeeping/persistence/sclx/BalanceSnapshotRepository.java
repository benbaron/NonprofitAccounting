package nonprofitbookkeeping.persistence.sclx;

import jakarta.enterprise.context.ApplicationScoped;
import nonprofitbookkeeping.model.sclx.BalanceSnapshot;

/**
 * Repository for persisting {@link BalanceSnapshot} payloads.
 */
@ApplicationScoped
public class BalanceSnapshotRepository extends AbstractSclxBeanRepository<BalanceSnapshot>
{
    public BalanceSnapshotRepository()
    {
        super("sclx.BalanceSnapshot", BalanceSnapshot.class);
    }
}
