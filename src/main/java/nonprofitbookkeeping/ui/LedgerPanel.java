package nonprofitbookkeeping.ui;

import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.BorderPane;

/**
 * Ledger workspace with ledger subpanels.
 */
public class LedgerPanel extends BorderPane
{

	/**
	 * Creates the ledger workspace.
	 */
	public LedgerPanel()
	{
		TabPane subTabs = new TabPane();
		Tab registerTab = new Tab("Ledger Register", new LedgerRegisterPanel());
		Tab transactionEditorTab =
			new Tab("Transaction Editor", new TransactionEditorPanel());
		registerTab.setClosable(false);
		transactionEditorTab.setClosable(false);
		subTabs.getTabs().addAll(registerTab, transactionEditorTab);
		setCenter(subTabs);
	}

}
