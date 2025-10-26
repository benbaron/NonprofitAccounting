package nonprofitbookkeeping.ui.panels;

import java.util.function.Consumer;

import nonprofitbookkeeping.model.AccountingTransaction;

/**
 * Legacy entry point that now delegates to {@link JournalEntryWorkspaceFX}.
 *
 * <p>The historic Swing application referenced {@code GeneralJournalEntryPanelFX}
 * directly when embedding the general journal editor.  The modernised
 * implementation lives in {@link JournalEntryWorkspaceFX}; this class simply
 * exposes the former type while reusing the new workspace behaviour.</p>
 */
public class GeneralJournalEntryPanelFX extends JournalEntryWorkspaceFX
{
        /** Creates a new entry workspace that logs saved transactions. */
        public GeneralJournalEntryPanelFX()
        {
                super();
        }

        /**
         * Creates a workspace configured for capturing a new transaction.
         *
         * @param onSave callback invoked when the user saves the entry
         */
        public GeneralJournalEntryPanelFX(Consumer<AccountingTransaction> onSave)
        {
                super(onSave);
        }

        /**
         * Creates a workspace for editing an existing transaction.
         *
         * @param existing the transaction to edit (or {@code null} for new)
         * @param onSave   callback invoked with the saved transaction
         */
        public GeneralJournalEntryPanelFX(AccountingTransaction existing,
                        Consumer<AccountingTransaction> onSave)
        {
                super(existing, onSave);
        }
}
