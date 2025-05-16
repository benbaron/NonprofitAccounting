package nonprofitbookkeeping.api;

import java.util.Map;

/**
 * TrialBalanceResultIntf defines the contract for a trial balance result.
 */
public interface TrialBalanceResultIntf {

    /**
     * Returns a map of total debit amounts (as positive numbers) keyed by account type name.
     *
     * @return a Map where keys are account type names and values are total debits.
     */
    Map<String, Double> getDebitSums();

    /**
     * Returns a map of total credit amounts (as positive numbers) keyed by account type name.
     *
     * @return a Map where keys are account type names and values are total credits.
     */
    Map<String, Double> getCreditSums();

    /**
     * Indicates whether the ledger is balanced (total debits equal total credits).
     *
     * @return true if balanced; false otherwise.
     */
    boolean isBalanced();
}
