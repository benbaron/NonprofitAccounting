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
 * results, where the trial balance is computed upon instantiation. The primary computation logic
 * is also available via a static {@link #compute(Ledger, LocalDate, LocalDate)} method.
 */
public class TrialBalanceService implements TrialBalanceServiceIntf
{

    /**
     * A private static record implementing {@link TrialBalanceResultIntf} to hold the results
     * of a trial balance computation.
     * It stores debit and credit sums internally using {@link BigDecimal} for precision,
     * but converts these to {@link Double} when accessed via the interface methods,
     * which might lead to precision loss if not handled carefully by the caller.
     *
     * @param internalDebitMap A map of account IDs (String) to their total debit sums (as {@link BigDecimal}).
     * @param internalCreditMap A map of account IDs (String) to their total credit sums (as {@link BigDecimal}).
     * @param isBalanced A boolean indicating if the total debits equal total credits.
     */
    private static record TrialBalanceResultImpl(
            Map<String, BigDecimal> internalDebitMap,
            Map<String, BigDecimal> internalCreditMap,
            boolean isBalanced) implements TrialBalanceResultIntf {

        /**
         * {@inheritDoc}
         * Converts internal BigDecimal debit sums to Double for the returned map.
         */
        @Override
        public Map<String, Double> getDebitSums() {
            if (this.internalDebitMap == null) {
                return Collections.emptyMap();
            }
            return this.internalDebitMap.entrySet().stream()
                    .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().doubleValue()));
        }

        /**
         * {@inheritDoc}
         * Converts internal BigDecimal credit sums to Double for the returned map.
         */
        @Override
        public Map<String, Double> getCreditSums() {
            if (this.internalCreditMap == null) {
                return Collections.emptyMap();
            }
            return this.internalCreditMap.entrySet().stream()
                    .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().doubleValue()));
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean isBalanced() {
            return this.isBalanced;
        }
    }

	/** Stores the result of the trial balance computation performed at the time of service instantiation. */
	private final TrialBalanceResultIntf computedResult;

    /**
     * Constructs a new {@code TrialBalanceService} instance.
     * The trial balance is computed immediately upon instantiation using the provided
     * {@link Ledger} and date range ({@code from} and {@code to}, inclusive).
     * The result of this computation is stored and made available through the
     * {@link TrialBalanceServiceIntf} methods implemented by this class.
     *
     * @param ledger The {@link Ledger} for which to compute the trial balance.
     *               If null, the computed result will be empty and balanced.
     * @param from The start date of the period (inclusive) for which to compute the trial balance.
     *             If null, the computed result will be empty and balanced.
     * @param to The end date of the period (inclusive) for which to compute the trial balance.
     *           If null, or if {@code from} is after {@code to}, the computed result will be empty and balanced.
     */
    public TrialBalanceService(Ledger ledger, LocalDate from, LocalDate to) {
        this.computedResult = compute(ledger, from, to);
    }

	/**
	 * {@inheritDoc}
	 * <p>Returns the map of account IDs to total debit sums (as {@link Double}) from the trial balance
	 * that was computed when this service instance was created.
	 * </p>
	 */
	@Override public Map<String, Double> getDebitSums()
	{
		return this.computedResult.getDebitSums();
	}

	/**
	 * {@inheritDoc}
	 * <p>Returns the map of account IDs to total credit sums (as {@link Double}) from the trial balance
	 * that was computed when this service instance was created.
	 * </p>
	 */
	@Override public Map<String, Double> getCreditSums()
	{
		return this.computedResult.getCreditSums();
	}

	/**
	 * {@inheritDoc}
	 * <p>Returns whether the trial balance computed when this service instance was created
	 * is balanced (i.e., total debits equal total credits).
	 * </p>
	 */
	@Override public boolean isBalanced()
	{
		return this.computedResult.isBalanced();
	}

	/**
     * Computes the trial balance for a given {@link Ledger} within a specified date range.
     * This static method aggregates total debits and credits for each account by processing
     * transactions that fall within the {@code from} and {@code to} dates (inclusive).
     * Account balances are calculated using {@link BigDecimal} for precision.
     *
     * @param ledger The {@link Ledger} containing financial transactions.
     *               If null or contains no transactions, an empty, balanced result is returned.
     * @param from The start date of the period (inclusive).
     *             If null, or if {@code from} is after {@code to}, an empty, balanced result is returned.
     * @param to The end date of the period (inclusive).
     *           If null, or if {@code from} is after {@code to}, an empty, balanced result is returned.
     * @return A {@link TrialBalanceResultIntf} object encapsulating the debit sums per account,
     *         credit sums per account, and the overall balance status.
     *         The maps within the result use account numbers as keys and {@link BigDecimal} sums as values,
     *         though the interface methods {@link TrialBalanceResultIntf#getDebitSums()} and
     *         {@link TrialBalanceResultIntf#getCreditSums()} in the returned {@link TrialBalanceResultImpl}
     *         convert these to {@link Double}.
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
                System.err.println("Could not parse date for transaction: " + tx.getBookingDateTimestamp() + ", date string: " + tx.getDate());
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
