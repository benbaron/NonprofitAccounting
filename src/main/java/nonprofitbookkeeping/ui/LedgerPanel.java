package nonprofitbookkeeping.ui;

import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.BorderPane;
import nonprofitbookkeeping.ui.adapters.OrgAppPanelTabAdapter;

/**
 * Ledger workspace with ledger subpanels.
 */
public class LedgerPanel extends BorderPane
{
	private final TabPane subTabs = new TabPane();
	private final Tab registerTab;
	private final Tab transactionEditorTab;

	/**
	 * Creates the ledger workspace.
	 */
	public LedgerPanel()
	{
		this.registerTab = OrgAppPanelTabAdapter
			.toTab(new LedgerRegisterPanel());
		this.transactionEditorTab = OrgAppPanelTabAdapter
			.toTab(new TransactionEditorPanel());
		this.subTabs.getTabs().addAll(this.registerTab, this.transactionEditorTab);
		setCenter(this.subTabs);

		LedgerSelectionContext.selectedSubpanelProperty().addListener((obs, oldValue, newValue) -> {
			if (newValue == LedgerSelectionContext.LedgerSubpanel.EDITOR)
			{
				this.subTabs.getSelectionModel().select(this.transactionEditorTab);
			}
			else
			{
				this.subTabs.getSelectionModel().select(this.registerTab);
			}
		});

		this.subTabs.getSelectionModel().selectedItemProperty()
			.addListener((obs, oldTab, newTab) -> {
				if (newTab == this.transactionEditorTab)
				{
					LedgerSelectionContext
						.setSelectedSubpanel(LedgerSelectionContext.LedgerSubpanel.EDITOR);
				}
				else if (newTab == this.registerTab)
				{
					LedgerSelectionContext
						.setSelectedSubpanel(LedgerSelectionContext.LedgerSubpanel.REGISTER);
				}
			});
	}

}
