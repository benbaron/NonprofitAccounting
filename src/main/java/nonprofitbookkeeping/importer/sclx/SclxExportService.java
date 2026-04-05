package nonprofitbookkeeping.importer.sclx;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import nonprofitbookkeeping.persistence.DocumentRepository;
import nonprofitbookkeeping.persistence.JsonStorageRepository;
import nonprofitbookkeeping.persistence.sclx.AssetRepository;
import nonprofitbookkeeping.persistence.sclx.EventRepository;
import nonprofitbookkeeping.persistence.sclx.OrganizationRepository;
import nonprofitbookkeeping.persistence.sclx.OtherAssetItemRepository;
import nonprofitbookkeeping.persistence.sclx.OutstandingItemRepository;
import nonprofitbookkeeping.persistence.sclx.ReportingPeriodRepository;
import nonprofitbookkeeping.persistence.sclx.SupplyRepository;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Run-scoped SCLX export service.
 *
 * <p>Export strategy:
 * <ol>
 *   <li>Prefer the preserved raw import payload ({@code sclx.raw.&lt;runId&gt;}) when present.</li>
 *   <li>Otherwise assemble from typed SCLX repositories plus canonical export sections.</li>
 *   <li>Attach import summary metadata in an export manifest under root extensions.</li>
 * </ol>
 */
public class SclxExportService
{
    private static final String RAW_PREFIX = "sclx.raw.";
    private static final String IMPORT_SUMMARY_PREFIX = "sclx.importSummary.";
    private static final ObjectMapper MAPPER = SclxParser.buildDefaultMapper();

    private final JsonStorageRepository jsonStorageRepository;
    private final DocumentRepository documentRepository;
    private final NonprofitBookkeepingSclxExportService canonicalExportService;
    private final OrganizationRepository organizationRepository;
    private final ReportingPeriodRepository reportingPeriodRepository;
    private final EventRepository eventRepository;
    private final nonprofitbookkeeping.persistence.sclx.DocumentRepository sclxDocumentRepository;
    private final OutstandingItemRepository outstandingItemRepository;
    private final OtherAssetItemRepository otherAssetItemRepository;
    private final AssetRepository assetRepository;
    private final SupplyRepository supplyRepository;

    public SclxExportService()
    {
        this(
            new JsonStorageRepository(),
            new DocumentRepository(),
            new NonprofitBookkeepingSclxExportService(),
            new OrganizationRepository(),
            new ReportingPeriodRepository(),
            new EventRepository(),
            new nonprofitbookkeeping.persistence.sclx.DocumentRepository(),
            new OutstandingItemRepository(),
            new OtherAssetItemRepository(),
            new AssetRepository(),
            new SupplyRepository());
    }

    public SclxExportService(
        JsonStorageRepository jsonStorageRepository,
        DocumentRepository documentRepository,
        NonprofitBookkeepingSclxExportService canonicalExportService,
        OrganizationRepository organizationRepository,
        ReportingPeriodRepository reportingPeriodRepository,
        EventRepository eventRepository,
        nonprofitbookkeeping.persistence.sclx.DocumentRepository sclxDocumentRepository,
        OutstandingItemRepository outstandingItemRepository,
        OtherAssetItemRepository otherAssetItemRepository,
        AssetRepository assetRepository,
        SupplyRepository supplyRepository)
    {
        this.jsonStorageRepository = Objects.requireNonNull(jsonStorageRepository, "jsonStorageRepository");
        this.documentRepository = Objects.requireNonNull(documentRepository, "documentRepository");
        this.canonicalExportService = Objects.requireNonNull(canonicalExportService, "canonicalExportService");
        this.organizationRepository = Objects.requireNonNull(organizationRepository, "organizationRepository");
        this.reportingPeriodRepository = Objects.requireNonNull(reportingPeriodRepository, "reportingPeriodRepository");
        this.eventRepository = Objects.requireNonNull(eventRepository, "eventRepository");
        this.sclxDocumentRepository = Objects.requireNonNull(sclxDocumentRepository, "sclxDocumentRepository");
        this.outstandingItemRepository = Objects.requireNonNull(outstandingItemRepository, "outstandingItemRepository");
        this.otherAssetItemRepository = Objects.requireNonNull(otherAssetItemRepository, "otherAssetItemRepository");
        this.assetRepository = Objects.requireNonNull(assetRepository, "assetRepository");
        this.supplyRepository = Objects.requireNonNull(supplyRepository, "supplyRepository");
    }

    public SclxDocument exportDocument(String runId) throws SQLException
    {
        String normalizedRunId = requireRunId(runId);
        Optional<String> rawPayload = this.jsonStorageRepository.load(rawStorageKey(normalizedRunId));

        SclxDocument exported = rawPayload
            .map(this::parseRawPayload)
            .orElseGet(() -> assembleFromRepositories(normalizedRunId));

        return withManifest(exported, normalizedRunId, rawPayload.isPresent());
    }

    private SclxDocument assembleFromRepositories(String runId)
    {
        try
        {
            SclxDocument canonical = this.canonicalExportService.exportDocument();
            SclxDocument.Organization organization = runScopedSingleton(this.organizationRepository.loadAll(), runId, SclxDocument.Organization.class);
            SclxDocument.ReportingPeriod reportingPeriod = runScopedSingleton(this.reportingPeriodRepository.loadAll(), runId, SclxDocument.ReportingPeriod.class);

            return new SclxDocument(
                canonical.format(),
                canonical.version(),
                canonical.exportedAt(),
                organization != null ? organization : canonical.organization(),
                reportingPeriod != null ? reportingPeriod : canonical.reportingPeriod(),
                canonical.chartOfAccounts(),
                canonical.funds(),
                canonical.budgets(),
                canonical.people(),
                runScopedList(this.eventRepository.loadAll(), runId, SclxDocument.Event.class),
                runScopedList(this.sclxDocumentRepository.loadAll(), runId, SclxDocument.Document.class),
                canonical.transactions(),
                canonical.bankingItems(),
                runScopedList(this.outstandingItemRepository.loadAll(), runId, SclxDocument.OutstandingItem.class),
                runScopedList(this.otherAssetItemRepository.loadAll(), runId, SclxDocument.OtherAssetItem.class),
                canonical.supplementalItems(),
                runScopedList(this.assetRepository.loadAll(), runId, SclxDocument.Asset.class),
                runScopedList(this.supplyRepository.loadAll(), runId, SclxDocument.Supply.class),
                canonical.bankStatementImports(),
                canonical.extensions());
        }
        catch (SQLException ex)
        {
            throw new IllegalStateException("Failed to assemble SCLX export for runId " + runId, ex);
        }
    }

    private SclxDocument withManifest(SclxDocument document, String runId, boolean usedRawPayload) throws SQLException
    {
        Map<String, Object> extensions = new LinkedHashMap<>();
        if (document.extensions() != null)
        {
            extensions.putAll(document.extensions());
        }

        Map<String, Object> manifest = new LinkedHashMap<>();
        manifest.put("runId", runId);
        manifest.put("source", usedRawPayload ? "raw" : "assembled");
        manifest.put("rawPayloadKey", rawStorageKey(runId));

        Optional<String> importSummary = this.documentRepository.find(IMPORT_SUMMARY_PREFIX + runId);
        importSummary.ifPresent(summary -> manifest.put("importSummary", parseGenericJson(summary)));

        extensions.put("manifest", manifest);

        return new SclxDocument(
            document.format(),
            document.version(),
            document.exportedAt(),
            document.organization(),
            document.reportingPeriod(),
            document.chartOfAccounts(),
            document.funds(),
            document.budgets(),
            document.people(),
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

    private SclxDocument parseRawPayload(String payload)
    {
        try
        {
            return MAPPER.readValue(payload, SclxDocument.class);
        }
        catch (JsonProcessingException ex)
        {
            throw new IllegalStateException("Failed to parse stored raw SCLX payload.", ex);
        }
    }

    private Object parseGenericJson(String payload)
    {
        try
        {
            return MAPPER.readValue(payload, new TypeReference<Map<String, Object>>() { });
        }
        catch (JsonProcessingException ignored)
        {
            return payload;
        }
    }

    private static String requireRunId(String runId)
    {
        if (runId == null || runId.isBlank())
        {
            throw new IllegalArgumentException("runId must not be null or blank");
        }
        return runId;
    }

    private static String rawStorageKey(String runId)
    {
        return RAW_PREFIX + runId;
    }

    private <T> List<T> runScopedList(Map<String, ?> rows, String runId, Class<T> targetType)
    {
        String runPrefix = runId + "::";
        List<T> converted = new ArrayList<>();
        for (Map.Entry<String, ?> entry : rows.entrySet())
        {
            if (entry.getKey().startsWith(runPrefix))
            {
                converted.add(MAPPER.convertValue(entry.getValue(), targetType));
            }
        }
        return List.copyOf(converted);
    }

    private <T> T runScopedSingleton(Map<String, ?> rows, String runId, Class<T> targetType)
    {
        List<T> converted = runScopedList(rows, runId, targetType);
        return converted.isEmpty() ? null : converted.get(0);
    }
}
