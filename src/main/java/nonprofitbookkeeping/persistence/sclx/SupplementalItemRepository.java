package nonprofitbookkeeping.persistence.sclx;

import jakarta.enterprise.context.ApplicationScoped;
import nonprofitbookkeeping.model.sclx.SupplementalItem;

/**
 * Repository for persisting {@link SupplementalItem} payloads.
 */
@ApplicationScoped
public class SupplementalItemRepository extends AbstractSclxBeanRepository<SupplementalItem>
{
    public SupplementalItemRepository()
    {
        super("sclx.SupplementalItem", SupplementalItem.class);
    }
}
