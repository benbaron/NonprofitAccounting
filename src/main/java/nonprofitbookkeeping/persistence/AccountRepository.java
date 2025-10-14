
package nonprofitbookkeeping.persistence;

import nonprofitbookkeeping.core.Database;
import nonprofitbookkeeping.model.Account;
import nonprofitbookkeeping.model.AccountSide;
import nonprofitbookkeeping.model.AccountType;

import java.math.BigDecimal;
import java.sql.*;
import java.util.*;

public class AccountRepository
{
	
	public void upsert(Account a) throws SQLException
	{
		String sql =
			"""
				    MERGE INTO account(account_number, name, account_code, account_type, increase_side,
				                       parent_account_id, currency, opening_balance)
				    KEY(account_number)
				    VALUES(?,?,?,?,?,?,?,?)
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
	
	public List<Account> listAll() throws SQLException
	{
		List<Account> out = new ArrayList<>();
		
		try (Connection c = Database.get().getConnection();
			PreparedStatement ps = c.prepareStatement(
				"SELECT * FROM account ORDER BY account_number");
			ResultSet rs = ps.executeQuery())
		{
			
			while (rs.next())
			{
				Account a = new Account(
					rs.getString("account_number"),
					rs.getString("name"),
					rs.getString("increase_side") == null ? null :
						AccountSide.valueOf(rs.getString("increase_side")),
					rs.getString("account_type") == null ? null :
						AccountType.valueOf(rs.getString("account_type"))
				);
				a.setAccountCode(rs.getString("account_code"));
				a.setParentAccountId(rs.getString("parent_account_id"));
				a.setCurrency(rs.getString("currency"));
				a.setOpeningBalance(rs.getBigDecimal("opening_balance"));
				out.add(a);
			}
			
		}
		
		return out;
		
	}
	
	
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
	
}
