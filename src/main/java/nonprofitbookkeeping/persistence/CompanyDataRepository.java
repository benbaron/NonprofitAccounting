
package nonprofitbookkeeping.persistence;

import nonprofitbookkeeping.core.Database;
import nonprofitbookkeeping.model.Account;
import nonprofitbookkeeping.model.AccountingEntry;
import nonprofitbookkeeping.model.AccountingTransaction;
import nonprofitbookkeeping.model.Company;
import nonprofitbookkeeping.model.CompanyProfileModel;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Persists and loads the core {@link Company} aggregates using normalized tables instead of the legacy blob store.
 */
public class CompanyDataRepository
{
	
	private final AccountRepository accountRepository = new AccountRepository();
	private final JournalRepository journalRepository = new JournalRepository();
	private final CompanyProfileRepository profileRepository =
		new CompanyProfileRepository();
	
	/**
	 * Persists the supplied {@link Company} aggregate using the normalized persistence
	 * schema. The chart of accounts and journal entries are written within a single
	 * transaction so that related updates are committed atomically.
	 *
	 * @param company the aggregate to save; when {@code null} the operation is ignored
	 * @throws SQLException if any database interaction fails
	 */
	public void persist(Company company) throws SQLException
	{
		
		if (company == null)
		{
			return;
		}
		
		List<AccountingTransaction> transactions =
			company.getLedger() == null ||
				company.getLedger().getJournal() == null ?
					Collections.emptyList() :
					company.getLedger().getJournal().getJournalTransactions();
		
		List<Account> accounts =
			company.getChartOfAccounts() == null ? Collections.emptyList() :
				company.getChartOfAccounts().getAccounts();
		
		List<Account> preparedAccounts =
			ensureAccountsForTransactions(accounts, transactions);
		
		// Clear and restore journal data within a single transaction so account
		// and
		// journal changes are committed atomically.
		try (Connection connection = Database.get().getConnection())
		{
			boolean originalAutoCommit = connection.getAutoCommit();
			connection.setAutoCommit(false);
			
			try
			{
				this.journalRepository.replaceAll(connection,
					Collections.emptyList());
				this.accountRepository.replaceAll(connection, preparedAccounts);
				
				if (transactions != null && !transactions.isEmpty())
				{
					this.journalRepository.replaceAll(connection, transactions);
				}
				
				connection.commit();
			}
			catch (SQLException ex)
			{
				
				try
				{
					connection.rollback();
				}
				catch (SQLException rollbackEx)
				{
					ex.addSuppressed(rollbackEx);
				}
				
				throw ex;
			}
			finally
			{
				connection.setAutoCommit(originalAutoCommit);
			}
			
		}
		
		CompanyProfileModel profile = company.getCompanyProfileModel();
		this.profileRepository.save(profile);
		
	}
	
	/**
	 * Ensures that every transaction entry references an account row by adding
	 * placeholder accounts for any missing account numbers. This prevents foreign
	 * key issues when saving journal entries that reference an account that was not
	 * included in the provided chart of accounts.
	 *
	 * @param accounts      current chart of accounts
	 * @param transactions  journal transactions that may introduce new account numbers
	 * @return a list containing the original accounts plus any required placeholders
	 */
	private List<Account> ensureAccountsForTransactions(List<Account> accounts,
		List<AccountingTransaction> transactions)
	{
		Map<String, Account> byNumber = new LinkedHashMap<>();
		
		if (accounts != null)
		{
			
			for (Account account : accounts)
			{
				
				if (account == null)
				{
					continue;
				}
				
				String number = safeAccountNumber(account);
				
				if (number == null || number.isBlank())
				{
					continue;
				}
				
				byNumber.putIfAbsent(number, account);
			}
			
		}
		
		if (transactions != null)
		{
			
			for (AccountingTransaction transaction : transactions)
			{
				
				if (transaction == null || transaction.getEntries() == null)
				{
					continue;
				}
				
				for (AccountingEntry entry : transaction.getEntries())
				{
					
					if (entry == null)
					{
						continue;
					}
					
					String accountNumber = entry.getAccountNumber();
					
					if (accountNumber == null || accountNumber.isBlank() ||
						byNumber.containsKey(accountNumber))
					{
						continue;
					}
					
					Account placeholder = new Account();
					placeholder.setAccountNumber(accountNumber);
					String accountName = entry.getAccountName();
					
					if (accountName == null || accountName.isBlank())
					{
						accountName = accountNumber;
					}
					
					placeholder.setName(accountName);
					
					if (entry.getAccountSide() != null)
					{
						placeholder.setIncreaseSide(entry.getAccountSide());
					}
					
					byNumber.put(accountNumber, placeholder);
				}
				
			}
			
		}
		
		return new ArrayList<>(byNumber.values());
		
	}
	
	/**
	 * Safely retrieves the account number, returning {@code null} if the account is
	 * {@code null} or throws an unexpected {@link NullPointerException}.
	 *
	 * @param account account to inspect
	 * @return the account number or {@code null} when unavailable
	 */
	private String safeAccountNumber(Account account)
	{
		
		try
		{
			return account.getAccountNumber();
		}
		catch (NullPointerException ex)
		{
			return null;
		}
		
	}
	
	/**
	 * Loads the persisted company aggregate, reconstructing the chart of accounts,
	 * journal transactions, and profile data from their respective repositories.
	 *
	 * @return a fully populated {@link Company}
	 * @throws SQLException if any database query fails
	 */
	public Company load() throws SQLException
	{
		Company company = new Company();
		company.getChartOfAccounts()
			.replaceAllAccounts(this.accountRepository.listAll());
		company.getLedger().getJournal()
			.replaceAllTransactions(this.journalRepository.listTransactions());
		this.profileRepository.load()
			.ifPresent(company::setCompanyProfileModel);
		return company;
		
	}
	
}
