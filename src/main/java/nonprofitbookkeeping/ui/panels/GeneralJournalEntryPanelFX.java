package nonprofitbookkeeping.ui.panels;

import java.util.function.Consumer;

import nonprofitbookkeeping.model.AccountingTransaction;

/**
 * Backwards-compatible entry point that preserves the legacy
 * {@code GeneralJournalEntryPanelFX} type while delegating to the modern
 * {@link JournalEntryWorkspaceFX} implementation.  Older call-sites can
 * continue to construct this class and receive the updated workspace without
 * needing to change imports.
 */
public class GeneralJournalEntryPanelFX extends JournalEntryWorkspaceFX
{
        /** Convenience constructor mirroring the historical no-arg variant. */
        public GeneralJournalEntryPanelFX()
        {
                super();
        }

        /**
         * Creates a new panel that invokes {@code onSave} when a transaction is
         * persisted.
         *
         * @param onSave callback executed after the transaction is saved
         */
        public GeneralJournalEntryPanelFX(Consumer<AccountingTransaction> onSave)
        {
                super(onSave);
        }

        /**
         * Creates a panel that pre-populates the UI using an existing
         * transaction and notifies {@code onSave} when edits are committed.
         *
         * @param existing existing transaction to edit (may be {@code null})
         * @param onSave   callback executed after saving the transaction
         */
        public GeneralJournalEntryPanelFX(AccountingTransaction existing,
                        Consumer<AccountingTransaction> onSave)
        {
                super(existing, onSave);
        }
}
