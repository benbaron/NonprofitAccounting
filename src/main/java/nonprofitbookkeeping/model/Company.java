
package nonprofitbookkeeping.model;

import nonprofitbookkeeping.core.JacksonDataStorer;
import nonprofitbookkeeping.exception.ActionCancelledException;
import nonprofitbookkeeping.exception.NoFileCreatedException;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;

import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;


/**
 * Company
 */
public class Company implements Serializable
{
	/**
	 * serialVersionUID : long
	 */
	private static final long serialVersionUID = 6728014646115467637L;
	private ReadOnlyObjectWrapper<Company> companyObservable =
		new ReadOnlyObjectWrapper<Company>(selfInstance);
	private File currentFile = null;
	private JacksonDataStorer dataStorer = new JacksonDataStorer();
	
	private CompanyProfileModel companyProfileModel = new CompanyProfileModel();
	private Ledger ledger = new Ledger();
	private ChartOfAccounts chartOfAccounts = new ChartOfAccounts();
	private boolean companyIsOpen = false;
	private static Company selfInstance = null;
	
	
	/**
	 * 
	 * Constructor Company
	 */
	private Company()
	{
		this.companyObservable =
			new ReadOnlyObjectWrapper<Company>(selfInstance);
		this.currentFile = null;
		this.dataStorer = new JacksonDataStorer();
		
		this.companyProfileModel = new CompanyProfileModel();
		this.ledger = new Ledger();
		this.chartOfAccounts = new ChartOfAccounts();
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
		return selfInstance.companyObservable.get();
	}
	
	public static void setCompany(Company company)
	{
		// set the Company to the wrapper
		selfInstance.companyObservable.set(company);
	}
	
	
	/**
	 * Store back the data to persistent
	 * @throws NoFileCreatedException 
	 * @throws ActionCancelledException 
	 * @throws IOException 
	 */
	public void persist() throws IOException, ActionCancelledException, NoFileCreatedException
	{
		
		this.dataStorer.saveData(this.companyObservable,
			this.currentFile);
		
		
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
		this.currentFile = currentFile1;
		
		Company company = getCompany();
		
		company = this.dataStorer.loadData(Company.class, currentFile1);
		setCompany(company);
		
	}
	
	/**
	 * For setting a listener/observer
	 * 
	 * @return the property
	 */
	public
			ReadOnlyObjectProperty<Company>
			getCompanyObserver()
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
		this.companyProfileModel = companyProfileModel;
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
		this.ledger = ledger;
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
		this.currentFile = currentFile;
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
	
	
}
