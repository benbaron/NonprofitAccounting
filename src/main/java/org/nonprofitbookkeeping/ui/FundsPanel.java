package org.nonprofitbookkeeping.ui;

import javafx.scene.Node;
import nonprofitbookkeeping.service.FundAccountingService;
import nonprofitbookkeeping.ui.panels.FundsPanelFX;

/**
 * Intentional adapter boundary from canonical org.nonprofitbookkeeping.ui shell to legacy JavaFX funds workflow.
 */
public class FundsPanel implements AppPanel
{
    private final FundsPanelFX content = new FundsPanelFX(new FundAccountingService(), null);

    @Override
    public String title()
    {
        return "Funds";
    }

    @Override
    public Node root()
    {
        return content;
    }
}
