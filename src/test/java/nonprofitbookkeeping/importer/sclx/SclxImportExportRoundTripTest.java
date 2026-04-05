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
