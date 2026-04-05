package nonprofitbookkeeping.persistence.sclx;

import jakarta.enterprise.context.ApplicationScoped;
import nonprofitbookkeeping.model.sclx.OutstandingItem;

/**
 * Repository for persisting {@link OutstandingItem} payloads.
 */
@ApplicationScoped
public class OutstandingItemRepository extends AbstractSclxBeanRepository<OutstandingItem>
{
    public OutstandingItemRepository()
    {
        super("sclx.OutstandingItem", OutstandingItem.class);
    }
}
