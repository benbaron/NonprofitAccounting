
package nonprofitbookkeeping.model;

import java.util.ArrayList;
import java.util.List;

import java.time.MonthDay;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import nonprofitbookkeeping.model.ReportPeriodPreset;

/**
 * Represents the application settings model.
 * This class encapsulates various configuration options including company information,
 * user accounts, accounting defaults, and UI preferences.
 * Lombok's {@code @Data}, {@code @AllArgsConstructor}, and {@code @NoArgsConstructor}
 * are used for boilerplate code generation.
 */
@Data @AllArgsConstructor @NoArgsConstructor public class SettingsModel
{
	// Company Info
	/** The name of the organization. */
	@JsonProperty private String organizationName;
	/** The start date of the fiscal year (e.g., "MM-DD"). */
	@JsonProperty private String fiscalYearStart;
	/** The default currency code used in the application (e.g., "USD"). */
	@JsonProperty private String defaultCurrency;
	
	// User Accounts
	/** A list of user accounts configured in the system. */
	@JsonProperty private List<User> users = new ArrayList<>();
	
	// Accounting Settings
	/** The default account number or name for income transactions. */
	@JsonProperty private String defaultIncomeAccount;
	/** The default account number or name for expense transactions. */
	@JsonProperty private String defaultExpenseAccount;
	/** Flag indicating whether vouchers/invoices should be auto-numbered. */
	@JsonProperty private boolean autoNumberVouchers;
	
	// UI Preferences
        /** The name of the UI theme (e.g., "Dark", "Light"). */
        @JsonProperty private String theme;
        /** The language code for UI localization (e.g., "en_US", "fr_FR"). */
        @JsonProperty private String language;
       /** Pattern used for formatting currency values (e.g., "$#,##0.00"). */
       @JsonProperty private String currencyFormat = "$#,##0.00";

       // Application behaviour
       /** Flag indicating whether background autosave is enabled. */
       @JsonProperty private boolean autosaveEnabled = true;
       /** Interval, in minutes, between background autosave executions. */
       @JsonProperty private int autosaveIntervalMinutes = 5;
       /** User-selected default directory for company data. */
       @JsonProperty private String defaultCompanyDirectory;
       /** Path to the most recently used company file. */
       @JsonProperty private String lastUsedCompanyFile;

       // Reporting preferences
       /** Default report period selection applied across dashboards. */
       @JsonProperty private String defaultReportPeriod = ReportPeriodPreset.YEAR_TO_DATE.name();
       /** Whether the Year-To-Date period should be offered in report pickers. */
       @JsonProperty private boolean enableYearToDateOption = true;
       /** Whether the current fiscal year option should be available. */
       @JsonProperty private boolean enableFullYearOption = true;
       /** Whether the "Last Month" option should be available. */
       @JsonProperty private boolean enableLastMonthOption = true;
	
	/**
	 * Represents a user account within the settings model.
	 * Stores username and role.
	 */
	public static class User
	{
		/** The username for the user account. */
		private String username;
		/** The role assigned to the user (e.g., "Admin", "User"). */
		private String role;
		
		/**
		 * Constructs a new User.
		 * @param username The username for this user.
		 * @param role The role assigned to this user.
		 */
		public User(String username, String role)
		{
			this.username = username;
			this.role = role;
		}
		
		/**
		 * Gets the username of this user.
		 * @return The username.
		 */
		public String getUsername()
		{
			return this.username;
		}
		
		/**
		 * Sets the username for this user.
		 * @param username The username to set.
		 */
		public void setUsername(String username)
		{
			this.username = username;
		}
		
		/**
		 * Gets the role of this user.
		 * @return The user's role.
		 */
		public String getRole()
		{
			return this.role;
		}
		
		/**
		 * Sets the role for this user.
		 * @param role The role to set.
		 */
                public void setRole(String role)
                {
                        this.role = role;
                }

        }

        // Explicit Getters/Setters below are mostly redundant due to Lombok @Data
        // but are documented as they exist in the original code.

        /**
         * Indicates whether autosave is enabled.
         *
         * @return {@code true} when background autosave is enabled; {@code false} otherwise.
         */
        public boolean isAutosaveEnabled()
        {
                return this.autosaveEnabled;
        }

        /**
         * Enables or disables the autosave feature.
         *
         * @param autosaveEnabled {@code true} to enable autosave; {@code false} to disable it.
         */
        public void setAutosaveEnabled(boolean autosaveEnabled)
        {
                this.autosaveEnabled = autosaveEnabled;
        }

        /**
         * Retrieves the autosave interval in minutes.
         *
         * @return the number of minutes between autosave executions.
         */
        public int getAutosaveIntervalMinutes()
        {
                return this.autosaveIntervalMinutes;
        }

        /**
         * Updates the autosave interval.
         *
         * @param autosaveIntervalMinutes interval, in minutes, between autosave executions.
         */
        public void setAutosaveIntervalMinutes(int autosaveIntervalMinutes)
        {
                this.autosaveIntervalMinutes = autosaveIntervalMinutes;
        }

        /**
         * Returns the default company directory path configured by the user.
         *
         * @return the default company directory path or {@code null} when unset.
         */
        public String getDefaultCompanyDirectory()
        {
                return this.defaultCompanyDirectory;
        }

        /**
         * Sets the default company directory path.
         *
         * @param defaultCompanyDirectory directory path to use by default when opening/saving
         *                               company files.
         */
        public void setDefaultCompanyDirectory(String defaultCompanyDirectory)
        {
                this.defaultCompanyDirectory = defaultCompanyDirectory;
        }

        /**
         * Retrieves the last used company file path.
         *
         * @return the last used company file path or {@code null} when no history exists.
         */
        public String getLastUsedCompanyFile()
        {
                return this.lastUsedCompanyFile;
        }

        /**
         * Persists the path to the last used company file.
         *
         * @param lastUsedCompanyFile path to the most recently opened company file.
         */
        public void setLastUsedCompanyFile(String lastUsedCompanyFile)
        {
                this.lastUsedCompanyFile = lastUsedCompanyFile;
        }

        /**
         * Provides the default report period selection.
         *
         * @return the identifier of the default report period preset.
         */
        public String getDefaultReportPeriod()
        {
                return this.defaultReportPeriod;
        }

        /**
         * Updates the default report period selection.
         *
         * @param defaultReportPeriod identifier of the desired default report period preset.
         */
        public void setDefaultReportPeriod(String defaultReportPeriod)
        {
                this.defaultReportPeriod = defaultReportPeriod;
        }

        /**
         * Indicates whether the "Year-To-Date" report preset is available.
         *
         * @return {@code true} when the preset is offered; {@code false} otherwise.
         */
        public boolean isEnableYearToDateOption()
        {
                return this.enableYearToDateOption;
        }

        /**
         * Enables or disables the "Year-To-Date" report preset.
         *
         * @param enableYearToDateOption {@code true} to enable the preset; {@code false} otherwise.
         */
        public void setEnableYearToDateOption(boolean enableYearToDateOption)
        {
                this.enableYearToDateOption = enableYearToDateOption;
        }

        /**
         * Indicates whether the "Full Year" report preset is available.
         *
         * @return {@code true} when enabled; {@code false} otherwise.
         */
        public boolean isEnableFullYearOption()
        {
                return this.enableFullYearOption;
        }

        /**
         * Enables or disables the "Full Year" report preset option.
         *
         * @param enableFullYearOption {@code true} to enable; {@code false} otherwise.
         */
        public void setEnableFullYearOption(boolean enableFullYearOption)
        {
                this.enableFullYearOption = enableFullYearOption;
        }

        /**
         * Indicates whether the "Last Month" report preset is available.
         *
         * @return {@code true} when enabled; {@code false} otherwise.
         */
        public boolean isEnableLastMonthOption()
        {
                return this.enableLastMonthOption;
        }

        /**
         * Enables or disables the "Last Month" report preset option.
         *
         * @param enableLastMonthOption {@code true} to enable; {@code false} otherwise.
         */
        public void setEnableLastMonthOption(boolean enableLastMonthOption)
        {
                this.enableLastMonthOption = enableLastMonthOption;
        }

        /**
         * Gets the organization name.
         * @return The name of the organization.
         */
        public String getOrganizationName()
	{
		return this.organizationName;
	}
	
	/**
	 * Sets the organization name.
	 * @param name The name to set for the organization.
	 */
	public void setOrganizationName(String name)
	{
		this.organizationName = name;
	}
	
	/**
	 * Gets the fiscal year start date.
	 * @return The fiscal year start date string (e.g., "MM-DD").
	 */
	public String getFiscalYearStart()
	{
		return this.fiscalYearStart;
	}
	
	/**
	 * Sets the fiscal year start date.
	 * @param start The fiscal year start date string to set (e.g., "MM-DD").
	 */
	public void setFiscalYearStart(String start)
	{
		this.fiscalYearStart = start;
	}
	
        /**
         * Gets the default currency code.
         * @return The default currency code (e.g., "USD").
         */
        public String getDefaultCurrency()
        {
                return this.defaultCurrency;
        }

        /**
         * Returns the fiscal year start as a {@link MonthDay} when possible.
         *
         * @return month/day representation of the fiscal year start or {@code null}
         *         when the stored value cannot be parsed.
         */
        public MonthDay getFiscalYearStartMonthDay()
        {
                if (this.fiscalYearStart == null || this.fiscalYearStart.isBlank())
                {
                        return null;
                }

                try
                {
                        return MonthDay.parse(this.fiscalYearStart,
                                java.time.format.DateTimeFormatter.ofPattern("MM-dd"));
                }
                catch (Exception ex)
                {
                        return null;
                }
        }
	
	/**
	 * Sets the default currency code.
	 * @param currency The default currency code to set (e.g., "USD").
	 */
	public void setDefaultCurrency(String currency)
	{
		this.defaultCurrency = currency;
	}
	
	/**
	 * Gets the list of configured user accounts.
	 * @return A list of {@link User} objects.
	 */
	public List<User> getUsers()
	{
		return this.users;
	}
	
	/**
	 * Sets the list of configured user accounts.
	 * @param users A list of {@link User} objects to set.
	 */
	public void setUsers(List<User> users)
	{
		this.users = users;
	}
	
	/**
	 * Gets the default income account.
	 * @return The default income account identifier.
	 */
	public String getDefaultIncomeAccount()
	{
		return this.defaultIncomeAccount;
	}
	
	/**
	 * Sets the default income account.
	 * @param incomeAccount The default income account identifier to set.
	 */
	public void setDefaultIncomeAccount(String incomeAccount)
	{
		this.defaultIncomeAccount = incomeAccount;
	}
	
	/**
	 * Gets the default expense account.
	 * @return The default expense account identifier.
	 */
	public String getDefaultExpenseAccount()
	{
		return this.defaultExpenseAccount;
	}
	
	/**
	 * Sets the default expense account.
	 * @param expenseAccount The default expense account identifier to set.
	 */
	public void setDefaultExpenseAccount(String expenseAccount)
	{
		this.defaultExpenseAccount = expenseAccount;
	}
	
	/**
	 * Checks if auto-numbering for vouchers is enabled.
	 * @return {@code true} if auto-numbering is enabled, {@code false} otherwise.
	 */
	public boolean isAutoNumberVouchers()
	{
		return this.autoNumberVouchers;
	}
	
	/**
	 * Sets whether auto-numbering for vouchers is enabled.
	 * @param autoNumberVouchers {@code true} to enable, {@code false} to disable.
	 */
	public void setAutoNumberVouchers(boolean autoNumberVouchers)
	{
		this.autoNumberVouchers = autoNumberVouchers;
	}
	
	/**
	 * Gets the UI theme name.
	 * @return The name of the current UI theme.
	 */
	public String getTheme()
	{
		return this.theme;
	}
	
	/**
	 * Sets the UI theme name.
	 * @param theme The name of the UI theme to set.
	 */
	public void setTheme(String theme)
	{
		this.theme = theme;
	}
	
	/**
	 * Gets the UI language code.
	 * @return The language code (e.g., "en_US").
	 */
	public String getLanguage()
	{
		return this.language;
	}
	
	/**
	 * Sets the UI language code.
	 * @param language The language code to set (e.g., "en_US").
	 */
        public void setLanguage(String language)
        {
                this.language = language;
        }

       /**
        * Gets the currency format pattern.
        *
        * @return pattern used to format currency values
        */
       public String getCurrencyFormat()
       {
               return this.currencyFormat;
       }

       /**
        * Sets the currency format pattern.
        *
        * @param currencyFormat format pattern to set
        */
       public void setCurrencyFormat(String currencyFormat)
       {
               this.currencyFormat = currencyFormat;
       }

       /**
        * Indicates whether autosave is enabled.
        *
        * @return {@code true} if autosave is enabled, otherwise {@code false}.
        */
       public boolean isAutosaveEnabled()
       {
               return this.autosaveEnabled;
       }

       /**
        * Enables or disables autosave.
        *
        * @param autosaveEnabled {@code true} to enable autosave; {@code false} to disable.
        */
       public void setAutosaveEnabled(boolean autosaveEnabled)
       {
               this.autosaveEnabled = autosaveEnabled;
       }

       /**
        * Gets the autosave interval in minutes.
        *
        * @return autosave interval expressed in minutes.
        */
       public int getAutosaveIntervalMinutes()
       {
               return this.autosaveIntervalMinutes;
       }

       /**
        * Sets the autosave interval in minutes.
        *
        * @param autosaveIntervalMinutes the interval in minutes to use for autosave.
        */
       public void setAutosaveIntervalMinutes(int autosaveIntervalMinutes)
       {
               this.autosaveIntervalMinutes = autosaveIntervalMinutes;
       }

       /**
        * Gets the default company directory path.
        *
        * @return directory path used when opening or creating companies.
        */
       public String getDefaultCompanyDirectory()
       {
               return this.defaultCompanyDirectory;
       }

       /**
        * Sets the default company directory path.
        *
        * @param defaultCompanyDirectory directory path to set.
        */
       public void setDefaultCompanyDirectory(String defaultCompanyDirectory)
       {
               this.defaultCompanyDirectory = defaultCompanyDirectory;
       }

       /**
        * Gets the path of the last used company file.
        *
        * @return path to the last company file opened by the user.
        */
       public String getLastUsedCompanyFile()
       {
               return this.lastUsedCompanyFile;
       }

       /**
        * Sets the path of the last used company file.
        *
        * @param lastUsedCompanyFile company file path to set.
        */
       public void setLastUsedCompanyFile(String lastUsedCompanyFile)
       {
               this.lastUsedCompanyFile = lastUsedCompanyFile;
       }

       /**
        * Gets the default report period preset identifier.
        *
        * @return default report period preset.
        */
       public String getDefaultReportPeriod()
       {
               return this.defaultReportPeriod;
       }

       /**
        * Sets the default report period preset identifier.
        *
        * @param defaultReportPeriod preset identifier to set.
        */
       public void setDefaultReportPeriod(String defaultReportPeriod)
       {
               this.defaultReportPeriod = defaultReportPeriod;
       }

       /**
        * Indicates whether the Year-To-Date option is available in report filters.
        *
        * @return {@code true} when enabled; otherwise {@code false}.
        */
       public boolean isEnableYearToDateOption()
       {
               return this.enableYearToDateOption;
       }

       /**
        * Enables or disables the Year-To-Date option in report filters.
        *
        * @param enableYearToDateOption {@code true} to enable the option; otherwise {@code false}.
        */
       public void setEnableYearToDateOption(boolean enableYearToDateOption)
       {
               this.enableYearToDateOption = enableYearToDateOption;
       }

       /**
        * Indicates whether the full year option is available in report filters.
        *
        * @return {@code true} when enabled; otherwise {@code false}.
        */
       public boolean isEnableFullYearOption()
       {
               return this.enableFullYearOption;
       }

       /**
        * Enables or disables the full year option in report filters.
        *
        * @param enableFullYearOption {@code true} to enable the option; otherwise {@code false}.
        */
       public void setEnableFullYearOption(boolean enableFullYearOption)
       {
               this.enableFullYearOption = enableFullYearOption;
       }

       /**
        * Indicates whether the last month option is available in report filters.
        *
        * @return {@code true} when enabled; otherwise {@code false}.
        */
       public boolean isEnableLastMonthOption()
       {
               return this.enableLastMonthOption;
       }

       /**
        * Enables or disables the last month option in report filters.
        *
        * @param enableLastMonthOption {@code true} to enable the option; otherwise {@code false}.
        */
       public void setEnableLastMonthOption(boolean enableLastMonthOption)
       {
               this.enableLastMonthOption = enableLastMonthOption;
       }
	
}
