package nonprofitbookkeeping.persistence.sclx;

import jakarta.enterprise.context.ApplicationScoped;
import nonprofitbookkeeping.model.sclx.BankingItem;

/**
 * Repository for persisting {@link BankingItem} payloads.
 */
@ApplicationScoped
public class BankingItemRepository extends AbstractSclxBeanRepository<BankingItem>
{
    public BankingItemRepository()
    {
        super("sclx.BankingItem", BankingItem.class);
    }
}
