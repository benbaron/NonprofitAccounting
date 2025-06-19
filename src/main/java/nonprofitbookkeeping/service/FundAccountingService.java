
package nonprofitbookkeeping.service;

import nonprofitbookkeeping.model.Account;
import nonprofitbookkeeping.model.Fund;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.type.CollectionType;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import java.math.BigDecimal;
import java.util.*;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Service class for managing fund accounting operations.
 * This includes managing funds and accounts, their relationships,
 * retrieving fund balances, and performing fund transfers.
 * Funds and accounts are stored in in-memory maps.
 */
public class FundAccountingService
{
<<<<<<< Upstream, based on origin/codex/read-provided-xlsx-file
<<<<<<< Upstream, based on origin/codex/read-provided-xlsx-file
	
	/** In-memory map storing funds, keyed by fund name. */
	@JsonProperty private final Map<String, Fund> fundMap;
	/** In-memory map storing accounts, keyed by account name. */
	@JsonProperty private final Map<String, Account> accountMap;
	
	/** Logger for this service. */
	private static final Logger LOGGER = Logger.getLogger(FundAccountingService.class.getName());
	
	/** Filename used to persist funds to disk. */
	private static final String FUNDS_FILENAME = "funds.json";
	
	/**
	 * Constructs a new {@code FundAccountingService}.
	 * Initializes empty maps for storing funds and accounts.
	 */
	public FundAccountingService()
	{
		this.fundMap = new HashMap<>();
		this.accountMap = new HashMap<>();
	}
=======
        /** Constructs a new {@code FundAccountingService}. */
        public FundAccountingService()
        {
        }
>>>>>>> b1f07f2 Extend SQL support
=======
	
	/** In-memory map storing funds, keyed by fund name. */
	@JsonProperty private final Map<String, Fund> fundMap;
	/** In-memory map storing accounts, keyed by account name. */
	@JsonProperty private final Map<String, Account> accountMap;
	
	/**
	 * Constructs a new {@code FundAccountingService}.
	 * Initializes empty maps for storing funds and accounts.
	 */
	public FundAccountingService()
	{
		this.fundMap = new HashMap<>();
		this.accountMap = new HashMap<>();
	}
>>>>>>> 6159d55 Revert service changes
	
	/**
	 * Adds a new fund to the service.
	 * The fund is stored using its name as the key.
	 *
	 * @param fund The {@link Fund} to add. Must not be null.
	 * @throws IllegalArgumentException if a fund with the same name already exists, or if {@code fund} or its name is null.
	 */
<<<<<<< Upstream, based on origin/codex/read-provided-xlsx-file
<<<<<<< Upstream, based on origin/codex/read-provided-xlsx-file
	public void addFund(Fund fund)
	{
		
		if (fund == null || fund.getName() == null)
		{
			throw new IllegalArgumentException("Fund and fund name must not be null.");
		}
		
		if (this.fundMap.containsKey(fund.getName()))
		{
			throw new IllegalArgumentException(
				"Fund with name '" + fund.getName() + "' already exists.");
		}
		
		this.fundMap.put(fund.getName(), fund);
	}
=======
        public void addFund(Fund fund)
        {
                if (fund == null || fund.getName() == null) {
                        throw new IllegalArgumentException("Fund and fund name must not be null.");
                }
                try (Connection conn = DatabaseManager.getConnection();
                     PreparedStatement ps = conn.prepareStatement(
                             "MERGE INTO fund(fund_id,name,balance) KEY(fund_id) VALUES(?,?,?)"))
                {
                        ps.setString(1, fund.getName());
                        ps.setString(2, fund.getName());
                        ps.setBigDecimal(3, fund.getBalance());
                        ps.executeUpdate();
                } catch (SQLException e) {
                        throw new RuntimeException("Error adding fund", e);
                }
=======
	public void addFund(Fund fund)
	{
		if (fund == null || fund.getName() == null) {
            throw new IllegalArgumentException("Fund and fund name must not be null.");
>>>>>>> 6159d55 Revert service changes
        }
<<<<<<< Upstream, based on origin/codex/read-provided-xlsx-file
>>>>>>> b1f07f2 Extend SQL support
=======
		if (this.fundMap.containsKey(fund.getName()))
		{
			throw new IllegalArgumentException(
				"Fund with name '" + fund.getName() + "' already exists.");
		}
		
		this.fundMap.put(fund.getName(), fund);
	}
>>>>>>> 6159d55 Revert service changes
	
	/**
	 * Removes a fund from the service by its name.
	 * This method also ensures that the removed fund is disassociated from any accounts
	 * it was previously linked with by calling {@link Account#removeFund(Fund)}.
	 *
	 * @param fundName The name of the fund to remove.
	 * @return {@code true} if the fund was found and removed, {@code false} otherwise.
	 */
<<<<<<< Upstream, based on origin/codex/read-provided-xlsx-file
<<<<<<< Upstream, based on origin/codex/read-provided-xlsx-file
	public boolean removeFund(String fundName)
	{
		
		if (fundName == null)
		{
			return false;
		}
		
		Fund fund = this.fundMap.get(fundName);
		
		if (fund != null)
		{
			
			// Disassociate this fund from all related accounts
			List<String> associatedAccounts = new ArrayList<>(fund.getAccountIds());
			
			for (String accountId : associatedAccounts)
			{
				Account account = this.accountMap.get(accountId);
				
				if (account != null)
				{
					account.removeFund(fundName);
				}
				
				fund.removeAccount(accountId);
			}
			
			this.fundMap.remove(fundName);
			return true;
		}
		
		return false;
	}
=======
        public boolean removeFund(String fundName)
        {
                if (fundName == null) {
                        return false;
                }
                try (Connection conn = DatabaseManager.getConnection();
                     PreparedStatement ps = conn.prepareStatement("DELETE FROM fund WHERE fund_id=?"))
                {
                        ps.setString(1, fundName);
                        return ps.executeUpdate() > 0;
                } catch (SQLException e) {
                        throw new RuntimeException("Error removing fund", e);
                }
=======
	public boolean removeFund(String fundName)
	{
		if (fundName == null) {
            return false;
>>>>>>> 6159d55 Revert service changes
        }
<<<<<<< Upstream, based on origin/codex/read-provided-xlsx-file
>>>>>>> b1f07f2 Extend SQL support
=======
		Fund fund = this.fundMap.get(fundName);
		
		if (fund != null)
		{
			
			// Create a copy of the accounts list to iterate over, to avoid ConcurrentModificationException
            // if account.removeFund(fund) modifies the fund's account list indirectly.
            List<Account> associatedAccounts = new ArrayList<>(fund.getAccounts());
			for (Account account : associatedAccounts)
			{
				account.removeFund(fund); // This should also trigger fund.removeAccount(this)
			}
			
			this.fundMap.remove(fundName);
			return true;
		}
		
		return false;
	}
>>>>>>> 6159d55 Revert service changes
	
	/**
	 * Adds a new account to the service.
	 * The account is stored using its name as the key.
	 *
	 * @param account The {@link Account} to add. Must not be null.
	 * @throws IllegalArgumentException if an account with the same name already exists, or if {@code account} or its name is null.
	 */
<<<<<<< Upstream, based on origin/codex/read-provided-xlsx-file
<<<<<<< Upstream, based on origin/codex/read-provided-xlsx-file
	public void addAccount(Account account)
	{
		
		if (account == null || account.getName() == null)
		{
			throw new IllegalArgumentException("Account and account name must not be null.");
		}
		
		if (this.accountMap.containsKey(account.getName()))
		{
			throw new IllegalArgumentException(
				"Account with name '" + account.getName() + "' already exists.");
		}
		
		this.accountMap.put(account.getName(), account);
	}
=======
        public void addAccount(Account account)
        {
                AccountService.addAccount(account);
=======
	public void addAccount(Account account)
	{
		if (account == null || account.getName() == null) {
            throw new IllegalArgumentException("Account and account name must not be null.");
>>>>>>> 6159d55 Revert service changes
        }
<<<<<<< Upstream, based on origin/codex/read-provided-xlsx-file
>>>>>>> b1f07f2 Extend SQL support
=======
		if (this.accountMap.containsKey(account.getName()))
		{
			throw new IllegalArgumentException(
				"Account with name '" + account.getName() + "' already exists.");
		}
		
		this.accountMap.put(account.getName(), account);
	}
>>>>>>> 6159d55 Revert service changes
	
	/**
	 * Removes an account from the service by its name.
	 * This method also ensures that the removed account is disassociated from any funds
	 * it was previously linked with by calling {@link Fund#removeAccount(Account)}.
	 *
	 * @param accountName The name of the account to remove.
	 * @return {@code true} if the account was found and removed, {@code false} otherwise.
	 */
<<<<<<< Upstream, based on origin/codex/read-provided-xlsx-file
<<<<<<< Upstream, based on origin/codex/read-provided-xlsx-file
	public boolean removeAccount(String accountName)
	{
		
		if (accountName == null)
		{
			return false;
		}
		
		Account account = this.accountMap.get(accountName);
		
		if (account != null)
		{
			// Disassociate account from all related funds
			List<String> associatedFunds = new ArrayList<>(account.getAssociatedFundIds());
			
			for (String fundId : associatedFunds)
			{
				Fund fund = this.fundMap.get(fundId);
				
				if (fund != null)
				{
					fund.removeAccount(accountName);
				}
				
				account.removeFund(fundId);
			}
			
			this.accountMap.remove(accountName);
			return true;
		}
		
		return false;
	}
=======
        public boolean removeAccount(String accountName)
        {
                return AccountService.removeAccount(accountName);
=======
	public boolean removeAccount(String accountName)
	{
		if (accountName == null) {
            return false;
>>>>>>> 6159d55 Revert service changes
        }
<<<<<<< Upstream, based on origin/codex/read-provided-xlsx-file
>>>>>>> b1f07f2 Extend SQL support
=======
		Account account = this.accountMap.get(accountName);
		
		if (account != null)
		{
			// Create a copy for iteration to avoid ConcurrentModificationException
            List<Fund> associatedFunds = new ArrayList<>(account.getAssociatedFunds());
                        for (Fund fund : associatedFunds)
                        {
                                fund.removeAccount(account);
                        }
			
			this.accountMap.remove(accountName);
			return true;
		}
		
		return false;
	}
>>>>>>> 6159d55 Revert service changes
	
	/**
	 * Retrieves a list of all funds currently managed by this service.
	 *
	 * @return A new {@link ArrayList} containing all {@link Fund} objects.
	 *         This is a copy, so modifications to the returned list will not affect internal storage.
	 */
	public List<Fund> listFunds()
	{
		return new ArrayList<>(this.fundMap.values());
	}
	
	/**
	 * Saves all funds to a JSON file located in the given company directory.
	 *
	 * @param companyDirectory directory where the funds file should be written
	 * @throws IOException if writing fails or the directory is invalid
	 */
	public void saveFunds(File companyDirectory) throws IOException
	{
		
		if (companyDirectory == null || !companyDirectory.isDirectory())
		{
			throw new IOException("Company directory is invalid or not provided.");
		}
		
		File target = new File(companyDirectory, FUNDS_FILENAME);
		ObjectMapper mapper = new ObjectMapper();
		mapper.enable(SerializationFeature.INDENT_OUTPUT);
		
		try
		{
			mapper.writeValue(target, listFunds());
		}
		catch (IOException ex)
		{
			LOGGER.log(Level.SEVERE, "Failed to save funds to " + target.getAbsolutePath(), ex);
			throw ex;
		}
		
	}
	
	/**
	 * Loads funds from a JSON file located in the given company directory.
	 * Existing in-memory funds are cleared before loading new ones. If the
	 * file does not exist, this method returns without modifying the current state.
	 *
	 * @param companyDirectory directory where the funds file is located
	 * @throws IOException if reading fails or the directory is invalid
	 */
	public void loadFunds(File companyDirectory) throws IOException
	{
		this.fundMap.clear();
		
		if (companyDirectory == null || !companyDirectory.isDirectory())
		{
			throw new IOException("Company directory is invalid or not provided.");
		}
		
		File target = new File(companyDirectory, FUNDS_FILENAME);
		
		if (!target.exists() || target.length() == 0)
		{
			return; // nothing to load
		}
		
		ObjectMapper mapper = new ObjectMapper();
		CollectionType listType =
			mapper.getTypeFactory().constructCollectionType(List.class, Fund.class);
		
		try
		{
			List<Fund> loaded = mapper.readValue(target, listType);
			
			for (Fund f : loaded)
			{
				
				if (f.getName() != null)
				{
					this.fundMap.put(f.getName(), f);
				}
				
			}
			
		}
		catch (IOException ex)
		{
			LOGGER.log(Level.SEVERE, "Failed to load funds from " + target.getAbsolutePath(), ex);
			throw ex;
		}
		
	}
	
	/**
	 * Retrieves a list of all accounts currently managed by this service.
	 *
	 * @return A new {@link ArrayList} containing all {@link Account} objects.
	 *         This is a copy, so modifications to the returned list will not affect internal storage.
	 */
	public List<Account> listAccounts()
	{
		return new ArrayList<>(this.accountMap.values());
	}
	
	/**
	 * Gets the current balances for all funds.
	 * The balance for each fund is obtained via {@link Fund#getBalance()}.
	 *
	 * @return A {@link Map} where keys are fund names (String) and values are their
	 *         corresponding balances ({@link BigDecimal}).
	 */
	public Map<String, BigDecimal> getFundBalances()
	{
		Map<String, BigDecimal> balances = new HashMap<>();
		
		for (Fund fund : this.fundMap.values())
		{
			balances.put(fund.getName(), fund.getBalance());
		}
		
		return balances;
	}
	
	/**
	 * Transfers a specified amount between two funds.
	 * This method directly adjusts the balances of the source and destination funds
	 * using {@link Fund#setBalance(BigDecimal)}. It does not create accounting transactions.
	 *
	 * @param fromFund The name of the fund from which the amount will be transferred.
	 * @param toFund The name of the fund to which the amount will be transferred.
	 * @param amount The {@link BigDecimal} amount to transfer. Must be greater than zero.
	 * @throws IllegalArgumentException if {@code amount} is not greater than zero,
	 *                                  or if either {@code fromFund} or {@code toFund} does not exist.
	 */
<<<<<<< Upstream, based on origin/codex/read-provided-xlsx-file
<<<<<<< Upstream, based on origin/codex/read-provided-xlsx-file
	public void transferFunds(String fromFund, String toFund, BigDecimal amount)
	{
		
		if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0)
		{
			throw new IllegalArgumentException("Amount must be greater than zero.");
		}
		
		Fund from = this.fundMap.get(fromFund);
		Fund to = this.fundMap.get(toFund);
		
		if (from == null || to == null)
		{
			throw new IllegalArgumentException("Source or destination fund does not exist.");
		}
		
		// Perform the transfer logic (using BigDecimal for balance operations)
		from.setBalance(from.getBalance().subtract(amount));
		to.setBalance(to.getBalance().add(amount));
	}
=======
        public void transferFunds(String fromFund, String toFund, BigDecimal amount)
        {
                if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
                        throw new IllegalArgumentException("Amount must be greater than zero.");
                }
                try (Connection conn = DatabaseManager.getConnection()) {
                        conn.setAutoCommit(false);
                        try (PreparedStatement getStmt = conn.prepareStatement("SELECT balance FROM fund WHERE fund_id=?");
                             PreparedStatement updateStmt = conn.prepareStatement("UPDATE fund SET balance=? WHERE fund_id=?")) {
                                getStmt.setString(1, fromFund);
                                ResultSet rsFrom = getStmt.executeQuery();
                                if (!rsFrom.next()) throw new IllegalArgumentException("Source fund not found");
                                BigDecimal fromBal = rsFrom.getBigDecimal(1);

                                getStmt.setString(1, toFund);
                                ResultSet rsTo = getStmt.executeQuery();
                                if (!rsTo.next()) throw new IllegalArgumentException("Destination fund not found");
                                BigDecimal toBal = rsTo.getBigDecimal(1);

                                updateStmt.setBigDecimal(1, fromBal.subtract(amount));
                                updateStmt.setString(2, fromFund);
                                updateStmt.executeUpdate();

                                updateStmt.setBigDecimal(1, toBal.add(amount));
                                updateStmt.setString(2, toFund);
                                updateStmt.executeUpdate();

                                conn.commit();
                        } catch (SQLException e) {
                                conn.rollback();
                                throw e;
                        }
                } catch (SQLException e) {
                        throw new RuntimeException("Error transferring funds", e);
                }
        }
>>>>>>> b1f07f2 Extend SQL support
=======
	public void transferFunds(String fromFund, String toFund, BigDecimal amount)
	{
		if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0)
		{
			throw new IllegalArgumentException("Amount must be greater than zero.");
		}
		
		Fund from = this.fundMap.get(fromFund);
		Fund to = this.fundMap.get(toFund);
		
		if (from == null || to == null)
		{
			throw new IllegalArgumentException("Source or destination fund does not exist.");
		}
		
		// Perform the transfer logic (using BigDecimal for balance operations)
		from.setBalance(from.getBalance().subtract(amount));
		to.setBalance(to.getBalance().add(amount));
	}
>>>>>>> 6159d55 Revert service changes
	
}
