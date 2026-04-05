package nonprofitbookkeeping.persistence.sclx;

import jakarta.enterprise.context.ApplicationScoped;
import nonprofitbookkeeping.model.sclx.OtherAssetItem;

/**
 * Repository for persisting {@link OtherAssetItem} payloads.
 */
@ApplicationScoped
public class OtherAssetItemRepository extends AbstractSclxBeanRepository<OtherAssetItem>
{
    public OtherAssetItemRepository()
    {
        super("sclx.OtherAssetItem", OtherAssetItem.class);
    }
}
