package nonprofitbookkeeping.model;

import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

class CompanyPersistenceTest {

    @Test
    void accountsPersistAndReloadWithParentLinks() throws Exception {
        Company company = new Company();
        ChartOfAccounts chart = company.getChartOfAccounts();
        Account parent = new Account("100", "Parent", AccountSide.DEBIT);
        Account child = new Account("101", "Child", AccountSide.DEBIT);
        chart.addAccount(parent);
        chart.addSubAccount(parent, child);

        File tempFile = File.createTempFile("company", ".npbk");
        tempFile.deleteOnExit();

        CurrentCompany.forceCompanyLoad(company);
        CurrentCompany.setCurrentFile(tempFile);
        CurrentCompany.persist();

        // Reset loaded company
        CurrentCompany.forceCompanyLoad(null);

        CurrentCompany.loadFromPersistent(tempFile);
        Company loaded = CurrentCompany.getCompany();

        Account loadedChild = loaded.getChartOfAccounts().getAccount("101");
        Account loadedParent = loaded.getChartOfAccounts().getAccount("100");

        assertNotNull(loadedChild);
        assertNotNull(loadedParent);
        assertEquals(loadedParent.getAccountNumber(), loadedChild.getParentAccountId());
    }
}
