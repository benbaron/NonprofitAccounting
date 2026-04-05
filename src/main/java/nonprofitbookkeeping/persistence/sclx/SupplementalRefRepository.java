package nonprofitbookkeeping.persistence.sclx;

import jakarta.enterprise.context.ApplicationScoped;
import nonprofitbookkeeping.model.sclx.SupplementalRef;

/**
 * Repository for persisting {@link SupplementalRef} payloads.
 */
@ApplicationScoped
public class SupplementalRefRepository extends AbstractSclxBeanRepository<SupplementalRef>
{
    public SupplementalRefRepository()
    {
        super("sclx.SupplementalRef", SupplementalRef.class);
    }
}
