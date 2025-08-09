
package nonprofitbookkeeping.model;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Utility to create a lookup map from account number to {@link Account}
 * details. Separating this from {@link ChartOfAccounts} allows other
 * components to generate the map without depending on ChartOfAccounts
 * implementation details.
 */
public final class AccountNumberMap
{
	
	private final Map<String, Account> accountMap;
	
	/**
	 * Builds a map for the provided accounts.
	 *
	 * @param accounts collection of accounts to index
	 */
	public AccountNumberMap(Collection<Account> accounts)
	{
		
		if (accounts == null)
		{
			this.accountMap = Collections.emptyMap();
		}
		else
		{
			this.accountMap = accounts.stream()
				.collect(Collectors.toUnmodifiableMap(Account::getAccountNumber,
					a -> a,
					(a, b) -> a));
		}
		
	}
	
	/**
	 * Retrieves an account by its account number.
	 *
	 * @param accountNumber the account number to look up
	 * @return the matching account or {@code null} if not found
	 */
	public Account get(String accountNumber)
	{
		return this.accountMap.get(accountNumber);
	}
	
	/**
	 * Returns an unmodifiable view of the underlying map.
	 *
	 * @return map of account numbers to accounts
	 */
	public Map<String, Account> asMap()
	{
		return this.accountMap;
	}
	
}
