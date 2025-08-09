
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
	 * The unique identifier for this serializable class.
	 */
	private static final long serialVersionUID = 6728014646115467637L;
	
	/** The profile information for the company (e.g., name, address). Initialized by default. */
	@JsonProperty private CompanyProfileModel companyProfileModel = new CompanyProfileModel();
	/** The ledger containing all financial transactions for the company. Initialized by default. */
	@JsonProperty private Ledger ledger = new Ledger();
	/** The chart of accounts defining the structure of accounts for the company. Initialized by default. */
	@JsonProperty private ChartOfAccounts chartOfAccounts = new ChartOfAccounts();
	
	/**
	 * The file system path to the company's data file.
	 * This may be null if the company data is not associated with a file
	 * (e.g., new company not yet saved, or data loaded from a different source).
	 */
	private File companyFile = null;

	/**
	 * Constructs a new Company object.
	 * Initializes the company profile, ledger, and chart of accounts with default instances.
	 * The company file is initially null.
	 */
	public Company()
	{			
		this.companyProfileModel = new CompanyProfileModel();
		this.ledger = new Ledger();
		this.chartOfAccounts = new ChartOfAccounts();
	}
	
	/**
	 * Gets the company's profile information.
	 * @return The {@link CompanyProfileModel} associated with this company.
	 */
	public CompanyProfileModel getCompanyProfile()
	{
		return this.companyProfileModel;
	}
	
	
	/**
	 * Gets the company's ledger, which contains all financial transactions.
	 * @return The {@link Ledger} for this company.
	 */
	public Ledger getLedger()
	{
		return this.ledger;
	}
	
	/**
	 * Sets the company's ledger.
	 * @param ledger The {@link Ledger} to set. Must not be null.
	 * @throws NullPointerException if ledger is null.
	 */
	public void setLedger(Ledger ledger)
	{
		this.ledger = checkNotNull(ledger);
	}
	
	/**
	 * Gets the company's chart of accounts.
	 * @return The {@link ChartOfAccounts} for this company.
	 */
	public ChartOfAccounts getChartOfAccounts()
	{
		return this.chartOfAccounts;
	}
	
	/**
	 * Sets the company's chart of accounts.
	 * @param chart The {@link ChartOfAccounts} to set. Must not be null.
	 * @throws NullPointerException if chart is null.
	 */
	public void setChartOfAccounts(ChartOfAccounts chart)
	{
		this.chartOfAccounts = checkNotNull(chart);
		
	}

	/**
	 * Sets the company's profile model.
	 * @param created The {@link CompanyProfileModel} to set.
	 */
	public void setCompanyProfileModel(CompanyProfileModel created)
	{
		this.companyProfileModel = created;
	}

	/**
	 * Gets the company's profile model.
	 * This is an alias for {@link #getCompanyProfile()}.
	 * @return The {@link CompanyProfileModel} associated with this company.
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

	/**
	 * @param profile
	 */
        public void setCompanyProfile(CompanyProfileModel profile)
        {
                setCompanyProfileModel(checkNotNull(profile));

        }

        /**
         * Gets the company's name from the associated {@link CompanyProfileModel}.
         *
         * @return The company name, or {@code null} if no profile is set.
         */
        public String getName()
        {
                return (this.companyProfileModel != null)
                                ? this.companyProfileModel.getCompanyName()
                                : null;
        }
	
}
