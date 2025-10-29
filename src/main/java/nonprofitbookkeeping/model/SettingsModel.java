package nonprofitbookkeeping.model;

import java.time.DateTimeException;
import java.time.MonthDay;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents the application settings model.
 * This class encapsulates various configuration options including company information,
 * user accounts, accounting defaults, and UI preferences.
 */
public class SettingsModel
{
        // Company Info
        /** The name of the organization. */
        @JsonProperty private String organizationName;
        /** The start date of the fiscal year (e.g., "MM-DD"). */
        @JsonProperty private String fiscalYearStart;
        /** The default currency code used in the application (e.g., "USD"). */
        @JsonProperty private String defaultCurrency;

        // User Accounts
        /** Configured application users. */
        @JsonProperty private List<User> users = new ArrayList<>();

        // Accounting Settings
        /** The default account number or name for income transactions. */
        @JsonProperty private String defaultIncomeAccount;
        /** The default account number or name for expense transactions. */
        @JsonProperty private String defaultExpenseAccount;
        /** Flag indicating whether vouchers/invoices should be auto-numbered. */
        @JsonProperty private boolean autoNumberVouchers;

        // Autosave and filesystem preferences
        /** Flag indicating whether background autosave is enabled. */
        @JsonProperty private boolean autosaveEnabled = true;
        /** Autosave interval, in minutes. A value of 0 disables autosave. */
        @JsonProperty private int autosaveIntervalMinutes = 5;
        /** Default directory presented in file choosers. */
        @JsonProperty private String defaultDirectory;
        /** Path to the most recently opened company file. */
        @JsonProperty private String lastOpenedFile;
        /** User-selected default directory for company data. */
        @JsonProperty private String defaultCompanyDirectory;
        /** Path to the most recently used company file. */
        @JsonProperty private String lastUsedCompanyFile;

        // Reporting defaults
        /** Preferred default period for reports and account detail filters. */
        @JsonProperty private String defaultReportPeriod =
                ReportPeriodPreset.YEAR_TO_DATE.name();
        /** Calendar year to use when {@link #defaultReportPeriod} is {@link ReportPeriodPreset#FISCAL_YEAR}. */
        @JsonProperty private Integer defaultReportYear;
        /** Whether the Year-To-Date period should be offered in report pickers. */
        @JsonProperty private boolean enableYearToDateOption = true;
        /** Whether the current fiscal year option should be available. */
        @JsonProperty private boolean enableFullYearOption = true;
        /** Whether the "Last Month" option should be available. */
        @JsonProperty private boolean enableLastMonthOption = true;

        // UI Preferences
        /** The name of the UI theme (e.g., "Dark", "Light"). */
        @JsonProperty private String theme;
        /** The language code for UI localization (e.g., "en_US", "fr_FR"). */
        @JsonProperty private String language;
        /** BCP 47 tag representing the locale used for currency formatting. */
        @JsonProperty("currencyLocale") private String currencyLocaleTag;
        /** Pattern used for formatting currency values (e.g., "$#,##0.00"). */
        @JsonProperty private String currencyFormat = "$#,##0.00";

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

                /** Default constructor for Jackson. */
                public User()
                {
                }

                /**
                 * Constructs a new User.
                 *
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
                 *
                 * @return The username.
                 */
                public String getUsername()
                {
                        return this.username;
                }

                /**
                 * Sets the username for this user.
                 *
                 * @param username The username to set.
                 */
                public void setUsername(String username)
                {
                        this.username = username;
                }

                /**
                 * Gets the role of this user.
                 *
                 * @return The user's role.
                 */
                public String getRole()
                {
                        return this.role;
                }

                /**
                 * Sets the role for this user.
                 *
                 * @param role The role to set.
                 */
                public void setRole(String role)
                {
                        this.role = role;
                }
        }

        /**
         * Gets the organization name.
         *
         * @return The name of the organization.
         */
        public String getOrganizationName()
        {
                return this.organizationName;
        }

        /**
         * Sets the organization name.
         *
         * @param name The name to set for the organization.
         */
        public void setOrganizationName(String name)
        {
                this.organizationName = name;
        }

        /**
         * Gets the fiscal year start date.
         *
         * @return The fiscal year start date string (e.g., "MM-DD").
         */
        public String getFiscalYearStart()
        {
                return this.fiscalYearStart;
        }

        /**
         * Sets the fiscal year start date.
         *
         * @param start The fiscal year start date string to set (e.g., "MM-DD").
         */
        public void setFiscalYearStart(String start)
        {
                this.fiscalYearStart = start;
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

                String sanitized = this.fiscalYearStart.trim().replace('/', '-');
                String[] parts = sanitized.split("-");

                if (parts.length != 2)
                {
                        return null;
                }

                try
                {
                        int month = Integer.parseInt(parts[0]);
                        int day = Integer.parseInt(parts[1]);
                        return MonthDay.of(month, day);
                }
                catch (NumberFormatException | DateTimeException ex)
                {
                        return null;
                }
        }

        /**
         * Gets the default currency code.
         *
         * @return The default currency code (e.g., "USD").
         */
        public String getDefaultCurrency()
        {
                return this.defaultCurrency;
        }

        /**
         * Sets the default currency code.
         *
         * @param defaultCurrency currency code to set.
         */
        public void setDefaultCurrency(String defaultCurrency)
        {
                this.defaultCurrency = defaultCurrency;
        }

        /**
         * Gets the configured application users.
         *
         * @return list of users.
         */
        public List<User> getUsers()
        {
                return this.users;
        }

        /**
         * Sets the configured application users.
         *
         * @param users list of users to store.
         */
        public void setUsers(List<User> users)
        {
                this.users = (users == null) ? new ArrayList<>() : new ArrayList<>(users);
        }

        /**
         * Gets the default income account identifier.
         *
         * @return income account identifier.
         */
        public String getDefaultIncomeAccount()
        {
                return this.defaultIncomeAccount;
        }

        /**
         * Sets the default income account identifier.
         *
         * @param defaultIncomeAccount income account identifier to store.
         */
        public void setDefaultIncomeAccount(String defaultIncomeAccount)
        {
                this.defaultIncomeAccount = defaultIncomeAccount;
        }

        /**
         * Gets the default expense account identifier.
         *
         * @return expense account identifier.
         */
        public String getDefaultExpenseAccount()
        {
                return this.defaultExpenseAccount;
        }

        /**
         * Sets the default expense account identifier.
         *
         * @param defaultExpenseAccount expense account identifier to store.
         */
        public void setDefaultExpenseAccount(String defaultExpenseAccount)
        {
                this.defaultExpenseAccount = defaultExpenseAccount;
        }

        /**
         * Indicates whether vouchers should be auto-numbered.
         *
         * @return {@code true} when auto-numbering is enabled; {@code false} otherwise.
         */
        public boolean isAutoNumberVouchers()
        {
                return this.autoNumberVouchers;
        }

        /**
         * Enables or disables voucher auto-numbering.
         *
         * @param autoNumberVouchers {@code true} to enable auto-numbering; {@code false} to disable.
         */
        public void setAutoNumberVouchers(boolean autoNumberVouchers)
        {
                this.autoNumberVouchers = autoNumberVouchers;
        }

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
         * Retrieves the default filesystem directory presented in choosers.
         *
         * @return directory path or {@code null} when unset.
         */
        public String getDefaultDirectory()
        {
                return this.defaultDirectory;
        }

        /**
         * Updates the default filesystem directory presented in choosers.
         *
         * @param defaultDirectory directory path to store.
         */
        public void setDefaultDirectory(String defaultDirectory)
        {
                this.defaultDirectory = defaultDirectory;
        }

        /**
         * Retrieves the last opened bookkeeping file.
         *
         * @return absolute path to the most recently opened file or {@code null}.
         */
        public String getLastOpenedFile()
        {
                return this.lastOpenedFile;
        }

        /**
         * Stores the most recently opened bookkeeping file path.
         *
         * @param lastOpenedFile absolute path to persist.
         */
        public void setLastOpenedFile(String lastOpenedFile)
        {
                this.lastOpenedFile = lastOpenedFile;
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
         * Retrieves the default report year used for fiscal year reporting.
         *
         * @return the default report year or {@code null}.
         */
        public Integer getDefaultReportYear()
        {
                return this.defaultReportYear;
        }

        /**
         * Sets the default report year used for fiscal year reporting.
         *
         * @param defaultReportYear the default report year to store.
         */
        public void setDefaultReportYear(Integer defaultReportYear)
        {
                this.defaultReportYear = defaultReportYear;
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
         * Gets the organization theme preference.
         *
         * @return theme identifier.
         */
        public String getTheme()
        {
                return this.theme;
        }

        /**
         * Sets the UI theme name.
         *
         * @param theme The name of the UI theme to set.
         */
        public void setTheme(String theme)
        {
                this.theme = theme;
        }

        /**
         * Gets the UI language code.
         *
         * @return The language code (e.g., "en_US").
         */
        public String getLanguage()
        {
                return this.language;
        }

        /**
         * Sets the UI language code.
         *
         * @param language The language code to set (e.g., "en_US").
         */
        public void setLanguage(String language)
        {
                this.language = language;
        }

        /**
         * Resolves the locale used for currency formatting preferences.
         *
         * @return configured {@link Locale}, or the system default when unspecified.
         */
        public Locale getCurrencyLocale()
        {
                if (this.currencyLocaleTag != null && !this.currencyLocaleTag.isBlank())
                {
                        return Locale.forLanguageTag(this.currencyLocaleTag.replace('_', '-'));
                }

                if (this.language != null && !this.language.isBlank())
                {
                        Locale fallback = Locale.forLanguageTag(this.language.replace('_', '-'));

                        if (!fallback.getLanguage().isEmpty())
                        {
                                return fallback;
                        }
                }

                return Locale.getDefault();
        }

        /**
         * Updates the locale preference for currency formatting using a string tag.
         *
         * @param localeTag locale identifier in IETF BCP 47 format.
         */
        public void setCurrencyLocale(String localeTag)
        {
                this.currencyLocaleTag = localeTag;
        }

        /**
         * Updates the locale preference for currency formatting using a {@link Locale}.
         *
         * @param locale {@link Locale} to persist; {@code null} clears the preference.
         */
        public void setCurrencyLocale(Locale locale)
        {
                this.currencyLocaleTag = (locale == null) ? null : locale.toLanguageTag();
        }

        /**
         * Gets the currency format pattern.
         *
         * @return pattern used to format currency values.
         */
        public String getCurrencyFormat()
        {
                return this.currencyFormat;
        }

        /**
         * Sets the currency format pattern.
         *
         * @param currencyFormat format pattern to set.
         */
        public void setCurrencyFormat(String currencyFormat)
        {
                this.currencyFormat = currencyFormat;
        }
}
