package nonprofitbookkeeping.model.impex;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Map;

/**
 * Final normalized bank statement import record derived from SCLX.
 */
public record BankStatementRecord(
    String importId,
    SourceFormat sourceFormat,
    String sourceVersion,
    StatementKind statementKind,
    BankAccountRef bankAccount,
    String currency,
    LocalDate statementStart,
    LocalDate statementEnd,
    BalanceSnapshot ledgerBalance,
    BalanceSnapshot availableBalance,
    String documentId,
    Map<String, Object> extensions
) {

    public BankStatementRecord {
        if (isBlank(importId)) {
            throw new IllegalArgumentException("importId is required.");
        }
        if (sourceFormat == null) {
            throw new IllegalArgumentException("sourceFormat is required.");
        }
        if (statementKind == null) {
            throw new IllegalArgumentException("statementKind is required.");
        }
        if (bankAccount == null) {
            throw new IllegalArgumentException("bankAccount is required.");
        }
        if (statementStart == null) {
            throw new IllegalArgumentException("statementStart is required.");
        }
        if (statementEnd == null) {
            throw new IllegalArgumentException("statementEnd is required.");
        }
        if (statementEnd.isBefore(statementStart)) {
            throw new IllegalArgumentException("statementEnd must be on or after statementStart.");
        }
        extensions = extensions == null ? Map.of() : Map.copyOf(extensions);
    }

    public enum SourceFormat {
        OFX
    }

    public enum StatementKind {
        BANK,
        CREDIT_CARD
    }

    public record BankAccountRef(
        String bankId,
        String accountId,
        String accountType
    ) {
        public BankAccountRef {
            if (isBlank(accountId)) {
                throw new IllegalArgumentException("bankAccount.accountId is required.");
            }
        }
    }

    public record BalanceSnapshot(
        BigDecimal amount,
        OffsetDateTime asOf
    ) {
        public BalanceSnapshot {
            if (amount == null) {
                throw new IllegalArgumentException("balance amount is required.");
            }
            if (asOf == null) {
                throw new IllegalArgumentException("balance asOf is required.");
            }
        }
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
