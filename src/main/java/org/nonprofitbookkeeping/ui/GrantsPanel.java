package org.nonprofitbookkeeping.ui;

import javafx.scene.Node;
import nonprofitbookkeeping.service.GrantsService;
import nonprofitbookkeeping.ui.panels.GrantsPanelFX;

/**
 * B-shell wrapper panel for legacy grants management UI.
 */
public class GrantsPanel implements AppPanel
{
    private final GrantsPanelFX panel = new GrantsPanelFX(new GrantsService());

    @Override
    public String title()
    {
        return "Grants";
    }

    @Override
    public Node root()
    {
        return panel;
    }
}
