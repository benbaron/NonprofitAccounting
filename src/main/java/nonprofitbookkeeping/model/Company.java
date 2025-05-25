
package nonprofitbookkeeping.model;

import java.io.Serializable;
import com.fasterxml.jackson.annotation.JsonProperty;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Company
 */
public class Company implements Serializable
{
	/**
	 * serialVersionUID : long
	 */
	private static final long serialVersionUID = 6728014646115467637L;
	
	@JsonProperty private CompanyProfileModel companyProfileModel = new CompanyProfileModel();
	@JsonProperty private Ledger ledger = new Ledger();
	@JsonProperty private ChartOfAccounts chartOfAccounts = new ChartOfAccounts();
	
	/**
	 * 
	 * Constructor Company
	 */
	public Company()
	{			
		this.companyProfileModel = new CompanyProfileModel();
		this.ledger = new Ledger();
		this.chartOfAccounts = new ChartOfAccounts();
	}
	
	/**
	 * @return the companyProfileModel
	 */
	public CompanyProfileModel getCompanyProfile()
	{
		return this.companyProfileModel;
	}
	
	
	/**
	 * @return the ledger
	 */
	public Ledger getLedger()
	{
		return this.ledger;
	}
	
	/**
	 * @param ledger the ledger to set
	 */
	public void setLedger(Ledger ledger)
	{
		this.ledger = checkNotNull(ledger);
	}
	
	/**
	 * @return the chartOfAccounts
	 */
	public ChartOfAccounts getChartOfAccounts()
	{
		return this.chartOfAccounts;
	}
	
	/**
	 * @param chart
	 */
	public void setChartOfAccounts(ChartOfAccounts chart)
	{
		this.chartOfAccounts = checkNotNull(chart);
		
	}
	
}
