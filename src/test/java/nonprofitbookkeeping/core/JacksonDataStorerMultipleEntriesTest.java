package nonprofitbookkeeping.core;

import nonprofitbookkeeping.exception.ActionCancelledException;
import nonprofitbookkeeping.exception.NoFileCreatedException;
import nonprofitbookkeeping.model.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.junit.jupiter.api.Assertions.*;

public class JacksonDataStorerMultipleEntriesTest {
    @TempDir
    Path tempDir;

    @Test
    void testLoadZipWithMultipleEntries() throws IOException {
        File zipFile = this.tempDir.resolve("multi_entry.npbk").toFile();
        ObjectMapper mapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .enable(SerializationFeature.INDENT_OUTPUT);

        Company company = new Company();
        CompanyProfileModel profile = new CompanyProfileModel();
        profile.setCompanyName("MultiEntry Co");
        company.setCompanyProfileModel(profile);

        ChartOfAccounts coa = new ChartOfAccounts();
        Account account = new Account("100", "Cash", AccountSide.DEBIT);
        coa.addAccount(account);

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             FileOutputStream fos = new FileOutputStream(zipFile);
             ZipOutputStream zos = new ZipOutputStream(fos)) {

            mapper.writeValue(baos, coa);
            ZipEntry entry1 = new ZipEntry("chart_of_accounts.json");
            zos.putNextEntry(entry1);
            zos.write(baos.toByteArray());
            zos.closeEntry();

            baos.reset();
            mapper.writeValue(baos, company);
            ZipEntry entry2 = new ZipEntry("company_data.json");
            zos.putNextEntry(entry2);
            zos.write(baos.toByteArray());
            zos.closeEntry();
        }

        JacksonDataStorer dataStorer = new JacksonDataStorer();
        Company loaded = null;
		
		try
		{
			loaded = dataStorer.loadData(Company.class, zipFile);
		}
		catch (IOException | ActionCancelledException | NoFileCreatedException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

        assertNotNull(loaded, "Loaded company should not be null");
        assertEquals("MultiEntry Co", loaded.getCompanyProfileModel().getCompanyName());
    }
}
