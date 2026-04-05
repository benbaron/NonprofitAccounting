package nonprofitbookkeeping.persistence.sclx;

import jakarta.enterprise.context.ApplicationScoped;
import nonprofitbookkeeping.model.sclx.GuardianshipDetailsSupply;

/**
 * Repository for persisting {@link GuardianshipDetailsSupply} payloads.
 */
@ApplicationScoped
public class GuardianshipDetailsSupplyRepository extends AbstractSclxBeanRepository<GuardianshipDetailsSupply>
{
    public GuardianshipDetailsSupplyRepository()
    {
        super("sclx.GuardianshipDetailsSupply", GuardianshipDetailsSupply.class);
    }
}
