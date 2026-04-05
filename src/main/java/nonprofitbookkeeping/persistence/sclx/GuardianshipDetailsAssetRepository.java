package nonprofitbookkeeping.persistence.sclx;

import jakarta.enterprise.context.ApplicationScoped;
import nonprofitbookkeeping.model.sclx.GuardianshipDetailsAsset;

/**
 * Repository for persisting {@link GuardianshipDetailsAsset} payloads.
 */
@ApplicationScoped
public class GuardianshipDetailsAssetRepository extends AbstractSclxBeanRepository<GuardianshipDetailsAsset>
{
    public GuardianshipDetailsAssetRepository()
    {
        super("sclx.GuardianshipDetailsAsset", GuardianshipDetailsAsset.class);
    }
}
