
package nonprofitbookkeeping.core;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import nonprofitbookkeeping.model.*;

/**
 * Fluent builder that stages {@link Account} instances and produces a
 * {@link ChartOfAccounts} containing copies of those accounts when
 * {@link #build()} is invoked.
 */
public class ChartOfAccountsBuilder
{
        private final List<Account> pendingAccounts = new ArrayList<>();
	
	/**
	 * Constructs a ChartOfAccountsBuilder.
	 */
	public ChartOfAccountsBuilder()
	{
	}
	
	/**
	 * Creates a new ChartOfAccountsBuilder.
	 * @return A new ChartOfAccountsBuilder instance.
	 */
	public static ChartOfAccountsBuilder create()
	{
		return new ChartOfAccountsBuilder();
	}
	
	/**
	 * Adds a new account to the chart of accounts.
	 * @param accountNumber The account number.
	 * @param name The name of the account.
	 * @param increaseSide The side where the account increases (Debit or Credit).
	 * @return This ChartOfAccountsBuilder instance for chaining.
	 */
        public ChartOfAccountsBuilder addAccount(
                String accountNumber,
                String name,
                AccountSide increaseSide)
        {
                Account acc = new Account();
                acc.setAccountNumber(accountNumber);
                acc.setName(name);
                acc.setIncreaseSide(increaseSide);
                return addAccount(acc);
        }

        /**
         * Adds an existing {@link Account} instance to the staged accounts that will be
         * copied into the resulting {@link ChartOfAccounts} when {@link #build()} is
         * invoked.
         *
         * @param account the account to add; must not be {@code null}
         * @return this builder for fluent chaining
         * @throws NullPointerException if {@code account} is {@code null}
         */
        public ChartOfAccountsBuilder addAccount(Account account)
        {
                Account safeCopy = copyAccount(Objects.requireNonNull(account, "account"));
                this.pendingAccounts.add(safeCopy);
                return this;
        }

	
        /**
         * Builds the ChartOfAccounts using the staged accounts collected by this
         * builder.
         *
         * @return A new ChartOfAccounts instance containing copies of the staged
         *         accounts.
         */
        public ChartOfAccounts build()
        {
                ChartOfAccounts chart = new ChartOfAccounts();

                for (Account account : this.pendingAccounts)
                {
                        chart.addAccount(copyAccount(account));
                }

                return chart;
        }

        private static Account copyAccount(Account source)
        {
                Account copy = new Account();
                copy.setAccountNumber(source.getAccountNumber());
                copy.setName(source.getName());
                copy.setIncreaseSide(source.getIncreaseSide());
                copy.setAccountCode(source.getAccountCode());
                copy.setAccountType(source.getAccountType());
                copy.setParentAccountId(source.getParentAccountId());
                copy.setCurrency(source.getCurrency());
                copy.setOpeningBalance(source.getOpeningBalance());
                List<String> fundIds = source.getAssociatedFundIds();
                copy.setAssociatedFundIds(
                        fundIds == null ? new ArrayList<>() : new ArrayList<>(fundIds));
                return copy;
        }

	
}
