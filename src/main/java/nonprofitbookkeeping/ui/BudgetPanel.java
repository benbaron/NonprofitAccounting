package nonprofitbookkeeping.ui;

import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.BorderPane;
import nonprofitbookkeeping.ui.adapters.OrgAppPanelTabAdapter;

/**
 * Budget workspace with budget subpanels.
 */
public class BudgetPanel extends BorderPane
{

	/**
	 * Creates the budget workspace.
	 */
	public BudgetPanel()
	{
		TabPane subTabs = new TabPane();
		Tab editorTab = OrgAppPanelTabAdapter
			.toTab(new BudgetEditorPanel());
		Tab budgetVsActualTab = OrgAppPanelTabAdapter
			.toTab(new BudgetVsActualPanel());
		subTabs.getTabs().addAll(editorTab, budgetVsActualTab);
		setCenter(subTabs);
	}

}
