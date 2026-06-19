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
        Path db = tempDir.resolve("company-admin");
        Database.init(db);
        try { Database.get().ensureSchema(); } catch (Exception ex) { throw new RuntimeException(ex); }
        session = new UiSessionContext();
        session.openDatabase(db);
        AlternateDataContextService context = new AlternateDataContextService(new CompanyRepository(),
            new AlternateRecentsStore(new InMemoryPreferencesStore()), new AlternateDatabaseContextSwitcher(), session);
        context.setActiveDatabaseBasePath(db);
        service = new AlternateCompanyAdminService(new CompanyRepository(), context, session);
        CurrentCompany.forceCompanyLoad(null);
    }

    @Test
    void createRejectsInvalidRequiredFields()
    {
        assertThrows(IllegalArgumentException.class, () -> service.createCompany(
            new AlternateCompanyAdminService.CreateCompanyRequest("", "Non-Profit", "01-01", "USD", "2026-01-01", true)));
        assertThrows(IllegalArgumentException.class, () -> service.createCompany(
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
        assertEquals(id, session.activeCompanyId());
        assertTrue(session.isCompanyOpen());
        service.closeActiveCompany();
        assertFalse(session.isCompanyOpen());
        service.openCompany(id);
        assertEquals("Workflow Org", session.activeCompanyDisplayLabel());
    }

    @Test
    void deleteRequiresBackupExactNameAndInactiveCompany() throws Exception
    {
        long id = create("Delete Org");
        assertThrows(IllegalArgumentException.class, () -> service.deleteCompany(id, "Delete Org", false));
        assertThrows(IllegalArgumentException.class, () -> service.deleteCompany(id, "Wrong", true));
        assertThrows(IllegalStateException.class, () -> service.deleteCompany(id, "Delete Org", true));
        service.closeActiveCompany();
        service.deleteCompany(id, "Delete Org", true);
        assertTrue(service.listCompanies().isEmpty());
    }

    @Test
    void populateIsIdempotentAndDetectsAlreadyPopulatedCompany() throws Exception
    {
        long id = create("Populate Org");
        service.closeActiveCompany();
        AlternateCompanyAdminService.PopulateResult first = service.populateCompany(id);
        AlternateCompanyAdminService.PopulateResult second = service.populateCompany(id);
        assertTrue(first.populated());
        assertFalse(second.populated());
        assertTrue(second.message().contains("already"));
    }

    @Test
    void sampleCompanyIsExplicitAndDeterministic() throws Exception
    {
        long id = service.createSampleCompany();
        assertEquals(id, session.activeCompanyId());
        assertTrue(session.isSampleCompany());
        assertTrue(session.isPopulatedCompany());
        assertEquals("Sample Nonprofit Company", session.activeCompanyDisplayLabel());
    }

    private long create(String name) throws Exception
    {
        return service.createCompany(new AlternateCompanyAdminService.CreateCompanyRequest(name, "Non-Profit", "01-01", "USD", "2026-01-01", true));
    }

    private static class InMemoryPreferencesStore implements AlternateDataContextService.PreferencesStore
    {
        private final java.util.Map<String, String> values = new java.util.HashMap<>();
        public String get(String key, String defaultValue) { return values.getOrDefault(key, defaultValue); }
        public void put(String key, String value) { values.put(key, value); }
    }
}
