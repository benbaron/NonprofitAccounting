
package nonprofitbookkeeping.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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
        /** ISO language tag describing the locale used for currency formatting. */
        @JsonProperty private String currencyLocale =
                Locale.getDefault(Locale.Category.FORMAT).toLanguageTag();

        // File system preferences
        /** Default directory presented in file chooser dialogs. */
        @JsonProperty private String defaultDirectory;
        /** Tracks the most recently opened bookkeeping data file. */
        @JsonProperty private String lastOpenedFile;

        // Reporting preferences
        /** Preferred period used when opening report dialogs. */
        @JsonProperty private DefaultReportPeriod defaultReportPeriod =
                DefaultReportPeriod.CURRENT_MONTH;
	
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
         * Returns the locale that should be used for currency values. The locale is stored
         * internally as an IETF BCP 47 language tag so that it can be easily serialised.
         * When the locale has not been explicitly configured the system default format locale
         * is returned.
         *
         * @return locale describing the preferred currency formatting conventions
         */
        public Locale getCurrencyLocale()
        {
                if (this.currencyLocale == null || this.currencyLocale.isBlank())
                {
                        return Locale.getDefault(Locale.Category.FORMAT);
                }
                return Locale.forLanguageTag(this.currencyLocale);
        }

        /**
         * Sets the preferred locale for currency formatting.
         *
         * @param locale new locale to store
         */
        public void setCurrencyLocale(Locale locale)
        {
                this.currencyLocale =
                        (locale == null) ? null : locale.toLanguageTag();
        }

        /**
         * Returns the stored language tag used for persistence of the currency locale.
         *
         * @return language tag or {@code null} if not set
         */
        public String getCurrencyLocaleTag()
        {
                return this.currencyLocale;
        }

        /**
         * Updates the stored language tag representing the currency locale.
         *
         * @param localeTag new locale tag to persist
         */
        public void setCurrencyLocaleTag(String localeTag)
        {
                this.currencyLocale = localeTag;
        }

        /**
         * Gets the default directory presented to the user when opening or saving data files.
         *
         * @return configured default directory or {@code null}
         */
        public String getDefaultDirectory()
        {
                return this.defaultDirectory;
        }

        /**
         * Sets the default directory used by file choosers.
         *
         * @param defaultDirectory directory path to remember
         */
        public void setDefaultDirectory(String defaultDirectory)
        {
                this.defaultDirectory = defaultDirectory;
        }

        /**
         * Gets the most recently opened bookkeeping file path.
         *
         * @return last opened file path or {@code null} if no history is stored
         */
        public String getLastOpenedFile()
        {
                return this.lastOpenedFile;
        }

        /**
         * Remembers the most recently opened bookkeeping file.
         *
         * @param lastOpenedFile file path to persist
         */
        public void setLastOpenedFile(String lastOpenedFile)
        {
                this.lastOpenedFile = lastOpenedFile;
        }

        /**
         * Retrieves the preferred default reporting period selection for the UI.
         *
         * @return configured default report period, never {@code null}
         */
        public DefaultReportPeriod getDefaultReportPeriod()
        {
                return (this.defaultReportPeriod == null)
                        ? DefaultReportPeriod.CURRENT_MONTH
                        : this.defaultReportPeriod;
        }

        /**
         * Sets the default report period selection used by the UI.
         *
         * @param defaultReportPeriod period to store
         */
        public void setDefaultReportPeriod(DefaultReportPeriod defaultReportPeriod)
        {
                this.defaultReportPeriod = defaultReportPeriod;
        }

}
