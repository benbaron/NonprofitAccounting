package nonprofitbookkeeping.persistence.sclx;

import jakarta.enterprise.context.ApplicationScoped;
import nonprofitbookkeeping.model.sclx.BankAccount;

/**
 * Repository for persisting {@link BankAccount} payloads.
 */
@ApplicationScoped
public class BankAccountRepository extends AbstractSclxBeanRepository<BankAccount>
{
    public BankAccountRepository()
    {
        super("sclx.BankAccount", BankAccount.class);
    }
}
