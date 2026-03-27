package nonprofitbookkeeping.ui.adapters;

import nonprofitbookkeeping.ui.MainApplicationView;
import nonprofitbookkeeping.ui.panels.skeletons.SkeletonJournalPanel;

/**
 * Adapter-facing contract for navigating legacy workspaces without hard dependency on MainApplicationView.
 */
public interface LegacyWorkspaceNavigator
{
    void showPanel(MainApplicationView.PanelType panelType);

    void showCompanySelection();

    void showWorkspaceTabs();

    void updateCompanyOpenState(boolean companyOpen);

    SkeletonJournalPanel journalPanel();
}
