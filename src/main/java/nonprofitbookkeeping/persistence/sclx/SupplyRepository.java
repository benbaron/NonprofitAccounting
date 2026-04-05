package nonprofitbookkeeping.persistence.sclx;

import jakarta.enterprise.context.ApplicationScoped;
import nonprofitbookkeeping.model.sclx.Supply;

/**
 * Repository for persisting {@link Supply} payloads.
 */
@ApplicationScoped
public class SupplyRepository extends AbstractSclxBeanRepository<Supply>
{
    public SupplyRepository()
    {
        super("sclx.Supply", Supply.class);
    }
}
