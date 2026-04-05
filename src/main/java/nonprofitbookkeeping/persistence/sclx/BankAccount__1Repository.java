package nonprofitbookkeeping.persistence.sclx;

import jakarta.enterprise.context.ApplicationScoped;
import nonprofitbookkeeping.model.sclx.BankAccount__1;

/**
 * Repository for persisting {@link BankAccount__1} payloads.
 */
@ApplicationScoped
public class BankAccount__1Repository extends AbstractSclxBeanRepository<BankAccount__1>
{
    public BankAccount__1Repository()
    {
        super("sclx.BankAccount__1", BankAccount__1.class);
    }
}
