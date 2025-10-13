package nonprofitbookkeeping.tools;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import nonprofitbookkeeping.core.Database;
import nonprofitbookkeeping.model.Account;
import nonprofitbookkeeping.model.AccountingTransaction;
import nonprofitbookkeeping.model.ChartOfAccounts;
import nonprofitbookkeeping.model.Company;
import nonprofitbookkeeping.model.CompanyProfileModel;
import nonprofitbookkeeping.persistence.AccountRepository;
import nonprofitbookkeeping.persistence.JournalRepository;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
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

        private static final String ENTRY = "company_data.json";

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
                try (InputStream in = Files.newInputStream(zipFile);
                        ZipInputStream zin = new ZipInputStream(in))
                {
                        for (ZipEntry entry = zin.getNextEntry(); entry != null;
                                entry = zin.getNextEntry())
                        {
                                if (ENTRY.equals(entry.getName()))
                                {
                                        ObjectMapper mapper = new ObjectMapper()
                                                .registerModule(new JavaTimeModule());
                                        try (Reader reader = new InputStreamReader(zin, StandardCharsets.UTF_8))
                                        {
                                                return mapper.readValue(reader, Company.class);
                                        }
                                }
                        }
                }

                throw new IOException("Entry not found in archive: " + ENTRY);
        }

        private static void persistCompany(Company company) throws SQLException
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
        }

        private static void persistProfile(CompanyProfileModel profile) throws SQLException
        {
                if (profile == null)
                {
                        return;
                }

                try (Connection connection = Database.get().getConnection())
                {
                        connection.setAutoCommit(false);

                        try (PreparedStatement upsert = connection.prepareStatement("""
                                        MERGE INTO company_profile (id, name, address, phone, email,
                                                fiscal_year_start, base_currency, starting_balance_date,
                                                chart_of_accounts_type, admin_username, admin_password,
                                                default_bank_account, enable_fund_accounting, enable_inventory,
                                                enable_multi_currency)
                                        KEY(id)
                                        VALUES(1, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                                        """))
                        {
                                int i = 0;
                                upsert.setString(++i, profile.getCompanyName());
                                upsert.setString(++i, profile.getAddress());
                                upsert.setString(++i, profile.getPhone());
                                upsert.setString(++i, profile.getEmail());
                                upsert.setString(++i, profile.getFiscalYearStart());
                                upsert.setString(++i, profile.getBaseCurrency());
                                upsert.setString(++i, profile.getStartingBalanceDate());
                                upsert.setString(++i, profile.getChartOfAccountsType());
                                upsert.setString(++i, profile.getAdminUsername());
                                upsert.setString(++i, profile.getAdminPassword());
                                upsert.setString(++i, profile.getDefaultBankAccount());
                                upsert.setBoolean(++i, profile.isEnableFundAccounting());
                                upsert.setBoolean(++i, profile.isEnableInventory());
                                upsert.setBoolean(++i, profile.isEnableMultiCurrency());
                                upsert.executeUpdate();
                        }

                        connection.commit();
                }
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
