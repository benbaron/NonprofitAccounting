package nonprofitbookkeeping.model.impex;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Map;

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
    Map<String, Object> extensions,
    String rawJson)
{
    public BankStatementRecord
    {
        extensions = extensions == null ? Map.of() : Map.copyOf(extensions);
    }

    public enum SourceFormat
    {
        OFX,
        OTHER
    }

    public enum StatementKind
    {
        BANK,
        CREDIT_CARD,
        OTHER
    }

    public record BankAccountRef(
        String bankId,
        String accountId,
        String accountType)
    {
    }

    public record BalanceSnapshot(
        BigDecimal amount,
        OffsetDateTime asOf)
    {
    }
}
