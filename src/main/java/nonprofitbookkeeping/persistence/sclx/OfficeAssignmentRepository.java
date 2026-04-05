package nonprofitbookkeeping.persistence.sclx;

import jakarta.enterprise.context.ApplicationScoped;
import nonprofitbookkeeping.model.sclx.OfficeAssignment;

/**
 * Repository for persisting {@link OfficeAssignment} payloads.
 */
@ApplicationScoped
public class OfficeAssignmentRepository extends AbstractSclxBeanRepository<OfficeAssignment>
{
    public OfficeAssignmentRepository()
    {
        super("sclx.OfficeAssignment", OfficeAssignment.class);
    }
}
