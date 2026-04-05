package nonprofitbookkeeping.persistence.sclx;

import jakarta.enterprise.context.ApplicationScoped;
import nonprofitbookkeeping.model.sclx.Fund;

/**
 * Repository for persisting {@link Fund} payloads.
 */
@ApplicationScoped
public class FundRepository extends AbstractSclxBeanRepository<Fund>
{
    public FundRepository()
    {
        super("sclx.Fund", Fund.class);
    }
}
