
package nonprofitbookkeeping.model;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Embedded;
import jakarta.persistence.Transient;

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
<<<<<<< Upstream, based on origin/codex/read-provided-xlsx-file
@Table(name = "companies")

=======
@Table(name = "company")
>>>>>>> a0d4b45 Remove binary document and zip files
public class Company implements Serializable
{
	/**
	 * The unique identifier for this serializable class.
	 */
	private static final long serialVersionUID = 6728014646115467637L;
	
<<<<<<< Upstream, based on origin/codex/read-provided-xlsx-file
        /** Primary key. */
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        /** The profile information for the company (e.g., name, address). Initialized by default. */
        @Embedded private CompanyProfileModel companyProfileModel = new CompanyProfileModel();
        /** The ledger containing all financial transactions for the company. Initialized by default. */
        @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
        private Ledger ledger = new Ledger();
        /** The chart of accounts defining the structure of accounts for the company. Initialized by default. */
        @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
        private ChartOfAccounts chartOfAccounts = new ChartOfAccounts();

=======
        /** Unique database identifier for this company. */
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long companyId;

        /** The profile information for the company (e.g., name, address). Initialized by default. */
        @Embedded
        @JsonProperty private CompanyProfileModel companyProfileModel = new CompanyProfileModel();
	/** The ledger containing all financial transactions for the company. Initialized by default. */
        @Transient
        @JsonProperty private Ledger ledger = new Ledger();
	/** The chart of accounts defining the structure of accounts for the company. Initialized by default. */
        @Transient
        @JsonProperty private ChartOfAccounts chartOfAccounts = new ChartOfAccounts();
>>>>>>> a0d4b45 Remove binary document and zip files
	
<<<<<<< Upstream, based on origin/codex/read-provided-xlsx-file
        /**
         * Identifier used to reference this company in the database.
         * This replaces the old file based linkage and may be {@code null}
         * for transient company instances that have not yet been persisted.
         */
        @JsonProperty private String companyId;

=======
	/**
	 * The file system path to the company's data file.
	 * This may be null if the company data is not associated with a file
	 * (e.g., new company not yet saved, or data loaded from a different source).
	 */
        @Transient
        private File companyFile = null;
>>>>>>> a0d4b45 Remove binary document and zip files

	/**
	 * Constructs a new Company object.
	 * Initializes the company profile, ledger, and chart of accounts with default instances.
	 * The company file is initially null.
	 */
<<<<<<< Upstream, based on origin/codex/read-provided-xlsx-file
        public Company() 
        {
                this.companyProfileModel = new CompanyProfileModel();
                this.ledger = new Ledger();
                this.chartOfAccounts = new ChartOfAccounts();
        }

        public Long getId() {
                return this.id;
        }

        public void setId(Long id) {
                this.id = id;
=======
        public Company()
        {
                this.companyProfileModel = new CompanyProfileModel();
                this.ledger = new Ledger();
                this.chartOfAccounts = new ChartOfAccounts();
        }

        /**
         * Gets the database identifier for this company.
         * @return the company ID
         */
        public Long getCompanyId()
        {
                return this.companyId;
        }

        /**
         * Sets the database identifier for this company.
         * @param companyId the id to set
         */
        public void setCompanyId(Long companyId)
        {
                this.companyId = companyId;
>>>>>>> a0d4b45 Remove binary document and zip files
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
                return (this.companyProfileModel != null)
                                ? this.companyProfileModel.getCompanyName()
                                : null;
        }
	
}
