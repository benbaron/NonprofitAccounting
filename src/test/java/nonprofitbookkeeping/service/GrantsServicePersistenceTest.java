package nonprofitbookkeeping.service;

import nonprofitbookkeeping.TestDatabase;
import nonprofitbookkeeping.model.Grant;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GrantsServicePersistenceTest {

    @TempDir
    Path tempDir;

    @Test
    void saveAndLoadThroughDatabase() throws Exception {
        TestDatabase.reset(this.tempDir);

        GrantsService service = new GrantsService();
        Grant grant = new Grant("G1", "Grantor", BigDecimal.ONE, "2024", "Purpose", "Open");
        grant.setRestrictionClass("UNRESTRICTED");
        grant.setComplianceStatus("LATE_REPORT");
        grant.setNextReportDue("2026-12-31");
        service.addGrant(grant);

        service.saveGrants();

        GrantsService loadService = new GrantsService();
        loadService.loadGrants();

        List<Grant> grants = loadService.getAllGrants();
        assertEquals(1, grants.size());
        assertEquals("G1", grants.get(0).getGrantId());
        assertEquals("UNRESTRICTED", grants.get(0).getRestrictionClass());
        assertEquals("LATE_REPORT", grants.get(0).getComplianceStatus());
        assertEquals("2026-12-31", grants.get(0).getNextReportDue());
    }

    @Test
    void legacyZipMethodsDelegateToDatabase() throws Exception {
        TestDatabase.reset(this.tempDir);

        GrantsService service = new GrantsService();
        service.addGrant(new Grant("G2", "Legacy", BigDecimal.TEN, "2024", "Purpose", "Open"));

        service.saveGrantsToZip(null);

        GrantsService loadService = new GrantsService();
        loadService.loadGrantsFromZip(null);

        List<Grant> grants = loadService.getAllGrants();
        assertEquals(1, grants.size());
        assertEquals("G2", grants.get(0).getGrantId());
    }
}
