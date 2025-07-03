package nonprofitbookkeeping.service;

import nonprofitbookkeeping.model.Company;
import nonprofitbookkeeping.model.CompanyProfileModel;
import nonprofitbookkeeping.model.Grant;
import nonprofitbookkeeping.core.JacksonDataStorer;
import nonprofitbookkeeping.exception.ActionCancelledException;
import nonprofitbookkeeping.exception.NoFileCreatedException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static org.junit.jupiter.api.Assertions.*;

public class GrantsServiceZipTest {
    @TempDir
    Path tempDir;

    @Test
    void saveAndLoadInZip() throws IOException {
        File npbk = this.tempDir.resolve("test.npbk").toFile();

        // create minimal company file so zip exists
        Company c = new Company();
        CompanyProfileModel p = new CompanyProfileModel();
        p.setCompanyName("ZipCo");
        c.setCompanyProfileModel(p);
        try
		{
			new JacksonDataStorer().saveData(c, npbk);
		}
		catch (IOException | ActionCancelledException | NoFileCreatedException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

        GrantsService service = new GrantsService();
        service.addGrant(new Grant("G1", "Grantor", BigDecimal.ONE, "2024", "Purpose", "Open"));
        service.saveGrantsToZip(npbk);

        // verify entry exists
        boolean found = false;
        try (ZipInputStream zis = new ZipInputStream(new java.io.FileInputStream(npbk))) {
            ZipEntry e;
            while ((e = zis.getNextEntry()) != null) {
                if ("grants.json".equals(e.getName())) { found = true; break; }
            }
        }
        assertTrue(found, "Zip should contain grants.json entry");

        GrantsService loadService = new GrantsService();
        loadService.loadGrantsFromZip(npbk);
        List<Grant> grants = loadService.getAllGrants();
        assertEquals(1, grants.size());
        assertEquals("G1", grants.get(0).getGrantId());
    }
}
