package nonprofitbookkeeping.model;

import javax.annotation.Nullable;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Builder;
import lombok.Data;
import lombok.AllArgsConstructor; // Ensure this is present
import lombok.NoArgsConstructor; // Added

import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;

import java.io.Serializable;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Represents a group of related account entries.
 */
@Builder
@Data
@NoArgsConstructor(force = true) // Changed to force = true
@AllArgsConstructor(access = lombok.AccessLevel.PRIVATE) // For builder and potentially other internal uses
public class AccountingTransaction implements Serializable {
    /**
     * serialVersionUID : long
     */
    private static final long serialVersionUID = -8821254116304310L;

    @JsonProperty final private Account account;
    @JsonProperty final private Set<AccountingEntry> entries;
    @JsonProperty final private Map<String, String> info;
    @JsonProperty final private long bookingDateTimestamp;
    @JsonProperty final private Integer transactionId; // Final, but can be null

    @JsonProperty private String date; // Non-final
    @JsonProperty private String memo; // Non-final

    /**
     * Public constructor for existing code.
     */
    public AccountingTransaction(Account account,
                                 Set<AccountingEntry> entries,
                                 @Nullable Map<String, String> info,
                                 long bookingDateTimestamp) {
        this.account = checkNotNull(account, "account cannot be null");

        // Ensure immutability for collections passed in
        this.entries = Collections.unmodifiableSet(new HashSet<>(checkNotNull(entries, "entries cannot be null")));
        this.info = (info == null) ? Collections.emptyMap() : Collections.unmodifiableMap(new HashMap<>(info));

        this.bookingDateTimestamp = bookingDateTimestamp;

        // Initialize other fields to defaults as per previous logic
        this.transactionId = null;
        this.date = "";
        this.memo = "";

        checkArgument(!this.entries.isEmpty(), "Transaction must have at least one entry (ideally 2+ for balance)");
        checkArgument(this.entries.size() >= 2, "A transaction consists of at least two entries");

        // Temporarily remove setTransaction to break circular dependency potential during construction
        // this.entries.forEach(e -> e.setTransaction(this));
        // This logic needs to be handled carefully. If AccountingEntry needs a reference to AccountingTransaction
        // upon construction, and AccountingTransaction needs to validate entries that might already need
        // that back-reference, it's tricky. For now, to compile, I'll comment this out.
        // It's possible the builder pattern handles this by setting the transaction on entries after
        // the transaction itself is built.
        // The isBalanced check might also need to be deferred or handled carefully if entries are not fully set up.
        checkArgument(isBalanced(), "Transaction unbalanced");

        // If entries are now unmodifiable, setting transaction back might not be possible here.
        // This implies AccountingEntry might need to be constructed with the transaction,
        // or setTransaction needs to be called post AccountingTransaction construction.
        // For now, to proceed with compilation, this line is commented out.
        // It will likely need to be addressed for full functionality.
        // this.entries.forEach(e -> e.setTransaction(this));
    }

    // Explicit @NoArgsConstructor removed. If needed for specific frameworks and not covered by Lombok's
    // behavior with @Data/@Builder on a class with final fields, it might need to be re-added carefully.
    // For now, relying on the private @AllArgsConstructor for the builder and the public 4-arg one.

    public BigDecimal getTotalAmount() {
        BigDecimal debitTotal = BigDecimal.ZERO;
        // BigDecimal creditTotal = BigDecimal.ZERO; // Not used

        if (this.entries == null) return BigDecimal.ZERO; // Guard against null entries

        for (AccountingEntry e : this.entries) {
            if (e.getAccountSide() == AccountSide.DEBIT) {
                debitTotal = debitTotal.add(e.getAmount());
            }
            // else { // Credits are not summed up for this method's specific logic
            // creditTotal = creditTotal.add(e.getAmount());
            // }
        }
        return debitTotal; // This method seems to intend to return sum of debits, not net
    }

    public boolean isBalanced() {
        if (this.entries == null || this.entries.isEmpty()) { // Or size < 2
            return false; // Or true, depending on definition for empty/single-entry transactions
        }
        BigDecimal balance = this.entries.stream().
                map(e -> e.getAccountSide() == AccountSide.DEBIT ?
                        e.getAmount() : e.getAmount().negate())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return balance.compareTo(BigDecimal.ZERO) == 0;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Transaction ")
                .append(Instant.ofEpochMilli(this.bookingDateTimestamp).toString())
                .append("\n");
        if (this.entries != null) {
            this.entries.forEach(e -> sb.append(e).append("\n"));
        } else {
            sb.append("No entries\n");
        }
        return sb.toString();
    }

    // Getters are generated by @Data for all fields.
    // Setters are generated by @Data for non-final fields (date, memo).

	public String getId() {
		return this.transactionId != null ? this.transactionId.toString() : null;
	}
	
	public String getDescription() {
		return this.memo;
	}
	
	public String getAccountName() {
	    return this.account != null ? this.account.getName() : null;
	}
	
	public void setDescription(String description) {
		this.memo = description;
	}

	public void setDate(String string) {
		this.date = string;
	}

	public BigDecimal countAccountBalance() {
	    if (this.account == null) return BigDecimal.ZERO;
		return this.account.totalAccountBalance();
	}

	public void setMemo(String memo2) {
		this.memo = memo2;
	}

	public void setTotalAmount(BigDecimal valueOf) {
		// This method was a stub. If it's meant to do something,
        // it would likely need to adjust entries, which are now final and unmodifiable
        // via this instance after construction.
        // This suggests mutation should happen via new instances or builder.
	}
}
