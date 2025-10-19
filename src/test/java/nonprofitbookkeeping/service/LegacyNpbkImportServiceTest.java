package nonprofitbookkeeping.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import nonprofitbookkeeping.core.Database;
import nonprofitbookkeeping.model.Account;
import nonprofitbookkeeping.model.Company;
import nonprofitbookkeeping.persistence.CompanyRepository;
import nonprofitbookkeeping.persistence.AccountRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.List;
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
        void importArchive_createsPlaceholderAccountsForJournalEntries() throws Exception
        {
                Path thirdDb = this.tempDir.resolve("testdb_missing_accounts");
                Database.init(thirdDb);
                Database.get().ensureSchema();

                Path jsonFile = this.tempDir.resolve("missing_accounts.json");
                String json = """
                        {
                          "companyProfileModel": {"companyName": "Placeholder Co"},
                          "ledger": {
                            "journal": {
                              "journalTransactions": [
                                {
                                  "id": 1,
                                  "bookingDateTimestamp": 1,
                                  "entries": [
                                    {"amount": 100.00, "accountNumber": "1000", "accountSide": "DEBIT", "accountName": "Cash"},
                                    {"amount": 100.00, "accountNumber": "2000", "accountSide": "CREDIT", "accountName": "Revenue"}
                                  ]
                                }
                              ]
                            }
                          },
                          "chartOfAccounts": {"chartOfAccounts": []}
                        }
                        """;
                Files.writeString(jsonFile, json);

                LegacyNpbkImportService service = new LegacyNpbkImportService();
                long id = service.importArchive(jsonFile);

                assertTrue(id > 0, "Expected a generated company id");

                AccountRepository accountRepository = new AccountRepository();
                List<Account> storedAccounts = accountRepository.listAll();

                assertTrue(storedAccounts.stream().anyMatch(a -> "1000".equals(a.getAccountNumber())));
                assertTrue(storedAccounts.stream().anyMatch(a -> "2000".equals(a.getAccountNumber())));

                Account cash = storedAccounts.stream()
                        .filter(a -> "1000".equals(a.getAccountNumber()))
                        .findFirst()
                        .orElseThrow();
                assertEquals("Cash", cash.getName());
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
