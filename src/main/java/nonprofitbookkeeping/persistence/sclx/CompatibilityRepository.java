package nonprofitbookkeeping.persistence.sclx;

import jakarta.enterprise.context.ApplicationScoped;
import nonprofitbookkeeping.model.sclx.Compatibility;

/**
 * Repository for persisting {@link Compatibility} payloads.
 */
@ApplicationScoped
public class CompatibilityRepository extends AbstractSclxBeanRepository<Compatibility>
{
    public CompatibilityRepository()
    {
        super("sclx.Compatibility", Compatibility.class);
    }
}
