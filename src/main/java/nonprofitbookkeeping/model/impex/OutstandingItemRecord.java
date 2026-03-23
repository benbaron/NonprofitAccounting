package nonprofitbookkeeping.model.impex;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

/**
 * Final normalized outstanding item record derived from SCLX.
 */
public record OutstandingItemRecord(
    String outstandingItemId,
    String kind,
    LedgerLinkRef ledgerLink,
    WorkbookLinkRef workbookLink,
    LocalDate dateSentOrReceived,
    LocalDate incomingCheckOrTransferDate,
    String transferIdOrCheckNumber,
    LocalDate dateShowsOnStatement,
    String personOrBusinessName,
    String detailsNotes,
    String fromToCardMerchant,
    String accountForPaymentOrDeposit,
    BigDecimal amount,
    LocalDate dateReversed,
    String reversalReasonAndApproval,
    LedgerLinkRef reversalLedgerLink,
    String status,
    Map<String, Object> extensions
) {
    public OutstandingItemRecord {
        if (isBlank(outstandingItemId)) {
            throw new IllegalArgumentException("outstandingItemId is required.");
        }
        if (amount != null && amount.scale() > 2) {
            amount = amount.stripTrailingZeros();
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
