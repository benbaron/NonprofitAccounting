package nonprofitbookkeeping.persistence.sclx;

import jakarta.enterprise.context.ApplicationScoped;
import nonprofitbookkeeping.model.sclx.SupplementalKind;

/**
 * Repository for persisting {@link SupplementalKind} payloads.
 */
@ApplicationScoped
public class SupplementalKindRepository extends AbstractSclxBeanRepository<SupplementalKind>
{
    public SupplementalKindRepository()
    {
        super("sclx.SupplementalKind", SupplementalKind.class);
    }
}
