package nonprofitbookkeeping.reports.datasource;

import nonprofitbookkeeping.model.AccountSide;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;

/**
 * Configuration object describing how ledger transactions should be filtered
 * before being projected into report beans.
 */
public final class LedgerQueryCriteria
{
    private final AccountSide transactionSide;
    private final LocalDate startDate;
    private final LocalDate endDate;
    private final Set<String> accountNumbers;
    private final boolean requireAllAccounts;
    private final String memoContains;
    private final List<Predicate<nonprofitbookkeeping.model.AccountingTransaction>> additionalPredicates;

    private LedgerQueryCriteria(Builder builder)
    {
        this.transactionSide = builder.transactionSide;
        this.startDate = builder.startDate;
        this.endDate = builder.endDate;
        this.accountNumbers = Collections.unmodifiableSet(new LinkedHashSet<>(builder.accountNumbers));
        this.requireAllAccounts = builder.requireAllAccounts;
        this.memoContains = builder.memoContains;
        this.additionalPredicates = Collections.unmodifiableList(new ArrayList<>(builder.additionalPredicates));
    }

    public static Builder builder()
    {
        return new Builder();
    }

    public AccountSide getTransactionSide()
    {
        return this.transactionSide;
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

    public boolean isRequireAllAccounts()
    {
        return this.requireAllAccounts;
    }

    public String getMemoContains()
    {
        return this.memoContains;
    }

    public List<Predicate<nonprofitbookkeeping.model.AccountingTransaction>> getAdditionalPredicates()
    {
        return this.additionalPredicates;
    }

    /**
     * Builder for {@link LedgerQueryCriteria}.
     */
    public static final class Builder
    {
        private AccountSide transactionSide;
        private LocalDate startDate;
        private LocalDate endDate;
        private Set<String> accountNumbers = new LinkedHashSet<>();
        private boolean requireAllAccounts;
        private String memoContains;
        private List<Predicate<nonprofitbookkeeping.model.AccountingTransaction>> additionalPredicates = new ArrayList<>();

        private Builder()
        {

        }

        public Builder transactionSide(AccountSide transactionSide)
        {
            this.transactionSide = transactionSide;
            return this;
        }

        public Builder startDate(LocalDate startDate)
        {
            this.startDate = startDate;
            return this;
        }

        public Builder endDate(LocalDate endDate)
        {
            this.endDate = endDate;
            return this;
        }

        public Builder accountNumbers(Set<String> accountNumbers)
        {
            this.accountNumbers.clear();
            if (accountNumbers != null)
            {
                this.accountNumbers.addAll(accountNumbers);
            }
            return this;
        }

        public Builder addAccountNumber(String accountNumber)
        {
            if (accountNumber != null)
            {
                this.accountNumbers.add(accountNumber);
            }
            return this;
        }

        public Builder requireAllAccounts(boolean requireAllAccounts)
        {
            this.requireAllAccounts = requireAllAccounts;
            return this;
        }

        public Builder memoContains(String memoContains)
        {
            this.memoContains = memoContains;
            return this;
        }

        public Builder addPredicate(Predicate<nonprofitbookkeeping.model.AccountingTransaction> predicate)
        {
            if (predicate != null)
            {
                this.additionalPredicates.add(predicate);
            }
            return this;
        }

        public LedgerQueryCriteria build()
        {
            return new LedgerQueryCriteria(this);
        }
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }
        LedgerQueryCriteria that = (LedgerQueryCriteria) o;
        return this.requireAllAccounts == that.requireAllAccounts && this.transactionSide == that.transactionSide && Objects.equals(this.startDate, that.startDate) && Objects.equals(this.endDate, that.endDate) && Objects.equals(this.accountNumbers, that.accountNumbers) && Objects.equals(this.memoContains, that.memoContains) && Objects.equals(this.additionalPredicates, that.additionalPredicates);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(this.transactionSide, this.startDate, this.endDate, this.accountNumbers, this.requireAllAccounts, this.memoContains, this.additionalPredicates);
    }
}
