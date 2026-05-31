
package nonprofitbookkeeping.service;

import nonprofitbookkeeping.model.Account;
import nonprofitbookkeeping.model.Fund;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.type.CollectionType;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.*;

import com.fasterxml.jackson.annotation.JsonProperty;
import nonprofitbookkeeping.persistence.DocumentRepository;


/**
 * Service class for managing fund accounting operations.
 * This includes managing funds and accounts, their relationships,
 * retrieving fund balances, and performing fund transfers.
 * Funds and accounts are stored in in-memory maps.
 */
public class FundAccountingService
{
	
	/** In-memory map storing funds, keyed by fund name. */
	@JsonProperty private final Map<String, Fund> fundMap;
	/** In-memory map storing accounts, keyed by account name. */
	@JsonProperty private final Map<String, Account> accountMap;
	
	/** Logger for this service. */
	private static final Logger LOGGER =
		LoggerFactory.getLogger(FundAccountingService.class);
	
	/** Database document name used to persist funds. */
	private static final String DOCUMENT_NAME = "funds";
	
	/** The Constant MAPPER. */
	private static final ObjectMapper MAPPER = new ObjectMapper()
		.enable(SerializationFeature.INDENT_OUTPUT);
	
	/** The Constant LIST_TYPE. */
	private static final CollectionType LIST_TYPE =
		MAPPER.getTypeFactory().constructCollectionType(List.class, Fund.class);
	
	/**
	 * Constructs a new {@code FundAccountingService}.
	 * Initializes empty maps for storing funds and accounts.
	 */
	public FundAccountingService()
	{
		this.fundMap = new HashMap<>();
		this.accountMap = new HashMap<>();
		
	}
	
	/**
	 * Adds a new fund to the service.
	 * The fund is stored using its name as the key.
	 *
	 * @param fund The {@link Fund} to add. Must not be null.
	 * @throws IllegalArgumentException if a fund with the same name already exists, or if {@code fund} or its name is null.
	 */
	public void addFund(Fund fund)
	{
		
		if (fund == null || fund.getName() == null)
		{
			throw new IllegalArgumentException(
				"Fund and fund name must not be null.");
		}
		
		if (this.fundMap.containsKey(fund.getName()))
		{
			throw new IllegalArgumentException(
				"Fund with name '" + fund.getName() + "' already exists.");
		}
		
		this.fundMap.put(fund.getName(), fund);
		
	}
	
	/**
	 * Removes a fund from the service by its name.
	 * This method also ensures that the removed fund is disassociated from any accounts
	 * it was previously linked with by calling {@link Account#removeFund(Fund)}.
	 *
	 * @param fundName The name of the fund to remove.
	 * @return {@code true} if the fund was found and removed, {@code false} otherwise.
	 */
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
			List<String> associatedAccounts =
				new ArrayList<>(fund.getAccountIds());
			
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
	
	/**
	 * Adds a new account to the service.
	 * The account is stored using its name as the key.
	 *
	 * @param account The {@link Account} to add. Must not be null.
	 * @throws IllegalArgumentException if an account with the same name already exists, or if {@code account} or its name is null.
	 */
	public void addAccount(Account account)
	{
		
		if (account == null || account.getName() == null)
		{
			throw new IllegalArgumentException(
				"Account and account name must not be null.");
		}
		
		if (this.accountMap.containsKey(account.getName()))
		{
			throw new IllegalArgumentException(
				"Account with name '" + account.getName() +
					"' already exists.");
		}
		
		this.accountMap.put(account.getName(), account);
		
	}
	
	/**
	 * Removes an account from the service by its name.
	 * This method also ensures that the removed account is disassociated from any funds
	 * it was previously linked with by calling {@link Fund#removeAccount(Account)}.
	 *
	 * @param accountName The name of the account to remove.
	 * @return {@code true} if the account was found and removed, {@code false} otherwise.
	 */
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
			List<String> associatedFunds =
				new ArrayList<>(account.getAssociatedFundIds());
			
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
	 * Finds a fund by name.
	 *
	 * @param fundName the fund name
	 * @return the fund, or {@code null} when no matching fund exists
	 */
	public Fund findFund(String fundName)
	{
		return fundName == null ? null : this.fundMap.get(fundName);
	}

	/**
	 * Edits a fund's display name and balance.
	 *
	 * @param originalName current fund name
	 * @param newName replacement fund name
	 * @param balance replacement display balance
	 */
	public void editFund(String originalName, String newName, BigDecimal balance)
	{
		if (originalName == null || originalName.isBlank())
		{
			throw new IllegalArgumentException("Original fund name is required.");
		}
		if (newName == null || newName.isBlank())
		{
			throw new IllegalArgumentException("Fund name is required.");
		}
		Fund fund = this.fundMap.get(originalName);
		if (fund == null)
		{
			throw new IllegalArgumentException("Fund not found.");
		}
		String trimmedName = newName.trim();
		if (!originalName.equals(trimmedName) && this.fundMap.containsKey(trimmedName))
		{
			throw new IllegalArgumentException(
				"Fund with name '" + trimmedName + "' already exists.");
		}
		this.fundMap.remove(originalName);
		fund.setName(trimmedName);
		fund.setBalance(balance == null ? BigDecimal.ZERO : balance);
		this.fundMap.put(trimmedName, fund);
	}

	/**
	 * Saves all funds to the persistent document store.
	 *
	 * @param companyDirectory retained for backwards compatibility but ignored by the method
	 * @throws IOException if writing to the database fails
	 */
	public void saveFunds(File companyDirectory) throws IOException
	{
		
		try
		{
			String payload = MAPPER.writeValueAsString(listFunds());
			new DocumentRepository().upsert(DOCUMENT_NAME, payload);
			LOGGER.info("Funds saved to database document '{}'.",
				DOCUMENT_NAME);
		}
		catch (SQLException e)
		{
			throw new IOException("Failed to save funds to database", e);
		}
		
	}
	
	/**
	 * Loads funds from the persistent document store.
	 * Existing in-memory funds are cleared before loading new ones.
	 *
	 * @param companyDirectory retained for backwards compatibility but ignored by the method
	 * @throws IOException if reading from the database fails
	 */
	public void loadFunds(File companyDirectory) throws IOException
	{
		this.fundMap.clear();
		
		try
		{
			new DocumentRepository().find(DOCUMENT_NAME)
				.ifPresent(payload ->
				{
					
					try
					{
						List<Fund> loaded =
							MAPPER.readValue(payload, LIST_TYPE);
						
						for (Fund f : loaded)
						{
							
							if (f.getName() != null)
							{
								this.fundMap.put(f.getName(), f);
							}
							
						}
						
						LOGGER.info(
							"Funds loaded from database document '{}'.",
							DOCUMENT_NAME);
					}
					catch (IOException ex)
					{
						LOGGER.error(
							"Failed to deserialize funds JSON from database",
							ex);
					}
					
				});
		}
		catch (SQLException e)
		{
			throw new IOException("Failed to load funds from database", e);
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
	public void transferFunds(String fromFund, String toFund, BigDecimal amount)
	{
		
		if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0)
		{
			throw new IllegalArgumentException(
				"Amount must be greater than zero.");
		}
		
		Fund from = this.fundMap.get(fromFund);
		Fund to = this.fundMap.get(toFund);
		
		if (from == null || to == null)
		{
			throw new IllegalArgumentException(
				"Source or destination fund does not exist.");
		}
		
		// Perform the transfer logic (using BigDecimal for balance operations)
		from.setBalance(from.getBalance().subtract(amount));
		to.setBalance(to.getBalance().add(amount));
		
	}
	
}
