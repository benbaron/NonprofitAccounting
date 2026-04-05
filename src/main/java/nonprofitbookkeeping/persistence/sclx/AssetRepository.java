package nonprofitbookkeeping.persistence.sclx;

import jakarta.enterprise.context.ApplicationScoped;
import nonprofitbookkeeping.model.sclx.Asset;

/**
 * Repository for persisting {@link Asset} payloads.
 */
@ApplicationScoped
public class AssetRepository extends AbstractSclxBeanRepository<Asset>
{
    public AssetRepository()
    {
        super("sclx.Asset", Asset.class);
    }
}
