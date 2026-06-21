package nonprofitbookkeeping.model;

import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.io.TempDir;
import nonprofitbookkeeping.core.Database;
import nonprofitbookkeeping.core.FlywayMigrationRunner;

class CompanyPersistenceTest {

    @TempDir
    Path tempDir;

    @Test
    void accountsPersistAndReloadWithParentLinks() throws Exception {
        Path dbPath = this.tempDir.resolve("company-db");
        Database.init(dbPath);
        FlywayMigrationRunner.migrateCurrentDatabaseIfEnabled();
        Database.get().ensureSchema();

        Company company = new Company();
        ChartOfAccounts chart = company.getChartOfAccounts();
        Account parent = new Account("100", "Parent", AccountSide.DEBIT);
        Account child = new Account("101", "Child", AccountSide.DEBIT);
        chart.addAccount(parent);
        chart.addSubAccount(parent, child);

        CurrentCompany.forceCompanyLoad(company);
        CurrentCompany.persist();
        Long companyId = CurrentCompany.getCurrentCompanyId();
        assertNotNull(companyId, "Persisting the company should assign an id");

        // Reset loaded company
        CurrentCompany.forceCompanyLoad(null);

        CurrentCompany.loadFromPersistent(companyId);
        Company loaded = CurrentCompany.getCompany();

        Account loadedChild = loaded.getChartOfAccounts().getAccount("101");
        Account loadedParent = loaded.getChartOfAccounts().getAccount("100");

        assertNotNull(loadedChild);
        assertNotNull(loadedParent);
        assertEquals(loadedParent.getAccountNumber(), loadedChild.getParentAccountId());
    }
}
