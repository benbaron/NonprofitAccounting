package nonprofitbookkeeping.ui.adapters;

import javafx.scene.control.Tab;
import org.nonprofitbookkeeping.ui.AppPanel;

/**
 * Factory for adapting org.nonprofitbookkeeping.ui AppPanels into legacy Tab-based subpanels.
 */
public final class OrgAppPanelTabAdapter
{
    private OrgAppPanelTabAdapter() {}

    public static Tab toTab(AppPanel appPanel)
    {
        OrgAppPanelNodeAdapter content = new OrgAppPanelNodeAdapter(appPanel);
        Tab tab = new Tab(appPanel.title(), content);
        tab.setClosable(false);
        return tab;
    }
}
