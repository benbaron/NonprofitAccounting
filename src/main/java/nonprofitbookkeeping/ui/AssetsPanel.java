package nonprofitbookkeeping.ui;

import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.BorderPane;

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
		Tab registerTab = new Tab("Asset Register", new AssetsRegisterPanel());
		Tab depreciationRunsTab =
			new Tab("Depreciation Runs", new DepreciationRunsPanel());
		registerTab.setClosable(false);
		depreciationRunsTab.setClosable(false);
		subTabs.getTabs().addAll(registerTab, depreciationRunsTab);
		setCenter(subTabs);
	}

}
