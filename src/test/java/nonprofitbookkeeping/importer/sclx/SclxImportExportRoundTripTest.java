package nonprofitbookkeeping.importer.sclx;

import nonprofitbookkeeping.TestDatabase;
import nonprofitbookkeeping.model.Account;
import nonprofitbookkeeping.persistence.AccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SclxImportExportRoundTripTest
{
    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() throws SQLException
    {
        TestDatabase.reset(tempDir);
    }

    @Test
    void fullCycleRoundTripWithoutInBetweenChangesPreservesExactRawSource() throws Exception
    {
        String runId = "run-roundtrip-no-mod";
        String rawJson = sampleSourceJson();
        Path sourceFile = writeSclx(rawJson);

        new SclxImportService().importFile(
            sourceFile,
            new NonprofitBookkeepingSclxImportTarget(),
            options(runId));

        String exported = new NonprofitBookkeepingSclxExportService().exportJson(runId);

        assertEquals(rawJson, exported);
    }

    @Test
    void fullCycleRoundTripWithInBetweenProgramChangesStillPreservesExactRawSource() throws Exception
    {
        String runId = "run-roundtrip-with-mod";
        String rawJson = sampleSourceJson();
        Path sourceFile = writeSclx(rawJson);

        new SclxImportService().importFile(
            sourceFile,
            new NonprofitBookkeepingSclxImportTarget(),
            options(runId));

        AccountRepository accountRepository = new AccountRepository();
        Account importedAccount = accountRepository.listAll().stream().findFirst().orElseThrow();
        importedAccount.setName("Name changed by app interface");
        accountRepository.upsert(importedAccount);

        String exported = new NonprofitBookkeepingSclxExportService().exportJson(runId);

        assertEquals(rawJson, exported);
    }

    @Test
    void fullCycleRoundTripWithOrganizationFiscalYearDatesPreservesExactRawSource() throws Exception
    {
        String runId = "run-roundtrip-org-fiscal-dates";
        String rawJson = """
            {
              "format":"SCLX",
              "version":"1.3",
              "organization":{
                "organizationId":"org-1",
                "name":"Example Org",
                "baseCurrency":"USD",
                "fiscalYearStart":"2025-01-01",
                "fiscalYearEnd":"2025-12-31"
              }
            }
            """;
        Path sourceFile = writeSclx(rawJson);

        new SclxImportService().importFile(
            sourceFile,
            new NonprofitBookkeepingSclxImportTarget(),
            options(runId));

        String exported = new NonprofitBookkeepingSclxExportService().exportJson(runId);

        assertEquals(rawJson, exported);
    }

    @Test
    void fullCycleRoundTripWithReportingPeriodArrayDatesPreservesExactRawSource() throws Exception
    {
        String runId = "run-roundtrip-reporting-period-array-dates";
        String rawJson = """
            {
              "format":"SCLX",
              "version":"1.3",
              "reportingPeriod":{
                "startDate":[2026,1,1],
                "endDate":[2026,6,30],
                "label":"Q2 Report",
                "fiscalYear":2026,
                "periodType":"QUARTER"
              }
            }
            """;
        Path sourceFile = writeSclx(rawJson);

        new SclxImportService().importFile(
            sourceFile,
            new NonprofitBookkeepingSclxImportTarget(),
            options(runId));

        String exported = new NonprofitBookkeepingSclxExportService().exportJson(runId);

        assertEquals(rawJson, exported);
    }

    @Test
    void fullCycleRoundTripWithOutstandingItemArrayDatesPreservesExactRawSource() throws Exception
    {
        String runId = "run-roundtrip-outstanding-item-array-dates";
        String rawJson = """
            {
              "format":"SCLX",
              "version":"1.3",
              "outstandingItems":[
                {
                  "outstandingItemId":"outstanding-row-14",
                  "kind":"CHECK",
                  "ledgerLink":{"transactionId":"txn-1","lineId":"line-1"},
                  "dateSentOrReceived":[2026,5,22],
                  "incomingCheckOrTransferDate":[2026,5,23],
                  "dateShowsOnStatement":[2026,5,24],
                  "amount":"25.00",
                  "dateReversed":[2026,5,25],
                  "status":"OUTSTANDING"
                }
              ]
            }
            """;
        Path sourceFile = writeSclx(rawJson);

        new SclxImportService().importFile(
            sourceFile,
            new NonprofitBookkeepingSclxImportTarget(),
            options(runId));

        String exported = new NonprofitBookkeepingSclxExportService().exportJson(runId);

        assertEquals(rawJson, exported);
    }

    @Test
    void fullCycleRoundTripWithEventAndDocumentArrayDatesPreservesExactRawSource() throws Exception
    {
        String runId = "run-roundtrip-event-document-array-dates";
        String rawJson = """
            {
              "format":"SCLX",
              "version":"1.3",
              "events":[
                {
                  "eventId":"event-1",
                  "name":"Spring Event",
                  "startDate":[2026,5,1],
                  "endDate":[2026,5,2]
                }
              ],
              "documents":[
                {
                  "documentId":"doc-1",
                  "documentType":"RECEIPT",
                  "documentDate":[2026,5,3]
                }
              ]
            }
            """;
        Path sourceFile = writeSclx(rawJson);

        new SclxImportService().importFile(
            sourceFile,
            new NonprofitBookkeepingSclxImportTarget(),
            options(runId));

        String exported = new NonprofitBookkeepingSclxExportService().exportJson(runId);

        assertEquals(rawJson, exported);
    }

    @Test
    void fullCycleRoundTripWithAssetAndSupplyArrayDatesPreservesExactRawSource() throws Exception
    {
        String runId = "run-roundtrip-asset-supply-array-dates";
        String rawJson = """
            {
              "format":"SCLX",
              "version":"1.3",
              "assets":[
                {
                  "assetId":"asset-1",
                  "dateAcquired":[2026,4,1],
                  "description":"Chest"
                }
              ],
              "supplies":[
                {
                  "supplyId":"supply-1",
                  "itemNumber":"A-1",
                  "dateAcquired":[2026,4,2],
                  "description":"Paper"
                }
              ]
            }
            """;
        Path sourceFile = writeSclx(rawJson);

        new SclxImportService().importFile(
            sourceFile,
            new NonprofitBookkeepingSclxImportTarget(),
            options(runId));

        String exported = new NonprofitBookkeepingSclxExportService().exportJson(runId);

        assertEquals(rawJson, exported);
    }

    @Test
    void fullCycleRoundTripWithOtherAssetItemValueAndStatusPreservesExactRawSource() throws Exception
    {
        String runId = "run-roundtrip-other-asset-item-value-status";
        String rawJson = """
            {
              "format":"SCLX",
              "version":"1.3",
              "otherAssetItems":[
                {
                  "otherAssetItemId":"other-asset-1",
                  "ledgerLink":{"transactionId":"txn-1","lineId":"line-1"},
                  "paidTo":"Vendor",
                  "year":2026,
                  "reason":"Prepaid service",
                  "type":"OTHER",
                  "eventBudgetLabel":"Event budget",
                  "amountAsOfPriorYearEnd":"250.00",
                  "paidReturnedOnLedgerRowIndex":42,
                  "status":"OUTSTANDING"
                }
              ]
            }
            """;
        Path sourceFile = writeSclx(rawJson);

        new SclxImportService().importFile(
            sourceFile,
            new NonprofitBookkeepingSclxImportTarget(),
            options(runId));

        String exported = new NonprofitBookkeepingSclxExportService().exportJson(runId);

        assertEquals(rawJson, exported);
    }

    @Test
    void fullCycleRoundTripWithBankAccountOfficeAndCommitteePreservesExactRawSource() throws Exception
    {
        String runId = "run-roundtrip-bank-office-committee";
        String rawJson = """
            {
              "format":"SCLX",
              "version":"1.3",
              "bankAccounts":[
                {
                  "bankAccountId":"bank-1",
                  "accountName":"Checking",
                  "accountType":"CHECKING",
                  "currency":"USD"
                }
              ],
              "officeAssignments":[
                {
                  "officeAssignmentId":"office-1",
                  "personId":"person-1",
                  "roleTitle":"Treasurer",
                  "startDate":"2026-01-01",
                  "endDate":"2026-12-31",
                  "active":true
                }
              ],
              "committeeMemberships":[
                {
                  "committeeMembershipId":"committee-1",
                  "committeeType":"FINANCE",
                  "personId":"person-1",
                  "active":true
                }
              ]
            }
            """;
        Path sourceFile = writeSclx(rawJson);

        new SclxImportService().importFile(
            sourceFile,
            new NonprofitBookkeepingSclxImportTarget(),
            options(runId));

        String exported = new NonprofitBookkeepingSclxExportService().exportJson(runId);

        assertEquals(rawJson, exported);
    }

    @Test
    void fullCycleRoundTripWithCompatibilityPreservesExactRawSource() throws Exception
    {
        String runId = "run-roundtrip-compatibility";
        String rawJson = """
            {
              "format":"SCLX",
              "version":"1.3",
              "compatibility":{
                "minimumReaderVersion":"1.2",
                "lossyDowngradeTo":["1.2","1.0"]
              }
            }
            """;
        Path sourceFile = writeSclx(rawJson);

        new SclxImportService().importFile(
            sourceFile,
            new NonprofitBookkeepingSclxImportTarget(),
            options(runId));

        String exported = new NonprofitBookkeepingSclxExportService().exportJson(runId);

        assertEquals(rawJson, exported);
    }

    @Test
    void fullCycleRoundTripWithNestedGuardianEnumsAndDatesPreservesExactRawSource() throws Exception
    {
        String runId = "run-roundtrip-nested-guardian-enums-dates";
        String rawJson = """
            {
              "format":"SCLX",
              "version":"1.3",
              "assets":[
                {
                  "assetId":"asset-guard-1",
                  "dateAcquired":[2026,2,1],
                  "description":"Regalia",
                  "guardianshipDetails":{
                    "dateAsOf":[2026,2,2],
                    "confirmed":true,
                    "confirmationStatus":"CONFIRMED",
                    "notes":"checked"
                  },
                  "removalDetails":{
                    "approvedBy":"Seneschal",
                    "approvalDate":[2026,2,3],
                    "reason":"Transferred",
                    "numberRemoved":1,
                    "removed":true,
                    "removalType":"RETURNED"
                  }
                }
              ],
              "supplies":[
                {
                  "supplyId":"supply-guard-1",
                  "itemNumber":"S-1",
                  "dateAcquired":[2026,2,4],
                  "description":"Feast kit",
                  "guardianshipDetails":{
                    "dateAsOf":[2026,2,5],
                    "lastConfirmed":[2026,2,6],
                    "returned":false,
                    "notes":"ok"
                  },
                  "removalDetails":{
                    "approvedBy":"Exchequer",
                    "reason":"Disposed",
                    "numberRemoved":1,
                    "removed":true,
                    "removalType":"DESTROYED"
                  }
                }
              ]
            }
            """;
        Path sourceFile = writeSclx(rawJson);

        new SclxImportService().importFile(
            sourceFile,
            new NonprofitBookkeepingSclxImportTarget(),
            options(runId));

        String exported = new NonprofitBookkeepingSclxExportService().exportJson(runId);

        assertEquals(rawJson, exported);
    }

    private Path writeSclx(String rawJson) throws Exception
    {
        Path file = tempDir.resolve("source.sclx.json");
        Files.writeString(file, rawJson);
        return file;
    }

    private static SclxImportOptions options(String runId)
    {
        return new SclxImportOptions(
            true,
            true,
            true,
            true,
            null,
            runId,
            AccountImportMode.AS_IS,
            java.util.Map.of());
    }

    private static String sampleSourceJson()
    {
        return """
            {
              "format":"SCLX",
              "version":"1.3",
              "topLevelUnmodeled":"keep-exact",
              "chartOfAccounts":[
                {
                  "accountId":"acct-1000",
                  "Number":"1000",
                  "Name":"Cash",
                  "Type":"ASSET",
                  "IncreaseSide":"DEBIT",
                  "extraUnmodeled":"still-here"
                }
              ],
              "extensions":{
                "nestedUnknown":{"flag":true}
              }
            }
            """;
    }
}
