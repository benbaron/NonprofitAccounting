package nonprofitbookkeeping.persistence.sclx;

import jakarta.enterprise.context.ApplicationScoped;
import nonprofitbookkeeping.model.sclx.TransactionLine;

/**
 * Repository for persisting {@link TransactionLine} payloads.
 */
@ApplicationScoped
public class TransactionLineRepository extends AbstractSclxBeanRepository<TransactionLine>
{
    public TransactionLineRepository()
    {
        super("sclx.TransactionLine", TransactionLine.class);
    }
}
