
package nonprofitbookkeeping.tools;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import nonprofitbookkeeping.core.Database;
import nonprofitbookkeeping.model.*;
import nonprofitbookkeeping.persistence.AccountRepository;
import nonprofitbookkeeping.persistence.JournalRepository;

import java.io.*;
import java.nio.file.*;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.*;

public class JsonCompanyImporter
{
	
	private static final String ENTRY = "company_data.json";
	
	public static void importZip(Path zipFile) throws Exception
	{
		byte[] json = readEntry(zipFile, ENTRY);
		Company company = new ObjectMapper()
			.registerModule(new JavaTimeModule())
			.readValue(json, Company.class);
		
		AccountRepository accounts = new AccountRepository();
		
		ChartOfAccounts coa = company.getChartOfAccounts();
		
		List<Account> aclist = coa.getAccounts();
		
		for (Account a : aclist)
		{
			accounts.upsert(a);
		}
		
		JournalRepository journal = new JournalRepository();
		
		if (company.getLedger() != null &&
			company.getLedger().getJournal() != null)
		{
			
			for (AccountingTransaction t : company.getLedger().getJournal()
				.getJournalTransactions())
			{
				journal.upsertTransaction(t);
			}
			
		}
		
		persistProfile(company.getCompanyProfileModel());
		
	}
	
	private static void persistProfile(CompanyProfileModel p)
		throws SQLException
	{
		if (p == null)
			return;
		
		try (var c = Database.get().getConnection();
			var ps = c.prepareStatement(
				"""
					  UPDATE company_profile SET
					    name=?, address=?, phone=?, email=?,
					     fiscal_year_start=?, base_currency=?, starting_balance_date=?,
					     chart_of_accounts_type=?, admin_username=?, admin_password=?,
					     default_bank_account=?, enable_fund_accounting=?, enable_inventory=?, enable_multi_currency=?
					   WHERE id=1
					"""))
		{
			int i = 0;
			ps.setString(++i, p.getCompanyName());
			ps.setString(++i, p.getAddress());
			ps.setString(++i, p.getPhone());
			ps.setString(++i, p.getEmail());
			ps.setString(++i, p.getFiscalYearStart());
			ps.setString(++i, p.getBaseCurrency());
			ps.setString(++i, p.getStartingBalanceDate());
			ps.setString(++i, p.getChartOfAccountsType());
			ps.setString(++i, p.getAdminUsername());
			ps.setString(++i, p.getAdminPassword());
			ps.setString(++i, p.getDefaultBankAccount());
			ps.setBoolean(++i, p.isEnableFundAccounting());
			ps.setBoolean(++i, p.isEnableInventory());
			ps.setBoolean(++i, p.isEnableMultiCurrency());
			ps.executeUpdate();
		}
		
	}
	
	private static byte[] readEntry(Path zip, String name) throws IOException
	{
		
		try (ZipInputStream zin = new ZipInputStream(Files.newInputStream(zip)))
		{
			
			for (ZipEntry e = zin.getNextEntry(); e != null;
				e = zin.getNextEntry())
			{
				
				if (e.getName().equals(name))
				{
					return zin.readAllBytes();
				}
				
			}
			
		}
		
		throw new FileNotFoundException("Entry not found: " + name);
		
	}
	
}
