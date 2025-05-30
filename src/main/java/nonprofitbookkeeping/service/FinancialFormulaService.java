/**
 * nonprofit-scaledger-ribbon.zip_expanded
 * FinancialFormulaService.java
 * FinancialFormulaService
 */
package nonprofitbookkeeping.service;

import java.math.BigDecimal;
import java.util.List;
import nonprofitbookkeeping.model.Ledger;
import nonprofitbookkeeping.model.AccountingTransaction; // Added import

/**
 * Service class for applying financial formulas to ledger data.
 */
public class FinancialFormulaService
{

	/**
	 * Applies a predefined set of financial formulas to the given ledger.
	 * Currently, this method implements a placeholder formula:
	 * It calculates the sum of {@code totalAmount} for all transactions in the ledger.
	 * The {@code totalAmount} for a single transaction is typically the sum of its debit entries.
	 *
	 * @param ledger The {@link Ledger} object containing the transactions to be analyzed.
	 * @return A {@code double} representing the result of the applied formula.
	 *         For the current placeholder, this is the sum of all transaction total amounts.
	 *         Returns {@code 0.0} if the ledger is null, has no transactions, or
	 *         if transactions or their amounts are null.
	 */
	public static double applyFormulas(Ledger ledger)
	{
		if (ledger == null) {
			return 0.0;
		}

		List<AccountingTransaction> transactions = ledger.getTransactions();
		if (transactions == null || transactions.isEmpty()) {
			return 0.0;
		}

		BigDecimal sum = BigDecimal.ZERO;
		for (AccountingTransaction tx : transactions) {
			if (tx != null) {
				BigDecimal totalAmount = tx.getTotalAmount();
				if (totalAmount != null) {
					sum = sum.add(totalAmount);
				}
			}
		}
		
		return sum.doubleValue();
	}
	
}
