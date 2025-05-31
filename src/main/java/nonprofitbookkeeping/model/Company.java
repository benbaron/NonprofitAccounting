
package nonprofitbookkeeping.model;

import java.io.File;
import java.io.Serializable;
import com.fasterxml.jackson.annotation.JsonProperty;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Represents a company, encapsulating all its core data components such as
 * profile information, ledger, chart of accounts, and the associated data file.
 * This class serves as the top-level container for a company's bookkeeping data.
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
	 * The file system path to the company's data file.
	 * This may be null if the company data is not associated with a file
	 * (e.g., new company not yet saved, or data loaded from a different source).
	 */
	private File companyFile = null;

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

	/**
	 * @param created
	 */
	public void setCompanyProfileModel(CompanyProfileModel created)
	{
		this.companyProfileModel = created;
	}

	/**
	 * @return the companyProfileModel
	 */
	public CompanyProfileModel getCompanyProfileModel()
	{
		return this.companyProfileModel;
	}

	/**
	 * Gets the file associated with this company's data.
	 * This is typically the file from which the company data was loaded or to which it will be saved.
	 *
	 * @return The {@link File} object representing the company data file,
	 *         or {@code null} if no file has been associated with this company object.
	 */
	public File getCompanyFile()
	{
		return this.companyFile;
	}

	/**
	 * Sets the file associated with this company's data.
	 * This is typically the file from which the company data was loaded or to which it will be saved.
	 *
	 * @param companyFile The company data file. Can be null if no file is associated.
	 */
	public void setCompanyFile(File companyFile) {
		this.companyFile = companyFile;
	}

	/**
	 * Gets the parent directory of the company's data file.
	 * If the company file is not set, or if the company file does not have a parent
	 * (e.g., it's a root directory, though unlikely for a file), this method returns {@code null}.
	 *
	 * @return A {@link File} object representing the parent directory of the company file,
	 *         or {@code null} if the company file is not set or has no parent.
	 */
	public File getParentFile()
	{
		if (this.companyFile == null) {
            return null;
        }
        return this.companyFile.getParentFile();
	}
	
}
