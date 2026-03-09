package nonprofitbookkeeping.ui.adapters;

import javafx.scene.layout.BorderPane;
import org.nonprofitbookkeeping.ui.AppPanel;

/**
 * Adapts an {@link AppPanel} from the org.nonprofitbookkeeping.ui shell into a plain JavaFX node
 * that can be embedded inside legacy nonprofitbookkeeping.ui tabs/subpanels.
 */
public class OrgAppPanelNodeAdapter extends BorderPane
{
    private final AppPanel appPanel;

    public OrgAppPanelNodeAdapter(AppPanel appPanel)
    {
        if (appPanel == null)
        {
            throw new IllegalArgumentException("appPanel must not be null");
        }
        this.appPanel = appPanel;
        setCenter(appPanel.root());
    }

    public AppPanel getAppPanel()
    {
        return appPanel;
    }

    public void onSave()
    {
        appPanel.onSave();
    }

    public void onNew()
    {
        appPanel.onNew();
    }

    public void onCopy()
    {
        appPanel.onCopy();
    }

    public void onPaste()
    {
        appPanel.onPaste();
    }
}
