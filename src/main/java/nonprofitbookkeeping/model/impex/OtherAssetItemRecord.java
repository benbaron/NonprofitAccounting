package nonprofitbookkeeping.model.impex;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Final normalized other-asset item record derived from SCLX.
 */
public record OtherAssetItemRecord(
    String otherAssetItemId,
    LedgerLinkRef ledgerLink,
    WorkbookLinkRef workbookLink,
    String paidTo,
    Integer year,
    String reason,
    String type,
    String typeCode,
    String eventBudgetLabel,
    BigDecimal amountAsOfPriorYearEnd,
    Integer paidReturnedOnLedgerRowIndex,
    LedgerLinkRef settlementLedgerLink,
    String status,
    Map<String, Object> extensions
) {
    public OtherAssetItemRecord {
        if (isBlank(otherAssetItemId)) {
            throw new IllegalArgumentException("otherAssetItemId is required.");
        }
        extensions = extensions == null ? Map.of() : Map.copyOf(extensions);
    }

    public record LedgerLinkRef(
        String transactionId,
        String lineId
    ) {
    }

    public record WorkbookLinkRef(
        String sheetKey,
        Integer ledgerRowIndex
    ) {
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
