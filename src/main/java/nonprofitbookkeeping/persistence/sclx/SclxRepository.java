package nonprofitbookkeeping.persistence.sclx;

import jakarta.enterprise.context.ApplicationScoped;
import nonprofitbookkeeping.model.sclx.Sclx;

/**
 * Repository for persisting {@link Sclx} payloads.
 */
@ApplicationScoped
public class SclxRepository extends AbstractSclxBeanRepository<Sclx>
{
    public SclxRepository()
    {
        super("sclx.Sclx", Sclx.class);
    }
}
