package nonprofitbookkeeping.ui.panels.skeletons;

import nonprofitbookkeeping.ui.JournalShellNavigation;
import nonprofitbookkeeping.ui.panels.JournalPanelFX;

/**
 * Compatibility name for the tabbed shell's journal panel.
 *
 * <p>The former entry-per-row skeleton has been replaced by the canonical
 * transaction-block {@link JournalPanelFX}. Keeping this subclass avoids
 * changing older shell wiring while ensuring every debit, credit, and
 * transaction-wide note is displayed as one journal-entry unit.</p>
 */
public class SkeletonJournalPanel extends JournalPanelFX
{
    public SkeletonJournalPanel()
    {
        super(JournalShellNavigation::openLedgerTransaction);
    }

    /**
     * Retained for the existing Ctrl+F accelerator. The grouped journal does
     * not currently expose a text filter, so focus is moved to the journal
     * workspace itself.
     */
    public void focusSearchField()
    {
        requestFocus();
    }
}
