package nonprofitbookkeeping.ui;

import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.BorderPane;
import nonprofitbookkeeping.ui.adapters.OrgAppPanelTabAdapter;

/**
 * Assets workspace with asset subpanels.
 */
public class AssetsPanel extends BorderPane
{

	/**
	 * Creates the assets workspace.
	 */
	public AssetsPanel()
	{
		TabPane subTabs = new TabPane();
		Tab registerTab = OrgAppPanelTabAdapter
			.toTab(new AssetsRegisterPanel());
		Tab depreciationRunsTab = OrgAppPanelTabAdapter
			.toTab(new DepreciationRunsPanel());
		subTabs.getTabs().addAll(registerTab, depreciationRunsTab);
		setCenter(subTabs);
	}

}
