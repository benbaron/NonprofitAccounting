package nonprofitbookkeeping.persistence.sclx;

import jakarta.enterprise.context.ApplicationScoped;
import nonprofitbookkeeping.model.sclx.CommitteeMembership;

/**
 * Repository for persisting {@link CommitteeMembership} payloads.
 */
@ApplicationScoped
public class CommitteeMembershipRepository extends AbstractSclxBeanRepository<CommitteeMembership>
{
    public CommitteeMembershipRepository()
    {
        super("sclx.CommitteeMembership", CommitteeMembership.class);
    }
}
