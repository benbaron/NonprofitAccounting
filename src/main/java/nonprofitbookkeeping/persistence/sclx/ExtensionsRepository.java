package nonprofitbookkeeping.persistence.sclx;

import jakarta.enterprise.context.ApplicationScoped;
import nonprofitbookkeeping.model.sclx.Extensions;

/**
 * Repository for persisting {@link Extensions} payloads.
 */
@ApplicationScoped
public class ExtensionsRepository extends AbstractSclxBeanRepository<Extensions>
{
    public ExtensionsRepository()
    {
        super("sclx.Extensions", Extensions.class);
    }
}
