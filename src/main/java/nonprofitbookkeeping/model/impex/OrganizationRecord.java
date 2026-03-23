package nonprofitbookkeeping.model.impex;

import java.time.LocalDate;
import java.util.Map;

/**
 * Final normalized organization record derived from SCLX.
 */
public record OrganizationRecord(
    String organizationId,
    String name,
    String parentOrganization,
    String baseCurrency,
    LocalDate fiscalYearStart,
    LocalDate fiscalYearEnd,
    Map<String, Object> extensions
) {
    public OrganizationRecord {
        if (isBlank(organizationId)) {
            throw new IllegalArgumentException("organizationId is required.");
        }
        if (isBlank(name)) {
            throw new IllegalArgumentException("name is required.");
        }
        extensions = extensions == null ? Map.of() : Map.copyOf(extensions);
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
