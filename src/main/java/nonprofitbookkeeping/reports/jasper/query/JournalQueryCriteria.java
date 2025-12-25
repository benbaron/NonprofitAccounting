package nonprofitbookkeeping.reports.query;

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

        private JournalQueryCriteria(Builder builder)
        {
                this.transactionType = builder.transactionType;
                this.startDate = builder.startDate;
                this.endDate = builder.endDate;
                this.accountNumbers = Collections.unmodifiableSet(new LinkedHashSet<>(builder.accountNumbers));
                this.accountMatchMode = builder.accountMatchMode;
                this.memoContains = builder.memoContains;
                this.infoEquals = Collections.unmodifiableMap(new LinkedHashMap<>(builder.infoEquals));
                this.customPredicate = builder.customPredicate;
        }

        public String getTransactionType()
        {
                return this.transactionType;
        }

        public LocalDate getStartDate()
        {
                return this.startDate;
        }

        public LocalDate getEndDate()
        {
                return this.endDate;
        }

        public Set<String> getAccountNumbers()
        {
                return this.accountNumbers;
        }

        public AccountMatchMode getAccountMatchMode()
        {
                return this.accountMatchMode;
        }

        public String getMemoContains()
        {
                return this.memoContains;
        }

        public Map<String, String> getInfoEquals()
        {
                return this.infoEquals;
        }

        public Predicate<? super AccountingTransaction> getCustomPredicate()
        {
                return this.customPredicate;
        }

        public static Builder builder()
        {
                return new Builder();
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

                public Builder transactionType(String transactionType)
                {
                        this.transactionType = transactionType;
                        return this;
                }

                public Builder dateRange(LocalDate startDate, LocalDate endDate)
                {
                        this.startDate = startDate;
                        this.endDate = endDate;
                        return this;
                }

                public Builder addAccountNumber(String accountNumber)
                {
                        if (accountNumber != null && !accountNumber.isBlank())
                        {
                                this.accountNumbers.add(accountNumber.trim());
                        }
                        return this;
                }

                public Builder accountMatchMode(AccountMatchMode accountMatchMode)
                {
                        if (accountMatchMode != null)
                        {
                                this.accountMatchMode = accountMatchMode;
                        }
                        return this;
                }

                public Builder memoContains(String memoContains)
                {
                        this.memoContains = memoContains;
                        return this;
                }

                public Builder infoEquals(String key, String value)
                {
                        if (key != null && value != null)
                        {
                                this.infoEquals.put(key, value);
                        }
                        return this;
                }

                public Builder customPredicate(Predicate<? super AccountingTransaction> predicate)
                {
                        this.customPredicate = predicate;
                        return this;
                }

                public JournalQueryCriteria build()
                {
                        return new JournalQueryCriteria(this);
                }
        }

        @Override
        public String toString()
        {
                return "JournalQueryCriteria{" +
                        "transactionType='" + transactionType + '\'' +
                        ", startDate=" + startDate +
                        ", endDate=" + endDate +
                        ", accountNumbers=" + accountNumbers +
                        ", accountMatchMode=" + accountMatchMode +
                        ", memoContains='" + memoContains + '\'' +
                        ", infoEquals=" + infoEquals +
                        '}';
        }

        @Override
        public int hashCode()
        {
                return Objects.hash(transactionType, startDate, endDate, accountNumbers,
                        accountMatchMode, memoContains, infoEquals, customPredicate);
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
                return Objects.equals(this.transactionType, other.transactionType)
                        && Objects.equals(this.startDate, other.startDate)
                        && Objects.equals(this.endDate, other.endDate)
                        && Objects.equals(this.accountNumbers, other.accountNumbers)
                        && this.accountMatchMode == other.accountMatchMode
                        && Objects.equals(this.memoContains, other.memoContains)
                        && Objects.equals(this.infoEquals, other.infoEquals)
                        && Objects.equals(this.customPredicate, other.customPredicate);
        }
}
