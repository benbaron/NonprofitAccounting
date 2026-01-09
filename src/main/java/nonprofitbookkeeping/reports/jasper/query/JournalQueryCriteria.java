
package nonprofitbookkeeping.reports.jasper.query;

import java.time.LocalDate;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import nonprofitbookkeeping.model.AccountingTransaction;

/**
 * Immutable criteria object describing how journal transactions should be
 * filtered before mapping them into report beans.
 */
public class JournalQueryCriteria
{
	/** Determines how account number filters are interpreted. */
	public enum AccountMatchMode
	{
		/** At least one of the provided account numbers must appear. */
		ANY,
		/** All provided account numbers must appear on the transaction. */
		ALL
	}
	
	private final String transactionType;
	private final LocalDate startDate;
	private final LocalDate endDate;
	private final Set<String> accountNumbers;
	private final AccountMatchMode accountMatchMode;
	private final String memoContains;
	private final Map<String, String> infoEquals;
	private final Predicate<? super AccountingTransaction> customPredicate;
	
	/**
	 * Instantiates a new journal query criteria.
	 *
	 * @param builder the builder
	 */
	private JournalQueryCriteria(Builder builder)
	{
		this.transactionType = builder.transactionType;
		this.startDate = builder.startDate;
		this.endDate = builder.endDate;
		this.accountNumbers = Collections
			.unmodifiableSet(new LinkedHashSet<>(builder.accountNumbers));
		this.accountMatchMode = builder.accountMatchMode;
		this.memoContains = builder.memoContains;
		this.infoEquals = Collections
			.unmodifiableMap(new LinkedHashMap<>(builder.infoEquals));
		this.customPredicate = builder.customPredicate;
		
	}
	
	/**
	 * Mutable builder for {@link JournalQueryCriteria}.
	 */
	public static final class Builder
	{
		private String transactionType;
		private LocalDate startDate;
		private LocalDate endDate;
		private final Set<String> accountNumbers = new LinkedHashSet<>();
		private AccountMatchMode accountMatchMode = AccountMatchMode.ANY;
		private String memoContains;
		private final Map<String, String> infoEquals = new LinkedHashMap<>();
		private Predicate<? super AccountingTransaction> customPredicate;
		
		private Builder()
		{
		
		}
		
	}
	
	/**
	 * Override @see java.lang.Object#toString() 
	 */
	@Override
	public String toString()
	{
		return "JournalQueryCriteria{" +
			"transactionType='" + this.transactionType + '\'' +
			", startDate=" + this.startDate +
			", endDate=" + this.endDate +
			", accountNumbers=" + this.accountNumbers +
			", accountMatchMode=" + this.accountMatchMode +
			", memoContains='" + this.memoContains + '\'' +
			", infoEquals=" + this.infoEquals +
			'}';
		
	}
	
	@Override
	public int hashCode()
	{
		return Objects.hash(this.transactionType, this.startDate, this.endDate,
			this.accountNumbers,
			this.accountMatchMode, this.memoContains, this.infoEquals,
			this.customPredicate);
		
	}
	
	@Override
	public boolean equals(Object obj)
	{
		
		if (this == obj)
		{
			return true;
		}
		
		if (!(obj instanceof JournalQueryCriteria))
		{
			return false;
		}
		
		JournalQueryCriteria other = (JournalQueryCriteria) obj;
		return Objects.equals(this.transactionType, other.transactionType) &&
			Objects.equals(this.startDate, other.startDate) &&
			Objects.equals(this.endDate, other.endDate) &&
			Objects.equals(this.accountNumbers, other.accountNumbers) &&
			this.accountMatchMode == other.accountMatchMode &&
			Objects.equals(this.memoContains, other.memoContains) &&
			Objects.equals(this.infoEquals, other.infoEquals) &&
			Objects.equals(this.customPredicate, other.customPredicate);
		
	}
	
}
