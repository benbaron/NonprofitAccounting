package org.nonprofitbookkeeping.ui;

import nonprofitbookkeeping.core.Database;
import nonprofitbookkeeping.model.CurrentCompany;
import nonprofitbookkeeping.persistence.CompanyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class AlternateCompanyAdminServiceTest
{
    @TempDir Path tempDir;
    private AlternateCompanyAdminService service;
    private UiSessionContext session;

    @BeforeEach
    void setUp()
    {
        Path db = this.tempDir.resolve("company-admin");
        Database.init(db);
        try { Database.get().ensureSchema(); } catch (Exception ex) { throw new RuntimeException(ex); }
        this.session = new UiSessionContext();
        this.session.openDatabase(db);
        AlternateDataContextService context = new AlternateDataContextService(new CompanyRepository(),
            new AlternateRecentsStore(new InMemoryPreferencesStore()), new AlternateDatabaseContextSwitcher(), this.session);
        context.setActiveDatabaseBasePath(db);
        this.service = new AlternateCompanyAdminService(new CompanyRepository(), context, this.session);
        CurrentCompany.forceCompanyLoad(null);
    }

    @Test
    void createRejectsInvalidRequiredFields()
    {
        assertThrows(IllegalArgumentException.class, () -> this.service.createCompany(
            new AlternateCompanyAdminService.CreateCompanyRequest("", "Non-Profit", "01-01", "USD", "2026-01-01", true)));
        assertThrows(IllegalArgumentException.class, () -> this.service.createCompany(
            new AlternateCompanyAdminService.CreateCompanyRequest("Org", "Non-Profit", "1-1", "USD", "2026-01-01", true)));
    }

    @Test
    void duplicateCompanyNamesAreRejected() throws Exception
    {
        create("Duplicate Org");
        assertThrows(IllegalArgumentException.class, () -> create(" duplicate org "));
    }

    @Test
    void createOpenCloseWorkflowUpdatesSession() throws Exception
    {
        long id = create("Workflow Org");
        assertEquals(id, this.session.activeCompanyId());
        assertTrue(this.session.isCompanyOpen());
        this.service.closeActiveCompany();
        assertFalse(this.session.isCompanyOpen());
        this.service.openCompany(id);
        assertEquals("Workflow Org", this.session.activeCompanyDisplayLabel());
    }

    @Test
    void deleteRequiresBackupExactNameAndInactiveCompany() throws Exception
    {
        long id = create("Delete Org");
        assertThrows(IllegalArgumentException.class, () -> this.service.deleteCompany(id, "Delete Org", false));
        assertThrows(IllegalArgumentException.class, () -> this.service.deleteCompany(id, "Wrong", true));
        assertThrows(IllegalStateException.class, () -> this.service.deleteCompany(id, "Delete Org", true));
        this.service.closeActiveCompany();
        this.service.deleteCompany(id, "Delete Org", true);
        assertTrue(this.service.listCompanies().isEmpty());
    }

    @Test
    void populateIsIdempotentAndDetectsAlreadyPopulatedCompany() throws Exception
    {
        long id = create("Populate Org");
        this.service.closeActiveCompany();
        AlternateCompanyAdminService.PopulateResult first = this.service.populateCompany(id);
        AlternateCompanyAdminService.PopulateResult second = this.service.populateCompany(id);
        assertTrue(first.populated());
        assertFalse(second.populated());
        assertTrue(second.message().contains("already"));
    }

    @Test
    void sampleCompanyIsExplicitAndDeterministic() throws Exception
    {
        long id = this.service.createSampleCompany();
        assertEquals(id, this.session.activeCompanyId());
        assertTrue(this.session.isSampleCompany());
        assertTrue(this.session.isPopulatedCompany());
        assertEquals("Sample Nonprofit Company", this.session.activeCompanyDisplayLabel());
    }

    private long create(String name) throws Exception
    {
        return this.service.createCompany(new AlternateCompanyAdminService.CreateCompanyRequest(name, "Non-Profit", "01-01", "USD", "2026-01-01", true));
    }

    private static class InMemoryPreferencesStore implements AlternateDataContextService.PreferencesStore
    {
        private final java.util.Map<String, String> values = new java.util.HashMap<>();
        public String get(String key, String defaultValue) { return this.values.getOrDefault(key, defaultValue); }
        public void put(String key, String value) { this.values.put(key, value); }
    }
}
