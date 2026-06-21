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
    void importFileDoesNotPersistRawSource() throws IOException
    {
        Path tempFile = Files.createTempFile("sclx-import-service-test", ".json");
        String rawJson = "{\n  \"format\":\"SCLX\",\n  \"version\":\"1.3\",\n  \"unknown\":\"do not retain me\"\n}\n";
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

            assertNull(target.rawRunId);
            assertNull(target.rawSourceJson);
            assertFalse(target.rawPersistedBeforeBeginImport);
            assertTrue(target.beginImportCalled);
            assertEquals("1.3", result.version());
        }
        finally
        {
            Files.deleteIfExists(tempFile);
        }
    }

    @Test
    void skipsZeroLinesAndTransactionsWithoutPostingLines() throws IOException
    {
        Path tempFile = Files.createTempFile("sclx-zero-line-test", ".json");
        String rawJson = """
            {
              "format":"SCLX",
              "version":"1.3",
              "transactions":[
                {
                  "transactionId":"zero-only",
                  "description":"Workbook reference only",
                  "workbookLink":{"sheetKey":"Ledger","ledgerRowIndex":24},
                  "lines":[
                    {
                      "lineId":"zero-line",
                      "accountId":"Transfer Out",
                      "debit":"0.00",
                      "credit":"0.00"
                    }
                  ]
                },
                {
                  "transactionId":"mixed",
                  "description":"Posting transaction with an empty workbook split",
                  "lines":[
                    {
                      "lineId":"empty-line",
                      "accountId":"Expense",
                      "debit":"0.00",
                      "credit":"0.00"
                    },
                    {
                      "lineId":"posting-line",
                      "accountId":"Expense",
                      "debit":"25.00",
                      "credit":"0.00"
                    }
                  ]
                },
                {
                  "transactionId":"header-only",
                  "description":"Voided workbook row",
                  "lines":[]
                }
              ]
            }
            """;
        Files.writeString(tempFile, rawJson);

        try
        {
            RecordingTarget target = new RecordingTarget();
            SclxImportResult result = new SclxImportService().importFile(
                tempFile,
                target,
                SclxImportOptions.defaults());

            assertEquals(1, target.transactions.size());
            assertEquals("mixed", target.transactions.get(0).transactionId());
            assertEquals(1, target.transactions.get(0).lines().size());
            assertEquals("posting-line", target.transactions.get(0).lines().get(0).lineId());
            assertEquals(1, result.transactionCount());
            assertEquals(1, result.transactionLineCount());
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

    @Test
    void importFileAcceptsArrayDatesAcrossReportingEventDocumentAndTransaction() throws IOException
    {
        Path tempFile = Files.createTempFile("sclx-import-service-multi-array-date-test", ".json");
        String rawJson = """
            {
              "format":"SCLX",
              "version":"1.3",
              "organization":{
                "organizationId":"org-1",
                "name":"Org",
                "baseCurrency":"USD"
              },
              "reportingPeriod":{
                "startDate":[2026,1,1],
                "endDate":[2026,6,30],
                "label":"Q2",
                "fiscalYear":2026,
                "periodType":"QUARTER"
              },
              "events":[
                {
                  "eventId":"event-1",
                  "name":"Spring Crown",
                  "startDate":[2026,5,2],
                  "endDate":[2026,5,3]
                }
              ],
              "documents":[
                {
                  "documentId":"doc-1",
                  "documentType":"RECEIPT",
                  "documentDate":[2026,5,10]
                }
              ],
              "transactions":[
                {
                  "transactionId":"txn-1",
                  "transactionDate":[2026,5,11],
                  "postingDate":[2026,5,12],
                  "description":"Sample",
                  "lines":[
                    {
                      "lineId":"line-1",
                      "accountId":"Expense",
                      "debit":"10.00",
                      "credit":"0.00"
                    }
                  ]
                }
              ]
            }
            """;
        Files.writeString(tempFile, rawJson);

        try
        {
            RecordingTarget target = new RecordingTarget();
            SclxImportResult result = new SclxImportService().importFile(tempFile, target, SclxImportOptions.defaults());

            assertEquals("1.3", result.version());
            assertEquals(java.time.LocalDate.of(2026, 1, 1), target.reportingPeriod.startDate());
            assertEquals(java.time.LocalDate.of(2026, 6, 30), target.reportingPeriod.endDate());
            assertEquals(java.time.LocalDate.of(2026, 5, 2), target.events.get(0).startDate());
            assertEquals(java.time.LocalDate.of(2026, 5, 3), target.events.get(0).endDate());
            assertEquals(java.time.LocalDate.of(2026, 5, 10), target.documents.get(0).documentDate());
            assertEquals(java.time.LocalDate.of(2026, 5, 11), target.transactions.get(0).transactionDate());
            assertEquals(java.time.LocalDate.of(2026, 5, 12), target.transactions.get(0).postingDate());
        }
        finally
        {
            Files.deleteIfExists(tempFile);
        }
    }

    @Test
    void importFileParseFailureIncludesSourcePath() throws IOException
    {
        Path tempFile = Files.createTempFile("sclx-import-service-bad-json-test", ".json");
        Files.writeString(tempFile, "{\"format\":\"SCLX\",\"version\":\"1.3\",\"organization\":");

        try
        {
            RecordingTarget target = new RecordingTarget();
            SclxImportException ex = assertThrows(
                SclxImportException.class,
                () -> new SclxImportService().importFile(tempFile, target, SclxImportOptions.defaults()));

            assertTrue(ex.getMessage().contains("Failed to parse SCLX JSON from file"));
            assertTrue(ex.getMessage().contains(tempFile.toString()));
        }
        finally
        {
            Files.deleteIfExists(tempFile);
        }
    }

    @Test
    void importFileAcceptsArrayDatesInBankingAndBankStatementSections() throws IOException
    {
        Path tempFile = Files.createTempFile("sclx-import-service-banking-array-date-test", ".json");
        String rawJson = """
            {
              "format":"SCLX",
              "version":"1.3",
              "bankStatementImports":[
                {
                  "importId":"imp-1",
                  "sourceFormat":"CSV",
                  "statementKind":"MONTHLY",
                  "statementStart":[2026,1,1],
                  "statementEnd":[2026,1,31]
                }
              ],
              "bankingItems":[
                {
                  "bankingItemId":"banking-1",
                  "kind":"DEPOSIT",
                  "depositDate":[2026,1,15],
                  "amount":"10.00",
                  "ofx":{
                    "fitId":"fit-1",
                    "datePosted":[2026,1,16],
                    "dateUser":[2026,1,17],
                    "dateAvailable":[2026,1,18]
                  }
                }
              ]
            }
            """;
        Files.writeString(tempFile, rawJson);

        try
        {
            RecordingTarget target = new RecordingTarget();
            SclxImportResult result = new SclxImportService().importFile(tempFile, target, SclxImportOptions.defaults());

            assertEquals("1.3", result.version());
            assertEquals(java.time.LocalDate.of(2026, 1, 1), target.bankStatementImports.get(0).statementStart());
            assertEquals(java.time.LocalDate.of(2026, 1, 31), target.bankStatementImports.get(0).statementEnd());
            assertEquals(java.time.LocalDate.of(2026, 1, 15), target.bankingItems.get(0).depositDate());
            assertEquals(java.time.LocalDate.of(2026, 1, 16), target.bankingItems.get(0).ofx().datePosted());
            assertEquals(java.time.LocalDate.of(2026, 1, 17), target.bankingItems.get(0).ofx().dateUser());
            assertEquals(java.time.LocalDate.of(2026, 1, 18), target.bankingItems.get(0).ofx().dateAvailable());
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
        private SclxDocument.ReportingPeriod reportingPeriod;
        private List<SclxDocument.Event> events = List.of();
        private List<SclxDocument.Document> documents = List.of();
        private List<SclxDocument.Transaction> transactions = List.of();
        private List<SclxDocument.BankingItem> bankingItems = List.of();
        private List<SclxDocument.BankStatementImport> bankStatementImports = List.of();

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
        @Override public void importReportingPeriod(SclxDocument.ReportingPeriod reportingPeriod) { this.reportingPeriod = reportingPeriod; }
        @Override public void importAccounts(List<SclxDocument.Account> accounts) {}
        @Override public void importFunds(List<SclxDocument.Fund> funds) {}
        @Override public void importBudgets(List<SclxDocument.Budget> budgets) {}
        @Override public void importPeople(List<SclxDocument.Person> people) {}
        @Override public void importBankAccounts(List<SclxDocument.BankAccount> bankAccounts) {}
        @Override public void importOfficeAssignments(List<SclxDocument.OfficeAssignment> officeAssignments) {}
        @Override public void importCommitteeMemberships(List<SclxDocument.CommitteeMembership> committeeMemberships) {}
        @Override public void importEvents(List<SclxDocument.Event> events) { this.events = events; }
        @Override public void importDocuments(List<SclxDocument.Document> documents) { this.documents = documents; }
        @Override public void importTransactions(List<SclxDocument.Transaction> transactions) { this.transactions = transactions; }
        @Override public void importOutstandingItems(List<SclxDocument.OutstandingItem> outstandingItems) {}
        @Override public void importOtherAssetItems(List<SclxDocument.OtherAssetItem> otherAssetItems) {}
        @Override public void importSupplementalItems(List<SclxDocument.SupplementalItem> supplementalItems) {}
        @Override public void importAssets(List<SclxDocument.Asset> assets) {}
        @Override public void importSupplies(List<SclxDocument.Supply> supplies) {}
        @Override public void importBankingItems(List<SclxDocument.BankingItem> bankingItems) { this.bankingItems = bankingItems; }
        @Override public void importBankStatementImports(List<SclxDocument.BankStatementImport> bankStatementImports) { this.bankStatementImports = bankStatementImports; }
        @Override public void completeImport(SclxImportResult result) {}
    }
}
