package nonprofitbookkeeping.model.reports;

import java.time.LocalDate;
import java.util.Map;

/** Parameters selected in the alternate reports workspace and passed to report generation. */
public record ReportGenerationRequest(String reportId, ReportKind reportKind, String templateId,
    LocalDate startDate, LocalDate endDate, String fund, String account, String donor, ReportFormat format,
    Map<String, String> options)
{
    public ReportGenerationRequest
    {
        options = options == null ? Map.of() : Map.copyOf(options);
    }
}
