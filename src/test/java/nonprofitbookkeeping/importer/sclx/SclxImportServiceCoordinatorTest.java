package nonprofitbookkeeping.importer.sclx;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import nonprofitbookkeeping.persistence.DocumentRepository;
import org.junit.jupiter.api.Test;

class SclxImportServiceCoordinatorTest
{
    @Test
    void coordinatesEachImportLifecycleStepExactlyOnce()
    {
        SclxDocument document = parseDocument();
        CountingTarget target = new CountingTarget();

        SclxImportResult result = SclxImportService.importDocument(
            document, target, SclxImportOptions.defaults());

        assertEquals("1.3", result.version());
        assertEquals(1, target.beginImportCount);
        assertEquals(1, target.organizationImportCount);
        assertEquals(1, target.completeImportCount);
    }

    @Test
    void clearsThreadScopedRawPayloadWhenImportFails() throws Exception
    {
        DocumentRepository repository = new DocumentRepository();
        String key = "sclx.raw.coordinator-test";
        repository.upsert(key, "temporary payload");
        assertTrue(repository.find(key).isPresent());

        CountingTarget target = new CountingTarget()
        {
            @Override
            public void beginImport(SclxDocument document,
                SclxImportOptions options)
            {
                super.beginImport(document, options);
                throw new IllegalStateException("expected test failure");
            }
        };

        assertThrows(IllegalStateException.class,
            () -> SclxImportService.importDocument(
                parseDocument(), target, SclxImportOptions.defaults()));

        assertTrue(repository.find(key).isEmpty());
    }

    private static SclxDocument parseDocument()
    {
        return new SclxParser().parse("""
            {
              "format": "SCLX",
              "version": "1.3",
              "organization": {
                "organizationId": "org-1",
                "name": "Test Organization",
                "baseCurrency": "USD"
              }
            }
            """);
    }

    private static class CountingTarget implements SclxImportTarget
    {
        private int beginImportCount;
        private int organizationImportCount;
        private int completeImportCount;

        @Override
        public void beginImport(SclxDocument document,
            SclxImportOptions options)
        {
            this.beginImportCount++;
        }

        @Override
        public void importCompatibility(
            SclxDocument.Compatibility compatibility)
        {
        }

        @Override
        public void importOrganization(
            SclxDocument.Organization organization)
        {
            this.organizationImportCount++;
        }

        @Override
        public void importReportingPeriod(
            SclxDocument.ReportingPeriod reportingPeriod)
        {
        }

        @Override
        public void importAccounts(List<SclxDocument.Account> accounts)
        {
        }

        @Override
        public void importFunds(List<SclxDocument.Fund> funds)
        {
        }

        @Override
        public void importBudgets(List<SclxDocument.Budget> budgets)
        {
        }

        @Override
        public void importPeople(List<SclxDocument.Person> people)
        {
        }

        @Override
        public void importBankAccounts(
            List<SclxDocument.BankAccount> bankAccounts)
        {
        }

        @Override
        public void importOfficeAssignments(
            List<SclxDocument.OfficeAssignment> officeAssignments)
        {
        }

        @Override
        public void importCommitteeMemberships(
            List<SclxDocument.CommitteeMembership> committeeMemberships)
        {
        }

        @Override
        public void importEvents(List<SclxDocument.Event> events)
        {
        }

        @Override
        public void importDocuments(List<SclxDocument.Document> documents)
        {
        }

        @Override
        public void importTransactions(
            List<SclxDocument.Transaction> transactions)
        {
        }

        @Override
        public void importOutstandingItems(
            List<SclxDocument.OutstandingItem> outstandingItems)
        {
        }

        @Override
        public void importOtherAssetItems(
            List<SclxDocument.OtherAssetItem> otherAssetItems)
        {
        }

        @Override
        public void importSupplementalItems(
            List<SclxDocument.SupplementalItem> supplementalItems)
        {
        }

        @Override
        public void importAssets(List<SclxDocument.Asset> assets)
        {
        }

        @Override
        public void importSupplies(List<SclxDocument.Supply> supplies)
        {
        }

        @Override
        public void importBankingItems(
            List<SclxDocument.BankingItem> bankingItems)
        {
        }

        @Override
        public void importBankStatementImports(
            List<SclxDocument.BankStatementImport> bankStatementImports)
        {
        }

        @Override
        public void completeImport(SclxImportResult result)
        {
            this.completeImportCount++;
        }
    }
}
