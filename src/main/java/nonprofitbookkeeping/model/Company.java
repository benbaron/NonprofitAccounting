
package nonprofitbookkeeping.model;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Table;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Represents a company, encapsulating all its core data components such as
 * profile information, ledger, chart of accounts, and the associated data file.
 * This class serves as the top-level container for a company's bookkeeping data.
 */
@Entity
@Table(name = "companies")

public class Company implements Serializable
{
	/**
	 * The unique identifier for this serializable class.
	 */
	private static final long serialVersionUID = 6728014646115467637L;
	
	/** Primary key. */
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
	
	/** The profile information for the company (e.g., name, address). Initialized by default. */
	@Embedded private CompanyProfileModel companyProfileModel =
		new CompanyProfileModel();
	
	/** The ledger containing all financial transactions for the company. Initialized by default. */
	@OneToOne(cascade = CascadeType.ALL,
		orphanRemoval = true) private Ledger ledger = new Ledger();
	
	/** The chart of accounts defining the structure of accounts for the company. Initialized by default. */
	@OneToOne(cascade = CascadeType.ALL,
		orphanRemoval = true) private ChartOfAccounts chartOfAccounts =
			new ChartOfAccounts();
	
	
	/**
	 * Identifier used to reference this company in the database.
	 * This replaces the old file based linkage and may be {@code null}
	 * for transient company instances that have not yet been persisted.
	 */
	@JsonProperty private String companyId;
	
	
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
	
	public Long getId()
	{
		return this.id;
		
	}
	
	public void setId(Long id)
	{
		this.id = id;
		
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
	 * Returns the identifier for this company in the database.
	 *
	 * @return company identifier or {@code null} if not yet persisted
	 */
	public String getCompanyId()
	{
		return this.companyId;
		
	}
	
	/**
	 * Sets the identifier used to reference this company in the database.
	 *
	 * @param companyId database identifier for the company
	 */
	public void setCompanyId(String companyId)
	{
		this.companyId = companyId;
		
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
		return (this.companyProfileModel != null) ?
			this.companyProfileModel.getCompanyName() : null;
		
	}
	
}
