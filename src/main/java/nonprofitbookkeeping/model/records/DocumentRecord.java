package nonprofitbookkeeping.model.records;

import java.time.LocalDate;
import java.util.Map;

/**
 * Final normalized supporting document record derived from SCLX.
 */
public record DocumentRecord(
    String documentId,
    String documentType,
    String referenceNumber,
    LocalDate documentDate,
    String fileName,
    String notes,
    Map<String, Object> extensions
) {
    public DocumentRecord {
        if (isBlank(documentId)) {
            throw new IllegalArgumentException("documentId is required.");
        }
        extensions = extensions == null ? Map.of() : Map.copyOf(extensions);
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
