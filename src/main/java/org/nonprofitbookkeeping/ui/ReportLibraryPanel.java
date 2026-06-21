package org.nonprofitbookkeeping.ui;

import javafx.scene.Node;
import nonprofitbookkeeping.ui.panels.ReportsPanelFX;

/**
 * Intentional adapter boundary from canonical org.nonprofitbookkeeping.ui shell to legacy JavaFX reports workspace implementation.
 */
public class ReportLibraryPanel implements AppPanel
{
    private final ReportsPanelFX content = new ReportsPanelFX();

    @Override
    public String title()
    {
        return "Reports";
    }

    @Override
    public Node root()
    {
        return this.content;
    }
}
