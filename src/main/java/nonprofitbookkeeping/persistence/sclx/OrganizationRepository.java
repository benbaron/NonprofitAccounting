package nonprofitbookkeeping.persistence.sclx;

import jakarta.enterprise.context.ApplicationScoped;
import nonprofitbookkeeping.model.sclx.Organization;

/**
 * Repository for persisting {@link Organization} payloads.
 */
@ApplicationScoped
public class OrganizationRepository extends AbstractSclxBeanRepository<Organization>
{
    public OrganizationRepository()
    {
        super("sclx.Organization", Organization.class);
    }
}
