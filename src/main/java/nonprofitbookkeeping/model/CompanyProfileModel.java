/**
 * 
 */
package nonprofitbookkeeping.model;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CompanyProfileModel implements Serializable
{
	/**
	 * serialVersionUID : long
	 */
	private static final long serialVersionUID = 2138956211273265518L;
	@JsonProperty private String companyName;
	@JsonProperty private String legalStructure;
	@JsonProperty private String taxId;
	@JsonProperty private String address;
	@JsonProperty private String phone;
	@JsonProperty private String email;
	
	@JsonProperty private String fiscalYearStart;
	@JsonProperty private String baseCurrency;
	@JsonProperty private String startingBalanceDate;
	@JsonProperty private String chartOfAccountsType;
	
	@JsonProperty private String adminUsername;
	@JsonProperty private String adminPassword;
	@JsonProperty private String defaultBankAccount;
	
	@JsonProperty private boolean enableFundAccounting;
	@JsonProperty private boolean enableInventory;
	@JsonProperty private boolean enableMultiCurrency;

	
	/**
	 * 
	 * Override @see java.lang.Object#toString()
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


	/**
	 * @return the companyName
	 */
	public String getCompanyName()
	{
		return this.companyName;
	}


	/**
	 * @param companyName the companyName to set
	 */
	public void setCompanyName(String companyName)
	{
		this.companyName = companyName;
	}


	/**
	 * @return the legalStructure
	 */
	public String getLegalStructure()
	{
		return this.legalStructure;
	}


	/**
	 * @param legalStructure the legalStructure to set
	 */
	public void setLegalStructure(String legalStructure)
	{
		this.legalStructure = legalStructure;
	}


	/**
	 * @return the taxId
	 */
	public String getTaxId()
	{
		return this.taxId;
	}


	/**
	 * @param taxId the taxId to set
	 */
	public void setTaxId(String taxId)
	{
		this.taxId = taxId;
	}


	/**
	 * @return the address
	 */
	public String getAddress()
	{
		return this.address;
	}


	/**
	 * @param address the address to set
	 */
	public void setAddress(String address)
	{
		this.address = address;
	}


	/**
	 * @return the phone
	 */
	public String getPhone()
	{
		return this.phone;
	}


	/**
	 * @param phone the phone to set
	 */
	public void setPhone(String phone)
	{
		this.phone = phone;
	}


	/**
	 * @return the email
	 */
	public String getEmail()
	{
		return this.email;
	}


	/**
	 * @param email the email to set
	 */
	public void setEmail(String email)
	{
		this.email = email;
	}


	/**
	 * @return the fiscalYearStart
	 */
	public String getFiscalYearStart()
	{
		return this.fiscalYearStart;
	}


	/**
	 * @param fiscalYearStart the fiscalYearStart to set
	 */
	public void setFiscalYearStart(String fiscalYearStart)
	{
		this.fiscalYearStart = fiscalYearStart;
	}


	/**
	 * @return the baseCurrency
	 */
	public String getBaseCurrency()
	{
		return this.baseCurrency;
	}


	/**
	 * @param baseCurrency the baseCurrency to set
	 */
	public void setBaseCurrency(String baseCurrency)
	{
		this.baseCurrency = baseCurrency;
	}


	/**
	 * @return the startingBalanceDate
	 */
	public String getStartingBalanceDate()
	{
		return this.startingBalanceDate;
	}


	/**
	 * @param startingBalanceDate the startingBalanceDate to set
	 */
	public void setStartingBalanceDate(String startingBalanceDate)
	{
		this.startingBalanceDate = startingBalanceDate;
	}


	/**
	 * @return the chartOfAccountsType
	 */
	public String getChartOfAccountsType()
	{
		return this.chartOfAccountsType;
	}


	/**
	 * @param chartOfAccountsType the chartOfAccountsType to set
	 */
	public void setChartOfAccountsType(String chartOfAccountsType)
	{
		this.chartOfAccountsType = chartOfAccountsType;
	}


	/**
	 * @return the adminUsername
	 */
	public String getAdminUsername()
	{
		return this.adminUsername;
	}


	/**
	 * @param adminUsername the adminUsername to set
	 */
	public void setAdminUsername(String adminUsername)
	{
		this.adminUsername = adminUsername;
	}


	/**
	 * @return the adminPassword
	 */
	public String getAdminPassword()
	{
		return this.adminPassword;
	}


	/**
	 * @param adminPassword the adminPassword to set
	 */
	public void setAdminPassword(String adminPassword)
	{
		this.adminPassword = adminPassword;
	}


	/**
	 * @return the defaultBankAccount
	 */
	public String getDefaultBankAccount()
	{
		return this.defaultBankAccount;
	}


	/**
	 * @param defaultBankAccount the defaultBankAccount to set
	 */
	public void setDefaultBankAccount(String defaultBankAccount)
	{
		this.defaultBankAccount = defaultBankAccount;
	}


	/**
	 * @return the enableFundAccounting
	 */
	public boolean isEnableFundAccounting()
	{
		return this.enableFundAccounting;
	}


	/**
	 * @param enableFundAccounting the enableFundAccounting to set
	 */
	public void setEnableFundAccounting(boolean enableFundAccounting)
	{
		this.enableFundAccounting = enableFundAccounting;
	}


	/**
	 * @return the enableInventory
	 */
	public boolean isEnableInventory()
	{
		return this.enableInventory;
	}


	/**
	 * @param enableInventory the enableInventory to set
	 */
	public void setEnableInventory(boolean enableInventory)
	{
		this.enableInventory = enableInventory;
	}


	/**
	 * @return the enableMultiCurrency
	 */
	public boolean isEnableMultiCurrency()
	{
		return this.enableMultiCurrency;
	}


	/**
	 * @param enableMultiCurrency the enableMultiCurrency to set
	 */
	public void setEnableMultiCurrency(boolean enableMultiCurrency)
	{
		this.enableMultiCurrency = enableMultiCurrency;
	}



	
}
