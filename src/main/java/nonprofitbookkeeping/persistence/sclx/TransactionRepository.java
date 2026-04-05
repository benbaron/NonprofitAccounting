package nonprofitbookkeeping.persistence.sclx;

import jakarta.enterprise.context.ApplicationScoped;
import nonprofitbookkeeping.model.sclx.Transaction;

/**
 * Repository for persisting {@link Transaction} payloads.
 */
@ApplicationScoped
public class TransactionRepository extends AbstractSclxBeanRepository<Transaction>
{
    public TransactionRepository()
    {
        super("sclx.Transaction", Transaction.class);
    }
}
