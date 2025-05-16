
package nonprofitbookkeeping.model;

import com.google.common.base.MoreObjects;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import nonprofitbookkeeping.api.AccountDetails;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
@Data
@AllArgsConstructor
@NoArgsConstructor
/**
 * Represents an immutable collection of available accounts.
 */
public class ChartOfAccounts implements Serializable
{
	/**
	 * serialVersionUID : long
	 */
	private static final long serialVersionUID = 6545569795380871696L;
	private final Map<String, AccountDetails> accountNumberToAccountDetails;
	
	/**
	 * 
	 * Constructor ChartOfAccounts
	 * @param accountDetails
	 */
	public ChartOfAccounts(Set<AccountDetails> accountDetails)
	{
		checkNotNull(accountDetails);
		checkArgument(!accountDetails.isEmpty());
		this.accountNumberToAccountDetails = new HashMap<>();
		accountDetails
			.forEach(ad -> this.accountNumberToAccountDetails.put(ad.getAccountNumber(), ad));
	}
	
	/**
	 * 
	 * @return
	 */
	public Map<String, AccountDetails> getAccountNumberToAccountDetails()
	{
		return new HashMap<>(this.accountNumberToAccountDetails);
	}
	
	/**
	 * 
	 * Override @see java.lang.Object#toString()
	 */
	@Override public String toString()
	{
		return MoreObjects.toStringHelper(this)
			.add("accounts", this.accountNumberToAccountDetails.values())
			.toString();
	}
	
}
