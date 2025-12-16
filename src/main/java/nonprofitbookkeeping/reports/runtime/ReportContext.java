package nonprofitbookkeeping.reports.runtime;

import java.time.LocalDate;

/**
 * Common filter context for reports.
 *
 * You can extend this with additional fields as needed.
 */
public record ReportContext(
    int fiscalYear,
    Integer branchId,
    LocalDate fromDate,
    LocalDate toDate
) {
}
