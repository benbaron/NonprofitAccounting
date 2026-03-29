package org.nonprofitbookkeeping.ui;

import javafx.scene.Node;
import java.util.Objects;
import nonprofitbookkeeping.service.GrantsService;
import nonprofitbookkeeping.ui.panels.GrantsPanelFX;

/**
 * B-shell wrapper panel for legacy grants management UI.
 */
public class GrantsPanel implements AppPanel
{
    private final GrantsPanelFX panel;

    public GrantsPanel()
    {
        this(new GrantsService());
    }

    public GrantsPanel(GrantsService grantsService)
    {
        this(new GrantsPanelFX(Objects.requireNonNull(grantsService, "grantsService")));
    }

    GrantsPanel(GrantsPanelFX panel)
    {
        this.panel = Objects.requireNonNull(panel, "panel");
    }

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
