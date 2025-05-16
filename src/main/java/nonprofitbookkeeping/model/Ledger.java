
package nonprofitbookkeeping.model;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Represents a set of accounts and their transactions.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
final public class Ledger implements Serializable
{
	/**
	 * serialVersionUID : long
	 */
	private static final long serialVersionUID = 8752049840895321935L;

	final private Journal journal = new Journal();
	private ChartOfAccounts coa;
	public CompanyProfileModel companyProfile;
	
	/**  
	 * Constructor Ledger
	 */
	public Ledger()
	{
		this.coa = null;
	}
	
	public List<AccountingTransaction>getTransactions()
	{
		return this.journal.getJournalTransactions();
	}

	/**
	 * @return
	 */
	@JsonIgnore
	public static List<String> getAccountNames()
	{
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @return
	 */
	public ChartOfAccounts getCoa()
	{
		// TODO Auto-generated method stub
		return this.coa;
	}

	/**
	 * @return the companyProfile
	 */
	public CompanyProfileModel getCompanyProfile()
	{
		return this.companyProfile;
	}

	/**
	 * @param companyProfile the companyProfile to set
	 */
	public void setCompanyProfile(CompanyProfileModel companyProfile)
	{
		this.companyProfile = companyProfile;
	}

	/**
	 * @return the journal
	 */
	public Journal getJournal()
	{
		return this.journal;
	}

	/**
	 * @param coa the coa to set
	 */
	public void setCoa(ChartOfAccounts coa)
	{
		this.coa = coa;
	}
	
}
