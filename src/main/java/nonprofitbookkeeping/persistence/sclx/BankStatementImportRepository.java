package nonprofitbookkeeping.persistence.sclx;

import jakarta.enterprise.context.ApplicationScoped;
import nonprofitbookkeeping.model.sclx.BankStatementImport;

/**
 * Repository for persisting {@link BankStatementImport} payloads.
 */
@ApplicationScoped
public class BankStatementImportRepository extends AbstractSclxBeanRepository<BankStatementImport>
{
    public BankStatementImportRepository()
    {
        super("sclx.BankStatementImport", BankStatementImport.class);
    }
}
