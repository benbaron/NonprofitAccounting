package nonprofitbookkeeping.ui.adapters;

import nonprofitbookkeeping.ui.MainApplicationView;
import nonprofitbookkeeping.ui.panels.skeletons.SkeletonJournalPanel;

/**
 * Bridges MainApplicationView to a narrow navigation contract used by legacy menu/actions.
 */
public class MainApplicationViewNavigatorAdapter implements LegacyWorkspaceNavigator
{
    private final MainApplicationView mainApplicationView;

    public MainApplicationViewNavigatorAdapter(MainApplicationView mainApplicationView)
    {
        this.mainApplicationView = mainApplicationView;
    }

    @Override
    public void showPanel(MainApplicationView.PanelType panelType)
    {
        mainApplicationView.showPanel(panelType);
    }

    @Override
    public void showCompanySelection()
    {
        mainApplicationView.showCompanySelection();
    }

    @Override
    public void showWorkspaceTabs()
    {
        mainApplicationView.showWorkspaceTabs();
    }

    @Override
    public void updateCompanyOpenState(boolean companyOpen)
    {
        mainApplicationView.updateCompanyOpenState(companyOpen);
    }

    @Override
    public SkeletonJournalPanel journalPanel()
    {
        return mainApplicationView.getJournalPanel();
    }
}
