package nonprofitbookkeeping.persistence.sclx;

import jakarta.enterprise.context.ApplicationScoped;
import nonprofitbookkeeping.model.sclx.BudgetLine;

/**
 * Repository for persisting {@link BudgetLine} payloads.
 */
@ApplicationScoped
public class BudgetLineRepository extends AbstractSclxBeanRepository<BudgetLine>
{
    public BudgetLineRepository()
    {
        super("sclx.BudgetLine", BudgetLine.class);
    }
}
