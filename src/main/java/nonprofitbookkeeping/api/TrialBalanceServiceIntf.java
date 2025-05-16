
package nonprofitbookkeeping.api;

import java.util.Map;

/**
 * TrialBalanceResultIntf defines the contract for a trial balance result.
 * Implementations of this interface provide methods to retrieve
 * aggregated debit and credit totals by account, as well as a check to
 * determine if the ledger is balanced.
 */
public interface TrialBalanceServiceIntf
{

	/**
	 * Returns a map where the keys are account identifiers (as Strings) 
	 * and the values are the total debit amounts for those accounts.
	 *
	 * @return a Map of debit sums per account.
	 */
	Map<String, Double> getDebitSums();
	
	/**
	 * Returns a map where the keys are account identifiers (as Strings)
	 * and the values are the total credit amounts for those accounts.
	 *
	 * @return a Map of credit sums per account.
	 */
	Map<String, Double> getCreditSums();
	
	/**
	 * Returns a boolean indicating whether the ledger is balanced.
	 * In a balanced ledger, total debits equal total credits.
	 *
	 * @return true if the ledger is balanced, false otherwise.
	 */
	boolean isBalanced();
	

}
