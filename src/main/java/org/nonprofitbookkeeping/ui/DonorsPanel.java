package org.nonprofitbookkeeping.ui;

import javafx.scene.Node;
import java.io.File;
import java.util.Objects;
import nonprofitbookkeeping.service.DonorService;
import nonprofitbookkeeping.ui.panels.DonorsPanelFX;

/**
 * B-shell wrapper panel for legacy donors management UI.
 */
public class DonorsPanel implements AppPanel
{
    private final DonorsPanelFX panel;

    public DonorsPanel()
    {
        this(new DonorService(), null);
    }

    public DonorsPanel(DonorService donorService, File companyDirectory)
    {
        this(new DonorsPanelFX(Objects.requireNonNull(donorService, "donorService"), companyDirectory));
    }

    DonorsPanel(DonorsPanelFX panel)
    {
        this.panel = Objects.requireNonNull(panel, "panel");
    }

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
