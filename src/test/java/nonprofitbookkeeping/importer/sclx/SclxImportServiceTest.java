package nonprofitbookkeeping.importer.sclx;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SclxImportServiceTest
{
    @Test
    void importFilePersistsRawSourceBeforeMapping() throws IOException
    {
        Path tempFile = Files.createTempFile("sclx-import-service-test", ".json");
        String rawJson = "{\n  \"format\":\"SCLX\",\n  \"version\":\"1.3\",\n  \"unknown\":\"preserve me\"\n}\n";
        Files.writeString(tempFile, rawJson);

        try
        {
            SclxImportOptions options = new SclxImportOptions(
                true,
                true,
                true,
                true,
                null,
                "run-raw-001",
                AccountImportMode.AS_IS,
                java.util.Map.of());
            RecordingTarget target = new RecordingTarget();

            SclxImportResult result = new SclxImportService().importFile(tempFile, target, options);

            assertEquals("run-raw-001", target.rawRunId);
            assertEquals(rawJson, target.rawSourceJson);
            assertTrue(target.rawPersistedBeforeBeginImport);
            assertEquals("1.3", result.version());
        }
        finally
        {
            Files.deleteIfExists(tempFile);
        }
    }

    @Test
    void importFileAcceptsOrganizationFiscalYearArrayDates() throws IOException
    {
        Path tempFile = Files.createTempFile("sclx-import-service-array-date-test", ".json");
        String rawJson = """
            {
              "format":"SCLX",
              "version":"1.3",
              "organization":{
                "organizationId":"org-1",
                "name":"Org",
                "baseCurrency":"USD",
                "fiscalYearStart":[2025,1,1],
                "fiscalYearEnd":[2025,12,31]
              }
            }
            """;
        Files.writeString(tempFile, rawJson);

        try
        {
            RecordingTarget target = new RecordingTarget();
            SclxImportResult result = new SclxImportService().importFile(tempFile, target, SclxImportOptions.defaults());

            assertEquals("1.3", result.version());
            assertEquals(java.time.LocalDate.of(2025, 1, 1), target.organization.fiscalYearStart());
            assertEquals(java.time.LocalDate.of(2025, 12, 31), target.organization.fiscalYearEnd());
        }
        finally
        {
            Files.deleteIfExists(tempFile);
        }
    }

    private static final class RecordingTarget implements SclxImportTarget
    {
        private boolean beginImportCalled;
        private boolean rawPersistedBeforeBeginImport;
        private String rawSourceJson;
        private String rawRunId;
        private SclxDocument.Organization organization;

        @Override
        public void persistRawSource(String rawSourceJson, SclxImportOptions options)
        {
            this.rawSourceJson = rawSourceJson;
            this.rawRunId = options.effectiveImportRunId();
            this.rawPersistedBeforeBeginImport = !beginImportCalled;
        }

        @Override
        public void beginImport(SclxDocument document, SclxImportOptions options)
        {
            this.beginImportCalled = true;
        }

        @Override public void importCompatibility(SclxDocument.Compatibility compatibility) {}
        @Override public void importOrganization(SclxDocument.Organization organization) { this.organization = organization; }
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
}
