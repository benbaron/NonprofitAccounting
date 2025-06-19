/**
 * nonprofit-scaledger-ribbon.zip_expanded AccountService.java AccountService
 */

package nonprofitbookkeeping.service;

import java.math.BigDecimal; // Added for AccountBalance and method return type
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors; // Added for cleaner list transformation

import nonprofitbookkeeping.model.Ledger;

import nonprofitbookkeeping.model.Account;
import nonprofitbookkeeping.model.AccountType;
import nonprofitbookkeeping.model.AccountSide;

/**
<<<<<<< Upstream, based on origin/codex/read-provided-xlsx-file
 * Service class for managing {@link Account} objects. Provides methods for
 * retrieving and manipulating accounts. Accounts are stored in an in-memory
 * list.
=======
 * Service class for managing {@link Account} objects.
 * Provides methods for retrieving and manipulating accounts using a
 * SQL database for persistence.
>>>>>>> b1f07f2 Extend SQL support
 */
/**
 * Service class providing static methods for managing {@link Account} objects.
 * This includes operations such as retrieving all accounts, calculating account balances,
 * adding new accounts, removing accounts, and clearing the account list.
 * Data is stored in a database managed by {@link DatabaseManager}. The methods
 * open their own connections and perform simple SQL operations.
 */
public class AccountService
{
<<<<<<< Upstream, based on origin/codex/read-provided-xlsx-file
	/**
	 * In-memory list to store {@link Account} objects.
	 * This static list serves as the central storage for accounts managed by this service.
	 */
	private static List<Account> accounts = new ArrayList<>();
	
=======

>>>>>>> b1f07f2 Extend SQL support
	/**
	 * Represents the balance of a specific account, including its ID, name, and balance amount.
	 * This is a Java Record, providing a concise way to group related data.
	 *
	 * @param accountId The unique identifier (account number) of the account.
	 * @param accountName The name of the account.
	 * @param balance The current financial balance of the account.
	 */
	public static record AccountBalance(String accountId, String accountName, BigDecimal balance)
	{
	}
	
	/**
	 * Retrieves the balance for all accounts currently managed by this service.
	 * This method iterates through all stored accounts, computes their individual balances
	 * using {@link Account#totalAccountBalance(Ledger)}, and returns a list of
	 * {@link AccountBalance} records.
	 *
	 * @return A {@code List<AccountBalance>} containing the ID, name, and balance
	 *         for each account. Returns an empty list if no accounts are stored or available.
	 */
	public static List<AccountBalance> getBalanceResults(Ledger ledger)
	{
		List<Account> allAccounts = getAllAccounts();
		
		if (allAccounts.isEmpty())
		{
			return Collections.emptyList();
		}
		
		return allAccounts
				.stream()
				.map(account -> 
				new AccountBalance(account.getAccountNumber(),
					account.getName(), 
					account.totalAccountBalance(ledger)))
				.collect(Collectors.toList());
	}
	
	/**
	 * Retrieves all accounts currently managed by this service.
	 *
	 * @return A new {@code List<Account>} containing all stored accounts.
	 *         Returns an empty list if no accounts are currently stored.
	 *         The returned list is a shallow copy of the internal list, so modifications
	 *         to the list itself (e.g., adding/removing elements) will not affect the
	 *         service's internal storage. However, modifications to the {@link Account}
	 *         objects within the list will affect the original objects.
	 */
<<<<<<< Upstream, based on origin/codex/read-provided-xlsx-file
	public static List<Account> getAllAccounts()
	{
		// Return a copy to prevent external modification of the internal list structure
		return new ArrayList<>(accounts);
	}
	
	/**
	 * Clears all accounts from the internal in-memory storage.
	 * This method is useful for resetting the state, for example, during testing
	 * or when loading a new set of company data.
	 */
	public static void clearAccounts()
	{
		
		if (accounts != null)
		{
			accounts.clear();
		}
		else
		{
			// This case should ideally not be reached if 'accounts' is always initialized.
			accounts = new ArrayList<>();
		}
		
	}
	
=======
        public static List<Account> getAllAccounts()
        {
                List<Account> result = new ArrayList<>();
                try (Connection conn = DatabaseManager.getConnection();
                     PreparedStatement ps = conn.prepareStatement(
                             "SELECT account_number,name,account_code,account_type,increase_side,currency,opening_balance FROM account"))
                {
                        ResultSet rs = ps.executeQuery();
                        while (rs.next()) {
                                Account a = new Account();
                                a.setAccountNumber(rs.getString(1));
                                a.setName(rs.getString(2));
                                a.setAccountCode(rs.getString(3));
                                String type = rs.getString(4);
                                if (type != null) {
                                        a.setAccountType(AccountType.valueOf(type));
                                }
                                String side = rs.getString(5);
                                if (side != null) {
                                        a.setIncreaseSide(AccountSide.valueOf(side));
                                }
                                a.setCurrency(rs.getString(6));
                                a.setOpeningBalance(rs.getBigDecimal(7));
                                result.add(a);
                        }
                } catch (SQLException e) {
                        throw new RuntimeException("Error loading accounts", e);
                }
                return result;
        }

        /**
         * Deletes all accounts from the database. Useful for resetting state
         * during testing or when loading a new set of company data.
         */
        public static void clearAccounts() {
                try (Connection conn = DatabaseManager.getConnection();
                     PreparedStatement ps = conn.prepareStatement("DELETE FROM account"))
                {
                        ps.executeUpdate();
                } catch (SQLException e) {
                        throw new RuntimeException("Error clearing accounts", e);
                }
        }

>>>>>>> b1f07f2 Extend SQL support
	/**
	 * Adds a new {@link Account} to the in-memory storage.
	 * The account is not added if it is null, or if its account number is null or blank.
	 * Note: This method does not check for duplicate account numbers; the underlying list
	 * will allow accounts with the same account number if added.
	 *
	 * @param account The {@link Account} object to be added.
	 */
<<<<<<< Upstream, based on origin/codex/read-provided-xlsx-file
	public static void addAccount(Account account)
	{
		
		if (account == null || account.getAccountNumber() == null ||
			account.getAccountNumber().trim().isEmpty())
		{
			// Optionally, log a warning or throw an IllegalArgumentException here
			return;
		}
		
		if (accounts == null)
		{ // Defensive check, though 'accounts' is initialized at declaration
			accounts = new ArrayList<>();
		}
		
		accounts.add(account);
	}
	
	/**
	 * Removes an account from the in-memory storage based on its account ID (account number).
	 * No action is taken if the provided {@code accountId} is null or blank, or if the internal
	 * accounts list is not initialized.
=======
        public static void addAccount(Account account) {
                if (account == null || account.getAccountNumber() == null || account.getAccountNumber().trim().isEmpty()) {
                        return;
                }
                try (Connection conn = DatabaseManager.getConnection();
                     PreparedStatement ps = conn.prepareStatement(
                             "MERGE INTO account(account_number,name,account_code,account_type,increase_side,currency,opening_balance) KEY(account_number) VALUES(?,?,?,?,?,?,?)"))
                {
                        ps.setString(1, account.getAccountNumber());
                        ps.setString(2, account.getName());
                        ps.setString(3, account.getAccountCode());
                        ps.setString(4, account.getAccountType() == null ? null : account.getAccountType().name());
                        ps.setString(5, account.getIncreaseSide() == null ? null : account.getIncreaseSide().name());
                        ps.setString(6, account.getCurrency());
                        ps.setBigDecimal(7, account.getOpeningBalance());
                        ps.executeUpdate();
                } catch (SQLException e) {
                        throw new RuntimeException("Error adding account", e);
                }
        }

        /**
         * Removes an account from the database based on its account number.
         * No action is taken if the provided {@code accountId} is null or blank.
>>>>>>> b1f07f2 Extend SQL support
	 *
	 * @param accountId The unique identifier (account number) of the {@link Account} to be removed.
	 * @return {@code true} if an account with the specified ID was found and removed;
	 *         {@code false} otherwise (e.g., if {@code accountId} is invalid, or no such account exists).
	 */
<<<<<<< Upstream, based on origin/codex/read-provided-xlsx-file
	public static boolean removeAccount(String accountId)
	{
		
		if (accountId == null || accountId.trim().isEmpty() || accounts == null)
		{
			return false;
		}
		
		return accounts.removeIf(account -> accountId.equals(account.getAccountNumber()));
	}
=======
        public static boolean removeAccount(String accountId) {
                if (accountId == null || accountId.trim().isEmpty()) {
                        return false;
                }
                try (Connection conn = DatabaseManager.getConnection();
                     PreparedStatement ps = conn.prepareStatement("DELETE FROM account WHERE account_number = ?"))
                {
                        ps.setString(1, accountId);
                        return ps.executeUpdate() > 0;
                } catch (SQLException e) {
                        throw new RuntimeException("Error removing account", e);
                }
        }
>>>>>>> b1f07f2 Extend SQL support
	
}
