package nonprofitbookkeeping.model.impex;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

/**
 * Final normalized supply inventory record derived from SCLX.
 */
public record SupplyRecord(
    String supplyId,
    String itemNumber,
    LocalDate dateAcquired,
    String description,
    Integer count,
    BigDecimal approxValueTotal,
    BigDecimal valuePerItem,
    GuardianRecord guardian,
    GuardianshipDetailsRecord guardianshipDetails,
    RemovalDetailsRecord removalDetails,
    String additionalNotes,
    Map<String, Object> extensions
) {
    public SupplyRecord {
        if (isBlank(supplyId)) {
            throw new IllegalArgumentException("supplyId is required.");
        }
        extensions = extensions == null ? Map.of() : Map.copyOf(extensions);
    }

    public record GuardianRecord(
        String legalName,
        String email,
        String phone
    ) {
    }

    public record GuardianshipDetailsRecord(
        LocalDate dateAsOf,
        LocalDate lastConfirmed,
        Boolean returned,
        String notes
    ) {
    }

    public record RemovalDetailsRecord(
        String approvedBy,
        String reason,
        Integer numberRemoved,
        Boolean removed,
        String removalType
    ) {
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
