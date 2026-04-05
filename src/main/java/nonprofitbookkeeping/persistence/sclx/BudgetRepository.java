package nonprofitbookkeeping.persistence.sclx;

import jakarta.enterprise.context.ApplicationScoped;
import nonprofitbookkeeping.model.sclx.Budget;

/**
 * Repository for persisting {@link Budget} payloads.
 */
@ApplicationScoped
public class BudgetRepository extends AbstractSclxBeanRepository<Budget>
{
    public BudgetRepository()
    {
        super("sclx.Budget", Budget.class);
    }
}
