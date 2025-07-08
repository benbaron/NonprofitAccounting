package nonprofitbookkeeping.core;

import nonprofitbookkeeping.model.Company; // Assuming Company is a concrete class that can be instantiated
import nonprofitbookkeeping.model.CompanyProfileModel; // Company has a CompanyProfileModel
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir; // For creating temporary files/directories

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipEntry;

import static org.junit.jupiter.api.Assertions.*;

public class JacksonDataStorerZipTest {

    @TempDir
    Path tempDir; // JUnit 5 temporary directory

    @Test
    void testSaveAndLoadZipFormat() throws IOException {
        System.out.println("START: testSaveAndLoadZipFormat");
        JacksonDataStorer dataStorer = new JacksonDataStorer();

        // 1. Create a sample Company object
        Company originalCompany = new Company();
        CompanyProfileModel profile = new CompanyProfileModel();
        profile.setCompanyName("Test Zip Co");
        profile.setFiscalYearStart("01/01");
        profile.setBaseCurrency("USD"); // Corrected method name
        originalCompany.setCompanyProfileModel(profile); // Corrected method name
        // Add more simple, serializable data to the company if needed for a more robust test

        File testFile = this.tempDir.resolve("test_company.npbk").toFile();

        // 2. Use JacksonDataStorer.saveData() to save it
        try {
            System.out.println("CALLING: dataStorer.saveData()");
            dataStorer.saveData(originalCompany, testFile);
            System.out.println("RETURNED: dataStorer.saveData()");
        } catch (Exception e) {
            fail("Saving data failed: " + e.getMessage());
        }

        // 3. Verify that the created file is a valid zip file and contains "company_data.json"
        assertTrue(testFile.exists(), "Test file should exist after saving.");
        boolean foundJsonEntry = false;
        boolean foundCoaEntry = false;
        try (FileInputStream fis = new FileInputStream(testFile);
             ZipInputStream zis = new ZipInputStream(fis)) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                if ("company_data.json".equals(entry.getName())) {
                    foundJsonEntry = true;
                } else if ("chart_of_accounts.json".equals(entry.getName())) {
                    foundCoaEntry = true;
                }
            }
        } catch (IOException e) {
            fail("Error reading zip file: " + e.getMessage());
        }
        assertTrue(foundJsonEntry, "Zip file should contain 'company_data.json' entry.");
        assertTrue(foundCoaEntry, "Zip file should contain 'chart_of_accounts.json' entry.");

        // 4. Use JacksonDataStorer.loadData() to load the Company object back
        Company loadedCompany = null;
        try {
            System.out.println("CALLING: dataStorer.loadData()");
            loadedCompany = dataStorer.loadData(Company.class, testFile);
            System.out.println("RETURNED: dataStorer.loadData()");
        } catch (Exception e) {
            fail("Loading data failed: " + e.getMessage());
        }

        // 5. Assert that the loaded company object is equal to the original
        assertNotNull(loadedCompany, "Loaded company should not be null.");
        assertNotNull(loadedCompany.getCompanyProfileModel(), "Loaded company profile should not be null."); // Corrected method name
        assertNotNull(loadedCompany.getChartOfAccounts(), "Chart should load from separate entry.");
        assertEquals(originalCompany.getCompanyProfileModel().getCompanyName(), loadedCompany.getCompanyProfileModel().getCompanyName(), "Company names should match."); // Corrected method name
        assertEquals(originalCompany.getCompanyProfileModel().getFiscalYearStart(), loadedCompany.getCompanyProfileModel().getFiscalYearStart(), "Fiscal year starts should match."); // Corrected method name
        assertEquals(originalCompany.getCompanyProfileModel().getBaseCurrency(), loadedCompany.getCompanyProfileModel().getBaseCurrency(), "Currencies should match."); // Corrected method name
        // Add more assertions if other fields were set in originalCompany
        System.out.println("END: testSaveAndLoadZipFormat");
    }
}
