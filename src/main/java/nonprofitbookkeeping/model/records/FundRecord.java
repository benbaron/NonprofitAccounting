package nonprofitbookkeeping.model.records;

import java.util.Map;

/**
 * Final normalized fund record derived from SCLX.
 */
public record FundRecord(
    String fundId,
    String name,
    boolean restricted,
    String description,
    Map<String, Object> extensions
) {
    public FundRecord {
        if (isBlank(fundId)) {
            throw new IllegalArgumentException("fundId is required.");
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
