package nonprofitbookkeeping.model.impex;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Final normalized banking-side settlement record derived from SCLX.
 */
public record BankingItemRecord(
    String bankingItemId,
    BankingItemKind kind,
    String bankAccountId,
    String transactionId,
    List<String> lineIds,
    LocalDate clearedDate,
    BigDecimal amount,
    String checkNumber,
    String payee,
    LocalDate depositDate,
    String payer,
    String depositId,
    String memo,
    BankingItemSource source,
    BankingItemStatus status,
    String importId,
    OfxTransactionRecord ofx,
    Map<String, Object> extensions
) {

    public BankingItemRecord {
        if (isBlank(bankingItemId)) {
            throw new IllegalArgumentException("bankingItemId is required.");
        }
        if (kind == null) {
            throw new IllegalArgumentException("kind is required.");
        }
        if (clearedDate == null) {
            throw new IllegalArgumentException("clearedDate is required.");
        }
        if (amount == null) {
            throw new IllegalArgumentException("amount is required.");
        }
        if (amount.signum() < 0) {
            throw new IllegalArgumentException("amount must be non-negative.");
        }

        lineIds = lineIds == null ? List.of() : List.copyOf(lineIds);
        extensions = extensions == null ? Map.of() : Map.copyOf(extensions);

        if (kind == BankingItemKind.CHECK) {
            if (isBlank(checkNumber)) {
                throw new IllegalArgumentException("checkNumber is required for CHECK.");
            }
            if (isBlank(payee)) {
                throw new IllegalArgumentException("payee is required for CHECK.");
            }
        }

        if (kind == BankingItemKind.DEPOSIT) {
            if (depositDate == null) {
                throw new IllegalArgumentException("depositDate is required for DEPOSIT.");
            }
            if (isBlank(payer)) {
                throw new IllegalArgumentException("payer is required for DEPOSIT.");
            }
        }
    }

    public enum BankingItemKind {
        CHECK,
        DEPOSIT,
        OTHER_WITHDRAWAL,
        OTHER_CREDIT,
        BANK_FEE,
        INTEREST,
        ADJUSTMENT
    }

    public enum BankingItemSource {
        MANUAL,
        BANK_IMPORT,
        BANK_RECONCILIATION,
        OFX_IMPORT,
        SYSTEM_GENERATED
    }

    public enum BankingItemStatus {
        PENDING,
        OUTSTANDING,
        CLEARED,
        VOID
    }

    public record OfxTransactionRecord(
        String fitId,
        String transactionType,
        LocalDate datePosted,
        LocalDate dateUser,
        LocalDate dateAvailable,
        String checkNumber,
        String referenceNumber,
        String name,
        String memo,
        String payeeId,
        String sic,
        String serverTransactionId,
        String correctFitId,
        String correctAction,
        Map<String, Object> extensions
    ) {
        public OfxTransactionRecord {
            extensions = extensions == null ? Map.of() : Map.copyOf(extensions);
        }
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
