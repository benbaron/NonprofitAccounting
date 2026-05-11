package org.nonprofitbookkeeping.ui;

import javafx.scene.Node;
import nonprofitbookkeeping.service.InventoryService;
import nonprofitbookkeeping.ui.panels.InventoryPanelFX;

/**
 * Panel-host wrapper for the JavaFX inventory workflow.
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
