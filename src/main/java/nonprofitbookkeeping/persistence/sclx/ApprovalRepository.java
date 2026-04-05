package nonprofitbookkeeping.persistence.sclx;

import jakarta.enterprise.context.ApplicationScoped;
import nonprofitbookkeeping.model.sclx.Approval;

/**
 * Repository for persisting {@link Approval} payloads.
 */
@ApplicationScoped
public class ApprovalRepository extends AbstractSclxBeanRepository<Approval>
{
    public ApprovalRepository()
    {
        super("sclx.Approval", Approval.class);
    }
}
