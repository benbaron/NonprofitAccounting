/**
 * 
 */
package nonprofitbookkeeping.model;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents the profile information for a company.
 * This includes contact details, financial year settings, administrative credentials,
 * and feature enablement flags.
 * Lombok's {@code @Data}, {@code @AllArgsConstructor}, and {@code @NoArgsConstructor}
 * are used for boilerplate code generation.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CompanyProfileModel implements Serializable
{
	/**
	 * The unique identifier for this serializable class.
	 */
	private static final long serialVersionUID = 2138956211273265518L;
	/** The official name of the company. */
	@JsonProperty private String companyName;
	/** The legal structure of the company (e.g., LLC, Corporation, Non-Profit). */
	@JsonProperty private String legalStructure;
	/** The company's tax identification number. */
	@JsonProperty private String taxId;
	/** The physical address of the company. */
	@JsonProperty private String address;
	/** The primary phone number for the company. */
	@JsonProperty private String phone;
	/** The primary email address for the company. */
	@JsonProperty private String email;
	
	/** The start date of the company's fiscal year (e.g., "MM-DD"). */
	@JsonProperty private String fiscalYearStart;
	/** The base currency used for accounting (e.g., "USD", "EUR"). */
	@JsonProperty private String baseCurrency;
	/** The date from which starting balances are recorded. */
	@JsonProperty private String startingBalanceDate;
	/** The type or template of the chart of accounts used (e.g., "Standard Non-Profit"). */
	@JsonProperty private String chartOfAccountsType;
	
	/** The username for the administrator account. */
	@JsonProperty private String adminUsername;
	/** The password for the administrator account. Note: Storing passwords in plain text is insecure. */
	@JsonProperty private String adminPassword;
	/** The default bank account number used for various operations. */
        @JsonProperty private String defaultBankAccount;
        /** Directory where the company file is stored. */
        @JsonProperty private String companyFileDir;
        /** Name of the company file including extension. */
        @JsonProperty private String companyFileName;
	
	/** Flag to indicate if fund accounting features are enabled. */
	@JsonProperty private boolean enableFundAccounting;
	/** Flag to indicate if inventory management features are enabled. */
	@JsonProperty private boolean enableInventory;
	/** Flag to indicate if multi-currency support is enabled. */
	@JsonProperty private boolean enableMultiCurrency;

	
	/**
	 * Returns a string representation of the CompanyProfileModel.
	 * Includes all fields of the class.
	 * Note: The adminPassword field is included in the output; consider redacting for security if logged.
	 * @return A string representation of this object.
	 */
	@Override public String toString()
	{
		return "CompanyProfileModel{" +
			"companyName='" + this.companyName + '\'' +
			", legalStructure='" + this.legalStructure + '\'' +
			", taxId='" + this.taxId + '\'' +
			", address='" + this.address + '\'' +
			", phone='" + this.phone + '\'' +
			", email='" + this.email + '\'' +
			", fiscalYearStart='" + this.fiscalYearStart + '\'' +
			", baseCurrency='" + this.baseCurrency + '\'' +
			", startingBalanceDate='" + this.startingBalanceDate + '\'' +
			", chartOfAccountsType='" + this.chartOfAccountsType + '\'' +
			", adminUsername='" + this.adminUsername + '\'' +
			", defaultBankAccount='" + this.defaultBankAccount + '\'' +
			", enableFundAccounting=" + this.enableFundAccounting +
			", enableInventory=" + this.enableInventory +
			", enableMultiCurrency=" + this.enableMultiCurrency +
			'}';
	}

	// Explicit getters and setters are generally not needed when using Lombok @Data,
	// but Javadoc is added here as they are present in the original code.
	// Lombok will generate these if they are removed.

	/**
	 * Gets the company name.
	 * @return The name of the company.
	 */
	public String getCompanyName()
	{
		return this.companyName;
	}


	/**
	 * Sets the company name.
	 * @param companyName The name to set for the company.
	 */
	public void setCompanyName(String companyName)
	{
		this.companyName = companyName;
	}


	/**
	 * Gets the legal structure of the company.
	 * @return The legal structure (e.g., LLC, Non-Profit).
	 */
	public String getLegalStructure()
	{
		return this.legalStructure;
	}


	/**
	 * Sets the legal structure of the company.
	 * @param legalStructure The legal structure to set.
	 */
	public void setLegalStructure(String legalStructure)
	{
		this.legalStructure = legalStructure;
	}


	/**
	 * Gets the company's tax identification number.
	 * @return The tax ID.
	 */
	public String getTaxId()
	{
		return this.taxId;
	}


	/**
	 * Sets the company's tax identification number.
	 * @param taxId The tax ID to set.
	 */
	public void setTaxId(String taxId)
	{
		this.taxId = taxId;
	}


	/**
	 * Gets the physical address of the company.
	 * @return The company address.
	 */
	public String getAddress()
	{
		return this.address;
	}


	/**
	 * Sets the physical address of the company.
	 * @param address The company address to set.
	 */
	public void setAddress(String address)
	{
		this.address = address;
	}


	/**
	 * Gets the primary phone number for the company.
	 * @return The company phone number.
	 */
	public String getPhone()
	{
		return this.phone;
	}


	/**
	 * Sets the primary phone number for the company.
	 * @param phone The company phone number to set.
	 */
	public void setPhone(String phone)
	{
		this.phone = phone;
	}


	/**
	 * Gets the primary email address for the company.
	 * @return The company email address.
	 */
	public String getEmail()
	{
		return this.email;
	}


	/**
	 * Sets the primary email address for the company.
	 * @param email The company email address to set.
	 */
	public void setEmail(String email)
	{
		this.email = email;
	}


	/**
	 * Gets the start date of the company's fiscal year.
	 * @return The fiscal year start date (e.g., "MM-DD").
	 */
	public String getFiscalYearStart()
	{
		return this.fiscalYearStart;
	}


	/**
	 * Sets the start date of the company's fiscal year.
	 * @param fiscalYearStart The fiscal year start date to set (e.g., "MM-DD").
	 */
	public void setFiscalYearStart(String fiscalYearStart)
	{
		this.fiscalYearStart = fiscalYearStart;
	}


	/**
	 * Gets the base currency used for accounting.
	 * @return The base currency code (e.g., "USD").
	 */
	public String getBaseCurrency()
	{
		return this.baseCurrency;
	}


	/**
	 * Sets the base currency used for accounting.
	 * @param baseCurrency The base currency code to set (e.g., "USD").
	 */
	public void setBaseCurrency(String baseCurrency)
	{
		this.baseCurrency = baseCurrency;
	}


	/**
	 * Gets the date from which starting balances are recorded.
	 * @return The starting balance date.
	 */
	public String getStartingBalanceDate()
	{
		return this.startingBalanceDate;
	}


	/**
	 * Sets the date from which starting balances are recorded.
	 * @param startingBalanceDate The starting balance date to set.
	 */
	public void setStartingBalanceDate(String startingBalanceDate)
	{
		this.startingBalanceDate = startingBalanceDate;
	}


	/**
	 * Gets the type or template of the chart of accounts used.
	 * @return The chart of accounts type.
	 */
	public String getChartOfAccountsType()
	{
		return this.chartOfAccountsType;
	}


	/**
	 * Sets the type or template of the chart of accounts used.
	 * @param chartOfAccountsType The chart of accounts type to set.
	 */
	public void setChartOfAccountsType(String chartOfAccountsType)
	{
		this.chartOfAccountsType = chartOfAccountsType;
	}


	/**
	 * Gets the username for the administrator account.
	 * @return The admin username.
	 */
	public String getAdminUsername()
	{
		return this.adminUsername;
	}


	/**
	 * Sets the username for the administrator account.
	 * @param adminUsername The admin username to set.
	 */
	public void setAdminUsername(String adminUsername)
	{
		this.adminUsername = adminUsername;
	}


	/**
	 * Gets the password for the administrator account.
	 * @return The admin password.
	 */
	public String getAdminPassword()
	{
		return this.adminPassword;
	}


	/**
	 * Sets the password for the administrator account.
	 * Note: Storing passwords directly should be avoided; consider hashed storage.
	 * @param adminPassword The admin password to set.
	 */
	public void setAdminPassword(String adminPassword)
	{
		this.adminPassword = adminPassword;
	}


	/**
	 * Gets the default bank account number used for various operations.
	 * @return The default bank account number.
	 */
	public String getDefaultBankAccount()
	{
		return this.defaultBankAccount;
	}


	/**
	 * Sets the default bank account number used for various operations.
	 * @param defaultBankAccount The default bank account number to set.
	 */
	public void setDefaultBankAccount(String defaultBankAccount)
	{
		this.defaultBankAccount = defaultBankAccount;
	}


	/**
	 * Checks if fund accounting features are enabled.
	 * @return {@code true} if fund accounting is enabled, {@code false} otherwise.
	 */
	public boolean isEnableFundAccounting()
	{
		return this.enableFundAccounting;
	}


	/**
	 * Sets whether fund accounting features are enabled.
	 * @param enableFundAccounting {@code true} to enable, {@code false} to disable.
	 */
	public void setEnableFundAccounting(boolean enableFundAccounting)
	{
		this.enableFundAccounting = enableFundAccounting;
	}


	/**
	 * Checks if inventory management features are enabled.
	 * @return {@code true} if inventory management is enabled, {@code false} otherwise.
	 */
	public boolean isEnableInventory()
	{
		return this.enableInventory;
	}


	/**
	 * Sets whether inventory management features are enabled.
	 * @param enableInventory {@code true} to enable, {@code false} to disable.
	 */
	public void setEnableInventory(boolean enableInventory)
	{
		this.enableInventory = enableInventory;
	}


	/**
	 * Checks if multi-currency support is enabled.
	 * @return {@code true} if multi-currency support is enabled, {@code false} otherwise.
	 */
	public boolean isEnableMultiCurrency()
	{
		return this.enableMultiCurrency;
	}


	/**
	 * Sets whether multi-currency support is enabled.
	 * @param enableMultiCurrency {@code true} to enable, {@code false} to disable.
	 */
	public void setEnableMultiCurrency(boolean enableMultiCurrency)
	{
		this.enableMultiCurrency = enableMultiCurrency;
	}

	/**
	 * @param absolutePath
	 */
        public void setCompanyFileDir(String absolutePath)
        {
                this.companyFileDir = absolutePath;

        }

	/**
	 * @param string
	 */
        public void setCompanyFileName(String string)
        {
                this.companyFileName = string;

        }

	/**
	 * @return
	 */
        public String getCompanyFileName()
        {
                return this.companyFileName;
        }

        public String getCompanyFileDir()
        {
                return this.companyFileDir;
        }


}
