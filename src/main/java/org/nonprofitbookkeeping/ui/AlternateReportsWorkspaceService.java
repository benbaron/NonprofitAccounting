package org.nonprofitbookkeeping.ui;

import nonprofitbookkeeping.model.reports.ReportGenerationRequest;
import nonprofitbookkeeping.model.reports.ReportFormat;
import nonprofitbookkeeping.model.reports.ReportKind;
import nonprofitbookkeeping.report.template.WorkbookSemanticReportService;
import nonprofitbookkeeping.reports.ReportMetadata;
import nonprofitbookkeeping.service.ReportService;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/** Service backing the native alternate reports workspace. */
public class AlternateReportsWorkspaceService
{
    private final ReportService reportService;
    private final WorkbookSemanticReportService semanticReportService;

    public AlternateReportsWorkspaceService()
    {
        this(new ReportService(), new WorkbookSemanticReportService());
    }

    public AlternateReportsWorkspaceService(ReportService reportService,
        WorkbookSemanticReportService semanticReportService)
    {
        this.reportService = Objects.requireNonNull(reportService, "reportService");
        this.semanticReportService = Objects.requireNonNull(semanticReportService, "semanticReportService");
    }

    public List<ReportCatalogItem> catalog()
    {
        List<ReportCatalogItem> reports = new ArrayList<>();
        reports.add(new ReportCatalogItem("legacy-income-statement", "Income Statement", ReportKind.LEGACY_FINANCIAL,
            "IncomeStmt", true, true, true, false, List.of("Include zero-balance accounts")));
        reports.add(new ReportCatalogItem("legacy-balance-sheet", "Balance Sheet", ReportKind.LEGACY_FINANCIAL,
            "BalanceStmt", true, true, false, false, List.of("Show net asset classes")));
        reports.add(new ReportCatalogItem("legacy-cash-flow", "Cash Flow", ReportKind.LEGACY_FINANCIAL,
            "WorkbookSummary", true, true, false, false, List.of()));
        reports.add(new ReportCatalogItem("legacy-donor-summary", "Donor Summary", ReportKind.LEGACY_FINANCIAL,
            "TransactionsList", true, true, false, true, List.of("Summarize by donor")));
        reports.add(new ReportCatalogItem("legacy-fund-activity", "Fund Activity Report", ReportKind.LEGACY_FINANCIAL,
            "FundTransfers", true, true, true, false, List.of("Include transfers")));
        this.semanticReportService.displayNames().forEach((templateId, name) -> reports.add(new ReportCatalogItem(
            "semantic-" + templateId, name, ReportKind.SEMANTIC_WORKBOOK, templateId,
            true, false, false, false, List.of())));
        return List.copyOf(reports);
    }

    public File generate(ReportGenerationRequest request) throws IOException
    {
        validate(request);
        return this.reportService.generateReport(request);
    }

    public List<ReportMetadata> history()
    {
        return ReportService.listGeneratedReports();
    }

    public String exportNamingConvention(ReportCatalogItem item, ReportFormat format, LocalDate endDate)
    {
        LocalDate effectiveDate = endDate == null ? LocalDate.now() : endDate;
        String slug = item.displayName().toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+", "-").replaceAll("(^-|-$)", "");
        return slug + "_" + effectiveDate.format(DateTimeFormatter.ISO_LOCAL_DATE) + "." + format.extension();
    }

    public void validate(ReportGenerationRequest request)
    {
        if (request == null) throw new IllegalArgumentException("Select a report before generating.");
        if (request.reportId() == null || request.reportId().isBlank()) throw new IllegalArgumentException("Report selection is required.");
        if (request.format() == null) throw new IllegalArgumentException("Output format is required.");
        if (!request.format().supported()) throw new IllegalArgumentException(request.format().explanation());
        if (request.startDate() == null || request.endDate() == null) throw new IllegalArgumentException("Start and end dates are required.");
        if (request.endDate().isBefore(request.startDate())) throw new IllegalArgumentException("End date cannot be before start date.");
    }
}
