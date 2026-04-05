package nonprofitbookkeeping.persistence.sclx;

import jakarta.enterprise.context.ApplicationScoped;
import nonprofitbookkeeping.model.sclx.WorkbookLink;

/**
 * Repository for persisting {@link WorkbookLink} payloads.
 */
@ApplicationScoped
public class WorkbookLinkRepository extends AbstractSclxBeanRepository<WorkbookLink>
{
    public WorkbookLinkRepository()
    {
        super("sclx.WorkbookLink", WorkbookLink.class);
    }
}
