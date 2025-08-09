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
 * This class might include methods for various financial calculations, ratios,
 * or analyses based on the ledger's transaction data.
 */
public class FinancialFormulaService
{

	/**
	 * Applies a predefined set of financial formulas to the given {@link Ledger}.
	 * <p>
	 * Currently, this method implements a placeholder formula: it calculates the sum
	 * of the "total amount" for all transactions in the ledger. The "total amount"
	 * of a single transaction is obtained via {@link AccountingTransaction#getTotalAmount()},
	 * which typically represents the sum of its debit entries.
	 * </p>
	 * <p>
	 * Future implementations could expand this method or add others to calculate
	 * various financial metrics (e.g., current ratio, debt-to-equity ratio, etc.).
	 * </p>
	 *
	 * @param ledger The {@link Ledger} object containing the transactions to be analyzed.
	 *               If null, or if it contains no transactions, or if transactions/amounts are null,
	 *               this method will return {@code 0.0}.
	 * @return A {@code double} representing the result of the applied formula.
	 *         For the current placeholder implementation, this is the sum of all transaction total amounts.
	 *         Returns {@code 0.0} if the input is invalid or results in no calculable sum.
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
