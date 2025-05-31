/**
 * nonprofit-scaledger-ribbon.zip_expanded
 * TrialBalanceService.java
 * TrialBalanceService
 */
package nonprofitbookkeeping.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import nonprofitbookkeeping.api.TrialBalanceResultIntf;
import nonprofitbookkeeping.api.TrialBalanceServiceIntf; // Assuming this exists for the class implements clause
import nonprofitbookkeeping.model.Ledger;
import nonprofitbookkeeping.model.AccountingTransaction;
import nonprofitbookkeeping.model.AccountingEntry;
import nonprofitbookkeeping.model.AccountSide;


/**
 * Service to compute trial balance from a ledger within a specified date range.
 * This service also implements {@link TrialBalanceServiceIntf} for instance-based
 * results, though the primary computation logic is provided via a static method.
 */
public class TrialBalanceService implements TrialBalanceServiceIntf
{

    /**
     * A private static record implementing {@link TrialBalanceResultIntf}.
     * It stores debit and credit sums using {@link BigDecimal} for precision internally,
     * but converts them to {@link Double} when exposing them via the interface methods.
     *
     * @param internalDebitMap A map of account IDs to their total debit sums (as BigDecimal).
     * @param internalCreditMap A map of account IDs to their total credit sums (as BigDecimal).
     * @param isBalanced True if total debits equal total credits, false otherwise.
     */
    private static record TrialBalanceResultImpl(
            Map<String, BigDecimal> internalDebitMap,
            Map<String, BigDecimal> internalCreditMap,
            boolean isBalanced) implements TrialBalanceResultIntf {

        @Override
        public Map<String, Double> getDebitSums() {
            if (this.internalDebitMap == null) {
                return Collections.emptyMap();
            }
            return this.internalDebitMap.entrySet().stream()
                    .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().doubleValue()));
        }

        @Override
        public Map<String, Double> getCreditSums() {
            if (this.internalCreditMap == null) {
                return Collections.emptyMap();
            }
            return this.internalCreditMap.entrySet().stream()
                    .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().doubleValue()));
        }

        @Override
        public boolean isBalanced() {
            return this.isBalanced;
        }
    }

	private final TrialBalanceResultIntf computedResult;

    /**
     * Constructs a new TrialBalanceService instance.
     * The trial balance is computed immediately upon instantiation using the provided
     * ledger and date range, and the result is stored for subsequent retrieval by
     * the getter methods.
     *
     * @param ledger The {@link Ledger} for which to compute the trial balance.
     * @param from The start date of the period (inclusive).
     * @param to The end date of the period (inclusive).
     */
    public TrialBalanceService(Ledger ledger, LocalDate from, LocalDate to) {
        this.computedResult = compute(ledger, from, to);
    }

	/**
	 * Gets the map of account IDs to total debit sums from the trial balance
	 * computed when this service instance was created.
	 *
	 * @return A map where keys are account IDs (String) and values are total debit amounts (Double).
	 *         Implements {@link TrialBalanceServiceIntf#getDebitSums()}.
	 */
	@Override public Map<String, Double> getDebitSums()
	{
		return this.computedResult.getDebitSums();
	}

	/**
	 * Gets the map of account IDs to total credit sums from the trial balance
	 * computed when this service instance was created.
	 *
	 * @return A map where keys are account IDs (String) and values are total credit amounts (Double).
	 *         Implements {@link TrialBalanceServiceIntf#getCreditSums()}.
	 */
	@Override public Map<String, Double> getCreditSums()
	{
		return this.computedResult.getCreditSums();
	}

	/**
	 * Checks if the trial balance computed when this service instance was created is balanced
	 * (i.e., total debits equal total credits).
	 *
	 * @return {@code true} if the trial balance is balanced, {@code false} otherwise.
	 *         Implements {@link TrialBalanceServiceIntf#isBalanced()}.
	 */
	@Override public boolean isBalanced()
	{
		return this.computedResult.isBalanced();
	}

	/**
     * Computes the trial balance for a given ledger within a specified date range.
     * It aggregates total debits and credits for each account based on transactions
     * falling within the date range (inclusive).
     *
     * @param ledger The {@link Ledger} containing financial transactions.
     * @param from The start date of the period (inclusive).
     * @param to The end date of the period (inclusive).
     * @return A {@link TrialBalanceResultIntf} object containing the debit sums,
     *         credit sums, and the balance status. Returns an empty, balanced result
     *         if the ledger is null, has no transactions, or the date range is invalid.
     */
	public static TrialBalanceResultIntf compute(Ledger ledger, LocalDate from, LocalDate to)
	{
		Map<String, BigDecimal> debitSums = new HashMap<>();
		Map<String, BigDecimal> creditSums = new HashMap<>();

		if (ledger == null || ledger.getTransactions() == null) {
			return new TrialBalanceResultImpl(debitSums, creditSums, true);
		}
        if (from == null || to == null || from.isAfter(to)) {
            // Invalid date range, return empty and balanced
            return new TrialBalanceResultImpl(debitSums, creditSums, true);
        }

		List<AccountingTransaction> transactions = ledger.getTransactions();
		if (transactions.isEmpty()) {
			return new TrialBalanceResultImpl(debitSums, creditSums, true);
		}

        DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE; // Assumes "YYYY-MM-DD"

		for (AccountingTransaction tx : transactions) {
			if (tx == null || tx.getEntries() == null || tx.getDate() == null) {
				continue;
			}

            LocalDate transactionDate;
            try {
                // Assuming tx.getDate() returns a string like "YYYY-MM-DD"
                transactionDate = LocalDate.parse(tx.getDate(), formatter);
            } catch (DateTimeParseException e) {
                // Log error or skip transaction if date is unparsable
                System.err.println("Could not parse date for transaction: " + tx.getId() + ", date string: " + tx.getDate());
                continue;
            }

            if (transactionDate.isBefore(from) || transactionDate.isAfter(to)) {
                continue; // Skip transaction if not in date range
            }

			for (AccountingEntry entry : tx.getEntries()) {
				if (entry == null || entry.getAmount() == null || entry.getAccountNumber() == null || entry.getAccountNumber().trim().isEmpty()) {
					continue;
				}
				String accountId = entry.getAccountNumber();
				BigDecimal amount = entry.getAmount();

				if (entry.getAccountSide() == AccountSide.DEBIT) {
					debitSums.put(accountId, debitSums.getOrDefault(accountId, BigDecimal.ZERO).add(amount));
				} else if (entry.getAccountSide() == AccountSide.CREDIT) {
					creditSums.put(accountId, creditSums.getOrDefault(accountId, BigDecimal.ZERO).add(amount));
				}
			}
		}

		BigDecimal totalDebits = debitSums.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add);
		BigDecimal totalCredits = creditSums.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add);
		boolean balanced = totalDebits.compareTo(totalCredits) == 0;

		return new TrialBalanceResultImpl(debitSums, creditSums, balanced);
	}
	
}
