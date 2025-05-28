package nonprofitbookkeeping.model.impex;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ImportedTransaction {
    private LocalDate datePosted;
    private BigDecimal amount;
    private String description; // Payee or name of the transaction
    private String memo; // Additional memo or details
    private String transactionId; // e.g., FITID from OFX
    private String currency; // Optional, currency code
    private String originalAccountType; // e.g., "BANK", "CREDITCARD"
    private String originalAccountNumber; // Account number from the imported file
}
