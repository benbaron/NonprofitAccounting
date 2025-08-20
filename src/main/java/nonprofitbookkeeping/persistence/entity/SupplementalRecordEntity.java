package nonprofitbookkeeping.persistence.entity;

import jakarta.persistence.*;

/**
 * Represents a single split or supplemental record associated with a ledger entry.
 */
@Entity
@Table(name = "supplemental_records")
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

    /** Default constructor. */
    public SupplementalRecordEntity() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getAssetAccount() {
        return assetAccount;
    }

    public void setAssetAccount(String assetAccount) {
        this.assetAccount = assetAccount;
    }

    public String getIncomeAccount() {
        return incomeAccount;
    }

    public void setIncomeAccount(String incomeAccount) {
        this.incomeAccount = incomeAccount;
    }

    public String getExpenseAccount() {
        return expenseAccount;
    }

    public void setExpenseAccount(String expenseAccount) {
        this.expenseAccount = expenseAccount;
    }

    public String getFundName() {
        return fundName;
    }

    public void setFundName(String fundName) {
        this.fundName = fundName;
    }

    public int getSequenceNumber() {
        return sequenceNumber;
    }

    public void setSequenceNumber(int sequenceNumber) {
        this.sequenceNumber = sequenceNumber;
    }

    public LedgerEntryEntity getLedgerEntry() {
        return ledgerEntry;
    }

    public void setLedgerEntry(LedgerEntryEntity ledgerEntry) {
        this.ledgerEntry = ledgerEntry;
    }
}
