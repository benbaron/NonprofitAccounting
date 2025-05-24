
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
	private static final ReadOnlyObjectWrapper<Company> companyObs =
		new ReadOnlyObjectWrapper<>();
	private static File currentFile = null;
	private static JacksonDataStorer dataStorer = new JacksonDataStorer();

	private CompanyProfileModel companyProfileModel = new CompanyProfileModel();
	private Ledger ledger = new Ledger();
	private ChartOfAccounts chartOfAccounts = new ChartOfAccounts();

	/**  
	 * Constructor Company
	 */
	public Company(CompanyProfileModel companyProfileModel, Ledger ledger)
	{
		this.companyProfileModel = companyProfileModel;
		this.ledger = ledger;
	}

	/**
	 * 
	 * Constructor Company
	 */
	public Company()
	{
		this.companyProfileModel = new CompanyProfileModel();
		this.ledger = new Ledger();
	}
	
	/**
	 * getCompanyDataFile
	 * @return
	 */
	public static Company getCompany()
	{
		// return the Company from the wrapper
		return companyObs.get();
	}
	
	public static void setCompany(Company cdf)
	{
		companyObs.set(cdf);
	}

	/**
	 * Store back the data to the currentInputFile
	 */
	public static void store()
	{
		try
		{
			dataStorer.saveData(companyObs, currentFile);
		}
		catch (IOException | ActionCancelledException | NoFileCreatedException e)
		{
			e.printStackTrace();
		}
	}
	
	/**
	 * @param currentFile1 The file to load into the model from the 
	 *                     data store
	 */
	public static void load(File currentFile1)
	{
		currentFile = currentFile1;
		
		Company company = getCompany();
		try
		{
			company = dataStorer.loadData(Company.class, currentFile1);
			setCompany(company);
		}
		catch (IOException | ActionCancelledException | NoFileCreatedException e)
		{
			e.printStackTrace();
		}
	}


	/**
	 * For setting a listener/observer
	 * 
	 * @return the property
	 */
	public static ReadOnlyObjectProperty<Company> 
		getCompanyProperty()
	{
		return companyObs.getReadOnlyProperty();
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
	public static File getCurrentFile()
	{
		return currentFile;
	}

	/**
	 * @param currentFile the currentFile to set
	 */
	public static void setCurrentFile(File currentFile)
	{
		Company.currentFile = currentFile;
	}

	/**
	 * @return the chartOfAccounts
	 */
	public ChartOfAccounts getChartOfAccounts()
	{
		return this.chartOfAccounts;
	}

	/**
	 * @param chartOfAccounts the chartOfAccounts to set
	 */
	public void setChartOfAccounts(ChartOfAccounts chartOfAccounts)
	{
		this.chartOfAccounts = chartOfAccounts;
	}

	/**
	 * @return the companyProfileModel
	 */
	public CompanyProfileModel getCompanyProfileModel()
	{
		return this.companyProfileModel;
	}


}
