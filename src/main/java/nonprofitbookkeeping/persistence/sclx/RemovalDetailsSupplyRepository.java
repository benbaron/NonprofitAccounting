package nonprofitbookkeeping.persistence.sclx;

import jakarta.enterprise.context.ApplicationScoped;
import nonprofitbookkeeping.model.sclx.RemovalDetailsSupply;

/**
 * Repository for persisting {@link RemovalDetailsSupply} payloads.
 */
@ApplicationScoped
public class RemovalDetailsSupplyRepository extends AbstractSclxBeanRepository<RemovalDetailsSupply>
{
    public RemovalDetailsSupplyRepository()
    {
        super("sclx.RemovalDetailsSupply", RemovalDetailsSupply.class);
    }
}
