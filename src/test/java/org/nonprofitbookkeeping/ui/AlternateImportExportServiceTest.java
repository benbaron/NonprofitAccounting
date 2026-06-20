package org.nonprofitbookkeeping.ui;

import nonprofitbookkeeping.importer.sclx.SclxImportResult;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AlternateImportExportServiceTest
{
    @Test
    void sclxSummaryMapsExistingSclxCountsIntoStandardImportCounts()
    {
        AlternateImportExportService service = new AlternateImportExportService();
        SclxImportResult sclx = new SclxImportResult("1.3", 2, 1, 1, 3, 4, 5, 6, 12, 7, 8, 9, 10, 11, 12, 13);

        ImportExportOperationResult result = service.summarizeSclxResult(sclx);

        assertEquals(92, result.created());
        assertEquals(0, result.updated());
        assertEquals(0, result.skipped());
        assertFalse(result.hasBlockingErrors());
        assertEquals(0, result.warningCount());
        assertEquals(0, result.errorCount());
    }

    @Test
    void blockingErrorsAreReportedBeforeCommit()
    {
        AlternateImportExportService service = new AlternateImportExportService();

        ImportExportOperationResult result = service.blockingError("Open a company before committing.");

        assertTrue(result.hasBlockingErrors());
        assertEquals(1, result.errorCount());
        assertTrue(result.describeCounts().contains("Errors: 1"));
    }
}
