package org.nonprofitbookkeeping.ui;

import nonprofitbookkeeping.model.Company;
import nonprofitbookkeeping.model.CurrentCompany;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class FundraisingPanelFactoryTest
{
    @Test
    void usesParentDirectoryWhenCompanyFileHasParent()
    {
        Company company = new Company();
        company.setCompanyFile(Path.of("data", "alpha", "company.json").toFile());
        CurrentCompany.forceCompanyLoad(company);

        File resolved = FundraisingPanelFactory.activeCompanyDirectoryForTests();
        assertEquals(Path.of("data", "alpha").toFile(), resolved);
    }

    @Test
    void returnsNullWhenNoCompanyLoaded()
    {
        CurrentCompany.forceCompanyLoad(null);

        assertNull(FundraisingPanelFactory.activeCompanyDirectoryForTests());
    }
}
