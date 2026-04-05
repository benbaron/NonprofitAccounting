package nonprofitbookkeeping.persistence.sclx;

import jakarta.enterprise.context.ApplicationScoped;
import nonprofitbookkeeping.model.sclx.OfxTransaction;

/**
 * Repository for persisting {@link OfxTransaction} payloads.
 */
@ApplicationScoped
public class OfxTransactionRepository extends AbstractSclxBeanRepository<OfxTransaction>
{
    public OfxTransactionRepository()
    {
        super("sclx.OfxTransaction", OfxTransaction.class);
    }
}
