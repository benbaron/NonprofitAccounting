
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
import nonprofitbookkeeping.persistence.CompanyProfileRepository;
import nonprofitbookkeeping.persistence.CompanyRepository;
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
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

// TODO: Auto-generated Javadoc
/**
 * One-time utility that converts legacy {@code .npbk} archives into an H2
 * database script.  The generated script can later be imported by the
 * application through {@link H2ScriptCompanyImporter}.
 */
public final class NpbkToH2ScriptMigrator
{
	
	/** The Constant ENTRY. */
	private static final String ENTRY = "company_data.json";
	
	/**
	 * Instantiates a new npbk to H 2 script migrator.
	 */
	private NpbkToH2ScriptMigrator()
	{
	
	}
	
	/**
	 * The main method.
	 *
	 * @param args the arguments
	 * @throws Exception the exception
	 */
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
	 * @throws Exception the exception
	 */
	public static void migrate(Path source, Path target) throws Exception
	{
		
		if (!Files.exists(source))
		{
			throw new IOException(
				"Legacy archive not found: " + source.toAbsolutePath());
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
		
		System.out.printf("Wrote H2 SQL script to %s%n",
			target.toAbsolutePath());
		
	}
	
	/**
	 * Read company.
	 *
	 * @param zipFile the zip file
	 * @return the company
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
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
					
					try (Reader reader =
						new InputStreamReader(zin, StandardCharsets.UTF_8))
					{
						return mapper.readValue(reader, Company.class);
					}
					
				}
				
			}
			
		}
		
		throw new IOException("Entry not found in archive: " + ENTRY);
		
	}
	
	/**
	 * Persist company.
	 *
	 * @param company the company
	 * @throws SQLException the SQL exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	private static void persistCompany(Company company)
		throws SQLException, IOException
	{
		AccountRepository accounts = new AccountRepository();
		ChartOfAccounts coa = company.getChartOfAccounts();
		List<Account> accountList = coa == null ? List.of() : coa.getAccounts();
		
		for (Account account : accountList)
		{
			accounts.upsert(account);
		}
		
		JournalRepository journal = new JournalRepository();
		
		if (company.getLedger() != null &&
			company.getLedger().getJournal() != null)
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
	
	/**
	 * Persist profile.
	 *
	 * @param profile the profile
	 * @throws SQLException the SQL exception
	 */
	private static void persistProfile(CompanyProfileModel profile)
		throws SQLException
	{
		new CompanyProfileRepository().save(profile);
		
	}
	
	/**
	 * Export script.
	 *
	 * @param target the target
	 * @throws SQLException the SQL exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	private static void exportScript(Path target)
		throws SQLException, IOException
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
	
	/**
	 * Cleanup temp database.
	 *
	 * @param tempDb the temp db
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	private static void cleanupTempDatabase(Path tempDb) throws IOException
	{
		Files.deleteIfExists(tempDb);
		Files.deleteIfExists(Path.of(tempDb.toString() + ".mv.db"));
		Files.deleteIfExists(Path.of(tempDb.toString() + ".trace.db"));
		
	}
	
}
