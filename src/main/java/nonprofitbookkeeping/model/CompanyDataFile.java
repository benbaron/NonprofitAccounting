
package nonprofitbookkeeping.model;

import nonprofitbookkeeping.core.JacksonDataStore;
import nonprofitbookkeeping.exception.ActionCancelledException;
import nonprofitbookkeeping.exception.NoFileCreatedException;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;

import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;


/**
 * Company Data File. 
 * This is a wrapper around the Ledger type.
 */
public class CompanyDataFile implements Serializable
{
	/**
	 * serialVersionUID : long
	 */
	private static final long serialVersionUID = 6728014646115467637L;

	private static final ReadOnlyObjectWrapper<CompanyDataFile> companyObs =
		new ReadOnlyObjectWrapper<>();
	private static File currentFile = null;
	private static JacksonDataStore dataStore = new JacksonDataStore();

	private CompanyProfileModel companyProfileModel = new CompanyProfileModel();
	private Ledger ledger = new Ledger();
	/**  
	 * Constructor CompanyDataFile
	 */
	public CompanyDataFile(CompanyProfileModel companyProfileModel, Ledger ledger )
	{
		this.companyProfileModel = companyProfileModel;
		this.ledger = ledger;
	}

	public CompanyDataFile()
	{
		companyProfileModel = new CompanyProfileModel();
		ledger = new Ledger();
	}
	
	/**
	 * getCompanyDataFile
	 * @return
	 */
	public static CompanyDataFile getCompanyDataFile()
	{
		// return the CompanyDataFile from the wrapper
		return companyObs.get();
	}
	
	public static void setCompanyDataFile(CompanyDataFile cdf)
	{
		CompanyDataFile.companyObs.set(cdf);
	}

	/**
	 * Store back the data to the currentInputFile
	 */
	public static void store()
	{
		try
		{
			dataStore.save(companyObs, currentFile);
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
		CompanyDataFile cdf;
		try
		{
			cdf = CompanyDataFile.dataStore.load(CompanyDataFile.class, currentFile1);
			setCompanyDataFile(cdf);
		}
		catch (IOException | ActionCancelledException | NoFileCreatedException e)
		{
			e.printStackTrace();
		}
	}


	/**
	 * @return the companyobs
	 */
	public static ReadOnlyObjectWrapper<CompanyDataFile> getCompanyobs()
	{
		return CompanyDataFile.companyObs;
	}
	
	/**
	 * For setting a listener/observer
	 * 
	 * @return the property
	 */
	public static ReadOnlyObjectProperty<CompanyDataFile> 
		getCompanyDataFileProperty()
	{
		return CompanyDataFile.companyObs.getReadOnlyProperty();
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
		return ledger;
	}

	/**
	 * @param ledger the ledger to set
	 */
	public void setLedger(Ledger ledger)
	{
		this.ledger = ledger;
	}

}
