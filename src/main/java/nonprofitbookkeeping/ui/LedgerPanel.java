package nonprofitbookkeeping.ui;

import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.BorderPane;
import nonprofitbookkeeping.ui.adapters.OrgAppPanelTabAdapter;

/**
 * Ledger workspace showing the register only.
 */
public class LedgerPanel extends BorderPane
{
	/**
	 * Creates the ledger workspace.
	 */
	public LedgerPanel()
	{
		TabPane subTabs = new TabPane();
		Tab registerTab = OrgAppPanelTabAdapter.toTab(new LedgerRegisterPanel());
		subTabs.getTabs().add(registerTab);
		setCenter(subTabs);
	}
}
