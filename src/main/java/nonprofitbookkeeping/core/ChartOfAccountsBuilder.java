
package nonprofitbookkeeping.core;

import java.util.ArrayList;
import java.util.List;

import nonprofitbookkeeping.model.Account;
import nonprofitbookkeeping.model.AccountSide;
import nonprofitbookkeeping.model.ChartOfAccounts;

/**
 * 
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
                this.pendingAccounts.add(acc);
                return this;
        }

	
        /**
         * Builds a new {@link ChartOfAccounts} instance populated with the accounts
         * registered through this builder.  Each invocation produces a fresh chart
         * instance so callers can keep using the builder to create additional
         * charts if desired.
         *
         * @return A populated {@link ChartOfAccounts}.
         */
        public ChartOfAccounts build()
        {
                ChartOfAccounts chart = new ChartOfAccounts();

                for (Account pending : this.pendingAccounts)
                {
                        chart.addAccount(copyAccount(pending));
                }

                return chart;
        }

        private static Account copyAccount(Account original)
        {
                Account copy = new Account();
                copy.setAccountNumber(original.getAccountNumber());
                copy.setName(original.getName());
                copy.setIncreaseSide(original.getIncreaseSide());
                return copy;
        }


}
