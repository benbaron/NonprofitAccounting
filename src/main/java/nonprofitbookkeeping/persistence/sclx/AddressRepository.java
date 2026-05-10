package nonprofitbookkeeping.persistence.sclx;

import jakarta.enterprise.context.ApplicationScoped;
import nonprofitbookkeeping.model.sclx.Address;

/**
 * Repository for persisting {@link Address} payloads.
 */
@ApplicationScoped
public class AddressRepository extends AbstractSclxBeanRepository<Address>
{
    
    /**
     * Instantiates a new address repository.
     */
    public AddressRepository()
    {
        super("sclx.Address", Address.class);
    }
}
