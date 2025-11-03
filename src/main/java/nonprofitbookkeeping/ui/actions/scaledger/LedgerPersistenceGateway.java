package nonprofitbookkeeping.ui.actions.scaledger;

import nonprofitbookkeeping.model.AccountingTransaction;

/**
 * Boundary interface that isolates the ledger importer from the
 * persistence mechanism used by the main application.
 */
public interface LedgerPersistenceGateway
{
    /**
     * Persist the provided transaction along with its child entries.
     *
     * @param transaction transaction to persist
     * @return the saved transaction (including any generated identifiers)
     */
    AccountingTransaction saveTransactionWithEntries(AccountingTransaction transaction);
}
