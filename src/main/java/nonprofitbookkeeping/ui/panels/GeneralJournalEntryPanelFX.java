package nonprofitbookkeeping.ui.panels;

import java.util.function.Consumer;

import nonprofitbookkeeping.model.AccountingTransaction;

/**
 * Compatibility wrapper that exposes the reimagined journal entry workspace
 * under the historic {@code GeneralJournalEntryPanelFX} type. Existing views
 * and tests continue to refer to this class while the new implementation
 * lives in {@link JournalEntryWorkspaceFX}.
 */
public class GeneralJournalEntryPanelFX extends JournalEntryWorkspaceFX
{
        public GeneralJournalEntryPanelFX(Consumer<AccountingTransaction> onSave)
        {
                super(onSave);
        }

        public GeneralJournalEntryPanelFX(AccountingTransaction existing,
                        Consumer<AccountingTransaction> onSave)
        {
                super(existing, onSave);
        }

        public GeneralJournalEntryPanelFX()
        {
                super();
        }
}

