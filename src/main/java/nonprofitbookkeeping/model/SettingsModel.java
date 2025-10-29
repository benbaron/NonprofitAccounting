package nonprofitbookkeeping.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.MonthDay;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

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
        @JsonProperty private String defaultReportPeriod = ReportPeriodPreset.YEAR_TO_DATE.name();
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
                        this.username = username;
                        this.role = role;
                }
        }

        // ---------------------------------------------------------------------
        // Basic getters and setters
        // ---------------------------------------------------------------------

        public String getOrganizationName()
        {
                return this.organizationName;
        }

        public void setOrganizationName(String organizationName)
        {
                this.organizationName = organizationName;
        }

        public String getFiscalYearStart()
        {
                return this.fiscalYearStart;
        }

        public void setFiscalYearStart(String fiscalYearStart)
        {
                this.fiscalYearStart = fiscalYearStart;
        }

        public String getDefaultCurrency()
        {
                return this.defaultCurrency;
        }

        public void setDefaultCurrency(String defaultCurrency)
        {
                this.defaultCurrency = defaultCurrency;
        }

        public List<User> getUsers()
        {
                return this.users;
        }

        public void setUsers(List<User> users)
        {
                this.users = (users == null) ? new ArrayList<>() : new ArrayList<>(users);
        }

        public String getDefaultIncomeAccount()
        {
                return this.defaultIncomeAccount;
        }

        public void setDefaultIncomeAccount(String defaultIncomeAccount)
        {
                this.defaultIncomeAccount = defaultIncomeAccount;
        }

        public String getDefaultExpenseAccount()
        {
                return this.defaultExpenseAccount;
        }

        public void setDefaultExpenseAccount(String defaultExpenseAccount)
        {
                this.defaultExpenseAccount = defaultExpenseAccount;
        }

        public boolean isAutoNumberVouchers()
        {
                return this.autoNumberVouchers;
        }

        public void setAutoNumberVouchers(boolean autoNumberVouchers)
        {
                this.autoNumberVouchers = autoNumberVouchers;
        }

        public boolean isAutosaveEnabled()
        {
                return this.autosaveEnabled;
        }

        public void setAutosaveEnabled(boolean autosaveEnabled)
        {
                this.autosaveEnabled = autosaveEnabled;
        }

        public int getAutosaveIntervalMinutes()
        {
                return this.autosaveIntervalMinutes;
        }

        public void setAutosaveIntervalMinutes(int autosaveIntervalMinutes)
        {
                this.autosaveIntervalMinutes = autosaveIntervalMinutes;
        }

        public String getDefaultDirectory()
        {
                return this.defaultDirectory;
        }

        public void setDefaultDirectory(String defaultDirectory)
        {
                this.defaultDirectory = defaultDirectory;
        }

        public String getLastOpenedFile()
        {
                return this.lastOpenedFile;
        }

        public void setLastOpenedFile(String lastOpenedFile)
        {
                this.lastOpenedFile = lastOpenedFile;
        }

        public String getDefaultCompanyDirectory()
        {
                return this.defaultCompanyDirectory;
        }

        public void setDefaultCompanyDirectory(String defaultCompanyDirectory)
        {
                this.defaultCompanyDirectory = defaultCompanyDirectory;
        }

        public String getLastUsedCompanyFile()
        {
                return this.lastUsedCompanyFile;
        }

        public void setLastUsedCompanyFile(String lastUsedCompanyFile)
        {
                this.lastUsedCompanyFile = lastUsedCompanyFile;
        }

        public String getDefaultReportPeriod()
        {
                return this.defaultReportPeriod;
        }

        public void setDefaultReportPeriod(String defaultReportPeriod)
        {
                this.defaultReportPeriod = defaultReportPeriod;
        }

        public Integer getDefaultReportYear()
        {
                return this.defaultReportYear;
        }

        public void setDefaultReportYear(Integer defaultReportYear)
        {
                this.defaultReportYear = defaultReportYear;
        }

        public boolean isEnableYearToDateOption()
        {
                return this.enableYearToDateOption;
        }

        public void setEnableYearToDateOption(boolean enableYearToDateOption)
        {
                this.enableYearToDateOption = enableYearToDateOption;
        }

        public boolean isEnableFullYearOption()
        {
                return this.enableFullYearOption;
        }

        public void setEnableFullYearOption(boolean enableFullYearOption)
        {
                this.enableFullYearOption = enableFullYearOption;
        }

        public boolean isEnableLastMonthOption()
        {
                return this.enableLastMonthOption;
        }

        public void setEnableLastMonthOption(boolean enableLastMonthOption)
        {
                this.enableLastMonthOption = enableLastMonthOption;
        }

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

        public String getCurrencyFormat()
        {
                return this.currencyFormat;
        }

        public void setCurrencyFormat(String currencyFormat)
        {
                this.currencyFormat = currencyFormat;
        }

        // ---------------------------------------------------------------------
        // Derived helpers
        // ---------------------------------------------------------------------

        /**
         * Resolves the locale to use for currency formatting.
         *
         * @return configured {@link Locale} or the JVM default when unset.
         */
        public String getFiscalYearStart()
        {
                if (this.currencyLocaleTag != null && !this.currencyLocaleTag.isBlank())
                {
                        return Locale.forLanguageTag(this.currencyLocaleTag.replace('_', '-'));
                }

                if (this.language != null && !this.language.isBlank())
                {
                        return Locale.forLanguageTag(this.language.replace('_', '-'));
                }

                return Locale.getDefault();
        }

        /**
         * Stores the locale to use for currency formatting using a {@link Locale} instance.
         *
         * @param locale locale to persist, {@code null} clears the preference
         */
        public void setCurrencyLocale(Locale locale)
        {
                this.currencyLocaleTag = (locale == null) ? null : locale.toLanguageTag();
        }

        /**
         * Stores the locale to use for currency formatting using a language tag.
         *
         * @param localeTag language tag (e.g. {@code en-US}) to persist
         */
        public void setCurrencyLocale(String localeTag)
        {
                this.currencyLocaleTag = localeTag;
        }

        /**
         * Returns the start of the fiscal year as a {@link MonthDay}.
         *
         * @return parsed month/day or {@code null} when parsing fails or not provided
         */
        public void setCurrencyLocale(String localeTag)
        {
                if (this.fiscalYearStart == null || this.fiscalYearStart.isBlank())
                {
                        return null;
                }

                String normalized = this.fiscalYearStart.trim().replace('/', '-');

                if (!normalized.startsWith("--"))
                {
                        normalized = "--" + normalized;
                }

                try
                {
                        return MonthDay.parse(normalized);
                }
                catch (DateTimeParseException ex)
                {
                        return null;
                }
        }
}
