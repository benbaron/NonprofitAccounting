package nonprofitbookkeeping.model.impex;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public record BankingItemRecord(
    String bankingItemId,
    String kind,
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
    String source,
    String status,
    String importId,
    OfxTransactionRecord ofx,
    Map<String, Object> extensions,
    String rawJson)
{
    public BankingItemRecord
    {
        lineIds = lineIds == null ? List.of() : List.copyOf(lineIds);
        extensions = extensions == null ? Map.of() : Map.copyOf(extensions);
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
        Map<String, Object> extensions)
    {
        public OfxTransactionRecord
        {
            extensions = extensions == null ? Map.of() : Map.copyOf(extensions);
        }
    }
}
