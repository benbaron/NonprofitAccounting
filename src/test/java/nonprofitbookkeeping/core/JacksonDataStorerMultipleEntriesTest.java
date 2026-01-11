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
import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.junit.jupiter.api.Assertions.*;

@SuppressWarnings("removal")
public class JacksonDataStorerMultipleEntriesTest {
    @TempDir
    Path tempDir;

    @Test
    void testLoadZipWithMultipleEntries() throws IOException, ActionCancelledException, NoFileCreatedException {
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
        company.setChartOfAccounts(coa);

        AccountingTransaction transaction = new AccountingTransaction();
        transaction.setBookingDateTimestamp(System.currentTimeMillis());
        Set<AccountingEntry> entries = new HashSet<>();
        entries.add(new AccountingEntry(new BigDecimal("25.00"), "100", AccountSide.DEBIT));
        entries.add(new AccountingEntry(new BigDecimal("25.00"), "200", AccountSide.CREDIT));
        transaction.setEntries(entries);
        company.getLedger().getJournal().addTransaction(transaction);
        CurrentCompany.forceCompanyLoad(company);

        try
        {
                try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        FileOutputStream fos = new FileOutputStream(zipFile);
                        ZipOutputStream zos = new ZipOutputStream(fos))
                {

                        mapper.writeValue(baos, coa);
                        ZipEntry entry1 = new ZipEntry("chart_of_accounts.json");
                        zos.putNextEntry(entry1);
                        zos.write(baos.toByteArray());
                        zos.closeEntry();

                        baos.reset();
                        mapper.writeValue(baos, profile);
                        ZipEntry entry2 = new ZipEntry("company_profile.json");
                        zos.putNextEntry(entry2);
                        zos.write(baos.toByteArray());
                        zos.closeEntry();

                        baos.reset();
                        mapper.writeValue(baos, company.getLedger());
                        ZipEntry entry3 = new ZipEntry("ledger.json");
                        zos.putNextEntry(entry3);
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
                        fail("Loading modular archive should not throw: " + e.getMessage());
                }

                assertNotNull(loaded, "Loaded company should not be null");
                assertEquals("MultiEntry Co", loaded.getCompanyProfileModel().getCompanyName());
                assertEquals(1, loaded.getChartOfAccounts().getRootAccounts().size(),
                        "Chart of accounts should be populated from modular entry");
                assertEquals(1, loaded.getLedger().getJournal().getJournalTransactions().size(),
                        "Ledger transactions should be restored from modular entry");
        }
        finally
        {
                CurrentCompany.forceCompanyLoad(null);
        }
    }

    @Test
    void testLoadZipWithSeparatedEntriesOnly() throws IOException, ActionCancelledException, NoFileCreatedException {
        File zipFile = this.tempDir.resolve("separated_entries.npbk").toFile();
        ObjectMapper mapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .enable(SerializationFeature.INDENT_OUTPUT);

        CompanyProfileModel profile = new CompanyProfileModel();
        profile.setCompanyName("Separated Co");

        Ledger ledger = new Ledger();

        ChartOfAccounts coa = new ChartOfAccounts();
        Account savings = new Account("200", "Savings", AccountSide.DEBIT);
        coa.addAccount(savings);

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             FileOutputStream fos = new FileOutputStream(zipFile);
             ZipOutputStream zos = new ZipOutputStream(fos)) {

            mapper.writeValue(baos, profile);
            ZipEntry profileEntry = new ZipEntry("company_profile.json");
            zos.putNextEntry(profileEntry);
            zos.write(baos.toByteArray());
            zos.closeEntry();

            baos.reset();
            mapper.writeValue(baos, ledger);
            ZipEntry ledgerEntry = new ZipEntry("ledger.json");
            zos.putNextEntry(ledgerEntry);
            zos.write(baos.toByteArray());
            zos.closeEntry();

            baos.reset();
            mapper.writeValue(baos, coa);
            ZipEntry chartEntry = new ZipEntry("chart_of_accounts.json");
            zos.putNextEntry(chartEntry);
            zos.write(baos.toByteArray());
            zos.closeEntry();
        }

        JacksonDataStorer dataStorer = new JacksonDataStorer();
        Company loaded = dataStorer.loadData(Company.class, zipFile);

        assertNotNull(loaded, "Loaded company should not be null");
        assertEquals("Separated Co", loaded.getCompanyProfileModel().getCompanyName());
        assertEquals(1, loaded.getChartOfAccounts().getAccounts().size());
        assertNotNull(loaded.getLedger(), "Ledger should be reconstructed from ledger.json");
    }
}
