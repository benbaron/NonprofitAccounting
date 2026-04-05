package nonprofitbookkeeping.persistence.sclx;

import jakarta.enterprise.context.ApplicationScoped;
import nonprofitbookkeeping.model.sclx.RemovalDetailsAsset;

/**
 * Repository for persisting {@link RemovalDetailsAsset} payloads.
 */
@ApplicationScoped
public class RemovalDetailsAssetRepository extends AbstractSclxBeanRepository<RemovalDetailsAsset>
{
    public RemovalDetailsAssetRepository()
    {
        super("sclx.RemovalDetailsAsset", RemovalDetailsAsset.class);
    }
}
