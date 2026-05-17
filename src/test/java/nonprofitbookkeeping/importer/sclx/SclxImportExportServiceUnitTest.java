package nonprofitbookkeeping.importer.sclx;

import nonprofitbookkeeping.persistence.DocumentRepository;
import nonprofitbookkeeping.persistence.JsonStorageRepository;
import nonprofitbookkeeping.persistence.sclx.AssetRepository;
import nonprofitbookkeeping.persistence.sclx.EventRepository;
import nonprofitbookkeeping.persistence.sclx.OrganizationRepository;
import nonprofitbookkeeping.persistence.sclx.OtherAssetItemRepository;
import nonprofitbookkeeping.persistence.sclx.OutstandingItemRepository;
import nonprofitbookkeeping.persistence.sclx.ReportingPeriodRepository;
import nonprofitbookkeeping.persistence.sclx.SupplyRepository;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SclxImportExportServiceUnitTest
{
    @Test
    void importAndExportServicesPreserveRawPayloadAndExposeManifest() throws Exception
    {
        String runId = "run-unit-001";
        Path file = Files.createTempFile("sclx-import-export-unit", ".json");
        String rawJson = """
            {
              "format":"SCLX",
              "version":"1.3",
              "chartOfAccounts":[{"accountId":"acct-1","Name":"Cash","Type":"ASSET","IncreaseSide":"DEBIT"}],
              "unknown":"keep"
            }
            """;
        Files.writeString(file, rawJson);

        try
        {
            RecordingImportTarget target = new RecordingImportTarget();
            SclxImportOptions options = new SclxImportOptions(
                true,
                true,
                true,
                true,
                null,
                runId,
                AccountImportMode.AS_IS,
                Map.of());

            SclxImportResult result = new SclxImportService().importFile(file, target, options);

            assertEquals(runId, target.runId);
            assertEquals(rawJson, target.rawSourceJson);
            assertEquals("1.3", result.version());

            SclxExportService exportService = new SclxExportService(
                new StubJsonStorageRepository(Map.of("sclx.raw." + runId, rawJson)),
                new StubDocumentRepository(Map.of("sclx.importSummary." + runId, "{\"warnings\":[]}")),
                new NonprofitBookkeepingSclxExportService(),
                new OrganizationRepository(),
                new ReportingPeriodRepository(),
                new EventRepository(),
                new nonprofitbookkeeping.persistence.sclx.DocumentRepository(),
                new OutstandingItemRepository(),
                new OtherAssetItemRepository(),
                new AssetRepository(),
                new SupplyRepository());

            SclxDocument exported = exportService.exportDocument(runId);

            assertEquals("SCLX", exported.format());
            assertEquals("1.3", exported.version());
            Object manifestRaw = exported.extensions().get("manifest");
            assertTrue(manifestRaw instanceof Map<?, ?>);
            Map<?, ?> manifest = (Map<?, ?>) manifestRaw;
            assertEquals("raw", manifest.get("source"));
            assertEquals(runId, manifest.get("runId"));
        }
        finally
        {
            Files.deleteIfExists(file);
        }
    }

    private static final class RecordingImportTarget implements SclxImportTarget
    {
        private String rawSourceJson;
        private String runId;

        @Override
        public void persistRawSource(String rawSourceJson, SclxImportOptions options)
        {
            this.rawSourceJson = rawSourceJson;
            this.runId = options.effectiveImportRunId();
        }

        @Override public void beginImport(SclxDocument document, SclxImportOptions options) {}
        @Override public void importCompatibility(SclxDocument.Compatibility compatibility) {}
        @Override public void importOrganization(SclxDocument.Organization organization) {}
        @Override public void importReportingPeriod(SclxDocument.ReportingPeriod reportingPeriod) {}
        @Override public void importAccounts(List<SclxDocument.Account> accounts) {}
        @Override public void importFunds(List<SclxDocument.Fund> funds) {}
        @Override public void importBudgets(List<SclxDocument.Budget> budgets) {}
        @Override public void importPeople(List<SclxDocument.Person> people) {}
        @Override public void importBankAccounts(List<SclxDocument.BankAccount> bankAccounts) {}
        @Override public void importOfficeAssignments(List<SclxDocument.OfficeAssignment> officeAssignments) {}
        @Override public void importCommitteeMemberships(List<SclxDocument.CommitteeMembership> committeeMemberships) {}
        @Override public void importEvents(List<SclxDocument.Event> events) {}
        @Override public void importDocuments(List<SclxDocument.Document> documents) {}
        @Override public void importTransactions(List<SclxDocument.Transaction> transactions) {}
        @Override public void importOutstandingItems(List<SclxDocument.OutstandingItem> outstandingItems) {}
        @Override public void importOtherAssetItems(List<SclxDocument.OtherAssetItem> otherAssetItems) {}
        @Override public void importSupplementalItems(List<SclxDocument.SupplementalItem> supplementalItems) {}
        @Override public void importAssets(List<SclxDocument.Asset> assets) {}
        @Override public void importSupplies(List<SclxDocument.Supply> supplies) {}
        @Override public void importBankingItems(List<SclxDocument.BankingItem> bankingItems) {}
        @Override public void importBankStatementImports(List<SclxDocument.BankStatementImport> bankStatementImports) {}
        @Override public void completeImport(SclxImportResult result) {}
    }

    private static final class StubJsonStorageRepository extends JsonStorageRepository
    {
        private final Map<String, String> values;

        private StubJsonStorageRepository(Map<String, String> values)
        {
            this.values = values;
        }

        @Override
        public Optional<String> load(String key)
        {
            return Optional.ofNullable(this.values.get(key));
        }
    }

    private static final class StubDocumentRepository extends DocumentRepository
    {
        private final Map<String, String> values;

        private StubDocumentRepository(Map<String, String> values)
        {
            this.values = values;
        }

        @Override
        public Optional<String> find(String name)
        {
            return Optional.ofNullable(this.values.get(name));
        }
    }
}
