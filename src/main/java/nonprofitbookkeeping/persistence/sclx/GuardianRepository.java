package nonprofitbookkeeping.persistence.sclx;

import jakarta.enterprise.context.ApplicationScoped;
import nonprofitbookkeeping.model.sclx.Guardian;

/**
 * Repository for persisting {@link Guardian} payloads.
 */
@ApplicationScoped
public class GuardianRepository extends AbstractSclxBeanRepository<Guardian>
{
    public GuardianRepository()
    {
        super("sclx.Guardian", Guardian.class);
    }
}
