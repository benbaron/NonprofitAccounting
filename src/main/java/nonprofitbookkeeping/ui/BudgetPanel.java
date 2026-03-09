package nonprofitbookkeeping.ui;

import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.BorderPane;
import org.nonprofitbookkeeping.ui.BudgetEditorPanel;
import org.nonprofitbookkeeping.ui.BudgetVsActualPanel;

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
		Tab editorTab = new Tab("Budget Editor", new BudgetEditorPanel());
		Tab budgetVsActualTab =
			new Tab("Budget vs Actual", new BudgetVsActualPanel());
		editorTab.setClosable(false);
		budgetVsActualTab.setClosable(false);
		subTabs.getTabs().addAll(editorTab, budgetVsActualTab);
		setCenter(subTabs);
	}

}
