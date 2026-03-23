package nonprofitbookkeeping.model.impex;

import java.time.LocalDate;
import java.util.Map;

/**
 * Final normalized event record derived from SCLX.
 */
public record EventRecord(
    String eventId,
    String name,
    LocalDate startDate,
    LocalDate endDate,
    String hostingOrganizationId,
    Map<String, Object> extensions
) {
    public EventRecord {
        if (isBlank(eventId)) {
            throw new IllegalArgumentException("eventId is required.");
        }
        if (isBlank(name)) {
            throw new IllegalArgumentException("name is required.");
        }
        if (startDate != null && endDate != null && endDate.isBefore(startDate)) {
            throw new IllegalArgumentException("endDate must be on or after startDate.");
        }
        extensions = extensions == null ? Map.of() : Map.copyOf(extensions);
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
