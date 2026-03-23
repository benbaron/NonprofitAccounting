package nonprofitbookkeeping.model.impex;

import java.time.LocalDate;
import java.util.Map;

/**
 * Final normalized reporting period record derived from SCLX.
 */
public record ReportingPeriodRecord(
    LocalDate startDate,
    LocalDate endDate,
    String label,
    Integer fiscalYear,
    String periodType,
    Map<String, Object> extensions
) {
    public ReportingPeriodRecord {
        if (startDate == null) {
            throw new IllegalArgumentException("startDate is required.");
        }
        if (endDate == null) {
            throw new IllegalArgumentException("endDate is required.");
        }
        if (endDate.isBefore(startDate)) {
            throw new IllegalArgumentException("endDate must be on or after startDate.");
        }
        extensions = extensions == null ? Map.of() : Map.copyOf(extensions);
    }
}
