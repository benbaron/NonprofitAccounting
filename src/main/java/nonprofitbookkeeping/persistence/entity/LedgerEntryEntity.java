package nonprofitbookkeeping.persistence.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * JPA entity representing a ledger entry. Core transaction information is
 * stored in this table while split details live in {@link SupplementalRecordEntity}.
 */
@Entity
@Table(name = "ledger_entries")
@Data
@NoArgsConstructor
public class LedgerEntryEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String entryDate;
    private String checkNumber;
    private String cleared;
    private String toFrom;
    private String memoString;
    private String budgetTracking;

    @OneToMany(mappedBy = "ledgerEntry", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SupplementalRecordEntity> supplementalRecords = new ArrayList<>();
}
