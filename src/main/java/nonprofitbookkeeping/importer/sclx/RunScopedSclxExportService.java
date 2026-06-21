package nonprofitbookkeeping.importer.sclx;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import nonprofitbookkeeping.persistence.DocumentRepository;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Exports an SCLX document for a specific import run.
 *
 * <p>Export strategy:
 * <ol>
 *     <li>Prefer the preserved raw SCLX payload for exact fidelity.</li>
 *     <li>If missing, assemble from typed SCLX repositories (run scoped) and fall back to
 *     canonical/native tables as needed.</li>
 *     <li>Attach import warnings/counts from {@code sclx.importSummary.&lt;runId&gt;} as export metadata.</li>
 * </ol>
 */
public class RunScopedSclxExportService
{
    private static final ObjectMapper MAPPER = new ObjectMapper().findAndRegisterModules();

    private final DocumentRepository documentRepository;
    private final NonprofitBookkeepingSclxExportService canonicalExportService;

    private final nonprofitbookkeeping.persistence.sclx.OrganizationRepository organizationRepository;
    private final nonprofitbookkeeping.persistence.sclx.ReportingPeriodRepository reportingPeriodRepository;
    private final nonprofitbookkeeping.persistence.sclx.AccountRepository accountRepository;
    private final nonprofitbookkeeping.persistence.sclx.FundRepository fundRepository;
    private final nonprofitbookkeeping.persistence.sclx.BudgetRepository budgetRepository;
    private final nonprofitbookkeeping.persistence.sclx.PersonRepository personRepository;
    private final nonprofitbookkeeping.persistence.sclx.EventRepository eventRepository;
    private final nonprofitbookkeeping.persistence.sclx.DocumentRepository sclxDocumentRepository;
    private final nonprofitbookkeeping.persistence.sclx.TransactionRepository transactionRepository;
    private final nonprofitbookkeeping.persistence.sclx.BankingItemRepository bankingItemRepository;
    private final nonprofitbookkeeping.persistence.sclx.OutstandingItemRepository outstandingItemRepository;
    private final nonprofitbookkeeping.persistence.sclx.OtherAssetItemRepository otherAssetItemRepository;
    private final nonprofitbookkeeping.persistence.sclx.SupplementalItemRepository supplementalItemRepository;
    private final nonprofitbookkeeping.persistence.sclx.AssetRepository assetRepository;
    private final nonprofitbookkeeping.persistence.sclx.SupplyRepository supplyRepository;
    private final nonprofitbookkeeping.persistence.sclx.BankStatementImportRepository bankStatementImportRepository;

    public RunScopedSclxExportService()
    {
        this.documentRepository = new DocumentRepository();
        this.canonicalExportService = new NonprofitBookkeepingSclxExportService();
        this.organizationRepository = new nonprofitbookkeeping.persistence.sclx.OrganizationRepository();
        this.reportingPeriodRepository = new nonprofitbookkeeping.persistence.sclx.ReportingPeriodRepository();
        this.accountRepository = new nonprofitbookkeeping.persistence.sclx.AccountRepository();
        this.fundRepository = new nonprofitbookkeeping.persistence.sclx.FundRepository();
        this.budgetRepository = new nonprofitbookkeeping.persistence.sclx.BudgetRepository();
        this.personRepository = new nonprofitbookkeeping.persistence.sclx.PersonRepository();
        this.eventRepository = new nonprofitbookkeeping.persistence.sclx.EventRepository();
        this.sclxDocumentRepository = new nonprofitbookkeeping.persistence.sclx.DocumentRepository();
        this.transactionRepository = new nonprofitbookkeeping.persistence.sclx.TransactionRepository();
        this.bankingItemRepository = new nonprofitbookkeeping.persistence.sclx.BankingItemRepository();
        this.outstandingItemRepository = new nonprofitbookkeeping.persistence.sclx.OutstandingItemRepository();
        this.otherAssetItemRepository = new nonprofitbookkeeping.persistence.sclx.OtherAssetItemRepository();
        this.supplementalItemRepository = new nonprofitbookkeeping.persistence.sclx.SupplementalItemRepository();
        this.assetRepository = new nonprofitbookkeeping.persistence.sclx.AssetRepository();
        this.supplyRepository = new nonprofitbookkeeping.persistence.sclx.SupplyRepository();
        this.bankStatementImportRepository = new nonprofitbookkeeping.persistence.sclx.BankStatementImportRepository();
    }

    public SclxDocument exportByRunId(String runId) throws SQLException
    {
        String effectiveRunId = Objects.requireNonNullElse(runId, "").trim();
        Optional<String> rawPayload = effectiveRunId.isEmpty()
            ? Optional.empty()
            : this.documentRepository.find("sclx.raw." + effectiveRunId);

        SclxDocument document = rawPayload
            .map(this::parseRawDocument)
            .orElseGet(() -> assembleFromRepositories(effectiveRunId));

        return withExportMetadata(document, effectiveRunId);
    }

    private SclxDocument parseRawDocument(String raw)
    {
        try
        {
            return MAPPER.readValue(raw, SclxDocument.class);
        }
        catch (JsonProcessingException ex)
        {
            throw new IllegalStateException("Failed to parse preserved SCLX raw payload", ex);
        }
    }

    private SclxDocument assembleFromRepositories(String runId)
    {
        try
        {
            SclxDocument canonical = this.canonicalExportService.exportDocument();

            SclxDocument.Organization organization = pickFirst(
                asList(this.organizationRepository.loadAll(), runId, SclxDocument.Organization.class),
                canonical.organization());
            SclxDocument.ReportingPeriod reportingPeriod = pickFirst(
                asList(this.reportingPeriodRepository.loadAll(), runId, SclxDocument.ReportingPeriod.class),
                canonical.reportingPeriod());

            List<SclxDocument.Account> accounts = pickList(
                asList(this.accountRepository.loadAll(), runId, SclxDocument.Account.class),
                canonical.chartOfAccounts());
            List<SclxDocument.Fund> funds = pickList(
                asList(this.fundRepository.loadAll(), runId, SclxDocument.Fund.class),
                canonical.funds());
            List<SclxDocument.Budget> budgets = pickList(
                asList(this.budgetRepository.loadAll(), runId, SclxDocument.Budget.class),
                canonical.budgets());
            List<SclxDocument.Person> people = pickList(
                asList(this.personRepository.loadAll(), runId, SclxDocument.Person.class),
                canonical.people());
            List<SclxDocument.Event> events = pickList(
                asList(this.eventRepository.loadAll(), runId, SclxDocument.Event.class),
                canonical.events());
            List<SclxDocument.Document> documents = pickList(
                asList(this.sclxDocumentRepository.loadAll(), runId, SclxDocument.Document.class),
                canonical.documents());
            List<SclxDocument.Transaction> transactions = pickList(
                asList(this.transactionRepository.loadAll(), runId, SclxDocument.Transaction.class),
                canonical.transactions());
            List<SclxDocument.BankingItem> bankingItems = pickList(
                asList(this.bankingItemRepository.loadAll(), runId, SclxDocument.BankingItem.class),
                canonical.bankingItems());
            List<SclxDocument.OutstandingItem> outstandingItems = pickList(
                asList(this.outstandingItemRepository.loadAll(), runId, SclxDocument.OutstandingItem.class),
                canonical.outstandingItems());
            List<SclxDocument.OtherAssetItem> otherAssetItems = pickList(
                asList(this.otherAssetItemRepository.loadAll(), runId, SclxDocument.OtherAssetItem.class),
                canonical.otherAssetItems());
            List<SclxDocument.SupplementalItem> supplementalItems = pickList(
                asList(this.supplementalItemRepository.loadAll(), runId, SclxDocument.SupplementalItem.class),
                canonical.supplementalItems());
            List<SclxDocument.Asset> assets = pickList(
                asList(this.assetRepository.loadAll(), runId, SclxDocument.Asset.class),
                canonical.assets());
            List<SclxDocument.Supply> supplies = pickList(
                asList(this.supplyRepository.loadAll(), runId, SclxDocument.Supply.class),
                canonical.supplies());
            List<SclxDocument.BankStatementImport> bankStatementImports = pickList(
                asList(this.bankStatementImportRepository.loadAll(), runId, SclxDocument.BankStatementImport.class),
                canonical.bankStatementImports());

            return new SclxDocument(
                canonical.format(),
                canonical.version(),
                canonical.exportedAt(),
                canonical.compatibility(),
                organization,
                reportingPeriod,
                accounts,
                funds,
                budgets,
                people,
                canonical.bankAccounts(),
                canonical.officeAssignments(),
                canonical.committeeMemberships(),
                events,
                documents,
                transactions,
                bankingItems,
                outstandingItems,
                otherAssetItems,
                supplementalItems,
                assets,
                supplies,
                bankStatementImports,
                canonical.extensions());
        }
        catch (SQLException ex)
        {
            throw new IllegalStateException("Failed to assemble SCLX document from repositories", ex);
        }
    }

    private SclxDocument withExportMetadata(SclxDocument document, String runId)
    {
        Map<String, Object> extensions = new LinkedHashMap<>();
        if (document.extensions() != null)
        {
            extensions.putAll(document.extensions());
        }

        Map<String, Object> exportMetadata = new LinkedHashMap<>();
        exportMetadata.put("runId", runId);
        if (!runId.isBlank())
        {
            exportMetadata.putAll(loadImportSummary(runId));
        }

        extensions.put("exportMetadata", exportMetadata);

        return new SclxDocument(
            document.format(),
            document.version(),
            document.exportedAt(),
            document.compatibility(),
            document.organization(),
            document.reportingPeriod(),
            document.chartOfAccounts(),
            document.funds(),
            document.budgets(),
            document.people(),
            document.bankAccounts(),
            document.officeAssignments(),
            document.committeeMemberships(),
            document.events(),
            document.documents(),
            document.transactions(),
            document.bankingItems(),
            document.outstandingItems(),
            document.otherAssetItems(),
            document.supplementalItems(),
            document.assets(),
            document.supplies(),
            document.bankStatementImports(),
            Map.copyOf(extensions));
    }

    private Map<String, Object> loadImportSummary(String runId)
    {
        try
        {
            Optional<String> raw = this.documentRepository.find("sclx.importSummary." + runId);
            if (raw.isEmpty())
            {
                return Map.of();
            }
            return MAPPER.readValue(raw.get(), new TypeReference<Map<String, Object>>() {});
        }
        catch (Exception ex)
        {
            return Map.of("summaryLoadWarning", "Failed to load import summary: " + ex.getMessage());
        }
    }

    private <T> List<T> asList(Map<String, ?> rows, String runId, Class<T> targetType)
    {
        String prefix = runId + "::";
        List<T> items = new ArrayList<>();
        for (Map.Entry<String, ?> entry : rows.entrySet())
        {
            if (!runId.isBlank() && !entry.getKey().startsWith(prefix))
            {
                continue;
            }
            items.add(MAPPER.convertValue(entry.getValue(), targetType));
        }
        return items;
    }

    private static <T> T pickFirst(List<T> preferred, T fallback)
    {
        if (preferred == null || preferred.isEmpty())
        {
            return fallback;
        }
        return preferred.get(0);
    }

    private static <T> List<T> pickList(List<T> preferred, List<T> fallback)
    {
        if (preferred == null || preferred.isEmpty())
        {
            return fallback == null ? List.of() : fallback;
        }
        return preferred;
    }
}
