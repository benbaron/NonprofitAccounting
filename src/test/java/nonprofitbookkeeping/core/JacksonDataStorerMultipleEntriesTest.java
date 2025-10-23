package nonprofitbookkeeping.core;

import nonprofitbookkeeping.exception.ActionCancelledException;
import nonprofitbookkeeping.exception.NoFileCreatedException;
import nonprofitbookkeeping.model.Account;
import nonprofitbookkeeping.model.AccountSide;
import nonprofitbookkeeping.model.AccountingEntry;
import nonprofitbookkeeping.model.AccountingTransaction;
import nonprofitbookkeeping.model.ChartOfAccounts;
import nonprofitbookkeeping.model.Company;
import nonprofitbookkeeping.model.CompanyProfileModel;
import nonprofitbookkeeping.model.Ledger;
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
import java.util.LinkedHashSet;
import java.util.Set;
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

        CompanyProfileModel profile = new CompanyProfileModel();
        profile.setCompanyName("MultiEntry Co");

        ChartOfAccounts coa = new ChartOfAccounts();
        coa.addAccount(new Account("100", "Cash", AccountSide.DEBIT));

        Ledger ledger = new Ledger();
        AccountingTransaction txn = new AccountingTransaction();
        txn.setBookingDateTimestamp(1704067200000L);
        txn.setMemo("Seed entry");
        Set<AccountingEntry> entries = new LinkedHashSet<>();
        entries.add(new AccountingEntry(java.math.BigDecimal.TEN,
                "100", AccountSide.DEBIT, "Cash"));
        entries.add(new AccountingEntry(java.math.BigDecimal.TEN,
                "200", AccountSide.CREDIT, "Revenue"));
        txn.setEntries(entries);
        ledger.getJournal().addTransaction(txn);

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
        Company loaded = null;

                try
                {
                        loaded = dataStorer.loadData(Company.class, zipFile);
                }
                catch (IOException | ActionCancelledException | NoFileCreatedException e)
                {
                        fail("Exception loading modular archive: " + e.getMessage());
                }

        assertNotNull(loaded, "Loaded company should not be null");
        assertEquals("MultiEntry Co", loaded.getCompanyProfileModel().getCompanyName());
        assertEquals(1, loaded.getLedger().getJournal().getJournalTransactions().size());
        assertEquals("Seed entry",
                loaded.getLedger().getJournal().getJournalTransactions().get(0).getMemo());
    }
}
