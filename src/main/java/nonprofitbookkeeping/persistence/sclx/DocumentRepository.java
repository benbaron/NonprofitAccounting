package nonprofitbookkeeping.persistence.sclx;

import jakarta.enterprise.context.ApplicationScoped;
import nonprofitbookkeeping.model.sclx.Document;

/**
 * Repository for persisting {@link Document} payloads.
 */
@ApplicationScoped
public class DocumentRepository extends AbstractSclxBeanRepository<Document>
{
    public DocumentRepository()
    {
        super("sclx.Document", Document.class);
    }
}
