package nonprofitbookkeeping.ui.panels;

import java.util.function.Consumer;

import nonprofitbookkeeping.model.AccountingTransaction;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Legacy compatibility wrapper that exposes {@link JournalEntryWorkspaceFX}
 * using the historic {@code GeneralJournalEntryPanelFX} type.
 */
public class GeneralJournalEntryPanelFX extends JournalEntryWorkspaceFX
{
        private static final Logger LOGGER = LoggerFactory
                        .getLogger(GeneralJournalEntryPanelFX.class);

        /**
         * Creates a new workspace that invokes the supplied callback when the user
         * saves the transaction.
         *
         * @param onSave consumer invoked with the persisted transaction
         */
        public GeneralJournalEntryPanelFX(Consumer<AccountingTransaction> onSave)
        {
                super(onSave);
        }

        /**
         * Creates a workspace for editing an existing transaction.
         *
         * @param existing existing transaction to edit, or {@code null} for a new
         *                 entry
         * @param onSave   consumer invoked with the persisted transaction
         */
        public GeneralJournalEntryPanelFX(AccountingTransaction existing,
                        Consumer<AccountingTransaction> onSave)
        {
                super(existing, onSave);
        }

        /**
         * Convenience constructor that simply logs the saved transaction.
         */
        public GeneralJournalEntryPanelFX()
        {
                super(tx -> LOGGER.debug("{}", tx));
        }
}

