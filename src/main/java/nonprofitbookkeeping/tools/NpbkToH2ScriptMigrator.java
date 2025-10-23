package nonprofitbookkeeping.tools;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import nonprofitbookkeeping.core.Database;
import nonprofitbookkeeping.model.Account;
import nonprofitbookkeeping.model.AccountingTransaction;
import nonprofitbookkeeping.model.ChartOfAccounts;
import nonprofitbookkeeping.model.Company;
import nonprofitbookkeeping.model.CompanyProfileModel;
import nonprofitbookkeeping.model.Ledger;
import nonprofitbookkeeping.persistence.AccountRepository;
import nonprofitbookkeeping.persistence.CompanyProfileRepository;
import nonprofitbookkeeping.persistence.CompanyRepository;
import nonprofitbookkeeping.persistence.JournalRepository;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * One-time utility that converts legacy {@code .npbk} archives into an H2
 * database script.  The generated script can later be imported by the
 * application through {@link H2ScriptCompanyImporter}.
 */
public final class NpbkToH2ScriptMigrator
{

        private static final String COMPANY_ENTRY = "company_data.json";
        private static final String CHART_ENTRY = "chart_of_accounts.json";
        private static final String PROFILE_ENTRY = "company_profile.json";
        private static final String LEDGER_ENTRY = "ledger.json";

        private NpbkToH2ScriptMigrator()
        {
        }

        public static void main(String[] args) throws Exception
        {
                if (args.length != 2)
                {
                        System.err.println(
                                "Usage: NpbkToH2ScriptMigrator <input.npbk> <output.sql>");
                        System.exit(1);
                }

                Path source = Paths.get(args[0]);
                Path target = Paths.get(args[1]);

                migrate(source, target);
        }

        /**
         * Migrates the specified {@code .npbk} archive into an H2 SQL script.
         *
         * @param source Path to the legacy archive file.
         * @param target Path to the SQL script to be generated.
         */
        public static void migrate(Path source, Path target) throws Exception
        {
                if (!Files.exists(source))
                {
                        throw new IOException("Legacy archive not found: " + source.toAbsolutePath());
                }

                Path tempDb = Files.createTempFile("npbk-migration", ".db");

                try
                {
                        Database.init(tempDb);
                        Database.get().ensureSchema();

                        Company company = readCompany(source);

                        if (company != null)
                        {
                                persistCompany(company);
                        }

                        exportScript(target);
                }
                finally
                {
                        cleanupTempDatabase(tempDb);
                }

                System.out.printf("Wrote H2 SQL script to %s%n", target.toAbsolutePath());
        }

        private static Company readCompany(Path zipFile) throws IOException
        {
                ObjectMapper mapper = new ObjectMapper()
                        .registerModule(new JavaTimeModule())
                        .configure(JsonParser.Feature.AUTO_CLOSE_SOURCE, false);

                try (InputStream in = Files.newInputStream(zipFile);
                        ZipInputStream zin = new ZipInputStream(in))
                {
                        Company company = null;
                        ChartOfAccounts chart = null;
                        CompanyProfileModel profile = null;
                        Ledger ledger = null;

                        for (ZipEntry entry = zin.getNextEntry(); entry != null;
                                entry = zin.getNextEntry())
                        {
                                if (entry.isDirectory())
                                {
                                        continue;
                                }

                                String normalizedName = entry.getName().replace('\\', '/');
                                int lastSlash = normalizedName.lastIndexOf('/');
                                String fileName = lastSlash >= 0 ? normalizedName.substring(lastSlash + 1) : normalizedName;

                                if (COMPANY_ENTRY.equals(fileName))
                                {
                                        company = mapper.readValue(zin, Company.class);
                                }
                                else if (CHART_ENTRY.equals(fileName))
                                {
                                        chart = mapper.readValue(zin, ChartOfAccounts.class);
                                }
                                else if (PROFILE_ENTRY.equals(fileName))
                                {
                                        profile = mapper.readValue(zin, CompanyProfileModel.class);
                                }
                                else if (LEDGER_ENTRY.equals(fileName))
                                {
                                        ledger = mapper.readValue(zin, Ledger.class);
                                }

                                zin.closeEntry();
                        }

                        if (company == null && profile == null && ledger == null && chart == null)
                        {
                                throw new IOException("No company entries found in archive: " + zipFile.toAbsolutePath());
                        }

                        if (company == null)
                        {
                                company = new Company();
                        }

                        if (profile != null)
                        {
                                company.setCompanyProfileModel(profile);
                        }

                        if (ledger != null)
                        {
                                company.setLedger(ledger);
                        }

                        if (chart != null)
                        {
                                company.setChartOfAccounts(chart);
                        }

                        return company;
                }
        }

        private static void persistCompany(Company company) throws SQLException, IOException
        {
                AccountRepository accounts = new AccountRepository();
                ChartOfAccounts coa = company.getChartOfAccounts();
                List<Account> accountList = coa == null ? List.of() : coa.getAccounts();

                for (Account account : accountList)
                {
                        accounts.upsert(account);
                }

                JournalRepository journal = new JournalRepository();

                if (company.getLedger() != null && company.getLedger().getJournal() != null)
                {
                        for (AccountingTransaction txn : company.getLedger().getJournal()
                                .getJournalTransactions())
                        {
                                journal.upsertTransaction(txn);
                        }
                }

                persistProfile(company.getCompanyProfileModel());

                CompanyRepository companyRepository = new CompanyRepository();
                companyRepository.save(null, company);
        }

        private static void persistProfile(CompanyProfileModel profile) throws SQLException
        {
                new CompanyProfileRepository().save(profile);
        }

        private static void exportScript(Path target) throws SQLException, IOException
        {
                Path parent = target.toAbsolutePath().getParent();
                if (parent != null)
                {
                        Files.createDirectories(parent);
                }

                String escaped = target.toAbsolutePath().toString().replace("'", "''");

                try (Connection connection = Database.get().getConnection();
                        Statement statement = connection.createStatement())
                {
                        statement.execute("SCRIPT DROP TO '" + escaped + "'");
                }
        }

        private static void cleanupTempDatabase(Path tempDb) throws IOException
        {
                Files.deleteIfExists(tempDb);
                Files.deleteIfExists(Path.of(tempDb.toString() + ".mv.db"));
                Files.deleteIfExists(Path.of(tempDb.toString() + ".trace.db"));
        }
}
