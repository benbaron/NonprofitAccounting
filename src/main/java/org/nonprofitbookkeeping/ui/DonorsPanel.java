package org.nonprofitbookkeeping.ui;

import javafx.scene.Node;
import nonprofitbookkeeping.service.DonorService;
import nonprofitbookkeeping.ui.panels.DonorsPanelFX;

/**
 * B-shell wrapper panel for legacy donors management UI.
 */
public class DonorsPanel implements AppPanel
{
    private final DonorsPanelFX panel = new DonorsPanelFX(new DonorService(), null);

    @Override
    public String title()
    {
        return "Donors";
    }

    @Override
    public Node root()
    {
        return panel;
    }
}
