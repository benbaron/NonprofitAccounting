package org.nonprofitbookkeeping.ui;

import nonprofitbookkeeping.service.DonorService;
import nonprofitbookkeeping.service.GrantsService;

import java.io.File;

/**
 * Centralized factory for B-shell fundraising panel adapters.
 */
final class FundraisingPanelFactory
{
    private static final DonorService DONOR_SERVICE = new DonorService();
    private static final GrantsService GRANTS_SERVICE = new GrantsService();

    private FundraisingPanelFactory()
    {
    }

    static DonorsPanel createDonorsPanel()
    {
        File companyDirectory = null;
        return new DonorsPanel(DONOR_SERVICE, companyDirectory);
    }

    static GrantsPanel createGrantsPanel()
    {
        return new GrantsPanel(GRANTS_SERVICE);
    }
}
