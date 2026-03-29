package org.nonprofitbookkeeping.ui;

import nonprofitbookkeeping.model.Company;
import nonprofitbookkeeping.model.CurrentCompany;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.lang.reflect.Field;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class FundraisingPanelFactoryTest
{
    @Test
    void donorsPanelUsesParentDirectoryWhenCompanyFileHasParent() throws Exception
    {
        Company company = new Company();
        company.setCompanyFile(Path.of("data", "alpha", "company.json").toFile());
        CurrentCompany.forceCompanyLoad(company);

        DonorsPanel donorsPanel = FundraisingPanelFactory.createDonorsPanel();
        File resolved = extractCompanyDirectory(donorsPanel);
        assertEquals(Path.of("data", "alpha").toFile(), resolved);
    }

    @Test
    void donorsPanelUsesNullDirectoryWhenNoCompanyLoaded() throws Exception
    {
        CurrentCompany.forceCompanyLoad(null);

        DonorsPanel donorsPanel = FundraisingPanelFactory.createDonorsPanel();
        assertNull(extractCompanyDirectory(donorsPanel));
    }

    private static File extractCompanyDirectory(DonorsPanel donorsPanel) throws Exception
    {
        Field panelField = DonorsPanel.class.getDeclaredField("panel");
        panelField.setAccessible(true);
        Object panelFx = panelField.get(donorsPanel);
        Field companyDirectoryField = panelFx.getClass().getDeclaredField("companyDirectory");
        companyDirectoryField.setAccessible(true);
        return (File) companyDirectoryField.get(panelFx);
    }
}
