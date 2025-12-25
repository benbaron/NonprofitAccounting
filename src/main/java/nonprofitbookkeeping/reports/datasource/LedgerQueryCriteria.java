package nonprofitbookkeeping.reports.datasource;

import java.time.LocalDate;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import nonprofitbookkeeping.model.AccountSide;
import nonprofitbookkeeping.model.AccountingTransaction;

/**
 * Criteria used for filtering ledger transactions.
 */
public class LedgerQueryCriteria
{
    private final LocalDate startDate;
    private final LocalDate endDate;
    private final String memoContains;
    private final Set<String> accountNumbers;
    private final boolean requireAllAccounts;
    private final AccountSide transactionSide;
    private final Predicate<? super AccountingTransaction> predicate;

    private LedgerQueryCriteria(Builder builder)
    {
        this.startDate = builder.startDate;
        this.endDate = builder.endDate;
        this.memoContains = builder.memoContains;
        this.accountNumbers = Collections.unmodifiableSet(
            new LinkedHashSet<>(builder.accountNumbers));
        this.requireAllAccounts = builder.requireAllAccounts;
        this.transactionSide = builder.transactionSide;
        this.predicate = builder.predicate;
    }

    public static Builder builder()
    {
        return new Builder();
    }

    public LocalDate getStartDate()
    {
        return this.startDate;
    }

    public LocalDate getEndDate()
    {
        return this.endDate;
    }

    public String getMemoContains()
    {
        return this.memoContains;
    }

    public Set<String> getAccountNumbers()
    {
        return this.accountNumbers;
    }

    public boolean isRequireAllAccounts()
    {
        return this.requireAllAccounts;
    }

    public AccountSide getTransactionSide()
    {
        return this.transactionSide;
    }

    public Predicate<? super AccountingTransaction> getPredicate()
    {
        return this.predicate;
    }

    public static final class Builder
    {
        private LocalDate startDate;
        private LocalDate endDate;
        private String memoContains;
        private final Set<String> accountNumbers = new LinkedHashSet<>();
        private boolean requireAllAccounts;
        private AccountSide transactionSide;
        private Predicate<? super AccountingTransaction> predicate;

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

        public Builder memoContains(String memoContains)
        {
            this.memoContains = memoContains;
            return this;
        }

        public Builder addAccountNumber(String accountNumber)
        {
            if (accountNumber != null && !accountNumber.isBlank())
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

        public Builder transactionSide(AccountSide transactionSide)
        {
            this.transactionSide = transactionSide;
            return this;
        }

        public Builder addPredicate(
            Predicate<? super AccountingTransaction> predicate)
        {
            this.predicate = predicate;
            return this;
        }

        public LedgerQueryCriteria build()
        {
            return new LedgerQueryCriteria(this);
        }
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(startDate, endDate, memoContains, accountNumbers,
            requireAllAccounts, transactionSide, predicate);
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (!(obj instanceof LedgerQueryCriteria))
        {
            return false;
        }
        LedgerQueryCriteria other = (LedgerQueryCriteria) obj;
        return Objects.equals(startDate, other.startDate)
            && Objects.equals(endDate, other.endDate)
            && Objects.equals(memoContains, other.memoContains)
            && Objects.equals(accountNumbers, other.accountNumbers)
            && requireAllAccounts == other.requireAllAccounts
            && transactionSide == other.transactionSide
            && Objects.equals(predicate, other.predicate);
    }
}
