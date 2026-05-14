package org.nonprofitbookkeeping.ui;

import javafx.scene.Node;
import nonprofitbookkeeping.service.InventoryService;
import nonprofitbookkeeping.ui.panels.InventoryPanelFX;

/**
 * Intentional adapter boundary from canonical org.nonprofitbookkeeping.ui shell to legacy JavaFX inventory workflow.
 */
public class InventoryPanel implements AppPanel
{
    private final InventoryPanelFX content = new InventoryPanelFX(new InventoryService(), null);

    @Override
    public String title()
    {
        return "Inventory";
    }

    @Override
    public Node root()
    {
        return content;
    }
}
