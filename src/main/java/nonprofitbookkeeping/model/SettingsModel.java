
package nonprofitbookkeeping.model;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.MonthDay;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

// TODO: Auto-generated Javadoc
/**
 * Represents the application settings model.
 * This class encapsulates the different configuration options persisted for the
 * application and exposes strongly typed accessors for consumers.
 */
public class SettingsModel
{
	// Company information
	/** The name of the organization. */
	@JsonProperty private String organizationName;
	/** The start of the fiscal year stored as a string (e.g. "MM-DD"). */
	@JsonAlias("fiscalYearStartMonthDay")
	@JsonProperty private String fiscalYearStart;
	/** ISO 4217 code of the default currency. */
	@JsonProperty private String defaultCurrency;
	
	// User accounts
	/** Configured application users. */
	@JsonProperty private List<User> users = new ArrayList<>();
	
	// Accounting defaults
	/** The default income account identifier. */
	@JsonProperty private String defaultIncomeAccount;
	/** The default expense account identifier. */
	@JsonProperty private String defaultExpenseAccount;
	/** Whether vouchers/invoices should be auto-numbered. */
	@JsonProperty private boolean autoNumberVouchers;
	
	// Autosave and filesystem preferences
	/** Flag indicating whether background autosave is enabled. */
	@JsonProperty private boolean autosaveEnabled = true;
	/** Autosave interval in minutes, 0 disables autosave. */
	@JsonProperty private int autosaveIntervalMinutes = 5;
	/** Default directory presented in file choosers. */
	@JsonProperty private String defaultDirectory;
	/** Path to the most recently opened bookkeeping file. */
	@JsonProperty private String lastOpenedFile;
	/** User selected default company directory. */
	@JsonProperty private String defaultCompanyDirectory;
	/** Path to the most recently used company file. */
	@JsonProperty private String lastUsedCompanyFile;
	
	// Reporting defaults
	/** Preferred default report period preset identifier. */
	@JsonProperty private String defaultReportPeriod =
		ReportPeriodPreset.YEAR_TO_DATE.name();
	/** Calendar year used when the fiscal year preset is selected. */
	@JsonProperty private Integer defaultReportYear;
	/** Whether the "Year-To-Date" period should be offered. */
	@JsonProperty private boolean enableYearToDateOption = true;
	/** Whether the "Full Year" period should be offered. */
	@JsonProperty private boolean enableFullYearOption = true;
	/** Whether the "Last Month" period should be offered. */
	@JsonProperty private boolean enableLastMonthOption = true;
	
	// UI preferences
	/** Preferred theme. */
	@JsonProperty private String theme;
	/** Preferred language/locale identifier. */
	@JsonProperty private String language;
	/** Preferred text color for rows marked as pending. */
	@JsonProperty private String pendingRowTextColor = "Black";
	/** BCP 47 tag representing the locale used for currency formatting. */
	@JsonProperty("currencyLocale") private String currencyLocaleTag;
	/** Pattern used to format currency values. */
	@JsonProperty private String currencyFormat = "$#,##0.00";
	
	/**
	 * Represents a user account within the settings model.
	 */
	public static class User
	{
		/** Username for the account. */
		private String username;
		/** Role assigned to the user. */
		private String role;
		
		
		/**
		 * Creates a new user instance.
		 *
		 * @param username the username
		 * @param role the associated role
		 */
		public User(String username, String role)
		{
			this.username = username;
			this.role = role;
			
		}
		
		/** Default constructor for serialization frameworks. */
		public User()
		{
			
		}
		
		/**
		 * Gets the username.
		 *
		 * @return the username
		 */
		public String getUsername()
		{
			return this.username;
			
		}
		
		/**
		 * Sets the username.
		 *
		 * @param username the new username
		 */
		public void setUsername(String username)
		{
			this.username = username;
			
		}
		
		/**
		 * Gets the role.
		 *
		 * @return the role
		 */
		public String getRole()
		{
			return this.role;
			
		}
		
		/**
		 * Sets the role.
		 *
		 * @param role the new role
		 */
	public void setRole(String role)
	{
		this.role = role;
		
	}
		
	}
	
	// ---------------------------------------------------------------------
	// Basic getters and setters
	// ---------------------------------------------------------------------
	
	/**
	 * Gets the organization name.
	 *
	 * @return the organization name
	 */
	public String getOrganizationName()
	{
		return this.organizationName;
		
	}
	
	/**
	 * Sets the organization name.
	 *
	 * @param organizationName the new organization name
	 */
	public void setOrganizationName(String organizationName)
	{
		this.organizationName = organizationName;
		
	}
	
	/**
	 * Gets the fiscal year start.
	 *
	 * @return the fiscal year start
	 */
	public String getFiscalYearStart()
	{
		return this.fiscalYearStart;
		
	}
	
	/**
	 * Sets the fiscal year start.
	 *
	 * @param fiscalYearStart the new fiscal year start
	 */
	public void setFiscalYearStart(String fiscalYearStart)
	{
		this.fiscalYearStart = fiscalYearStart;
		
	}
	
	/**
	 * Gets the default currency.
	 *
	 * @return the default currency
	 */
	public String getDefaultCurrency()
	{
		return this.defaultCurrency;
		
	}
	
	/**
	 * Sets the default currency.
	 *
	 * @param defaultCurrency the new default currency
	 */
	public void setDefaultCurrency(String defaultCurrency)
	{
		this.defaultCurrency = defaultCurrency;
		
	}
	
	/**
	 * Gets the users.
	 *
	 * @return the users
	 */
	public List<User> getUsers()
	{
		return this.users;
		
	}
	
	/**
	 * Sets the users.
	 *
	 * @param users the new users
	 */
	public void setUsers(List<User> users)
	{
		this.users =
			(users == null) ? new ArrayList<>() : new ArrayList<>(users);
		
	}
	
	/**
	 * Gets the default income account.
	 *
	 * @return the default income account
	 */
	public String getDefaultIncomeAccount()
	{
		return this.defaultIncomeAccount;
		
	}
	
	/**
	 * Sets the default income account.
	 *
	 * @param defaultIncomeAccount the new default income account
	 */
	public void setDefaultIncomeAccount(String defaultIncomeAccount)
	{
		this.defaultIncomeAccount = defaultIncomeAccount;
		
	}
	
	/**
	 * Gets the default expense account.
	 *
	 * @return the default expense account
	 */
	public String getDefaultExpenseAccount()
	{
		return this.defaultExpenseAccount;
		
	}
	
	/**
	 * Sets the default expense account.
	 *
	 * @param defaultExpenseAccount the new default expense account
	 */
	public void setDefaultExpenseAccount(String defaultExpenseAccount)
	{
		this.defaultExpenseAccount = defaultExpenseAccount;
		
	}
	
	/**
	 * Checks if is auto number vouchers.
	 *
	 * @return true, if is auto number vouchers
	 */
	public boolean isAutoNumberVouchers()
	{
		return this.autoNumberVouchers;
		
	}
	
	/**
	 * Sets the auto number vouchers.
	 *
	 * @param autoNumberVouchers the new auto number vouchers
	 */
	public void setAutoNumberVouchers(boolean autoNumberVouchers)
	{
		this.autoNumberVouchers = autoNumberVouchers;
		
	}
	
	/**
	 * Checks if is autosave enabled.
	 *
	 * @return true, if is autosave enabled
	 */
	public boolean isAutosaveEnabled()
	{
		return this.autosaveEnabled;
		
	}
	
	/**
	 * Sets the autosave enabled.
	 *
	 * @param autosaveEnabled the new autosave enabled
	 */
	public void setAutosaveEnabled(boolean autosaveEnabled)
	{
		this.autosaveEnabled = autosaveEnabled;
		
	}
	
	/**
	 * Gets the autosave interval minutes.
	 *
	 * @return the autosave interval minutes
	 */
	public int getAutosaveIntervalMinutes()
	{
		return this.autosaveIntervalMinutes;
		
	}
	
	/**
	 * Sets the autosave interval minutes.
	 *
	 * @param autosaveIntervalMinutes the new autosave interval minutes
	 */
	public void setAutosaveIntervalMinutes(int autosaveIntervalMinutes)
	{
		this.autosaveIntervalMinutes = autosaveIntervalMinutes;
		
	}
	
	/**
	 * Gets the default directory.
	 *
	 * @return the default directory
	 */
	public String getDefaultDirectory()
	{
		return this.defaultDirectory;
		
	}
	
	/**
	 * Sets the default directory.
	 *
	 * @param defaultDirectory the new default directory
	 */
	public void setDefaultDirectory(String defaultDirectory)
	{
		this.defaultDirectory = defaultDirectory;
		
	}
	
	/**
	 * Gets the last opened file.
	 *
	 * @return the last opened file
	 */
	public String getLastOpenedFile()
	{
		return this.lastOpenedFile;
		
	}
	
	/**
	 * Sets the last opened file.
	 *
	 * @param lastOpenedFile the new last opened file
	 */
	public void setLastOpenedFile(String lastOpenedFile)
	{
		this.lastOpenedFile = lastOpenedFile;
		
	}
	
	/**
	 * Gets the default company directory.
	 *
	 * @return the default company directory
	 */
	public String getDefaultCompanyDirectory()
	{
		return this.defaultCompanyDirectory;
		
	}
	
	/**
	 * Sets the default company directory.
	 *
	 * @param defaultCompanyDirectory the new default company directory
	 */
	public void setDefaultCompanyDirectory(String defaultCompanyDirectory)
	{
		this.defaultCompanyDirectory = defaultCompanyDirectory;
		
	}
	
	/**
	 * Gets the last used company file.
	 *
	 * @return the last used company file
	 */
	public String getLastUsedCompanyFile()
	{
		return this.lastUsedCompanyFile;
		
	}
	
	/**
	 * Sets the last used company file.
	 *
	 * @param lastUsedCompanyFile the new last used company file
	 */
	public void setLastUsedCompanyFile(String lastUsedCompanyFile)
	{
		this.lastUsedCompanyFile = lastUsedCompanyFile;
		
	}
	
	/**
	 * Gets the default report period.
	 *
	 * @return the default report period
	 */
	public String getDefaultReportPeriod()
	{
		return this.defaultReportPeriod;
		
	}
	
	/**
	 * Sets the default report period.
	 *
	 * @param defaultReportPeriod the new default report period
	 */
	public void setDefaultReportPeriod(String defaultReportPeriod)
	{
		this.defaultReportPeriod = defaultReportPeriod;
		
	}
	
	/**
	 * Gets the default report year.
	 *
	 * @return the default report year
	 */
	public Integer getDefaultReportYear()
	{
		return this.defaultReportYear;
		
	}
	
	/**
	 * Sets the default report year.
	 *
	 * @param defaultReportYear the new default report year
	 */
	public void setDefaultReportYear(Integer defaultReportYear)
	{
		this.defaultReportYear = defaultReportYear;
		
	}
	
	/**
	 * Checks if is enable year to date option.
	 *
	 * @return true, if is enable year to date option
	 */
	public boolean isEnableYearToDateOption()
	{
		return this.enableYearToDateOption;
		
	}
	
	/**
	 * Sets the enable year to date option.
	 *
	 * @param enableYearToDateOption the new enable year to date option
	 */
	public void setEnableYearToDateOption(boolean enableYearToDateOption)
	{
		this.enableYearToDateOption = enableYearToDateOption;
		
	}
	
	/**
	 * Checks if is enable full year option.
	 *
	 * @return true, if is enable full year option
	 */
	public boolean isEnableFullYearOption()
	{
		return this.enableFullYearOption;
		
	}
	
	/**
	 * Sets the enable full year option.
	 *
	 * @param enableFullYearOption the new enable full year option
	 */
	public void setEnableFullYearOption(boolean enableFullYearOption)
	{
		this.enableFullYearOption = enableFullYearOption;
		
	}
	
	/**
	 * Checks if is enable last month option.
	 *
	 * @return true, if is enable last month option
	 */
	public boolean isEnableLastMonthOption()
	{
		return this.enableLastMonthOption;
		
	}
	
	/**
	 * Sets the enable last month option.
	 *
	 * @param enableLastMonthOption the new enable last month option
	 */
	public void setEnableLastMonthOption(boolean enableLastMonthOption)
	{
		this.enableLastMonthOption = enableLastMonthOption;
		
	}
	
	/**
	 * Gets the theme.
	 *
	 * @return the theme
	 */
	public String getTheme()
	{
		return this.theme;
		
	}
	
	/**
	 * Sets the theme.
	 *
	 * @param theme the new theme
	 */
	public void setTheme(String theme)
	{
		this.theme = theme;
		
	}
	
	/**
	 * Gets the language.
	 *
	 * @return the language
	 */
	public String getLanguage()
	{
		return this.language;
		
	}
	
	/**
	 * Sets the language.
	 *
	 * @param language the new language
	 */
	public void setLanguage(String language)
	{
		this.language = language;
		
	}

	/**
	 * Gets the pending row text color.
	 *
	 * @return the pending row text color
	 */
	public String getPendingRowTextColor()
	{
		return this.pendingRowTextColor;
		
	}

	/**
	 * Sets the pending row text color.
	 *
	 * @param pendingRowTextColor the new pending row text color
	 */
	public void setPendingRowTextColor(String pendingRowTextColor)
	{
		this.pendingRowTextColor = pendingRowTextColor;
		
	}
	
	/**
	 * Gets the currency format.
	 *
	 * @return the currency format
	 */
	public String getCurrencyFormat()
	{
		return this.currencyFormat;
		
	}
	
	/**
	 * Sets the currency format.
	 *
	 * @param currencyFormat the new currency format
	 */
	public void setCurrencyFormat(String currencyFormat)
	{
		this.currencyFormat = currencyFormat;
		
	}
	
	// ---------------------------------------------------------------------
	// Derived helpers
	// ---------------------------------------------------------------------
	
	
	/**
	 * Stores the locale to use for currency formatting using a {@link Locale} instance.
	 *
	 * @param locale locale to persist, {@code null} clears the preference
	 */
	public void setCurrencyLocale(Locale locale)
	{
		this.currencyLocaleTag =
			(locale == null) ? null : locale.toLanguageTag();
		
	}
	
	
	/**
	 * Gets the currency locale.
	 *
	 * @return the currency locale
	 */
	public Locale getCurrencyLocale()
	{
		
		if (this.currencyLocaleTag == null || this.currencyLocaleTag.isBlank())
		{
			return null;
		}
		
		return Locale.forLanguageTag(this.currencyLocaleTag);
		
	}
	
	/**
	 * Gets the fiscal year start month day.
	 *
	 * @return the fiscal year start month day
	 */
	public MonthDay getFiscalYearStartMonthDay()
	{
		
		if (this.fiscalYearStart == null || this.fiscalYearStart.isBlank())
		{
			return null;
		}
		
		try
		{
			return MonthDay.parse(this.fiscalYearStart);
		}
		catch (DateTimeParseException ex)
		{
			return null;
		}
		
	}
	
}
