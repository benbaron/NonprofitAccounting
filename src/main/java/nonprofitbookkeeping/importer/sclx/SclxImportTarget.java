package nonprofitbookkeeping.importer.sclx;

import java.util.List;

/**
 * Integration boundary between the generic SCLX importer and the concrete
 * NonprofitBookkeeping persistence/services layer.
 *
 * <p>Implement this interface against your repositories, service layer, or
 * transaction save flow. The importer intentionally stages SCLX data in the
 * same order that the document model expects readers to load it.
 */
public interface SclxImportTarget
{
    default void persistRawSource(String rawSourceJson, SclxImportOptions options)
    {
        // default no-op for targets that do not persist source payloads
    }

    void beginImport(SclxDocument document, SclxImportOptions options);

    void importOrganization(SclxDocument.Organization organization);

    void importReportingPeriod(SclxDocument.ReportingPeriod reportingPeriod);

    void importAccounts(List<SclxDocument.Account> accounts);

    void importFunds(List<SclxDocument.Fund> funds);

    void importBudgets(List<SclxDocument.Budget> budgets);

    void importPeople(List<SclxDocument.Person> people);

    void importEvents(List<SclxDocument.Event> events);

    void importDocuments(List<SclxDocument.Document> documents);

    void importTransactions(List<SclxDocument.Transaction> transactions);

    void importOutstandingItems(List<SclxDocument.OutstandingItem> outstandingItems);

    void importOtherAssetItems(List<SclxDocument.OtherAssetItem> otherAssetItems);

    void importSupplementalItems(List<SclxDocument.SupplementalItem> supplementalItems);

    void importAssets(List<SclxDocument.Asset> assets);

    void importSupplies(List<SclxDocument.Supply> supplies);

    void importBankingItems(List<SclxDocument.BankingItem> bankingItems);

    void importBankStatementImports(List<SclxDocument.BankStatementImport> bankStatementImports);

    void completeImport(SclxImportResult result);
}
