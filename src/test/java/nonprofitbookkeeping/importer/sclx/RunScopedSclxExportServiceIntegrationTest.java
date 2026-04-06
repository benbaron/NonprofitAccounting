package nonprofitbookkeeping.importer.sclx;

import nonprofitbookkeeping.TestDatabase;
import nonprofitbookkeeping.persistence.DocumentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class RunScopedSclxExportServiceIntegrationTest
{
    private static final String RUN_ID = "integration-run-1";

    @TempDir
    Path tempDir;

    private SclxImportService importService;
    private NonprofitBookkeepingSclxImportTarget importTarget;
    private RunScopedSclxExportService exportService;
    private DocumentRepository documentRepository;

    @BeforeEach
    void setUp() throws SQLException
    {
        TestDatabase.reset(tempDir);
        importService = new SclxImportService();
        importTarget = new NonprofitBookkeepingSclxImportTarget();
        exportService = new RunScopedSclxExportService();
        documentRepository = new DocumentRepository();
    }

    @Test
    void exportByRunId_prefersPreservedRawPayload() throws SQLException
    {
        SclxDocument source = sampleDocumentWithRootExtension();

        importService.importDocument(source, importTarget, new SclxImportOptions(
            true,
            true,
            true,
            true,
            null,
            RUN_ID,
            AccountImportMode.AS_IS,
            Map.of()));

        SclxDocument exported = exportService.exportByRunId(RUN_ID);

        assertNotNull(exported.extensions());
        assertEquals("raw-preserved", exported.extensions().get("sourceMarker"));
        @SuppressWarnings("unchecked")
        Map<String, Object> metadata = (Map<String, Object>) exported.extensions().get("exportMetadata");
        assertNotNull(metadata);
        assertEquals(RUN_ID, metadata.get("runId"));
        assertEquals(List.of(), metadata.get("warnings"));
        assertTrue(metadata.containsKey("canonicalWriteCounts"));
        assertTrue(metadata.containsKey("rawStagingWriteCounts"));
    }

    @Test
    void exportByRunId_assemblesFromRepositoriesWhenRawIsMissing() throws SQLException
    {
        SclxDocument source = sampleDocumentWithRootExtension();
        importService.importDocument(source, importTarget, new SclxImportOptions(
            true,
            true,
            true,
            true,
            null,
            RUN_ID,
            AccountImportMode.AS_IS,
            Map.of()));

        documentRepository.delete("sclx.raw." + RUN_ID);

        SclxDocument exported = exportService.exportByRunId(RUN_ID);

        assertNotNull(exported);
        assertFalse(exported.chartOfAccounts().isEmpty(), "Fallback export should include canonical accounts.");
        assertFalse(exported.transactions().isEmpty(), "Fallback export should include canonical transactions.");

        @SuppressWarnings("unchecked")
        Map<String, Object> metadata = (Map<String, Object>) exported.extensions().get("exportMetadata");
        assertNotNull(metadata);
        assertEquals(RUN_ID, metadata.get("runId"));
        assertTrue(metadata.containsKey("canonicalWriteCounts"));
    }

    private static SclxDocument sampleDocumentWithRootExtension()
    {
        SclxDocument.Account cash = new SclxDocument.Account(
            "1000",
            "Cash",
            "ASSET",
            null,
            "DEBIT",
            BigDecimal.ZERO,
            List.of(),
            "1000",
            "1000",
            null,
            true,
            List.of(),
            Map.of());

        SclxDocument.Account revenue = new SclxDocument.Account(
            "4000",
            "Donation Revenue",
            "INCOME",
            null,
            "CREDIT",
            BigDecimal.ZERO,
            List.of(),
            "4000",
            "4000",
            null,
            true,
            List.of(),
            Map.of());

        SclxDocument.TransactionLine debit = new SclxDocument.TransactionLine(
            "line-1",
            "1000",
            new BigDecimal("50.00"),
            null,
            null,
            null,
            null,
            null,
            null,
            "deposit",
            List.of(),
            null,
            null,
            null,
            null,
            null,
            null,
            List.of(),
            Map.of());

        SclxDocument.TransactionLine credit = new SclxDocument.TransactionLine(
            "line-2",
            "4000",
            null,
            new BigDecimal("50.00"),
            null,
            null,
            null,
            null,
            null,
            "donation",
            List.of(),
            null,
            null,
            null,
            null,
            null,
            null,
            List.of(),
            Map.of());

        SclxDocument.Transaction transaction = new SclxDocument.Transaction(
            "txn-1",
            LocalDate.of(2026, 3, 1),
            LocalDate.of(2026, 3, 1),
            "Donation received",
            "ref-1",
            null,
            null,
            null,
            null,
            "Donor",
            "POSTED",
            "import",
            null,
            null,
            null,
            new SclxDocument.WorkbookLink("Ledger", 5),
            null,
            List.of(),
            null,
            List.of(debit, credit),
            Map.of());

        return new SclxDocument(
            "SCLX",
            "1.3",
            OffsetDateTime.parse("2026-03-15T10:15:30Z"),
            null,
            null,
            null,
            List.of(cash, revenue),
            List.of(),
            List.of(),
            List.of(),
            List.of(),
            List.of(),
            List.of(),
            List.of(),
            List.of(),
            List.of(transaction),
            List.of(),
            List.of(),
            List.of(),
            List.of(),
            List.of(),
            List.of(),
            List.of(),
            Map.of("sourceMarker", "raw-preserved"));
    }
}
