package nonprofitbookkeeping.persistence.sclx;

import jakarta.enterprise.context.ApplicationScoped;
import nonprofitbookkeeping.model.sclx.AppraisalDetails;

/**
 * Repository for persisting {@link AppraisalDetails} payloads.
 */
@ApplicationScoped
public class AppraisalDetailsRepository extends AbstractSclxBeanRepository<AppraisalDetails>
{
    public AppraisalDetailsRepository()
    {
        super("sclx.AppraisalDetails", AppraisalDetails.class);
    }
}
