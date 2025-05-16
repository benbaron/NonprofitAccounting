
package nonprofitbookkeeping.model;

import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SettingsModel
{
	// Company Info
	private String organizationName;
	private String fiscalYearStart;
	private String defaultCurrency;
	
	// User Accounts
	private List<User> users = new ArrayList<>();
	
	// Accounting Settings
	private String defaultIncomeAccount;
	private String defaultExpenseAccount;
	private boolean autoNumberVouchers;
	
	// UI Preferences
	private String theme;
	private String language;
	
	/**
	 * 
	 */
	public static class User
	{
		private String username;
		private String role;
		
		public User(String username, String role)
		{
			this.username = username;
			this.role = role;
		}
		
		public String getUsername()
		{
			return this.username;
		}
		
		public void setUsername(String username)
		{
			this.username = username;
		}
		
		public String getRole()
		{
			return this.role;
		}
		
		public void setRole(String role)
		{
			this.role = role;
		}
		
	}
	
	// Company Info Getters/Setters
	public String getOrganizationName()
	{
		return this.organizationName;
	}
	
	public void setOrganizationName(String name)
	{
		this.organizationName = name;
	}
	
	public String getFiscalYearStart()
	{
		return this.fiscalYearStart;
	}
	
	public void setFiscalYearStart(String start)
	{
		this.fiscalYearStart = start;
	}
	
	public String getDefaultCurrency()
	{
		return this.defaultCurrency;
	}
	
	public void setDefaultCurrency(String currency)
	{
		this.defaultCurrency = currency;
	}
	
	// User List
	public List<User> getUsers()
	{
		return this.users;
	}
	
	public void setUsers(List<User> users)
	{
		this.users = users;
	}
	
	// Accounting
	public String getDefaultIncomeAccount()
	{
		return this.defaultIncomeAccount;
	}
	
	public void setDefaultIncomeAccount(String incomeAccount)
	{
		this.defaultIncomeAccount = incomeAccount;
	}
	
	public String getDefaultExpenseAccount()
	{
		return this.defaultExpenseAccount;
	}
	
	public void setDefaultExpenseAccount(String expenseAccount)
	{
		this.defaultExpenseAccount = expenseAccount;
	}
	
	public boolean isAutoNumberVouchers()
	{
		return this.autoNumberVouchers;
	}
	
	public void setAutoNumberVouchers(boolean autoNumberVouchers)
	{
		this.autoNumberVouchers = autoNumberVouchers;
	}
	
	// UI Preferences
	public String getTheme()
	{
		return this.theme;
	}
	
	public void setTheme(String theme)
	{
		this.theme = theme;
	}
	
	public String getLanguage()
	{
		return this.language;
	}
	
	public void setLanguage(String language)
	{
		this.language = language;
	}
	
}
