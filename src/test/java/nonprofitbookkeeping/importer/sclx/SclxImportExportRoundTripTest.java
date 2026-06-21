package nonprofitbookkeeping.importer.sclx;

import nonprofitbookkeeping.TestDatabase;
import nonprofitbookkeeping.model.Account;
import nonprofitbookkeeping.persistence.AccountRepository;
import nonprofitbookkeeping.persistence.DocumentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Verifies the post-import export policy.
 *
 * <p>The complete source SCLX document is deliberately not retained. Exports
 * are reconstructed from the typed and canonical application records, so
 * modeled values survive while unknown source-only formatting and fields do
 * not.</p>
 */
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
    void importDoesNotRetainRawSourceAndCanonicalExportContainsImportedAccount()
        throws Exception
    {
        String runId = "run-canonical-roundtrip";
        String rawJson = sampleSourceJson();

        new SclxImportService().importFile(
            writeSclx(rawJson),
            new NonprofitBookkeepingSclxImportTarget(),
            options(runId));

        DocumentRepository documentRepository = new DocumentRepository();
        assertTrue(documentRepository.find("sclx.raw." + runId).isEmpty());
        assertTrue(documentRepository.find("sclx.raw.latest").isEmpty());

        SclxDocument exported =
            new NonprofitBookkeepingSclxExportService().exportDocument();

        SclxDocument.Account cash = exported.chartOfAccounts().stream()
            .filter(account -> "acct-1000".equals(account.accountId()))
            .findFirst()
            .orElseThrow();
        assertEquals("Cash", cash.name());
        assertEquals("ASSET", cash.type());
    }

    @Test
    void canonicalExportReflectsChangesMadeAfterImport() throws Exception
    {
        String runId = "run-canonical-after-edit";

        new SclxImportService().importFile(
            writeSclx(sampleSourceJson()),
            new NonprofitBookkeepingSclxImportTarget(),
            options(runId));

        AccountRepository accountRepository = new AccountRepository();
        Account importedAccount = accountRepository.listAll().stream()
            .filter(account -> "acct-1000".equals(account.getAccountNumber()))
            .findFirst()
            .orElseThrow();
        importedAccount.setName("Name changed by app interface");
        accountRepository.upsert(importedAccount);

        SclxDocument exported =
            new NonprofitBookkeepingSclxExportService().exportDocument();
        SclxDocument.Account changed = exported.chartOfAccounts().stream()
            .filter(account -> "acct-1000".equals(account.accountId()))
            .findFirst()
            .orElseThrow();

        assertEquals("Name changed by app interface", changed.name());
    }

    @Test
    void unknownRawFieldsAreNotReintroducedIntoCanonicalExport()
        throws Exception
    {
        String runId = "run-canonical-no-unknowns";
        String rawJson = sampleSourceJson();

        new SclxImportService().importFile(
            writeSclx(rawJson),
            new NonprofitBookkeepingSclxImportTarget(),
            options(runId));

        String exported =
            new NonprofitBookkeepingSclxExportService().exportJson(runId);

        assertFalse(exported.contains("topLevelUnmodeled"));
        assertFalse(exported.contains("extraUnmodeled"));
        assertFalse(exported.contains("keep-exact"));
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
