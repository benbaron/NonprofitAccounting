package nonprofitbookkeeping.persistence.sclx;

import jakarta.enterprise.context.ApplicationScoped;
import nonprofitbookkeeping.model.sclx.Account;

/**
 * Repository for persisting {@link Account} payloads.
 */
@ApplicationScoped
public class AccountRepository extends AbstractSclxBeanRepository<Account>
{
    
    /**
     * Instantiates a new account repository.
     */
    public AccountRepository()
    {
        super("sclx.Account", Account.class);
    }
}
