package nonprofitbookkeeping.persistence.sclx;

import jakarta.enterprise.context.ApplicationScoped;
import nonprofitbookkeeping.model.sclx.BankAccountSigner;

/**
 * Repository for persisting {@link BankAccountSigner} payloads.
 */
@ApplicationScoped
public class BankAccountSignerRepository extends AbstractSclxBeanRepository<BankAccountSigner>
{
    public BankAccountSignerRepository()
    {
        super("sclx.BankAccountSigner", BankAccountSigner.class);
    }
}
