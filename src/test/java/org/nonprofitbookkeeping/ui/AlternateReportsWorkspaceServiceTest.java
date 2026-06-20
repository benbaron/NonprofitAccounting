package org.nonprofitbookkeeping.ui;

import nonprofitbookkeeping.model.reports.ReportGenerationRequest;
import nonprofitbookkeeping.model.reports.ReportFormat;
import nonprofitbookkeeping.model.reports.ReportKind;
import nonprofitbookkeeping.report.template.WorkbookSemanticReportService;
import nonprofitbookkeeping.service.ReportService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class AlternateReportsWorkspaceServiceTest
{
    @TempDir Path tempDir;

    @Test
    void catalogIncludesLegacyFinancialAndSemanticWorkbookReports()
    {
        AlternateReportsWorkspaceService service = new AlternateReportsWorkspaceService(new CapturingReportService(tempDir), new WorkbookSemanticReportService());
        assertTrue(service.catalog().stream().anyMatch(item -> item.id().equals("legacy-income-statement")
            && item.kind() == ReportKind.LEGACY_FINANCIAL));
        assertTrue(service.catalog().stream().anyMatch(item -> item.id().equals("semantic-BalanceStmt")
            && item.kind() == ReportKind.SEMANTIC_WORKBOOK));
    }

    @Test
    void selectedReportTypeDateRangeFormatAndParametersReachGenerationService() throws Exception
    {
        CapturingReportService reportService = new CapturingReportService(tempDir);
        AlternateReportsWorkspaceService service = new AlternateReportsWorkspaceService(reportService, new WorkbookSemanticReportService());
        ReportGenerationRequest request = new ReportGenerationRequest("legacy-fund-activity",
            ReportKind.LEGACY_FINANCIAL, "FundTransfers",
            LocalDate.of(2026, 1, 1), LocalDate.of(2026, 3, 31), "General Fund",
            "4000", "Donor A", ReportFormat.CSV, Map.of("Include transfers", "true"));

        service.generate(request);

        assertSame(request, reportService.lastRequest);
        assertEquals("legacy-fund-activity", reportService.lastRequest.reportId());
        assertEquals(ReportKind.LEGACY_FINANCIAL, reportService.lastRequest.reportKind());
        assertEquals(LocalDate.of(2026, 1, 1), reportService.lastRequest.startDate());
        assertEquals(LocalDate.of(2026, 3, 31), reportService.lastRequest.endDate());
        assertEquals(ReportFormat.CSV, reportService.lastRequest.format());
        assertEquals("General Fund", reportService.lastRequest.fund());
        assertEquals("4000", reportService.lastRequest.account());
        assertEquals("Donor A", reportService.lastRequest.donor());
        assertEquals("true", reportService.lastRequest.options().get("Include transfers"));
    }

    @Test
    void validationRejectsMissingDatesAndUnsupportedFormats()
    {
        AlternateReportsWorkspaceService service = new AlternateReportsWorkspaceService(new CapturingReportService(tempDir), new WorkbookSemanticReportService());
        ReportGenerationRequest missingDate = new ReportGenerationRequest("semantic-BalanceStmt",
            ReportKind.SEMANTIC_WORKBOOK, "BalanceStmt", null,
            LocalDate.of(2026, 1, 31), null, null, null, ReportFormat.TEXT, Map.of());
        assertThrows(IllegalArgumentException.class, () -> service.generate(missingDate));

        ReportGenerationRequest unsupportedFormat = new ReportGenerationRequest("semantic-BalanceStmt",
            ReportKind.SEMANTIC_WORKBOOK, "BalanceStmt", LocalDate.of(2026, 1, 1),
            LocalDate.of(2026, 1, 31), null, null, null, ReportFormat.PDF, Map.of());
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> service.generate(unsupportedFormat));
        assertTrue(ex.getMessage().contains("PDF export is not implemented"));
    }

    @Test
    void namingConventionUsesReportDateAndSupportedExtension()
    {
        AlternateReportsWorkspaceService service = new AlternateReportsWorkspaceService(new CapturingReportService(tempDir), new WorkbookSemanticReportService());
        ReportCatalogItem item = service.catalog().stream().filter(r -> r.id().equals("legacy-income-statement")).findFirst().orElseThrow();
        assertEquals("income-statement_2026-12-31.csv", service.exportNamingConvention(item, ReportFormat.CSV, LocalDate.of(2026, 12, 31)));
    }

    private static final class CapturingReportService extends ReportService
    {
        private final Path tempDir;
        private ReportGenerationRequest lastRequest;

        private CapturingReportService(Path tempDir)
        {
            this.tempDir = tempDir;
        }

        @Override
        public File generateReport(ReportGenerationRequest request) throws IOException
        {
            this.lastRequest = request;
            Path output = tempDir.resolve(request.reportId() + "." + request.format().extension());
            Files.writeString(output, "report");
            return output.toFile();
        }
    }
}
