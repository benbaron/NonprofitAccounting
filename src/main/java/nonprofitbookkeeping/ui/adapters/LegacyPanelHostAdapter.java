package nonprofitbookkeeping.ui.adapters;

import javafx.scene.Node;
import org.nonprofitbookkeeping.ui.AppPanelId;
import org.nonprofitbookkeeping.ui.PanelHost;

/**
 * Adapter layer for presenting legacy panel/navigation requests in the canonical shell host.
 */
public class LegacyPanelHostAdapter
{
    private final PanelHost panelHost;

    public LegacyPanelHostAdapter(PanelHost panelHost)
    {
        this.panelHost = panelHost;
    }

    public void show(AppPanelId panelId)
    {
        panelHost.show(panelId);
    }

    public Node activePanelRoot()
    {
        return panelHost.getCenter();
    }
}
