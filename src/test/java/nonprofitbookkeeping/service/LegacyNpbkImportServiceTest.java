package nonprofitbookkeeping.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import nonprofitbookkeeping.core.Database;
import nonprofitbookkeeping.core.FlywayMigrationRunner;
import nonprofitbookkeeping.model.Account;
import nonprofitbookkeeping.model.AccountSide;
import nonprofitbookkeeping.model.AccountingEntry;
import nonprofitbookkeeping.model.AccountingTransaction;
import nonprofitbookkeeping.model.Company;
import nonprofitbookkeeping.model.CurrentCompany;
import nonprofitbookkeeping.persistence.CompanyRepository;
import nonprofitbookkeeping.persistence.AccountRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.junit.jupiter.api.Assertions.*;

class LegacyNpbkImportServiceTest
{
        private static final ObjectMapper MAPPER = new ObjectMapper().registerModule(new JavaTimeModule());

        @TempDir Path tempDir;

        @BeforeEach
        void setUp() throws SQLException
        {
                Path dbBase = this.tempDir.resolve("testdb");
                Database.init(dbBase);
                FlywayMigrationRunner.migrateCurrentDatabaseIfEnabled();
                Database.get().ensureSchema();
        }

        @Test
        void importArchive_readsZipArchive() throws Exception
        {
                Company company = createCompany("Zip Co");
                Path archive = createZipArchive(company, this.tempDir.resolve("legacy.npbk"));

                LegacyNpbkImportService service = new LegacyNpbkImportService();
                long id = service.importArchive(archive);

                assertTrue(id > 0, "Expected a generated company id");
                Company stored = new CompanyRepository().load(id);
                assertEquals("Zip Co", stored.getCompanyProfileModel().getCompanyName());
        }

        @Test
        void importArchive_supportsNestedZipEntry() throws Exception
        {
                Company company = createCompany("Nested Co");
                Path archive = createZipArchiveWithNestedEntry(company, this.tempDir.resolve("nested.npbk"));

                LegacyNpbkImportService service = new LegacyNpbkImportService();
                long id = service.importArchive(archive);

                assertTrue(id > 0, "Expected a generated company id");
                Company stored = new CompanyRepository().load(id);
                assertEquals("Nested Co", stored.getCompanyProfileModel().getCompanyName());
        }

        @Test
        void importArchive_supportsPlainJson() throws Exception
        {
                // Reinitialize database to ensure a clean state
                Path secondDb = this.tempDir.resolve("testdb2");
                Database.init(secondDb);
                FlywayMigrationRunner.migrateCurrentDatabaseIfEnabled();
                Database.get().ensureSchema();

                Company company = createCompany("JSON Co");
                Path jsonFile = this.tempDir.resolve("legacy.json");
                MAPPER.writeValue(jsonFile.toFile(), company);

                LegacyNpbkImportService service = new LegacyNpbkImportService();
                long id = service.importArchive(jsonFile);

                assertTrue(id > 0, "Expected a generated company id");
                Company stored = new CompanyRepository().load(id);
                assertEquals("JSON Co", stored.getCompanyProfileModel().getCompanyName());
        }

        @Test
        void importArchive_createsMissingAccountsForJournalEntries() throws Exception
        {
                Company company = createCompany("Ledger Co");

                AccountingTransaction transaction = new AccountingTransaction();
                transaction.setId(1);
                transaction.setBookingDateTimestamp(1L);
                Set<AccountingEntry> entries = new LinkedHashSet<>();
                entries.add(new AccountingEntry(new BigDecimal("100.00"),
                        "1000",
                        AccountSide.DEBIT,
                        "Cash"));
                entries.add(new AccountingEntry(new BigDecimal("100.00"),
                        "2000",
                        AccountSide.CREDIT,
                        "Income"));
                transaction.setEntries(entries);
                company.getLedger().getJournal().replaceAllTransactions(List.of(transaction));

                CurrentCompany.forceCompanyLoad(company);

                Path archive = createZipArchive(company,
                        this.tempDir.resolve("ledger_missing_accounts.npbk"));

                LegacyNpbkImportService service = new LegacyNpbkImportService();
                long id = service.importArchive(archive);

                assertTrue(id > 0, "Expected a generated company id");

                List<Account> storedAccounts = new AccountRepository().listAll();
                assertEquals(2, storedAccounts.size(),
                        "Expected placeholder accounts to be created for ledger entries");
                assertTrue(storedAccounts.stream()
                        .anyMatch(a -> "1000".equals(a.getAccountNumber())));
                assertTrue(storedAccounts.stream()
                        .anyMatch(a -> "2000".equals(a.getAccountNumber())));

                CurrentCompany.forceCompanyLoad(null);
        }

        private static Company createCompany(String name)
        {
                Company company = new Company();
                company.getCompanyProfileModel().setCompanyName(name);
                return company;
        }

        private static Path createZipArchive(Company company, Path destination) throws IOException
        {
                return createZipArchive(company, destination, "");
        }

        private static Path createZipArchive(Company company, Path destination, String prefix) throws IOException
        {
                try (OutputStream out = Files.newOutputStream(destination);
                        ZipOutputStream zip = new ZipOutputStream(out))
                {
                        zip.putNextEntry(new ZipEntry(prefix + "company_data.json"));
                        byte[] jsonBytes = MAPPER.writeValueAsBytes(company);
                        zip.write(jsonBytes);
                        zip.closeEntry();
                }
                return destination;
        }

        private static Path createZipArchiveWithNestedEntry(Company company, Path destination) throws IOException
        {
                try (OutputStream out = Files.newOutputStream(destination);
                        ZipOutputStream zip = new ZipOutputStream(out))
                {
                        zip.putNextEntry(new ZipEntry("legacy/company_data.json"));
                        byte[] jsonBytes = MAPPER.writeValueAsBytes(company);
                        zip.write(jsonBytes);
                        zip.closeEntry();
                }
                return destination;
        }
}
