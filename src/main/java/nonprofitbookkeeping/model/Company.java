
package nonprofitbookkeeping.model;

import nonprofitbookkeeping.core.JacksonDataStorer;
import nonprofitbookkeeping.exception.ActionCancelledException;
import nonprofitbookkeeping.exception.NoFileCreatedException;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;

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

	@JsonIgnore	private ReadOnlyObjectWrapper<Company> companyObservable;
	@JsonIgnore	private File currentFile = null;
	@JsonIgnore	private JacksonDataStorer dataStorer = new JacksonDataStorer();
	@JsonIgnore	private boolean companyIsOpen = false;
	@JsonIgnore	private static Company selfInstance = null;
	
	@JsonProperty private CompanyProfileModel companyProfileModel = new CompanyProfileModel();
	@JsonProperty private Ledger ledger = new Ledger();
	@JsonProperty private ChartOfAccounts chartOfAccounts = new ChartOfAccounts();

	
	/**  
	 * Constructor Company
	 * @param companyObservable
	 * @param currentFile
	 * @param dataStorer
	 * @param companyProfileModel
	 * @param ledger
	 * @param chartOfAccounts
	 * @param companyIsOpen
	 */
	private Company()
	{	
		Company.selfInstance = this;	
		this.companyObservable = new ReadOnlyObjectWrapper<Company>(checkNotNull(selfInstance));
		this.dataStorer = new JacksonDataStorer();
		this.companyProfileModel = new CompanyProfileModel();
		this.ledger = new Ledger();
		this.chartOfAccounts = new ChartOfAccounts();
		this.companyIsOpen = false;
	}

	
	/**
	 * getCompany
	 * @return
	 */
	public static Company getCompany()
	{
		
		if (selfInstance == null)
		{
			selfInstance = new Company();
		}

		// return the Company from the wrapper
		return checkNotNull(selfInstance.companyObservable.get());
	}
	
	/**
	 * 
	 * @param company
	 */
	public static void setCompany(Company company)
	{
		// set the Company to the wrapper
		selfInstance.companyObservable.set(checkNotNull(company));
	}
	
	
	/**
	 * Store back the data to persistent
	 * @throws NoFileCreatedException 
	 * @throws ActionCancelledException 
	 * @throws IOException 
	 */
	public void persist() throws IOException, ActionCancelledException, NoFileCreatedException
	{
		
		this.dataStorer.saveData(
			checkNotNull(this.companyObservable),
			checkNotNull(this.currentFile));
		
	}
	
	/**
	 * @param currentFile1 The file to load into the 
	 * model from the data store
	 * @throws NoFileCreatedException 
	 * @throws ActionCancelledException 
	 * @throws IOException 
	 */
	public void loadFromPersistent(File currentFile1)	throws IOException, ActionCancelledException,
														NoFileCreatedException
	{
		this.currentFile = checkNotNull(currentFile1);
		Company company = getCompany();		
		company = this.dataStorer.loadData(Company.class, currentFile1);
		setCompany(company);
		
	}
	
	/**
	 * For setting a listener/observer
	 * 
	 * @return the property
	 */
	public ReadOnlyObjectProperty<Company>	getCompanyObserver()
	{
		return this.companyObservable.getReadOnlyProperty();
	}
	
	/**
	 * @return the companyProfileModel
	 */
	public CompanyProfileModel getCompanyProfile()
	{
		return this.companyProfileModel;
	}
	
	/**
	 * @param companyProfileModel the companyProfileModel to set
	 */
	public void setCompanyProfileModel(CompanyProfileModel companyProfileModel)
	{
		this.companyProfileModel = checkNotNull(companyProfileModel);
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
	 * @return the currentFile
	 */
	public File getCurrentFile()
	{
		return this.currentFile;
	}
	
	/**
	 * @param currentFile the currentFile to set
	 */
	public void setCurrentFile(File currentFile)
	{
		this.currentFile = checkNotNull(currentFile);
	}
	
	/**
	 * @return the chartOfAccounts
	 */
	public ChartOfAccounts getChartOfAccounts()
	{
		return this.chartOfAccounts;
	}
	
	/**
	 * 
	 */
	public void close()
	{
		this.companyIsOpen = false;
	}
	
	/**
	 * @return Open(T/F)
	 */
	public boolean isOpen()
	{
		return this.companyIsOpen;
	}
	
	/**
	 * Mark company as open
	 */
	public void open()
	{
		this.companyIsOpen = true;
	}


	/**
	 * @param chart
	 */
	public void setChartOfAccounts(ChartOfAccounts chart)
	{
		this.chartOfAccounts = checkNotNull(chart);
		
	}
	
	
}
