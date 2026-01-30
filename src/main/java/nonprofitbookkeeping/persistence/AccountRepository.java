
package nonprofitbookkeeping.persistence;

import nonprofitbookkeeping.core.Database;
import nonprofitbookkeeping.model.Account;
import nonprofitbookkeeping.model.AccountSide;
import nonprofitbookkeeping.model.AccountType;
import nonprofitbookkeeping.model.supplemental.SupplementalLineKind;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Repository that manages {@link Account} rows and their associated fund mappings
 * inside the database. The APIs are intentionally simple to keep persistence logic
 * consistent across import/export operations.
 */
public class AccountRepository
{
	
	/**
	 * Inserts or updates an account row and its fund associations.
	 *
	 * @param a account to persist
	 * @throws SQLException if any database statement fails
	 */
	public void upsert(Account a) throws SQLException
	{
		String sql =
			"""
				                                MERGE INTO account(account_number, name, account_code, account_type, increase_side,
				                       parent_account_id, currency, opening_balance, supplemental_kinds)
				    KEY(account_number)
				    VALUES(?,?,?,?,?,?,?,?,?)
				""";
		
		try (Connection c = Database.get().getConnection();
			PreparedStatement ps = c.prepareStatement(sql))
		{
			int i = 0;
			ps.setString(++i, a.getAccountNumber());
			ps.setString(++i, a.getName());
			ps.setString(++i, a.getAccountCode());
			ps.setString(++i,
				a.getAccountType() == null ? null : a.getAccountType().name());
			ps.setString(++i, a.getIncreaseSide() == null ? null :
				a.getIncreaseSide().name());
			ps.setString(++i, a.getParentAccountId());
			ps.setString(++i, a.getCurrency());
			ps.setBigDecimal(++i, a.getOpeningBalance() == null ?
				BigDecimal.ZERO : a.getOpeningBalance());
			ps.setString(++i,
				encodeSupplementalKinds(a.getSupplementalLineKinds()));
			ps.executeUpdate();
		}
		
		try (Connection c = Database.get().getConnection())
		{
			c.setAutoCommit(false);
			
			try (PreparedStatement del = c.prepareStatement(
				"DELETE FROM account_fund WHERE account_number=?"))
			{
				del.setString(1, a.getAccountNumber());
				del.executeUpdate();
			}
			
			if (a.getAssociatedFundIds() != null)
			{
				
				try (PreparedStatement ins = c.prepareStatement(
					"INSERT INTO account_fund(account_number, fund_id) VALUES (?,?)"))
				{
					
					for (String f : a.getAssociatedFundIds())
					{
						ins.setString(1, a.getAccountNumber());
						ins.setString(2, f);
						ins.addBatch();
					}
					
					ins.executeBatch();
				}
				
			}
			
			c.commit();
		}
		
	}
	
	/**
	 * Retrieves all accounts in account number order, eagerly loading any
	 * associated fund ids.
	 *
	 * @return ordered list of accounts
	 * @throws SQLException if reading from the database fails
	 */
	public List<Account> listAll() throws SQLException
	{
		Map<String, Account> byNumber = new LinkedHashMap<>();
		
		try (Connection c = Database.get().getConnection();
			PreparedStatement ps = c.prepareStatement(
				"SELECT * FROM account ORDER BY account_number");
			ResultSet rs = ps.executeQuery())
		{
			
			while (rs.next())
			{
				String accountNumber = rs.getString("account_number");
				
				if (accountNumber == null)
				{
					continue;
				}
				
				AccountSide increaseSide =
					rs.getString("increase_side") == null ? null :
						AccountSide.valueOf(rs.getString("increase_side"));
				Account a = new Account(accountNumber,
					rs.getString("name"),
					increaseSide);
				
				if (rs.getString("account_type") != null)
				{
					a.setAccountType(
						AccountType.valueOf(rs.getString("account_type")));
				}
				
				a.setAccountCode(rs.getString("account_code"));
				a.setParentAccountId(rs.getString("parent_account_id"));
				a.setCurrency(rs.getString("currency"));
				a.setOpeningBalance(rs.getBigDecimal("opening_balance"));
				a.setSupplementalLineKinds(
					decodeSupplementalKinds(rs.getString("supplemental_kinds")));
				byNumber.put(a.getAccountNumber(), a);
			}
			
		}
		
		if (!byNumber.isEmpty())
		{
			
			try (Connection c = Database.get().getConnection();
				PreparedStatement ps = c.prepareStatement(
					"SELECT account_number, fund_id FROM account_fund ORDER BY account_number, fund_id");
				ResultSet rs = ps.executeQuery())
			{
				
				while (rs.next())
				{
					Account account =
						byNumber.get(rs.getString("account_number"));
					
					if (account != null)
					{
						account.addFund(rs.getString("fund_id"));
					}
					
				}
				
			}
			
		}
		
		return new ArrayList<>(byNumber.values());
		
	}
	
	
	/**
	 * Deletes an account by its account number.
	 *
	 * @param accountNumber identifier of the account to remove
	 * @throws SQLException if the delete fails
	 */
	public void delete(String accountNumber) throws java.sql.SQLException
	{
		
		try (
			java.sql.Connection c =
				nonprofitbookkeeping.core.Database.get().getConnection();
			java.sql.PreparedStatement ps = c
				.prepareStatement("DELETE FROM account WHERE account_number=?"))
		{
			ps.setString(1, accountNumber);
			ps.executeUpdate();
		}
		
	}
	
	/**
	 * Atomically replaces all accounts with the supplied collection, resetting
	 * existing fund associations in the process.
	 *
	 * @param accounts complete set of accounts to persist
	 * @throws SQLException if any database write fails
	 */
	public void replaceAll(List<Account> accounts) throws SQLException
	{
		
		try (Connection c = Database.get().getConnection())
		{
			boolean originalAutoCommit = c.getAutoCommit();
			c.setAutoCommit(false);
			
			try
			{
				replaceAll(c, accounts);
				c.commit();
			}
			catch (SQLException ex)
			{
				
				try
				{
					c.rollback();
				}
				catch (SQLException rollbackEx)
				{
					ex.addSuppressed(rollbackEx);
				}
				
				throw ex;
			}
			finally
			{
				c.setAutoCommit(originalAutoCommit);
			}
			
		}
		
	}
	
	/**
	 * Performs the replacement of all account rows using the provided connection.
	 * Fund associations are removed and then recreated for the supplied accounts.
	 *
	 * @param c         open database connection with an active transaction
	 * @param accounts  accounts to insert
	 * @throws SQLException if any statement fails
	 */
	void replaceAll(Connection c, List<Account> accounts) throws SQLException
	{
		
		if (c == null)
		{
			throw new IllegalArgumentException("connection required");
		}
		
		try (
			PreparedStatement deleteFunds =
				c.prepareStatement("DELETE FROM account_fund");
			PreparedStatement deleteAccounts =
				c.prepareStatement("DELETE FROM account"))
		{
			deleteFunds.executeUpdate();
			deleteAccounts.executeUpdate();
		}
		
		if (accounts == null || accounts.isEmpty())
		{
			return;
		}
		
		try (PreparedStatement insertAccount = c.prepareStatement(
			"INSERT INTO account(account_number, name, account_code, account_type, " +
				"increase_side, parent_account_id, currency, opening_balance, supplemental_kinds) " +
				"VALUES (?,?,?,?,?,?,?,?,?)");
			PreparedStatement insertFund = c.prepareStatement(
				"INSERT INTO account_fund(account_number, fund_id) VALUES (?,?)"))
		{
			
			for (Account account : accounts)
			{
				int i = 0;
				insertAccount.setString(++i, account.getAccountNumber());
				insertAccount.setString(++i, account.getName());
				insertAccount.setString(++i, account.getAccountCode());
				insertAccount.setString(++i,
					account.getAccountType() == null ? null :
						account.getAccountType().name());
				insertAccount.setString(++i,
					account.getIncreaseSide() == null ? null :
						account.getIncreaseSide().name());
				insertAccount.setString(++i, account.getParentAccountId());
				insertAccount.setString(++i, account.getCurrency());
				BigDecimal openingBalance = account.getOpeningBalance();
				insertAccount.setBigDecimal(++i,
					openingBalance == null ? BigDecimal.ZERO : openingBalance);
				insertAccount.setString(++i,
					encodeSupplementalKinds(account.getSupplementalLineKinds()));
				insertAccount.addBatch();
				
				List<String> funds = account.getAssociatedFundIds();
				
				if (funds != null)
				{
					
					for (String fundId : funds)
					{
						
						if (fundId == null || fundId.isBlank())
						{
							continue;
						}
						
						insertFund.setString(1, account.getAccountNumber());
						insertFund.setString(2, fundId);
						insertFund.addBatch();
					}
					
				}
				
			}
			
			insertAccount.executeBatch();
			insertFund.executeBatch();
		}
		
	}

	private static String encodeSupplementalKinds(List<SupplementalLineKind> kinds)
	{
		
		if (kinds == null || kinds.isEmpty())
		{
			return null;
		}
		
		return kinds.stream()
			.filter(kind -> kind != null)
			.map(SupplementalLineKind::name)
			.sorted()
			.reduce((a, b) -> a + "," + b)
			.orElse(null);
	}

	private static List<SupplementalLineKind> decodeSupplementalKinds(String value)
	{
		
		if (value == null || value.isBlank())
		{
			return List.of();
		}
		
		String[] parts = value.split(",");
		List<SupplementalLineKind> kinds = new ArrayList<>();
		
		for (String part : parts)
		{
			String trimmed = part.trim();
			
			if (trimmed.isEmpty())
			{
				continue;
			}
			
			try
			{
				kinds.add(SupplementalLineKind.valueOf(trimmed));
			}
			catch (IllegalArgumentException ex)
			{
				// ignore unknown values for forward compatibility
			}
			
		}
		
		return kinds;
	}
	
}
