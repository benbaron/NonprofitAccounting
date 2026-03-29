package org.nonprofitbookkeeping.ui;

import nonprofitbookkeeping.service.DonorService;
import nonprofitbookkeeping.service.GrantsService;
import nonprofitbookkeeping.model.Company;
import nonprofitbookkeeping.model.CurrentCompany;

import java.io.File;

/**
 * Centralized factory for B-shell fundraising panel adapters.
 */
final class FundraisingPanelFactory
{
    private FundraisingPanelFactory()
    {
    }

    static DonorsPanel createDonorsPanel()
    {
        return new DonorsPanel(new DonorService(), resolveActiveCompanyDirectory());
    }

    static GrantsPanel createGrantsPanel()
    {
        return new GrantsPanel(new GrantsService());
    }

    private static File resolveActiveCompanyDirectory()
    {
        Company company = CurrentCompany.getCompany();
        if (company == null)
        {
            return null;
        }
        File parent = company.getParentFile();
        if (parent != null)
        {
            return parent;
        }
        return company.getCompanyFile();
    }

    static File activeCompanyDirectoryForTests()
    {
        return resolveActiveCompanyDirectory();
    }
}
