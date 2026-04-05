package nonprofitbookkeeping.persistence.sclx;

import jakarta.enterprise.context.ApplicationScoped;
import nonprofitbookkeeping.model.sclx.ReportingPeriod;

/**
 * Repository for persisting {@link ReportingPeriod} payloads.
 */
@ApplicationScoped
public class ReportingPeriodRepository extends AbstractSclxBeanRepository<ReportingPeriod>
{
    public ReportingPeriodRepository()
    {
        super("sclx.ReportingPeriod", ReportingPeriod.class);
    }
}
