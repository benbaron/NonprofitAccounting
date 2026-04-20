package nonprofitbookkeeping.model.records;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

/**
 * Final normalized fixed-asset inventory record derived from SCLX.
 */
public record AssetRecord(
    String assetId,
    LocalDate dateAcquired,
    String description,
    Integer itemCount,
    BigDecimal approxValueTotal,
    BigDecimal accumulatedDepreciation,
    BigDecimal valuePerItem,
    AssetItemType itemType,
    String usedFor,
    BigDecimal lotPaidTotal,
    Integer lotItemCount,
    GuardianRecord currentGuardian,
    GuardianshipDetailsRecord guardianshipDetails,
    RemovalDetailsRecord removalDetails,
    Map<String, Object> extensions
) {
    public AssetRecord {
        if (isBlank(assetId)) {
            throw new IllegalArgumentException("assetId is required.");
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
        Boolean confirmed,
        String confirmationStatus,
        String notes
    ) {
    }

    public record RemovalDetailsRecord(
        String approvedBy,
        LocalDate approvalDate,
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
