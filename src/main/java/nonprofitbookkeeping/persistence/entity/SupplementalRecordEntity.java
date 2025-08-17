package nonprofitbookkeeping.persistence.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents a single split or supplemental record associated with a ledger entry.
 */
@Entity
@Table(name = "supplemental_records")
@Data
@NoArgsConstructor
public class SupplementalRecordEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private double amount;
    private String assetAccount;
    private String incomeAccount;
    private String expenseAccount;
    private String fundName;
    private int sequenceNumber;

    @ManyToOne
    @JoinColumn(name = "ledger_entry_id")
    private LedgerEntryEntity ledgerEntry;
}
