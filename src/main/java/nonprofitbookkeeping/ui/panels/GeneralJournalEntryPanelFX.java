package nonprofitbookkeeping.ui.panels;

import java.util.function.Consumer;

import nonprofitbookkeeping.model.AccountingTransaction;

/**
 * Legacy facade that preserves the historical {@code GeneralJournalEntryPanelFX}
 * type while delegating all behaviour to {@link JournalEntryWorkspaceFX}.
 * Existing callers can continue instantiating this class without needing to
 * know about the newer workspace implementation.
 */
public class GeneralJournalEntryPanelFX extends JournalEntryWorkspaceFX
{
        /**
         * Creates a new panel for recording a transaction with the provided
         * save callback.
         *
         * @param onSave consumer invoked when the transaction should be
         *               persisted
         */
        public GeneralJournalEntryPanelFX(Consumer<AccountingTransaction> onSave)
        {
                super(onSave);
        }

        /**
         * Creates a panel for editing an existing transaction or, when
         * {@code existing} is {@code null}, a new transaction.
         *
         * @param existing the transaction to edit, or {@code null}
         * @param onSave   consumer invoked with the transaction when saved
         */
        public GeneralJournalEntryPanelFX(AccountingTransaction existing,
                        Consumer<AccountingTransaction> onSave)
        {
                super(existing, onSave);
        }

        /** Convenience constructor that logs the transaction when saved. */
        public GeneralJournalEntryPanelFX()
        {
                super();
        }
}
